package com.github.angelmunoz.types

import io.github.oshai.kotlinlogging.KLogger


data class ShowParams(val path: String, val recursive: Boolean = false)

interface IFileSystem {
    fun listItems(path: String): List<String>
    fun walkDown(path: String): List<String>
}

interface ApplicationEnvironment {
    val fileSystem: IFileSystem
    fun <TLogOwner> getLogger(cls: Class<TLogOwner>): KLogger
}
