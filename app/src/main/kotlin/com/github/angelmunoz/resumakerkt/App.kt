/*
 * The main entry point for our app, here we can customize and orchestrate
 * our application's environment and the commands that will be available
 */
package com.github.angelmunoz.resumakerkt

import com.github.angelmunoz.resumakerkt.cli_options.Resumaker
import com.github.angelmunoz.resumakerkt.handlers.generateResume
import picocli.CommandLine
import kotlin.system.exitProcess


fun main(vararg argv: String) {

    // The App Environment is the DI container for the application
    val mainCmd = Resumaker(::getAppEnv, ::generateResume)

    exitProcess(CommandLine(mainCmd).execute(*argv))
}
