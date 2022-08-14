package com.prox.fileutils.model

open class BaseAudio(
    title: String = "",
    displayName: String = "",
    mineType: String = "",
    size: Long = 0,
    dateAdded: Long = 0,
    dateModified: Long = 0,
    data: String = "",
    var album: String = "",
    var artist: String = "",
    var duration: Long = 0
) : BaseFile(title, displayName, mineType, size, dateAdded, dateModified, data)