package com.github.angelmunoz

import com.github.angelmunoz.handlers.gatherFsItems
import com.github.angelmunoz.types.IFileSystem
import com.github.angelmunoz.types.ShowParams
import kotlin.test.Test

/**
 * These are our handler tests
 * since handlers are flexible enough to not need the whole application's environment
 * we can provide fake services that will allow us to test our handlers in isolation
 * and without side effects like reading from disk or logging to the console
 * each test may or may not provide a different parameter instances to test different
 * scenarios
 */
class HandlerTests {

    private val fs = object : IFileSystem {
        override fun listItems(path: String): List<String> {
            return listOf("/dir1", "/file1", "/file2")
        }

        override fun walkDown(path: String): List<String> {
            return listOf("/file1", "/dir1/file1", "/dir2/file1", "/dir2/dir1/file1")
        }
    }

    @Test
    fun getherFsItemsBringsPlainList() {

        val items = gatherFsItems(fs, ShowParams(".", false))

        assert(items.size == 3)
    }

    @Test
    fun getherFsItemsBringsNestedItems() {

        val items = gatherFsItems(fs, ShowParams(".", true))

        assert(items.size == 4)
        assert(items[1] == "/dir1/file1")
    }
}
