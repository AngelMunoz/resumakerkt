/**
 * This package contains the command line options for our application
 * each command should be a class that implements Runnable or Callable<Int>
 * If our command is performing side effects that we don't care too much about like printing to the console
 * we can use Runnable, If our command is performing transformations on data or other resources
 * we should use Callable<Int> to signal if our pipeline was successful or not
 *
 * To ensure sub commands have access to global options (and allowing it to work with our approach),
 * they have to be implemented as class methods
 * @see <a href="https://picocli.info/#_subcommands_as_methods">picocli docs</a>
 */
package com.github.angelmunoz.resumakerkt.cli_options

import com.github.angelmunoz.resumakerkt.handlers.ResumeGenerator
import com.github.angelmunoz.resumakerkt.types.ApplicationEnvironment
import com.github.angelmunoz.resumakerkt.types.GenerateParams
import kotlinx.coroutines.Runnable
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

@Command(name = "resumaker")
class Resumaker(private val env: ApplicationEnvironment, private val generator: ResumeGenerator) : Runnable {

    @Parameters(description = ["The Json file where your resume is stored"])
    lateinit var resume: String

    @Option(
            names = ["-o", "--outDir"],
            description = ["Generate a resume from a template"]
    )
    var outDir = "./generated"

    @Option(
            names = ["-t", "--template"],
            description = ["The template to use"]
    )
    var template = "default.html"

    @Option(
            names = ["-l", "--language"],
            description = ["The language to use, if not specified it will generate all of the available languages"]
    )
    var languages: List<String> = emptyList()

    override fun run() {
        val logger = env.getLogger(this.javaClass)
        logger.info { "Generating pdf files from '$resume'" }
        val paths = generator(env.resumeLocator, env.templateRenderer, env.pdfConverter, resume, GenerateParams(outDir, template, languages)).toList()
        logger.info { "Generated ${paths.size} PDF Files" }
    }
}
