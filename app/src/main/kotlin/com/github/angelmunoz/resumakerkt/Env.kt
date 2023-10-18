package com.github.angelmunoz.resumakerkt

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.github.angelmunoz.resumakerkt.services.*
import com.github.angelmunoz.resumakerkt.types.*
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import com.openhtmltopdf.util.XRLog
import io.github.oshai.kotlinlogging.DelegatingKLogger
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.pebbletemplates.pebble.PebbleEngine
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.bindInstance
import org.kodein.di.bindSingleton
import org.kodein.di.instance


/**
 * This function orchestrates the creation of our DI container.
 * It takes a LogLevel because each command might decide to ignore or override the default log level
 * It also ensures that other libraries are compatible with our own logger and don't pollute the console
 * @param logLevel the log level to use
 */
@OptIn(ExperimentalSerializationApi::class)
fun getApplicationEnvironment(logLevel: LogLevel): DI {

  // Ensure that html to pdf logger only outputs warnings or errors rather than info due its verbosity
  XRLog.setLoggingEnabled(false)

  // create our own application wide logger
  val logger = KotlinLogging.logger("com.github.angelmunoz.resumakerkt")

  // set the desired log level
  val under = (logger as DelegatingKLogger<*>).underlyingLogger as Logger

  when (logLevel) {
    LogLevel.Info -> under.level = Level.INFO
    LogLevel.Debug -> under.level = Level.DEBUG
    LogLevel.Trace -> under.level = Level.TRACE
  }

  return DI {
    bindInstance<String>(tag = "working-dir") { System.getProperty("user.dir") }
    bindInstance<KLogger> { logger }
    bindInstance<PebbleEngine> { PebbleEngine.Builder().build() }
    bindInstance<Json> {
      Json {
        isLenient = true
        ignoreUnknownKeys = true
        explicitNulls = false
      }
    }
    bindSingleton<() -> PdfRendererBuilder> { { PdfRendererBuilder() } }
    // From here and below we want to type the instances as well as the bind type
    // just to be sure that when we update the code we don't break the contract
    // or that we don't forget to update the bind type as breaking changes are introduced
    bindSingleton<IDocumentParser> { getDocumentParser(instance<KLogger>()) }
    bindSingleton<IFileLocator> {
      getFileLocator(instance<KLogger>(), instance<String>("working-dir"))
    }
    bindSingleton<IResumeLocator> {
      getResumeLocator(instance<Json>(), instance<IFileLocator>())
    }
    bindSingleton<ITemplateRenderer> {
      getTemplateRenderer(instance<KLogger>(), instance<PebbleEngine>(), instance<Json>(), instance<IFileLocator>())
    }
    bindSingleton<IPdfConverter> {
      getPdfConverter(
        instance<KLogger>(), instance<IFileLocator>(), instance<IDocumentParser>(), instance<() -> PdfRendererBuilder>()
      )
    }
    bindSingleton<IResumeGenerator> {
      getResumeGenerator(
        instance<KLogger>(), instance<IResumeLocator>(), instance<ITemplateRenderer>(), instance<IPdfConverter>()
      )
    }
  }
}
