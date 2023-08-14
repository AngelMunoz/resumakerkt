package com.github.angelmunoz.types


data class ShowParams(val path: String, val recursive: Boolean = false)

interface IFileSystem {
    fun listItems(path: String): List<String>
    fun walkDown(path: String): List<String>
}

interface ILogger {
    fun println(message: String): Unit
    fun printError(message: String): Unit
}

interface ApplicationEnvironment {
    val fileSystem: IFileSystem
    val logger: ILogger
}
