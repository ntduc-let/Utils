package com.ntduc.videoplayerutils.file_chooser

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.text.InputFilter
import android.text.InputType
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.ListPopupWindow
import com.ntduc.videoplayerutils.file_chooser.internals.UiUtil.dip2px
import com.ntduc.videoplayerutils.file_chooser.internals.UiUtil.hideKeyboardFrom
import com.ntduc.videoplayerutils.file_chooser.internals.FileUtil.deleteFileRecursively
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.ntduc.videoplayerutils.R
import com.ntduc.videoplayerutils.file_chooser.internals.FileUtil.NewFolderFilter
import com.ntduc.videoplayerutils.file_chooser.internals.UiUtil.getListYScroll
import java.io.File
import java.io.IOException
import java.lang.NullPointerException
import java.lang.ref.WeakReference

internal class onShowListener(c: ChooserDialog) : DialogInterface.OnShowListener {
    private val _c: WeakReference<ChooserDialog>

    init {
        _c = WeakReference(c)
    }

    @SuppressLint("DiscouragedApi")
    override fun onShow(dialog: DialogInterface) {
        // ensure that the buttons have the right order
        _c.get()!!._neutralBtn = _c.get()!!._alertDialog!!.getButton(AlertDialog.BUTTON_NEUTRAL)
        _c.get()!!._negativeBtn = _c.get()!!._alertDialog!!.getButton(AlertDialog.BUTTON_NEGATIVE)
        _c.get()!!._positiveBtn = _c.get()!!._alertDialog!!.getButton(AlertDialog.BUTTON_POSITIVE)
        val buttonBar = _c.get()!!._positiveBtn?.parent as ViewGroup
        var btnParams = buttonBar.layoutParams
        btnParams.width = ListPopupWindow.MATCH_PARENT
        buttonBar.layoutParams = btnParams
        buttonBar.removeAllViews()
        btnParams = _c.get()!!._neutralBtn?.layoutParams
        if (buttonBar is LinearLayout) {
            (btnParams as LinearLayout.LayoutParams).weight = 1f
            btnParams.width = 0
        }
        if (_c.get()!!._enableOptions) {
            buttonBar.addView(_c.get()!!._neutralBtn, 0, btnParams)
        } else {
            buttonBar.addView(Space(_c.get()!!._context), 0, btnParams)
        }
        buttonBar.addView(_c.get()!!._negativeBtn, 1)
        buttonBar.addView(_c.get()!!._positiveBtn, 2)
        if (_c.get()!!._enableMultiple) {
            _c.get()!!._positiveBtn?.visibility = View.INVISIBLE
        }
        if (_c.get()!!._enableOptions) {
            val buttonColor = _c.get()!!._neutralBtn?.currentTextColor ?: return
            val filter = PorterDuffColorFilter(
                buttonColor,
                PorterDuff.Mode.SRC_IN
            )
            _c.get()!!._neutralBtn?.text = ""
            _c.get()!!._neutralBtn?.visibility = View.VISIBLE
            val dots: Drawable? = if (_c.get()!!._optionsIconRes != -1) {
                ContextCompat.getDrawable(_c.get()!!._context, _c.get()!!._optionsIconRes)
            } else if (_c.get()!!._optionsIcon != null) {
                _c.get()!!._optionsIcon
            } else {
                ContextCompat.getDrawable(_c.get()!!._context, R.drawable.ic_menu_24dp)
            }
            if (dots != null) {
                dots.colorFilter = filter
                _c.get()!!._neutralBtn?.setCompoundDrawablesWithIntrinsicBounds(
                    dots,
                    null,
                    null,
                    null
                )
            }
            class Integer {
                var Int = 0
            }

            val scroll = Integer()
            _c.get()!!._list!!.addOnLayoutChangeListener { v: View, _: Int, _: Int, _: Int, _: Int, _: Int, oldTop: Int, _: Int, oldBottom: Int ->
                val oldHeight = oldBottom - oldTop
                if (v.height != oldHeight) {
                    var offset = oldHeight - v.height
                    val newScroll: Int = _c.get()!!._list?.let { getListYScroll(it) } ?: return@addOnLayoutChangeListener
                    if (scroll.Int != newScroll) offset += scroll.Int - newScroll
                    _c.get()!!._list!!.scrollListBy(offset)
                }
            }
            val showOptions = Runnable {
                if (_c.get()!!._options!!.height == 0) {
                    val viewTreeObserver = _c.get()!!._options!!.viewTreeObserver
                    viewTreeObserver.addOnPreDrawListener(object :
                        ViewTreeObserver.OnPreDrawListener {
                        override fun onPreDraw(): Boolean {
                            if (_c.get()!!._options!!.height <= 0) {
                                return false
                            }
                            viewTreeObserver.removeOnPreDrawListener(this)
                            scroll.Int = _c.get()!!._list?.let { getListYScroll(it) } ?: return true
                            if (_c.get()!!._options!!.parent is FrameLayout) {
                                val params =
                                    _c.get()!!._list!!.layoutParams as ViewGroup.MarginLayoutParams
                                params.bottomMargin = _c.get()!!._options!!.height
                                _c.get()!!._list!!.layoutParams = params
                            }
                            _c.get()!!._options!!.visibility = View.VISIBLE
                            _c.get()!!._options!!.requestFocus()
                            return true
                        }
                    })
                } else {
                    scroll.Int = _c.get()!!._list?.let { getListYScroll(it) } ?: return@Runnable
                    _c.get()!!._options!!.visibility = View.VISIBLE
                    _c.get()!!._options!!.requestFocus()
                    if (_c.get()!!._options!!.parent is FrameLayout) {
                        val params = _c.get()!!._list!!.layoutParams as ViewGroup.MarginLayoutParams
                        params.bottomMargin = _c.get()!!._options!!.height
                        _c.get()!!._list!!.layoutParams = params
                    }
                }
            }
            val hideOptions = Runnable {
                scroll.Int = _c.get()!!._list?.let { getListYScroll(it) } ?: return@Runnable
                _c.get()!!._options!!.visibility = View.GONE
                if (_c.get()!!._options!!.parent is FrameLayout) {
                    val params = _c.get()!!._list!!.layoutParams as ViewGroup.MarginLayoutParams
                    params.bottomMargin = 0
                    _c.get()!!._list!!.layoutParams = params
                }
            }
            _c.get()!!._neutralBtn?.setOnClickShrinkEffectListener(View.OnClickListener {
                if (_c.get()!!._newFolderView != null
                    && _c.get()!!._newFolderView!!.visibility == View.VISIBLE
                ) {
                    return@OnClickListener
                }
                if (_c.get()!!._options == null) {
                    // region Draw options view. (this only happens the first time one clicks on options)
                    // Root view (FrameLayout) of the ListView in the AlertDialog.
                    var rootId = _c.get()!!._context.resources.getIdentifier(
                        "contentPanel",
                        "id",
                        _c.get()!!._context.packageName
                    )
                    var tmpRoot = (dialog as AlertDialog).findViewById<ViewGroup>(rootId)
                    // In case the root id was changed or not found.
                    if (tmpRoot == null) {
                        rootId = _c.get()!!._context.resources.getIdentifier(
                            "contentPanel",
                            "id",
                            "android"
                        )
                        tmpRoot = dialog.findViewById(rootId)
                        if (tmpRoot == null) return@OnClickListener
                    }
                    val root: ViewGroup = tmpRoot

                    // Create options view.
                    val options = FrameLayout(_c.get()!!._context)
                    var params: ViewGroup.MarginLayoutParams
                    if (root is LinearLayout) {
                        params = LinearLayout.LayoutParams(
                            ListPopupWindow.MATCH_PARENT,
                            ListPopupWindow.WRAP_CONTENT
                        )
                        val param = _c.get()!!._list!!.layoutParams as LinearLayout.LayoutParams
                        param.weight = 1f
                        _c.get()!!._list!!.layoutParams = param
                    } else {
                        params = FrameLayout.LayoutParams(
                            ListPopupWindow.MATCH_PARENT,
                            ListPopupWindow.WRAP_CONTENT,
                            Gravity.BOTTOM
                        )
                    }
                    root.addView(options, params)
                    options.isFocusable = false
                    if (root is FrameLayout) {
                        _c.get()!!._list!!.bringToFront()
                    }
                    val ta = _c.get()!!._context.obtainStyledAttributes(R.styleable.FileChooser)
                    val style = ta.getResourceId(
                        R.styleable.FileChooser_fileChooserDialogStyle,
                        R.style.FileChooserDialogStyle
                    )
                    ta.recycle()
                    val buttonContext: Context = ContextThemeWrapper(
                        _c.get()!!._context, style
                    )

                    // Create a button for the option to create a new directory/folder.
                    val createDir = Button(
                        buttonContext, null,
                        android.R.attr.buttonBarButtonStyle
                    )
                    if (_c.get()!!._createDirRes != -1) {
                        createDir.setText(_c.get()!!._createDirRes)
                    } else if (_c.get()!!._createDir != null) {
                        createDir.text = _c.get()!!._createDir
                    } else {
                        createDir.setText(R.string.option_create_folder)
                    }
                    createDir.setTextColor(buttonColor)
                    // Drawable for the button.
                    val plus: Drawable? = if (_c.get()!!._createDirIconRes != -1) {
                        ContextCompat.getDrawable(_c.get()!!._context, _c.get()!!._createDirIconRes)
                    } else if (_c.get()!!._createDirIcon != null) {
                        _c.get()!!._createDirIcon
                    } else {
                        ContextCompat.getDrawable(_c.get()!!._context, R.drawable.ic_add_24dp)
                    }
                    if (plus != null) {
                        plus.colorFilter = filter
                        createDir.setCompoundDrawablesWithIntrinsicBounds(plus, null, null, null)
                    }
                    params = FrameLayout.LayoutParams(
                        ListPopupWindow.WRAP_CONTENT, ListPopupWindow.WRAP_CONTENT,
                        GravityCompat.START or Gravity.CENTER_VERTICAL
                    )
                    params.leftMargin = dip2px(10)
                    options.addView(createDir, params)

                    // Create a button for the option to delete a file.
                    val delete = Button(
                        buttonContext, null,
                        android.R.attr.buttonBarButtonStyle
                    )
                    if (_c.get()!!._deleteRes != -1) {
                        delete.setText(_c.get()!!._deleteRes)
                    } else if (_c.get()!!._delete != null) {
                        delete.text = _c.get()!!._delete
                    } else {
                        delete.setText(R.string.options_delete)
                    }
                    delete.setTextColor(buttonColor)
                    val bin: Drawable? = if (_c.get()!!._deleteIconRes != -1) {
                        ContextCompat.getDrawable(_c.get()!!._context, _c.get()!!._deleteIconRes)
                    } else if (_c.get()!!._deleteIcon != null) {
                        _c.get()!!._deleteIcon
                    } else {
                        ContextCompat.getDrawable(_c.get()!!._context, R.drawable.ic_delete_24dp)
                    }
                    if (bin != null) {
                        bin.colorFilter = filter
                        delete.setCompoundDrawablesWithIntrinsicBounds(bin, null, null, null)
                    }
                    params = FrameLayout.LayoutParams(
                        ListPopupWindow.WRAP_CONTENT, ListPopupWindow.WRAP_CONTENT,
                        GravityCompat.END or Gravity.CENTER_VERTICAL
                    )
                    params.rightMargin = dip2px(10)
                    options.addView(delete, params)
                    _c.get()!!._options = options
                    showOptions.run()

                    // Event Listeners.
                    createDir.setOnClickShrinkEffectListener(object : View.OnClickListener {
                        private var input: EditText? = null
                        override fun onClick(view: View) {
                            //Toast.makeText(getBaseContext(), "new folder clicked", Toast
                            // .LENGTH_SHORT).show();
                            hideOptions.run()
                            var newFolder = File(_c.get()!!._currentDir, "New folder")
                            var i = 1
                            while (newFolder.exists()) {
                                newFolder = File(_c.get()!!._currentDir, "New folder ($i)")
                                i++
                            }
                            if (input != null) {
                                input!!.setText(newFolder.name)
                            }
                            if (_c.get()!!._newFolderView == null) {
                                // region Draw a view with input to create new folder. (this only
                                // happens the first time one clicks on New folder)
                                var ta = _c.get()!!._context.obtainStyledAttributes(
                                    R.styleable.FileChooser
                                )
                                val style = ta.getResourceId(
                                    R.styleable.FileChooser_fileChooserNewFolderStyle,
                                    R.style.FileChooserNewFolderStyle
                                )
                                val context: Context = ContextThemeWrapper(
                                    _c.get()!!._context, style
                                )
                                ta.recycle()
                                ta = context.obtainStyledAttributes(R.styleable.FileChooser)
                                try {
                                    dialog.window!!
                                        .clearFlags(
                                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                                        )
                                    dialog.window!!
                                        .setSoftInputMode(
                                            WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or
                                                    ta.getInt(
                                                        R.styleable.FileChooser_fileChooserNewFolderSoftInputMode,
                                                        0x30
                                                    )
                                        )
                                } catch (e: NullPointerException) {
                                    e.printStackTrace()
                                }

                                // A semitransparent background overlay.
                                val overlay = FrameLayout(
                                    _c.get()!!._context
                                )
                                overlay.setBackgroundColor(
                                    ta.getColor(
                                        R.styleable.FileChooser_fileChooserNewFolderOverlayColor,
                                        0x60ffffff
                                    )
                                )
                                overlay.isScrollContainer = true
                                var params: ViewGroup.MarginLayoutParams = if (root is FrameLayout) {
                                    FrameLayout.LayoutParams(
                                        ListPopupWindow.MATCH_PARENT,
                                        ListPopupWindow.MATCH_PARENT,
                                        Gravity.CENTER
                                    )
                                } else {
                                    LinearLayout.LayoutParams(
                                        ListPopupWindow.MATCH_PARENT, ListPopupWindow.MATCH_PARENT
                                    )
                                }
                                root.addView(overlay, params)
                                overlay.setOnClickShrinkEffectListener(null)
                                overlay.visibility = View.INVISIBLE
                                _c.get()!!._newFolderView = overlay

                                // A LinearLayout and a pair of Space to center views.
                                val linearLayout = LinearLayout(
                                    _c.get()!!._context
                                )
                                params = FrameLayout.LayoutParams(
                                    ListPopupWindow.MATCH_PARENT, ListPopupWindow.WRAP_CONTENT,
                                    Gravity.CENTER
                                )
                                overlay.addView(linearLayout, params)
                                overlay.isFocusable = false
                                var widthWeight = ta.getFloat(
                                    R.styleable.FileChooser_fileChooserNewFolderWidthWeight, 0.56f
                                )
                                if (widthWeight <= 0) widthWeight = 0.56f
                                if (widthWeight > 1f) widthWeight = 1f
                                val leftSpace = Space(_c.get()!!._context)
                                params = LinearLayout.LayoutParams(
                                    0, ListPopupWindow.WRAP_CONTENT,
                                    (1f - widthWeight) / 2
                                )
                                linearLayout.addView(leftSpace, params)
                                leftSpace.isFocusable = false

                                // A solid holder view for the EditText and Buttons.
                                val holder = LinearLayout(
                                    _c.get()!!._context
                                )
                                holder.orientation = LinearLayout.VERTICAL
                                holder.setBackgroundColor(
                                    ta.getColor(
                                        R.styleable.FileChooser_fileChooserNewFolderBackgroundColor,
                                        -0x1
                                    )
                                )
                                val elevation = ta.getInt(
                                    R.styleable.FileChooser_fileChooserNewFolderElevation, 25
                                )
                                holder.elevation = elevation.toFloat()
                                params = LinearLayout.LayoutParams(
                                    0,
                                    ListPopupWindow.WRAP_CONTENT,
                                    widthWeight
                                )
                                linearLayout.addView(holder, params)
                                holder.isFocusable = false
                                val rightSpace = Space(
                                    _c.get()!!._context
                                )
                                params = LinearLayout.LayoutParams(
                                    0, ListPopupWindow.WRAP_CONTENT,
                                    (1f - widthWeight) / 2
                                )
                                linearLayout.addView(rightSpace, params)
                                rightSpace.isFocusable = false
                                val input = EditText(
                                    _c.get()!!._context
                                )
                                val color = ta.getColor(
                                    R.styleable.FileChooser_fileChooserNewFolderTextColor,
                                    buttonColor
                                )
                                input.setTextColor(color)
                                input.background.mutate().setColorFilter(
                                    color,
                                    PorterDuff.Mode.SRC_ATOP
                                )
                                input.setText(newFolder.name)
                                input.setSelectAllOnFocus(true)
                                input.isSingleLine = true
                                // There should be no suggestions, but...
                                input.inputType = (InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                                        or InputType.TYPE_TEXT_VARIATION_FILTER
                                        or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                                input.filters = arrayOf<InputFilter?>(
                                    if (_c.get()!!._newFolderFilter != null) _c.get()!!._newFolderFilter else NewFolderFilter()
                                )
                                input.gravity = Gravity.CENTER_HORIZONTAL
                                input.imeOptions = EditorInfo.IME_ACTION_DONE
                                params = LinearLayout.LayoutParams(
                                    ListPopupWindow.MATCH_PARENT,
                                    ListPopupWindow.WRAP_CONTENT
                                )
                                params.setMargins(3, 2, 3, 0)
                                holder.addView(input, params)
                                this.input = input

                                // A horizontal LinearLayout to hold buttons
                                val buttons = FrameLayout(
                                    _c.get()!!._context
                                )
                                params = LinearLayout.LayoutParams(
                                    ListPopupWindow.MATCH_PARENT,
                                    ListPopupWindow.WRAP_CONTENT
                                )
                                holder.addView(buttons, params)

                                // The Cancel button.
                                val cancel = Button(
                                    buttonContext, null,
                                    android.R.attr.buttonBarButtonStyle
                                )
                                if (_c.get()!!._newFolderCancelRes != -1) {
                                    cancel.setText(_c.get()!!._newFolderCancelRes)
                                } else if (_c.get()!!._newFolderCancel != null) {
                                    cancel.text = _c.get()!!._newFolderCancel
                                } else {
                                    cancel.setText(R.string.new_folder_cancel)
                                }
                                cancel.setTextColor(buttonColor)
                                params = FrameLayout.LayoutParams(
                                    ListPopupWindow.WRAP_CONTENT, ListPopupWindow.WRAP_CONTENT,
                                    GravityCompat.START
                                )
                                buttons.addView(cancel, params)

                                // The OK button.
                                val ok = Button(
                                    buttonContext, null,
                                    android.R.attr.buttonBarButtonStyle
                                )
                                if (_c.get()!!._newFolderOkRes != -1) {
                                    ok.setText(_c.get()!!._newFolderOkRes)
                                } else if (_c.get()!!._newFolderOk != null) {
                                    ok.text = _c.get()!!._newFolderOk
                                } else {
                                    ok.setText(R.string.new_folder_ok)
                                }
                                ok.setTextColor(buttonColor)
                                params = FrameLayout.LayoutParams(
                                    ListPopupWindow.WRAP_CONTENT, ListPopupWindow.WRAP_CONTENT,
                                    GravityCompat.END
                                )
                                buttons.addView(ok, params)
                                val id = cancel.hashCode()
                                cancel.id = id
                                ok.nextFocusLeftId = id
                                input.nextFocusLeftId = id

                                // Event Listeners.
                                input.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
                                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                                        hideKeyboardFrom(
                                            _c.get()!!._context, input
                                        )
                                        _c.get()!!.createNewDirectory(
                                            input.text.toString()
                                        )
                                        overlay.visibility = View.GONE
                                        overlay.clearFocus()
                                        if (_c.get()!!._enableDpad) {
                                            val b = _c.get()!!._neutralBtn
                                            b!!.isFocusable = true
                                            b.requestFocus()
                                            _c.get()!!._list!!.isFocusable = true
                                        }
                                        return@setOnEditorActionListener true
                                    }
                                    false
                                }
                                cancel.setOnClickShrinkEffectListener {
                                    hideKeyboardFrom(
                                        _c.get()!!._context, input
                                    )
                                    overlay.visibility = View.GONE
                                    overlay.clearFocus()
                                    if (_c.get()!!._enableDpad) {
                                        val b = _c.get()!!._neutralBtn
                                        b!!.isFocusable = true
                                        b.requestFocus()
                                        _c.get()!!._list!!.isFocusable = true
                                    }
                                }
                                ok.setOnClickShrinkEffectListener {
                                    hideKeyboardFrom(
                                        _c.get()!!._context, input
                                    )
                                    _c.get()!!.createNewDirectory(
                                        input.text.toString()
                                    )
                                    hideKeyboardFrom(
                                        _c.get()!!._context, input
                                    )
                                    overlay.visibility = View.GONE
                                    overlay.clearFocus()
                                    if (_c.get()!!._enableDpad) {
                                        val b = _c.get()!!._neutralBtn
                                        b!!.isFocusable = true
                                        b.requestFocus()
                                        _c.get()!!._list!!.isFocusable = true
                                    }
                                }
                                ta.recycle()
                                // endregion
                            }
                            if (_c.get()!!._newFolderView!!.visibility != View.VISIBLE) {
                                _c.get()!!._newFolderView!!.visibility = View.VISIBLE
                                if (_c.get()!!._enableDpad) {
                                    _c.get()!!._newFolderView!!.requestFocus()
                                    _c.get()!!._neutralBtn?.isFocusable = false
                                    _c.get()!!._list!!.isFocusable = false
                                }
                                if (_c.get()!!._pathView != null &&
                                    _c.get()!!._pathView!!.visibility == View.VISIBLE
                                ) {
                                    _c.get()!!._newFolderView!!.setPadding(
                                        0, dip2px(32),
                                        0, dip2px(12)
                                    )
                                } else {
                                    _c.get()!!._newFolderView!!.setPadding(
                                        0, dip2px(12),
                                        0, dip2px(12)
                                    )
                                }
                            } else {
                                _c.get()!!._newFolderView!!.visibility = View.GONE
                                if (_c.get()!!._enableDpad) {
                                    _c.get()!!._newFolderView!!.clearFocus()
                                    _c.get()!!._neutralBtn?.isFocusable = true
                                    _c.get()!!._list!!.isFocusable = true
                                }
                            }
                        }
                    })
                    delete.setOnClickShrinkEffectListener {
                        //Toast.makeText(_c.get()._context, "delete clicked", Toast.LENGTH_SHORT).show();
                        hideOptions.run()
                        if (_c.get()!!._chooseMode == ChooserDialog.CHOOSE_MODE_SELECT_MULTIPLE) {
                            var success = true
                            for (file in _c.get()!!._adapter!!.selected) {
                                _c.get()!!._result!!.onChoosePath(file!!.absolutePath, file)
                                if (success) {
                                    try {
                                        deleteFileRecursively(file)
                                    } catch (e: IOException) {
                                        Toast.makeText(
                                            _c.get()!!._context, e.message,
                                            Toast.LENGTH_LONG
                                        ).show()
                                        success = false
                                    }
                                }
                            }
                            _c.get()!!._adapter!!.clearSelected()
                            _c.get()!!._positiveBtn?.visibility = View.INVISIBLE
                            _c.get()!!._chooseMode = ChooserDialog.CHOOSE_MODE_NORMAL
                            _c.get()!!.refreshDirs()
                            return@setOnClickShrinkEffectListener
                        }
                        _c.get()!!._chooseMode =
                            if (_c.get()!!._chooseMode != ChooserDialog.CHOOSE_MODE_DELETE) ChooserDialog.CHOOSE_MODE_DELETE else ChooserDialog.CHOOSE_MODE_NORMAL
                        if (_c.get()!!._deleteModeIndicator == null) {
                            _c.get()!!._deleteModeIndicator = Runnable {
                                if (_c.get()!!._chooseMode == ChooserDialog.CHOOSE_MODE_DELETE) {
                                    val color1 = -0x7f010000
                                    val red = PorterDuffColorFilter(
                                        color1,
                                        PorterDuff.Mode.SRC_IN
                                    )
                                    _c.get()!!._neutralBtn?.compoundDrawables?.get(0)?.colorFilter =
                                        red
                                    _c.get()!!._neutralBtn?.setTextColor(color1)
                                    delete.compoundDrawables[0].colorFilter = red
                                    delete.setTextColor(color1)
                                } else {
                                    _c.get()!!._neutralBtn?.compoundDrawables?.get(0)?.clearColorFilter()
                                    _c.get()!!._neutralBtn?.setTextColor(buttonColor)
                                    delete.compoundDrawables[0].clearColorFilter()
                                    delete.setTextColor(buttonColor)
                                }
                            }
                        }
                        _c.get()!!._deleteModeIndicator!!.run()
                    }
                    // endregion
                } else if (_c.get()!!._options!!.visibility == View.VISIBLE) {
                    hideOptions.run()
                } else {
                    showOptions.run()
                }
            })
        }
    }
}