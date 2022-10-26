package com.ntduc.playerutils.file_chooser.tool

import java.io.File

class RootFile(path: String, private val name: String) : File(path) {
    override fun getName(): String {
        return name
    }

    override fun isDirectory(): Boolean {
        return true
    }

    override fun isHidden(): Boolean {
        return false
    }

    override fun lastModified(): Long {
        return 0L
    }
}