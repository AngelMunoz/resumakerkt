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

import com.github.angelmunoz.resumakerkt.types.*
import kotlinx.coroutines.Runnable
import org.kodein.di.DI
import org.kodein.di.instance
import picocli.CommandLine.*

@Command(name = "resumaker")
class Resumaker(private val getEnv: (LogLevel) -> DI) : Runnable {


  @Parameters(description = ["The Json file where your resume is stored"])
  lateinit var resume: String

  @Option(
    names = ["-o", "--outDir"], description = ["Generate a resume from a template"]
  )
  var outDir = "./generated"

  @Option(
    names = ["-t", "--template"], description = ["The template to use"]
  )
  var template = "default.html"

  @Option(
    names = ["-l", "--language"],
    description = ["The language to use, if not specified it will generate all of the available languages"]
  )
  var languages: List<String> = emptyList()

  @Option(
    names = ["--log-level"], description = ["Log level Valid values: \${COMPLETION-CANDIDATES}, defaults to 'Info'"]
  )
  var logLevel: LogLevel = LogLevel.Info

  override fun run() {
    val env = getEnv(logLevel)
    val resumeGenerator: IResumeGenerator by env.instance()
    resumeGenerator.generate(resume, outDir, template, languages)
  }
}
