package com.github.angelmunoz.resumakerkt

import arrow.core.raise.catch
import com.github.angelmunoz.resumakerkt.services.*
import com.github.angelmunoz.resumakerkt.types.*
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.pebbletemplates.pebble.PebbleEngine
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.kodein.di.*
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.github.oshai.kotlinlogging.DelegatingKLogger

@OptIn(ExperimentalSerializationApi::class)
fun getAppEnv(logLevel: LogLevel): DI {
    val logger = KotlinLogging.logger("ResumakerKT")
    val under = (logger as DelegatingKLogger<org.slf4j.Logger>).underlyingLogger as ch.qos.logback.classic.Logger
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
        bindSingleton<DocumentParser> { getDocumentParser(instance()) }
        bindSingleton<FileLocator> {
            getFileLocator(instance(), instance("working-dir"))
        }
        bindSingleton<ResumeLocator> {
            getResumeLocator(instance(), instance())
        }
        bindSingleton<TemplateRenderer> {
            getTemplateRenderer(instance(), instance(), instance(), instance())
        }
        bindSingleton<PdfConverter> {
            getPdfConverter(instance(), instance(), instance(), instance())
        }
    }
}
