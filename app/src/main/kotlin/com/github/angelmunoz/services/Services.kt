/**
 * This package could technically contain classes that implement the services
 * but for our purposes we'll just use functions if we ever need to use classes
 * (for stateful services) we can add them here as well
 */
package com.github.angelmunoz.services

import com.github.angelmunoz.types.IFileSystem
import com.github.angelmunoz.types.ILogger
import java.io.File


fun localFsFactory(): IFileSystem {
    return object : IFileSystem {
        override fun listItems(path: String): List<String> {
            val file = File(path)
            return file.listFiles().orEmpty().map { item -> item.absolutePath }.toList()
        }

        override fun walkDown(path: String): List<String> {
            val file = File(path)
            if (file.isDirectory) {
                return file.walkTopDown().map { item -> item.absolutePath }.toList()
            }
            return listOf()
        }
    }
}

fun simpleLoggerFactory(): ILogger {
    return object : ILogger {
        override fun println(message: String) {
            kotlin.io.println(message)
        }

        override fun printError(message: String) {
            kotlin.io.println(message)
        }
    }
}
