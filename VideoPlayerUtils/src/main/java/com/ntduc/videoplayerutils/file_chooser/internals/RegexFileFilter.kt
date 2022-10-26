package com.ntduc.videoplayerutils.file_chooser.internals

import java.io.File
import java.io.FileFilter
import java.util.regex.Pattern

/**
 * Created by coco on 6/7/15.
 */
class RegexFileFilter(dirOnly: Boolean, hidden: Boolean, ptn: String, flags: Int) : FileFilter {
    var m_allowHidden: Boolean = hidden
    var m_onlyDirectory: Boolean = dirOnly
    var m_pattern: Pattern?

    init {
        m_pattern = Pattern.compile(ptn, flags)
    }

    override fun accept(pathname: File): Boolean {
        if (!m_allowHidden) {
            if (pathname.isHidden) {
                return false
            }
        }
        if (m_onlyDirectory) {
            if (!pathname.isDirectory) {
                return false
            }
        }
        if (m_pattern == null) {
            return true
        }
        if (pathname.isDirectory) {
            return true
        }
        val name = pathname.name
        return m_pattern!!.matcher(name).matches()
    }
}