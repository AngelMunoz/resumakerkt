/**
 * This package contains the factory functions to provide services based on the interfaces defined in the types package
 * For the most part, here is where the implementation those interfaces live, since we're primarily using
 * Functional Interfaces (https://kotlinlang.org/docs/fun-interfaces.html) it usually contains functions.
 * It can however also contain classes that implement those interfaces.
 */
package com.github.angelmunoz.resumakerkt.services

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.github.angelmunoz.resumakerkt.types.*
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import io.github.oshai.kotlinlogging.KLogger
import io.pebbletemplates.pebble.PebbleEngine
import io.pebbletemplates.pebble.template.PebbleTemplate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import org.jsoup.Jsoup
import org.jsoup.helper.W3CDom
import java.io.File
import java.io.StringWriter


private fun locateResumeTemplate(
  logger: KLogger, fileLocator: IFileLocator, engine: PebbleEngine, templateNameOrPath: String
): Either<TemplateRenderingError, PebbleTemplate> {
  return either {
    try {
      if (templateNameOrPath.startsWith(".") && templateNameOrPath.endsWith(".html")) {
        logger.debug { "Custom template specified: '$templateNameOrPath'" }

        val file = fileLocator
          .find(templateNameOrPath, false)
          .mapLeft { TemplateRenderingError(it.error) }
          .bind()

        logger.trace { "File exists: ${file.exists()}" }

        engine.getTemplate(file.absolutePath)
      } else {
        logger.trace { "Using built-in template: '$templateNameOrPath'" }

        engine.getTemplate("templates/$templateNameOrPath")
      }
    } catch (err: Error) {
      logger.trace(err) { "Failed to obtain the template from disk." }
      val error = TemplateRenderingError(
        err.message ?: "Failed to obtain the Html Template: '$templateNameOrPath'"
      )
      raise(error)
    }
  }
}

fun getTemplateRenderer(
  logger: KLogger,
  engine: PebbleEngine,
  json: Json,
  fileLocator: IFileLocator
): ITemplateRenderer {

  return ITemplateRenderer { templateNameOrPath, resume ->

    StringWriter().use { writer ->
      either {
        val template = locateResumeTemplate(logger, fileLocator, engine, templateNameOrPath).bind()

        val payload = json.encodeToJsonElement(resume).jsonObject.toMap()
        try {
          template.evaluate(writer, payload)
        } catch (error: Error) {
          logger.trace(error) { "Attempted to evaluate and failed with $resume" }
          raise(TemplateRenderingError(error.message ?: "Failed to evaluate the template"))
        }
        writer.toString()
      }
    }
  }
}

fun getResumeLocator(json: Json, fileLocator: IFileLocator): IResumeLocator {
  return IResumeLocator { path ->
    either {
      val resume = fileLocator
        .find(path, false)
        .mapLeft { ResumeError(it.error) }
        .bind()
      json.decodeFromString(resume.readText())
    }
  }
}

fun getFileLocator(logger: KLogger, cwd: String): IFileLocator {
  return IFileLocator { path, ensureParent ->
    either {

      val file = File(cwd).resolve(path)
      try {
        if (ensureParent) {
          logger.trace { "Ensuring parent directory exists" }
          file.parentFile.mkdirs()
        }

        ensure(!file.isDirectory) { FileLocatorError("Path '${file.absolutePath} is not a file") }
      } catch (error: Error) {
        logger.trace(error) { "Failed to create parent directory" }
        raise(FileLocatorError(error.message ?: "Failed to create parent directory"))
      }
      file
    }
  }
}

fun getDocumentParser(logger: KLogger): IDocumentParser {
  return IDocumentParser { html ->
    either {
      try {
        Jsoup.parse(html).let { doc -> W3CDom().fromJsoup(doc) }
      } catch (error: Error) {
        logger.trace(error) { "Failed to parse HTML Document" }
        raise(error.message ?: "Failed to parse HTML Document")
      }
    }
  }
}

fun getPdfConverter(
  logger: KLogger,
  fileLocator: IFileLocator,
  documentParser: IDocumentParser,
  pdfRenderer: () -> PdfRendererBuilder,
): IPdfConverter {
  return IPdfConverter { html, outPath ->
    either {
      val document = documentParser
        .parse(html)
        .mapLeft { PdfConvertError(it) }
        .bind()
      val file = fileLocator
        .find(outPath, true)
        .mapLeft { PdfConvertError(it.error) }
        .bind()
      try {
        file.outputStream().use { os ->
          logger.debug { "Rendering document" }

          pdfRenderer().withW3cDocument(document, "/").toStream(os).run()
        }
      } catch (error: Error) {
        logger.trace(error) { "Failed to write the final PDF" }
        raise(PdfConvertError(error.message ?: "Failed to write the final PDF"))
      }
      file.absolutePath.toString()
    }
  }
}


fun getResumeGenerator(
  logger: KLogger,
  resumeLocator: IResumeLocator,
  templateRenderer: ITemplateRenderer,
  pdfConverter: IPdfConverter
): IResumeGenerator {
  return IResumeGenerator { resumePath, outDir, template, language ->
    logger.info { "Generating pdf files from '$resumePath'" }
    val resumeList = resumeLocator.getResume(resumePath).mapLeft { it.error }.getOrElse { emptyList() }.run {
      if (language.isEmpty()) {
        logger.info { "No language provided, generating all available languages." }
        this
      } else {
        this.filter { resume ->
          language.contains(resume.language.name)
        }
      }
    }

    for (resume in resumeList) {
      logger.info { "Generating resumes for languages: ${resume.language.name}" }
      val renderResult = templateRenderer.render(template, resume).mapLeft { it.error }.flatMap { htmlContent ->
        pdfConverter.convert(
          html = htmlContent,
          outPath = "${outDir}/${resume.language.name}.pdf"
        ).mapLeft { it.error }
      }
      when (renderResult) {
        is Either.Right -> logger.info { "Generated PDF at: '${renderResult.value}" }
        is Either.Left -> logger.error { "Unable to generate PDF: ${renderResult.leftOrNull()}" }
      }
    }

    logger.info { "Processed '${resumeList.size}' files." }

  }
}
