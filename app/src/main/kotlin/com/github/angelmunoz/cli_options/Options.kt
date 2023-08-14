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
package com.github.angelmunoz.cli_options

import com.github.angelmunoz.types.ApplicationEnvironment
import com.github.angelmunoz.types.IFileSystem
import com.github.angelmunoz.types.ShowParams
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.runBlocking
import picocli.CommandLine.Option

/**
 * This class is a command that will show the contents of a directory
 * @param env A DI container that has the required services of the application
 * @param action the action to perform when the command is executed
 */
class ShowDirectoryItems(private val env: ApplicationEnvironment, private val action: suspend (IFileSystem, ShowParams) -> List<String>) : Runnable {

    @Option(
        names = ["-p", "--path"], paramLabel = "Path", description = ["The path to show it's contents"]
    )
    var path = "."

    @Option(
        names = ["-r", "--recursive"],
        paramLabel = "Files Only",
        description = ["traverse the provided directory recursively"]
    )
    var recursive = false

    override fun run() {
        // although we're not doing any async work here
        // this should work fine for cases where we do
        runBlocking {
            val items = action(env.fileSystem, ShowParams(path, recursive))
            // To ease testing we'd like to separate our command in transformation stages
            // this way we will be able to test each stage independently
            // example: val transformedItems = transformItems(env.anotherService, items)
            // performFinalWork(transformedItems)
            for (item in items) env.logger.println(item)
        }
    }
}
