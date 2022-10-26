package com.ntduc.videoplayerutils.file_chooser.tool

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.ntduc.videoplayerutils.file_chooser.internals.UiUtil.resolveFileTypeIcon
import com.ntduc.videoplayerutils.file_chooser.internals.FileUtil.getReadableFileSize
import androidx.core.content.ContextCompat
import androidx.collection.SparseArrayCompat
import com.ntduc.videoplayerutils.R
import com.ntduc.videoplayerutils.file_chooser.internals.WrappedDrawable
import java.io.File
import java.lang.IndexOutOfBoundsException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by coco on 6/7/15.
 */
class DirAdapter(cxt: Context?, dateFormat: String?) : ArrayAdapter<File?>(
    cxt!!,
    R.layout.li_row_textview,
    R.id.text,
    listOf()
) {

    @SuppressLint("SimpleDateFormat")
    private fun init(dateFormat: String?) {
        _formatter = SimpleDateFormat(
            if (dateFormat != null && "" != dateFormat.trim { it <= ' ' }) dateFormat.trim { it <= ' ' } else "yyyy/MM/dd HH:mm:ss")
        if (defaultFolderIcon == null) defaultFolderIcon = ContextCompat.getDrawable(
            context, R.drawable.ic_folder
        )
        if (defaultFileIcon == null) defaultFileIcon =
            ContextCompat.getDrawable(context, R.drawable.ic_file)
        val ta = context.obtainStyledAttributes(R.styleable.FileChooser)
        val colorFilter = ta.getColor(
            R.styleable.FileChooser_fileListItemSelectedTint,
            context.resources.getColor(R.color.li_row_background_tint)
        )
        ta.recycle()
        _colorFilter = PorterDuffColorFilter(colorFilter, PorterDuff.Mode.MULTIPLY)
    }

    fun interface GetView {
        /**
         * @param file        file that should me displayed
         * @param isSelected  whether file is selected when _enableMultiple is set to true
         * @param isFocused   @deprecated! use fileListItemFocusedDrawable attribute instead
         * @param convertView see [ArrayAdapter.getView]
         * @param parent      see [ArrayAdapter.getView]
         * @param inflater    a layout inflater with the FileChooser theme wrapped context
         * @return your custom row item view
         */
        fun getView(
            file: File, isSelected: Boolean, isFocused: Boolean, convertView: View?,
            parent: ViewGroup, inflater: LayoutInflater
        ): View
    }

    // This function is called to show each view item
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val file = super.getItem(position) ?: return super.getView(position, convertView, parent)
        val isSelected = _selected[file.hashCode(), null] != null
        if (_getView != null) {
            return _getView!!.getView(
                file, isSelected, false, convertView, parent,
                LayoutInflater.from(context)
            )
        }
        val view = super.getView(position, convertView, parent) as ViewGroup
        val tvName = view.findViewById<TextView>(R.id.text)
        val tvSize = view.findViewById<TextView>(R.id.txt_size)
        val tvDate = view.findViewById<TextView>(R.id.txt_date)
        //ImageView ivIcon = (ImageView) view.findViewById(R.id.icon);
        tvDate.visibility = View.VISIBLE
        tvName.text = file.name
        val icon: Drawable
        if (file.isDirectory) {
            icon = defaultFolderIcon!!.constantState!!.newDrawable()
            tvSize.text = ""
            if (file.lastModified() != 0L) {
                tvDate.text = _formatter!!.format(Date(file.lastModified()))
            } else {
                tvDate.visibility = View.GONE
            }
        } else {
            var d: Drawable? = null
            if (isResolveFileType) {
                d = resolveFileTypeIcon(context, Uri.fromFile(file))
                if (d != null) {
                    d = WrappedDrawable(d, 24F, 24F)
                }
            }
            if (d == null) {
                d = defaultFileIcon
            }
            icon = d!!.constantState!!.newDrawable()
            tvSize.text = getReadableFileSize(file.length())
            tvDate.text = _formatter!!.format(Date(file.lastModified()))
        }
        if (file.isHidden) {
            val filter = PorterDuffColorFilter(
                -0x7f000001,
                PorterDuff.Mode.SRC_ATOP
            )
            icon.mutate().colorFilter = filter
        }
        tvName.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)
        val root = view.findViewById<View>(R.id.root)
        if (root.background == null) {
            root.setBackgroundResource(R.color.li_row_background)
        }
        if (!isSelected) {
            root.background.clearColorFilter()
        } else {
            root.background.colorFilter = _colorFilter
        }
        return view
    }

    fun setEntries(entries: List<File?>?) {
        setNotifyOnChange(false)
        super.clear()
        setNotifyOnChange(true)
        super.addAll(entries!!)
        //_hoveredIndex = -1;
    }

    override fun getItemId(position: Int): Long {
        return try {
            getItem(position).hashCode().toLong()
        } catch (e: IndexOutOfBoundsException) {
            try {
                getItem(0).hashCode().toLong()
            } catch (ex: IndexOutOfBoundsException) {
                0
            }
        }
    }

    fun selectItem(position: Int) {
        val id = getItemId(position).toInt()
        if (_selected[id, null] == null) {
            _selected.append(id, getItem(position))
        } else {
            _selected.delete(id)
        }
        notifyDataSetChanged()
    }

    fun isSelected(position: Int): Boolean {
        return isSelectedById(getItemId(position).toInt())
    }

    private fun isSelectedById(id: Int): Boolean {
        return _selected[id, null] != null
    }

    val isAnySelected: Boolean
        get() = _selected.size() > 0
    val selected: List<File?>
        get() {
            val list = ArrayList<File?>()
            for (i in 0 until _selected.size()) {
                list.add(_selected.valueAt(i))
            }
            return list
        }

    fun clearSelected() {
        try {
            _selected.clear()
        } catch (e: Resources.NotFoundException) {
            _selected = SparseArrayCompat()
        }
    }

    override fun isEmpty(): Boolean {
        return count == 0 || count == 1 && getItem(0) is RootFile
    }

    private var _formatter: SimpleDateFormat? = null
    var defaultFolderIcon: Drawable? = null
    var defaultFileIcon: Drawable? = null
    var isResolveFileType = false
    private var _colorFilter: PorterDuffColorFilter? = null
    private var _selected = SparseArrayCompat<File?>()
    private var _getView: GetView? = null
    val indexStack = Stack<Int>()

    init {
        init(dateFormat)
    }
}