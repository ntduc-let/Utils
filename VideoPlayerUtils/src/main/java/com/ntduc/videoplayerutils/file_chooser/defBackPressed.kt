package com.ntduc.videoplayerutils.file_chooser

import androidx.appcompat.app.AlertDialog
import com.ntduc.videoplayerutils.file_chooser.ChooserDialog.OnBackPressedListener
import androidx.appcompat.app.AppCompatDialog
import java.lang.ref.WeakReference

internal class defBackPressed(e: ChooserDialog) : OnBackPressedListener {
    private val _c: WeakReference<ChooserDialog>

    override fun onBackPressed(dialog: AlertDialog?) {
        if (_c.get()!!._entries.size > 0
            && _c.get()!!._entries[0]!!.name == ".."
        ) {
            if (_onBackPressed != null) {
                _onBackPressed!!.onBackPressed(dialog)
            } else {
                _defaultBack.onBackPressed(dialog)
            }
        } else {
            if (_onLastBackPressed != null) {
                _onLastBackPressed!!.onBackPressed(dialog)
            } else {
                _defaultLastBack.onBackPressed(dialog)
            }
        }
    }

    var _onBackPressed: OnBackPressedListener? = null
    var _onLastBackPressed: OnBackPressedListener? = null

    init {
        _c = WeakReference(e)
    }

    companion object {
        private val _defaultLastBack =
            OnBackPressedListener { obj: AppCompatDialog? -> obj!!.cancel() }
        private val _defaultBack = OnBackPressedListener { obj: AppCompatDialog? -> obj!!.cancel() }
    }
}