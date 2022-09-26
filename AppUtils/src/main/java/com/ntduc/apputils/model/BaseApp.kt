package com.ntduc.apputils.model

import android.graphics.drawable.Drawable

open class BaseApp(
    var name: String? = null,
    var packageName: String? = null,
    var icon: Drawable? = null,
    var storage: Long? = null
)