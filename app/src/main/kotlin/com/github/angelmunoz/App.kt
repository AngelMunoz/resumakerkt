/*
 * The main entry point for our app, here we can customize and orchestrate
 * our application's environment and the commands that will be available
 */
package com.github.angelmunoz

import com.github.angelmunoz.cli_options.ShowDirectoryItems
import com.github.angelmunoz.handlers.gatherFsItems
import com.github.angelmunoz.services.localFsFactory
import com.github.angelmunoz.types.ApplicationEnvironment
import com.github.angelmunoz.types.IFileSystem
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import picocli.CommandLine
import kotlin.system.exitProcess


fun main(vararg argv: String) {

    // The App Environment is the DI container for the application
    val appEnv = object : ApplicationEnvironment {
        private val fs = lazy { localFsFactory() }

        override val fileSystem: IFileSystem get() = fs.value
        override fun <TEnclosingCls> getLogger(cls: Class<TEnclosingCls>): KLogger = KotlinLogging.logger(cls.name)
    }
    val mainCmd = ShowDirectoryItems(appEnv, ::gatherFsItems)

    exitProcess(CommandLine(mainCmd).execute(*argv))
}
