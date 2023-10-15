package com.github.angelmunoz.resumakerkt

import com.github.angelmunoz.resumakerkt.services.getPdfConverter
import com.github.angelmunoz.resumakerkt.services.getResumeLocator
import com.github.angelmunoz.resumakerkt.services.getTemplateRenderer
import com.github.angelmunoz.resumakerkt.types.PdfConverter
import com.github.angelmunoz.resumakerkt.types.ResumeLocator
import com.github.angelmunoz.resumakerkt.types.TemplateRenderer
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.pebbletemplates.pebble.PebbleEngine
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import org.jsoup.helper.W3CDom
import org.kodein.di.*
import java.nio.file.Path

@OptIn(ExperimentalSerializationApi::class)
fun getAppEnv(): DI {
    return DI {
        bindInstance<Json> {
            Json {
                isLenient = true
                ignoreUnknownKeys = true
                explicitNulls = false
            }
        }
        bindMultiton<String, KLogger> { name: String ->
            KotlinLogging.logger(name)
        }
        bindInstance { PebbleEngine.Builder().build() }
        bindInstance(tag = "cwd-file-locator") {
            { path: String ->
                val cwd = System.getProperty("user.dir")
                Path.of(cwd, path).toAbsolutePath().toFile()
            }
        }
        bindInstance(tag = "cwd-file-locator-ensure-dir") {
            { path: String ->
                val cwd = System.getProperty("user.dir")
                val file = Path.of(cwd, path).toAbsolutePath().toFile()
                file.parentFile.mkdirs()
                file
            }
        }
        bindSingleton<ResumeLocator> {
            getResumeLocator(
                instance(),
                instance("cwd-file-locator")
            )
        }
        bindSingleton<TemplateRenderer> { getTemplateRenderer(instance(), instance(), instance("cwd-file-locator")) }
        bindSingleton<PdfConverter> {
            getPdfConverter(
                instance("cwd-file-locator-ensure-dir"),
                { html -> Jsoup.parse(html).let { doc -> W3CDom().fromJsoup(doc) } },
                { doc, os ->
                    PdfRendererBuilder().withW3cDocument(doc, "/").toStream(os).run()
                }
            )
        }
    }
}
