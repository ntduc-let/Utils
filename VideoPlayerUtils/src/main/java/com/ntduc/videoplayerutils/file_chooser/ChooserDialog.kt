package com.ntduc.videoplayerutils.file_chooser

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.*
import com.ntduc.videoplayerutils.file_chooser.internals.FileUtil.getStoragePath
import com.ntduc.videoplayerutils.file_chooser.permissions.PermissionsUtil.checkPermissions
import com.ntduc.videoplayerutils.file_chooser.internals.FileUtil.getStoragePaths
import com.ntduc.videoplayerutils.file_chooser.internals.FileUtil.createNewDirectory
import com.ntduc.videoplayerutils.file_chooser.internals.FileUtil.deleteFileRecursively
import androidx.annotation.StyleRes
import com.ntduc.videoplayerutils.file_chooser.internals.ExtFileFilter
import androidx.annotation.StringRes
import androidx.annotation.DrawableRes
import com.ntduc.videoplayerutils.file_chooser.internals.FileUtil.NewFolderFilter
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.ListPopupWindow
import com.ntduc.videoplayerutils.file_chooser.tool.DirAdapter
import com.ntduc.videoplayerutils.file_chooser.ChooserDialog.CanNavigateUp
import com.ntduc.videoplayerutils.file_chooser.ChooserDialog.CanNavigateTo
import com.ntduc.videoplayerutils.file_chooser.permissions.PermissionsUtil.OnPermissionListener
import com.ntduc.videoplayerutils.R
import com.ntduc.videoplayerutils.file_chooser.tool.RootFile
import java.io.File
import java.io.FileFilter
import java.io.IOException
import java.util.*
import java.util.Collections.sort

/**
 * Created by coco on 6/7/15.
 */
class ChooserDialog(activity: Activity, @StyleRes fileChooserTheme: Int) :
    AdapterView.OnItemClickListener, DialogInterface.OnClickListener,
    AdapterView.OnItemLongClickListener, AdapterView.OnItemSelectedListener {

    fun interface Result {
        fun onChoosePath(dir: String?, dirFile: File?)
    }

    private fun init(@StyleRes fileChooserTheme: Int? = null) {
        _onBackPressed = defBackPressed(this)
        _context = if (fileChooserTheme == null) {
            val typedValue = TypedValue()
            if (!_context.theme.resolveAttribute(
                    R.attr.fileChooserStyle, typedValue, true
                )
            ) {
                ContextThemeWrapper(_context, R.style.FileChooserStyle)
            } else {
                ContextThemeWrapper(_context, typedValue.resourceId)
            }
        } else {
            ContextThemeWrapper(_context, fileChooserTheme.toInt())
        }
    }

    fun withFilter(
        dirOnly: Boolean,
        allowHidden: Boolean,
        vararg suffixes: String
    ): ChooserDialog {
        _dirOnly = dirOnly
        _fileFilter = if (suffixes.isEmpty()) {
            if (dirOnly) FileFilter { file: File -> file.isDirectory && (!file.isHidden || allowHidden) } else FileFilter { file: File -> !file.isHidden || allowHidden }
        } else {
            ExtFileFilter(_dirOnly, allowHidden, *suffixes)
        }
        return this
    }

    fun withStartFile(startFile: String?): ChooserDialog {
        _currentDir = if (startFile != null) {
            File(startFile)
        } else {
            getStoragePath(_context, false)?.let { File(it) }
        }
        if (!_currentDir!!.isDirectory) {
            _currentDir = _currentDir!!.parentFile
        }
        if (_currentDir == null) {
            _currentDir = getStoragePath(_context, false)?.let { File(it) }
        }
        return this
    }

    fun withChosenListener(r: Result?): ChooserDialog {
        _result = r
        return this
    }

    /**
     * called every time [KeyEvent.KEYCODE_BACK] is caught,
     * and current directory is not the root of Primary/SdCard storage.
     */
    fun withOnBackPressedListener(listener: OnBackPressedListener?): ChooserDialog {
        if (_onBackPressed is defBackPressed) {
            (_onBackPressed as defBackPressed?)!!._onBackPressed = listener
        }
        return this
    }

    /**
     * called if [KeyEvent.KEYCODE_BACK] is caught,
     * and current directory is the root of Primary/SdCard storage.
     */
    fun withOnLastBackPressedListener(listener: OnBackPressedListener?): ChooserDialog {
        if (_onBackPressed is defBackPressed) {
            (_onBackPressed as defBackPressed?)!!._onLastBackPressed = listener
        }
        return this
    }

    /**
     * onCancelListener will be triggered on back pressed or clicked outside of dialog
     */
    fun withOnCancelListener(listener: DialogInterface.OnCancelListener?): ChooserDialog {
        _cancelListener = listener
        return this
    }

    fun build(): ChooserDialog {
        var ta = _context.obtainStyledAttributes(R.styleable.FileChooser)
        val builder = AlertDialog.Builder(
            _context,
            ta.getResourceId(
                R.styleable.FileChooser_fileChooserDialogStyle,
                R.style.FileChooserDialogStyle
            )
        )
        val style = ta.getResourceId(
            R.styleable.FileChooser_fileChooserListItemStyle,
            R.style.FileChooserListItemStyle
        )
        ta.recycle()
        val context: Context = ContextThemeWrapper(_context, style)
        ta = context.obtainStyledAttributes(R.styleable.FileChooser)
        val listview_item_selector = ta.getResourceId(
            R.styleable.FileChooser_fileListItemFocusedDrawable,
            R.drawable.listview_item_selector
        )
        ta.recycle()
        _adapter = DirAdapter(context, _dateFormat)
        if (_adapterSetter != null) _adapterSetter!!.apply(_adapter)
        refreshDirs()
        builder.setAdapter(_adapter, this)
        if (!_disableTitle) {
            if (_titleRes != -1) {
                builder.setTitle(_titleRes)
            } else if (_title != null) {
                builder.setTitle(_title)
            } else {
                builder.setTitle(R.string.choose_file)
            }
        }
        if (_iconRes != -1) {
            builder.setIcon(_iconRes)
        } else if (_icon != null) {
            builder.setIcon(_icon)
        }
        if (_layoutRes != -1) {
            builder.setView(_layoutRes)
        }
        if (_dirOnly || _enableMultiple) {
            // choosing folder, or multiple files picker
            val listener = DialogInterface.OnClickListener { _: DialogInterface?, _: Int ->
                if (_result != null) {
                    _result!!.onChoosePath(_currentDir!!.absolutePath, _currentDir)
                }
            }
            if (_okRes != -1) {
                builder.setPositiveButton(_okRes, listener)
            } else if (_ok != null) {
                builder.setPositiveButton(_ok, listener)
            } else {
                builder.setPositiveButton(R.string.title_choose, listener)
            }
        }
        if (_negativeRes != -1) {
            builder.setNegativeButton(_negativeRes, _negativeListener)
        } else if (_negative != null) {
            builder.setNegativeButton(_negative, _negativeListener)
        } else {
            builder.setNegativeButton(R.string.dialog_cancel, _negativeListener)
        }
        if (_cancelListener != null) {
            builder.setOnCancelListener(_cancelListener)
        }
        if (_onDismissListener != null) {
            builder.setOnDismissListener(_onDismissListener)
        }
        builder.setOnKeyListener(keyListener(this))
        _alertDialog = builder.create()
        _alertDialog!!.setCanceledOnTouchOutside(_cancelOnTouchOutside)
        _alertDialog!!.setOnShowListener(onShowListener(this))
        _list = _alertDialog!!.listView
        _list?.setOnItemClickListener(this)
        if (_enableMultiple) {
            _list?.setOnItemLongClickListener(this)
        }
        if (_enableDpad) {
            _list?.setSelector(listview_item_selector)
            _list?.setDrawSelectorOnTop(true)
            _list?.setItemsCanFocus(true)
            _list?.setOnItemSelectedListener(this)
            _list?.setChoiceMode(ListView.CHOICE_MODE_SINGLE)
        }
        _list?.requestFocus()
        return this
    }

    private fun showDialog() {
        val window = _alertDialog!!.window
        if (window != null) {
            val ta = _context.obtainStyledAttributes(R.styleable.FileChooser)
            window.setGravity(
                ta.getInt(
                    R.styleable.FileChooser_fileChooserDialogGravity,
                    Gravity.CENTER
                )
            )
            ta.recycle()
        }
        _alertDialog!!.show()
    }

    fun show(): ChooserDialog {
        if (_alertDialog == null || _list == null) {
            build()
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            showDialog()
            return this
        }
        if (_permissionListener == null) {
            _permissionListener = object : OnPermissionListener {
                override fun onPermissionGranted(permissions: Array<out String?>?) {
                    var show = false
                    for (permission in permissions!!) {
                        if (permission == Manifest.permission.READ_EXTERNAL_STORAGE
//                            || Build.VERSION.SDK_INT >= 33 && permission == Manifest.permission.READ_MEDIA_VIDEO
                        ) {
                            show = true
                            break
                        }
                    }
                    if (!show) return
                    if (_enableOptions) {
                        show = false
                        for (permission in permissions) {
                            if (permission == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                                show = true
                                break
                            }
                        }
                    }
                    if (!show) return
                    if (_adapter!!.isEmpty) refreshDirs()
                    showDialog()
                }

                override fun onPermissionDenied(permissions: Array<String?>?) {
                    //
                }

                override fun onShouldShowRequestPermissionRationale(permissions: Array<String?>?) {
                    Toast.makeText(
                        _context, "You denied the Read/Write permissions on SDCard.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        var permissions = arrayOf<String?>(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) /*: new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}*/
//        if (Build.VERSION.SDK_INT >= 33 && _context.applicationInfo.targetSdkVersion >= 33) {
//            permissions = arrayOf(Manifest.permission.READ_MEDIA_VIDEO)
//        }
        checkPermissions(_context, _permissionListener, *permissions)
        return this
    }

    private var displayRoot = false
    @SuppressLint("DiscouragedApi")
    private fun displayPath(path: String?) {
        var path = path
        if (_pathView == null) {
            var rootId =
                _context.resources.getIdentifier("contentPanel", "id", _context.packageName)
            var root = _alertDialog!!.findViewById<ViewGroup>(rootId)
            // In case the root id was changed or not found.
            if (root == null) {
                rootId = _context.resources.getIdentifier("contentPanel", "id", "android")
                root = _alertDialog!!.findViewById(rootId)
                if (root == null) return
            }
            val params: ViewGroup.MarginLayoutParams = if (root is LinearLayout) {
                LinearLayout.LayoutParams(
                    ListPopupWindow.MATCH_PARENT,
                    ListPopupWindow.WRAP_CONTENT
                )
            } else {
                FrameLayout.LayoutParams(
                    ListPopupWindow.MATCH_PARENT,
                    ListPopupWindow.WRAP_CONTENT,
                    Gravity.TOP
                )
            }
            var ta = _context.obtainStyledAttributes(R.styleable.FileChooser)
            val style = ta.getResourceId(
                R.styleable.FileChooser_fileChooserPathViewStyle,
                R.style.FileChooserPathViewStyle
            )
            val context: Context = ContextThemeWrapper(_context, style)
            ta.recycle()
            ta = context.obtainStyledAttributes(R.styleable.FileChooser)
            displayRoot =
                ta.getBoolean(R.styleable.FileChooser_fileChooserPathViewDisplayRoot, true)
            _pathView = TextView(context)
            root.addView(_pathView, 0, params)
            val elevation = ta.getInt(R.styleable.FileChooser_fileChooserPathViewElevation, 2)
            _pathView!!.elevation = elevation.toFloat()
            ta.recycle()
            if (_customizePathView != null) {
                _customizePathView!!.customize(_pathView)
            }
        }
        if (path == null) {
            _pathView!!.visibility = View.GONE
            val param = _list!!.layoutParams as ViewGroup.MarginLayoutParams
            if (_pathView!!.parent is FrameLayout) {
                param.topMargin = 0
            }
            _list!!.layoutParams = param
        } else {
            if (roots == null) {
                roots = getStoragePaths(_context)!!.keys
            }
            for (key in roots!!) {
                if (path!!.contains(key)) {
                    path = path.substring(if (displayRoot) key.lastIndexOf('/') + 1 else key.length)
                    break
                }
            }
            _pathView!!.text = path
            while (_pathView!!.lineCount > 1) {
                var i = path!!.indexOf("/")
                i = path.indexOf("/", i + 1)
                if (i == -1) break
                path = "..." + path.substring(i)
                _pathView!!.text = path
            }
            _pathView!!.visibility = View.VISIBLE
            val param = _list!!.layoutParams as ViewGroup.MarginLayoutParams
            if (_pathView!!.height == 0) {
                val viewTreeObserver = _pathView!!.viewTreeObserver
                viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        if (_pathView!!.height <= 0) {
                            return false
                        }
                        viewTreeObserver.removeOnPreDrawListener(this)
                        if (_pathView!!.parent is FrameLayout) {
                            param.topMargin = _pathView!!.height
                        }
                        _list!!.layoutParams = param
                        _list!!.post { _list!!.setSelection(0) }
                        return true
                    }
                })
            } else {
                if (_pathView!!.parent is FrameLayout) {
                    param.topMargin = _pathView!!.height
                }
                _list!!.layoutParams = param
            }
        }
    }

    private var roots: Set<String>? = null
    private fun listDirs() {
        _entries.clear()
        if (_currentDir == null) {
            _currentDir = getStoragePath(_context, false)?.let { File(it) }
        }

        // Get files
        val files = _currentDir!!.listFiles(_fileFilter)

        // Add the ".." entry
        val storagePaths = getStoragePaths(_context)
        val storageKeys: Set<String> = storagePaths!!.keys
        var withinVolume = false
        for (storageKey in storageKeys) {
            if (_currentDir!!.absolutePath.startsWith(storageKey)) {
                withinVolume = true
                break
            }
        }
        if (!withinVolume) {
            for (storageKey in storageKeys) {
                _entries.add(RootFile(storageKey, storagePaths[storageKey]!!)) //⇠
            }
        }
        var displayPath = false
        if (_entries.isEmpty() /*&& _currentDir.getParentFile() != null && _currentDir.getParentFile().canRead()*/) {
            _entries.add(_currentDir!!.parentFile?.let { RootFile(it.absolutePath, "..") })
            displayPath = true
        }
        if (files == null || !withinVolume) {
            if (_alertDialog != null && _alertDialog!!.isShowing && _displayPath) {
                displayPath(null)
            }
            return
        }
        val dirList: MutableList<File?> = LinkedList()
        val fileList: MutableList<File?> = LinkedList()
        for (f in files) {
            if (f.isDirectory) {
                dirList.add(f)
            } else {
                fileList.add(f)
            }
        }
        sortByName(dirList)
        sortByName(fileList)
        _entries.addAll(dirList)
        _entries.addAll(fileList)

        // #45: setup dialog title too
        if (_alertDialog != null && !_disableTitle) {
            if (_followDir) {
                if (displayPath) {
                    _alertDialog!!.setTitle(_currentDir!!.name)
                } else {
                    if (_titleRes != -1) {
                        _alertDialog!!.setTitle(_titleRes)
                    } else if (_title != null) {
                        _alertDialog!!.setTitle(_title)
                    } else {
                        _alertDialog!!.setTitle(R.string.choose_file)
                    }
                }
            }
        }

        // don't display path before alert dialog is shown
        // to avoid the exception under android M:
        //   Caused by android.util.AndroidRuntimeException: requestFeature() must be called before adding
        // content
        // issue #60
        if (_alertDialog != null && _alertDialog!!.isShowing && _displayPath) {
            if (displayPath) {
                displayPath(_currentDir!!.path)
            } else {
                displayPath(null)
            }
        }
    }

    private fun sortByName(list: List<File?>) {
        sort(list) { f1: File?, f2: File? ->
            f1!!.name.lowercase(Locale.getDefault()).compareTo(
                f2!!.name.lowercase(Locale.getDefault())
            )
        }
    }

    fun createNewDirectory(name: String?) {
        if (createNewDirectory(name!!, _currentDir)) {
            refreshDirs()
            return
        }
        val newDir = File(_currentDir, name)
        Toast.makeText(
            _context,
            "Couldn't create folder " + newDir.name + " at " + newDir.absolutePath,
            Toast.LENGTH_LONG
        ).show()
    }

    @JvmField
    var _deleteModeIndicator: Runnable? = null
    private var scrollTo = 0
    override fun onItemClick(parent_: AdapterView<*>?, list_: View, position: Int, id_: Long) {
        if (position < 0 || position >= _entries.size) return
        scrollTo = 0
        val file = _entries[position]
        if (file is RootFile) {
            if (_folderNavUpCB == null) _folderNavUpCB = _defaultNavUpCB
            /*if (_folderNavUpCB.canUpTo(file))*/run {
                _currentDir = file
                _chooseMode =
                    if (_chooseMode == CHOOSE_MODE_DELETE) CHOOSE_MODE_NORMAL else _chooseMode
                if (_deleteModeIndicator != null) _deleteModeIndicator!!.run()
                lastSelected = false
                if (!_adapter!!.indexStack.empty()) {
                    scrollTo = _adapter!!.indexStack.pop()
                }
            }
        } else {
            when (_chooseMode) {
                CHOOSE_MODE_NORMAL -> {
                    if (file!!.isDirectory) {
                        if (_folderNavToCB == null) _folderNavToCB = _defaultNavToCB
                        if (_folderNavToCB!!.canNavigate(file)) {
                            _currentDir = file
                            scrollTo = 0
                            _adapter!!.indexStack.push(position)
                        }
                    } else if (!_dirOnly && _result != null) {
                        _alertDialog!!.dismiss()
                        _result!!.onChoosePath(file.absolutePath, file)
                        if (_enableMultiple) {
                            _result!!.onChoosePath(_currentDir!!.absolutePath, _currentDir)
                        }
                        return
                    }
                    lastSelected = false
                }
                CHOOSE_MODE_SELECT_MULTIPLE -> if (file!!.isDirectory) {
                    if (_folderNavToCB == null) _folderNavToCB = _defaultNavToCB
                    if (_folderNavToCB!!.canNavigate(file)) {
                        _currentDir = file
                        scrollTo = 0
                        _adapter!!.indexStack.push(position)
                    }
                } else {
                    _adapter!!.selectItem(position)
                    if (!_adapter!!.isAnySelected) {
                        _chooseMode = CHOOSE_MODE_NORMAL
                        _positiveBtn!!.visibility = View.INVISIBLE
                    }
                    _result!!.onChoosePath(file.absolutePath, file)
                    return
                }
                CHOOSE_MODE_DELETE -> {
                    try {
                        deleteFileRecursively(file!!)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(_context, e.message, Toast.LENGTH_LONG).show()
                    }
                    _chooseMode = CHOOSE_MODE_NORMAL
                    if (_deleteModeIndicator != null) _deleteModeIndicator!!.run()
                    scrollTo = -1
                }
                else ->                     // ERROR! It shouldn't get here...
                    return
            }
        }
        refreshDirs()
        if (scrollTo != -1) {
            _list!!.setSelection(scrollTo)
            _list!!.post { _list!!.setSelection(scrollTo) }
        }
    }

    override fun onItemLongClick(
        parent: AdapterView<*>?,
        list: View,
        position: Int,
        id: Long
    ): Boolean {
        val file = _entries[position]
        if (file is RootFile || file!!.isDirectory) {
            return true
        }
        if (_adapter!!.isSelected(position)) return true
        _result!!.onChoosePath(file.absolutePath, file)
        _adapter!!.selectItem(position)
        _chooseMode = CHOOSE_MODE_SELECT_MULTIPLE
        _positiveBtn!!.visibility = View.VISIBLE
        if (_deleteModeIndicator != null) _deleteModeIndicator!!.run()
        return true
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        //
    }

    fun refreshDirs() {
        listDirs()
        _adapter!!.setEntries(_entries)
    }


    var lastSelected = false
    override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        lastSelected = position == _entries.size - 1
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        lastSelected = false
    }

    var _entries: MutableList<File?> = ArrayList()

    var _adapter: DirAdapter? = null

    var _currentDir: File? = null

    var _context: Context = activity

    var _alertDialog: AlertDialog? = null

    var _list: ListView? = null

    var _result: Result? = null
    private var _dirOnly = false
    private var _fileFilter: FileFilter? = null

    @StringRes
    private var _titleRes = -1

    @StringRes
    private var _okRes = -1

    @StringRes
    private var _negativeRes = -1
    private var _title: String? = null
    private var _ok: String? = null
    private var _negative: String? = null

    @DrawableRes
    private var _iconRes = -1
    private var _icon: Drawable? = null

    @LayoutRes
    private var _layoutRes = -1
    private var _dateFormat: String? = null
    private var _negativeListener: DialogInterface.OnClickListener? = null
    private var _cancelListener: DialogInterface.OnCancelListener? = null
    private var _onDismissListener: DialogInterface.OnDismissListener? = null
    private var _disableTitle = false

    var _enableOptions = false
    private var _followDir = false
    private var _displayPath = true

    var _pathView: TextView? = null
    private var _customizePathView: CustomizePathView? = null

    var _options: View? = null

    @StringRes
    var _createDirRes = -1

    @StringRes
    var _deleteRes = -1

    @StringRes
    var _newFolderCancelRes = -1

    @StringRes
    var _newFolderOkRes = -1

    var _createDir: String? = null

    var _delete: String? = null

    var _newFolderCancel: String? = null

    var _newFolderOk: String? = null

    @DrawableRes
    var _optionsIconRes = -1

    @DrawableRes
    var _createDirIconRes = -1

    @DrawableRes
    var _deleteIconRes = -1

    var _optionsIcon: Drawable? = null

    var _createDirIcon: Drawable? = null

    var _deleteIcon: Drawable? = null

    var _newFolderView: View? = null

    var _enableMultiple = false
    private var _permissionListener: OnPermissionListener? = null
    private var _cancelOnTouchOutside = false

    var _enableDpad: Boolean = true

    var _neutralBtn: Button? = null

    var _negativeBtn: Button? = null

    var _positiveBtn: Button? = null

    fun interface AdapterSetter {
        fun apply(adapter: DirAdapter?)
    }

    private var _adapterSetter: AdapterSetter? = null

    fun interface CanNavigateUp {
        fun canUpTo(dir: File?): Boolean
    }

    fun interface CanNavigateTo {
        fun canNavigate(dir: File?): Boolean
    }

    private var _folderNavUpCB: CanNavigateUp? = null
    private var _folderNavToCB: CanNavigateTo? = null

    /**
     * attempts to move to the parent directory
     *
     * @return true if successful. false otherwise
     */
    fun goBack(): Boolean {
        if (_entries.size > 0 &&
            _entries[0]!!.name == ".."
        ) {
            _list!!.performItemClick(_list, 0, 0)
            return true
        }
        return false
    }

    fun interface OnBackPressedListener {
        fun onBackPressed(dialog: AlertDialog?)
    }

    var _onBackPressed: OnBackPressedListener? = null

    var _chooseMode = CHOOSE_MODE_NORMAL

    var _newFolderFilter: NewFolderFilter? = null

    fun interface CustomizePathView {
        fun customize(pathView: TextView?)
    }

    companion object {
        private val _defaultNavUpCB = CanNavigateUp { dir: File? -> dir != null && dir.canRead() }
        private val _defaultNavToCB = CanNavigateTo { true }
        const val CHOOSE_MODE_NORMAL = 0
        const val CHOOSE_MODE_DELETE = 1
        const val CHOOSE_MODE_SELECT_MULTIPLE = 2
    }

    init {
        init(fileChooserTheme)
    }
}