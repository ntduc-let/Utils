package com.prox.fileutils.model

open class BaseFile(
    var title: String? = null,
    var displayName: String? = null,
    var mimeType: String? = null,
    var size: Long? = null,
    var dateAdded: Long? = null,
    var dateModified: Long? = null,
    var data: String? = null
)