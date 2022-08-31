package com.prox.fileutils.model

open class BaseAudio(
    title: String? = null,
    displayName: String? = null,
    mimeType: String? = null,
    size: Long? = null,
    dateAdded: Long? = null,
    dateModified: Long? = null,
    data: String? = null,
    var album: String? = null,
    var artist: String? = null,
    var duration: Long? = null
) : BaseFile(title, displayName, mimeType, size, dateAdded, dateModified, data)