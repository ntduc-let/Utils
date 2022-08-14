package com.prox.fileutils.model

open class BaseVideo(
    title: String = "",
    displayName: String = "",
    mineType: String = "",
    size: Long = 0,
    dateAdded: Long = 0,
    dateModified: Long = 0,
    data: String = "",
    var height: Long = 0,
    var width: Long = 0,
    var album: String = "",
    var artist: String = "",
    var duration: Long = 0,
    var bucketID: Long = 0,
    var bucketDisplayName: String = "",
    var resolution: String = ""
) : BaseFile(title, displayName, mineType, size, dateAdded, dateModified, data)