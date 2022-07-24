package com.ntduc.contextutils

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

fun Context.showDialog(title: String, msg: String) =
    AlertDialog.Builder(this).setTitle(title).setMessage(msg).show()

fun Context.showConfirmationDialog(
    title: String,
    msg: String,
    onResponse: (result: Boolean) -> Unit,
    positiveText: String = "Yes",
    negativeText: String = "No",
    cancelable: Boolean = false
) =
    AlertDialog.Builder(this).setTitle(title).setMessage(msg)
        .setPositiveButton(positiveText) { _, _ -> onResponse(true) }.setNegativeButton(
        negativeText
    ) { _, _ -> onResponse(false) }.setCancelable(cancelable).show()

fun Context.showSinglePicker(
    title: String,
    choices: Array<String>,
    onResponse: (index: Int) -> Unit,
    checkedItemIndex: Int = -1
) =
    AlertDialog.Builder(this).setTitle(title)
        .setSingleChoiceItems(choices, checkedItemIndex) { dialog, which ->
            onResponse(which)
            dialog.dismiss()
        }.show()

fun Context.showMultiPicker(
    title: String,
    choices: Array<String>,
    onResponse: (index: Int, isChecked: Boolean) -> Unit,
    checkedItems: BooleanArray? = null
) =
    AlertDialog.Builder(this).setTitle(title)
        .setMultiChoiceItems(choices, checkedItems) { _, which, isChecked ->
            onResponse(
                which,
                isChecked
            )
        }.setPositiveButton("Done", null).show()