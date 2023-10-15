/**
 * This package could technically contain classes that implement the services
 * but for our purposes we'll just use functions if we ever need to use classes
 * (for stateful services) we can add them here as well
 */
package com.github.angelmunoz.resumakerkt.services

import com.github.angelmunoz.resumakerkt.types.PdfConverter
import com.github.angelmunoz.resumakerkt.types.ResumeLocator
import com.github.angelmunoz.resumakerkt.types.TemplateRenderer
import io.pebbletemplates.pebble.PebbleEngine
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import org.w3c.dom.Document
import java.io.File
import java.io.OutputStream
import java.io.StringWriter


fun getTemplateRenderer(engine: PebbleEngine, json: Json, getUserlandTemplate: (String) -> File): TemplateRenderer {
    return TemplateRenderer { templateNameOrPath, resume ->
        StringWriter().use { writer ->
            val template =
                if (templateNameOrPath.startsWith(".") && templateNameOrPath.endsWith(".html")) {
                    val file = getUserlandTemplate(templateNameOrPath).absolutePath
                    engine.getTemplate(file)
                } else {
                    engine.getTemplate("templates/$templateNameOrPath")
                }

            val payload = json.encodeToJsonElement(resume).jsonObject.toMap()

            template.evaluate(writer, payload)
            writer.toString()
        }
    }
}

inline fun getResumeLocator(json: Json, crossinline getFile: (String) -> File): ResumeLocator {
    return ResumeLocator { path ->
        val resume = getFile(path)
        json.decodeFromString(resume.readText())
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
