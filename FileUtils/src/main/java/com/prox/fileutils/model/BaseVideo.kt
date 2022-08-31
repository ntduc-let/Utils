package com.prox.fileutils.model

open class BaseVideo(
    title: String? = null,
    displayName: String? = null,
    mimeType: String? = null,
    size: Long? = null,
    dateAdded: Long? = null,
    dateModified: Long? = null,
    data: String? = null,
    var height: Long? = null,
    var width: Long? = null,
    var album: String? = null,
    var artist: String? = null,
    var duration: Long? = null,
    var bucketID: Long? = null,
    var bucketDisplayName: String? = null,
    var resolution: String? = null
) : BaseFile(title, displayName, mimeType, size, dateAdded, dateModified, data)