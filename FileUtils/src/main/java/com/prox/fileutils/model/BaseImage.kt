package com.prox.fileutils.model

open class BaseImage(
    title: String = "",
    displayName: String = "",
    mineType: String = "",
    size: Long = 0,
    dateAdded: Long = 0,
    dateModified: Long = 0,
    data: String = "",
    var height: Long = 0,
    var width: Long = 0
) : BaseFile(title, displayName, mineType, size, dateAdded, dateModified, data)