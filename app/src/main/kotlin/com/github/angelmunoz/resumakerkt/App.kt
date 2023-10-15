/*
 * The main entry point for our app, here we can customize and orchestrate
 * our application's environment and the commands that will be available
 */
package com.github.angelmunoz.resumakerkt

import com.github.angelmunoz.resumakerkt.cli_options.Resumaker
import com.github.angelmunoz.resumakerkt.handlers.generateResume
import com.github.angelmunoz.resumakerkt.services.getPdfConverter
import com.github.angelmunoz.resumakerkt.services.getResumeLocator
import com.github.angelmunoz.resumakerkt.services.getTemplateRenderer
import com.github.angelmunoz.resumakerkt.types.*
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.pebbletemplates.pebble.PebbleEngine
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import org.jsoup.helper.W3CDom
import picocli.CommandLine
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.appendText
import kotlin.system.exitProcess


fun main(vararg argv: String) {

    // The App Environment is the DI container for the application
    val appEnv = object : ApplicationEnvironment {
        val json = Json {
            isLenient = true
            ignoreUnknownKeys = true
            explicitNulls = false
        }

        val engine = PebbleEngine.Builder().build()

        override val resumeLocator: ResumeLocator
            get() = getResumeLocator(json) { path ->
                val cwd = System.getProperty("user.dir")
                Path.of(cwd, path).toAbsolutePath().toFile()
            }
        override val templateRenderer: TemplateRenderer
            get() = getTemplateRenderer(engine, json)
        override val asyncResumeLocator: AsyncResumeLocator
            get() = TODO("Not yet implemented")
        override val pdfConverter: PdfConverter
            get() = getPdfConverter(
                    { path ->
                        val cwd = System.getProperty("user.dir")
                        val file = Path.of(cwd, path).toAbsolutePath().toFile()
                        file.parentFile.mkdirs()
                        file
                    },
                    { html -> Jsoup.parse(html).let { doc -> W3CDom().fromJsoup(doc) } },
                    { doc, os ->
                        PdfRendererBuilder().withW3cDocument(doc, "/").toStream(os).run()
                    }
            )

        override fun <TEnclosingCls> getLogger(cls: Class<TEnclosingCls>): KLogger = KotlinLogging.logger(cls.name)
    }
    val mainCmd = Resumaker(appEnv, ::generateResume)

    exitProcess(CommandLine(mainCmd).execute(*argv))
}
