/**
 * This package could technically contain classes that implement the services
 * but for our purposes we'll just use functions if we ever need to use classes
 * (for stateful services) we can add them here as well
 */
package com.github.angelmunoz.resumakerkt.services

import arrow.core.Either
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
import java.io.StringWriter
import java.nio.file.Path


fun getTemplateRenderer(logger: KLogger, engine: PebbleEngine, json: Json, fileLocator: FileLocator): TemplateRenderer {

    return TemplateRenderer { templateNameOrPath, resume ->
        fun getTemplate(templateNameOrPath: String): Either<TemplateRenderingError, PebbleTemplate> {
            return either {
                try {
                    if (templateNameOrPath.startsWith(".") && templateNameOrPath.endsWith(".html")) {
                        logger.debug { "Custom template specified: '$templateNameOrPath'" }
                        val file = fileLocator
                                .find(templateNameOrPath, false)
                                .mapLeft { TemplateRenderingError(it.error) }
                                .bind()
                        logger.debug { "File exists: ${file.exists()}" }
                        engine.getTemplate(file.absolutePath)
                    } else {
                        logger.trace { "Using built-in template: '$templateNameOrPath'" }
                        engine.getTemplate("templates/$templateNameOrPath")
                    }
                } catch (err: Error) {
                    logger.trace(err) { "Failed to obtain the template from disk." }
                    val error =
                            TemplateRenderingError(err.message
                                    ?: "Failed to obtain the Html Template: '$templateNameOrPath'")
                    raise(error)
                }
            }
        }

        StringWriter().use { writer ->
            either {
                val template = getTemplate(templateNameOrPath).bind()

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

fun getResumeLocator(json: Json, fileLocator: FileLocator): ResumeLocator {
    return ResumeLocator { path ->
        either {
            val resume = fileLocator.find(path, false).mapLeft { ResumeError(it.error) }.bind()
            json.decodeFromString(resume.readText())
        }
    }
}

fun getFileLocator(logger: KLogger, cwd: String): FileLocator {
    return FileLocator { path, ensureParent ->
        either {
            val file = Path.of(cwd, path).toAbsolutePath().toFile()
            try {
                ensure(file.isFile) { FileLocatorError("Path '${file.absolutePath} is not a file") }

                if (ensureParent)
                    file.parentFile.mkdirs()
            } catch (error: Error) {
                logger.trace(error) { "Failed to create parent directory" }
                raise(FileLocatorError(error.message ?: "Failed to create parent directory"))
            }
            file
        }
    }
}

fun getDocumentParser(logger: KLogger): DocumentParser {
    return DocumentParser { html ->
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
        fileLocator: FileLocator,
        documentParser: DocumentParser,
        pdfRenderer: () -> PdfRendererBuilder,
): PdfConverter {
    return PdfConverter { html, outPath ->
        either {
            val document = documentParser
                    .parse(html)
                    .mapLeft { PdfConvertError(it) }
                    .bind()
            val file = fileLocator
                    .find(outPath, true)
                    .mapLeft { PdfConvertError(it.error) }.bind()
            try {
                file.outputStream().use { os ->
                    logger.debug { "Rendering document " }

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
