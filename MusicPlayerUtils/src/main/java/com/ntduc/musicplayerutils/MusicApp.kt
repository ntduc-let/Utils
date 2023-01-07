package com.ntduc.musicplayerutils

import android.app.Application

open class MusicApp : Application(){
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        private var instance: MusicApp? = null

        fun getContext(): MusicApp {
            return instance!!
        }
    }
}