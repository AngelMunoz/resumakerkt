/**
 * This package could technically contain classes that implement the services
 * but for our purposes we'll just use functions if we ever need to use classes
 * (for stateful services) we can add them here as well
 */
package com.github.angelmunoz.resumakerkt.services

import com.github.angelmunoz.resumakerkt.types.PdfConverter
import com.github.angelmunoz.resumakerkt.types.ResumeLocator
import com.github.angelmunoz.resumakerkt.types.TemplateRenderer
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import io.pebbletemplates.pebble.PebbleEngine
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.jsoup.Jsoup
import org.jsoup.helper.W3CDom
import org.w3c.dom.Document
import java.io.File
import java.io.OutputStream
import java.io.StringWriter
import kotlin.io.path.toPath


fun getTemplateRenderer(engine: PebbleEngine, json: Json): TemplateRenderer {
    return TemplateRenderer { templateNameOrPath, resume ->
        StringWriter().use { writer ->
            val template = engine.getTemplate("templates/$templateNameOrPath")
            val payload = json.encodeToString(resume).let { json.parseToJsonElement(it).jsonObject.toMap() }
            template.evaluate(writer, payload)
            writer.toString()
        }
    }
}

inline fun getResumeLocator(json: Json, crossinline getFile: (String) -> File): ResumeLocator {
    return ResumeLocator { path ->
        val templateUrl = ResumeLocator::class.java.classLoader.getResource(path)
        val template = if (templateUrl != null) {
            val absPath = templateUrl.toURI().toPath().toAbsolutePath().toString()
            getFile(absPath)
        } else {
            getFile(path)
        }
        json.decodeFromString(template.readText())
    }
}

inline fun getPdfConverter(
        crossinline getFile: (String) -> File,
        crossinline getDocument: (String) -> Document,
        crossinline renderPdf: (Document, OutputStream) -> Unit,
): PdfConverter {
    return PdfConverter { html, outPath ->
        val file = getFile(outPath)
        file.outputStream().use { os ->
            val content = getDocument(html)
            renderPdf(content, os)
        }
        file.absolutePath.toString()
    }
}
