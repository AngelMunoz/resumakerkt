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
    val appEnv = getAppEnv()
    val mainCmd = Resumaker(appEnv, ::generateResume)

    exitProcess(CommandLine(mainCmd).execute(*argv))
}
