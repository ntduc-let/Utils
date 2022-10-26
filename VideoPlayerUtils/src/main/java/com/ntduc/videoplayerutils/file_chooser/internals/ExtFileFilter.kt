package com.ntduc.videoplayerutils.file_chooser.internals

import java.io.File
import java.io.FileFilter

/**
 * Created by coco on 6/7/15.
 */
class ExtFileFilter(
    var m_onlyDirectory: Boolean,
    var m_allowHidden: Boolean,
    vararg ext_list: String
) : FileFilter {
    private var m_ext: Array<out String>?

    init {
        m_ext = ext_list
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
        if (m_ext == null) {
            return true
        }
        if (pathname.isDirectory) {
            return true
        }
        val ext = pathname.extension
        for (e in m_ext!!) {
            if (ext.equals(e, ignoreCase = true)) {
                return true
            }
        }
        return false
    }
}