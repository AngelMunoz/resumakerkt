/**
 * This package contain our command handlers
 * functions here should have defined inputs -> outputs
 * that will ease testing and maintenance
 *
 * When adding new handlers, think about how will they compose together with other handlers
 * as if they were part of a pipeline, this will help us keep our handlers small and focused.
 */
package com.github.angelmunoz.handlers

import com.github.angelmunoz.types.IFileSystem
import com.github.angelmunoz.types.ShowParams


fun gatherFsItems(fs: IFileSystem, params: ShowParams): List<String> {
    // dumbed down example, we could perform much more work here
    return if (params.recursive) fs.walkDown(params.path)
    else fs.listItems(params.path)
}
