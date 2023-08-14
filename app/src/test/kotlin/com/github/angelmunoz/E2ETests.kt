package com.github.angelmunoz

import com.github.angelmunoz.cli_options.ShowDirectoryItems
import com.github.angelmunoz.handlers.gatherFsItems
import com.github.angelmunoz.types.ApplicationEnvironment
import com.github.angelmunoz.types.IFileSystem
import com.github.angelmunoz.types.ILogger
import picocli.CommandLine
import kotlin.test.Test

/**
 * this is a factory function that provides us with a new environment with
 * fake implementations of our dependencies, this will allow us to control
 * side effectual operations and assert on them like reading from disk
 * or logging to the console
 */
fun makeTestEnvironment(stdout: MutableList<String>, stderr: MutableList<String>): ApplicationEnvironment {
    return object : ApplicationEnvironment {
        override val fileSystem: IFileSystem
            get() = object : IFileSystem {
                override fun listItems(path: String): List<String> {
                    return listOf("/dir1", "/file1", "/file2")
                }

                override fun walkDown(path: String): List<String> {
                    return listOf("/file1", "/dir1/file1", "/dir2/file1", "/dir2/dir1/file1")
                }
            }
        override val logger: ILogger
            get() = object : ILogger {
                override fun println(message: String) {
                    stdout.add(message)
                }

                override fun printError(message: String) {
                    stderr.add(message)
                }
            }
    }
}

/**
 * These E2E tests are meant to test command line options and their interactions
 * with our provided environment and fake services
 * each test should arrange a new environment, register the necessary commands, subcommands
 * and options and then execute the command line
 */
class E2ETests {

    @Test
    fun showDirectoryItemsNonRecursive() {

        val stdoutSink = mutableListOf<String>()
        val stderrSink = mutableListOf<String>()

        val testEnv = makeTestEnvironment(stdoutSink, stderrSink)

        val cli = CommandLine(ShowDirectoryItems(testEnv, ::gatherFsItems))
        // act
        val result = cli.execute("-p", "./")
        assert(result == 0)
        assert(stdoutSink.size == 3)
        assert(stdoutSink[1] == "/file1")
    }

    @Test
    fun showDirectoryItemsRecursive() {

        val stdoutSink = mutableListOf<String>()
        val stderrSink = mutableListOf<String>()

        val testEnv = makeTestEnvironment(stdoutSink, stderrSink)

        val cli = CommandLine(ShowDirectoryItems(testEnv, ::gatherFsItems))
        // act
        val result = cli.execute("-p", "./", "-r")
        assert(result == 0)
        assert(stdoutSink.size == 4)
        assert(stdoutSink[1] == "/dir1/file1")
    }
}
