package com.prox.fileutils.model

open class BaseFile(
    var title: String = "",
    var displayName: String = "",
    var mineType: String = "",
    var size: Long = 0,
    var dateAdded: Long = 0,
    var dateModified: Long = 0,
    var data: String = ""
)