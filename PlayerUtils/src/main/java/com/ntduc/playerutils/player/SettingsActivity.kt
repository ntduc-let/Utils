package com.ntduc.playerutils.player

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.RecyclerView
import com.ntduc.playerutils.R
import java.util.*

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= 29) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            window.navigationBarColor = Color.TRANSPARENT
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        if (Build.VERSION.SDK_INT >= 29) {
            val layout = findViewById<LinearLayout>(R.id.settings_layout)
            layout.setOnApplyWindowInsetsListener { view: View, windowInsets: WindowInsets ->
                view.setPadding(
                    windowInsets.systemWindowInsetLeft,
                    windowInsets.systemWindowInsetTop,
                    windowInsets.systemWindowInsetRight,
                    0
                )
                if (recyclerView != null) {
                    recyclerView!!.setPadding(0, 0, 0, windowInsets.systemWindowInsetBottom)
                }
                windowInsets.consumeSystemWindowInsets()
                windowInsets
            }
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val preferenceAutoPiP = findPreference<Preference>("autoPiP")
            if (preferenceAutoPiP != null) {
                preferenceAutoPiP.isEnabled = Utils.isPiPSupported(requireContext())
            }
            val preferenceFrameRateMatching = findPreference<Preference>("frameRateMatching")
            if (preferenceFrameRateMatching != null) {
                preferenceFrameRateMatching.isEnabled = Build.VERSION.SDK_INT >= 23
            }
            val listPreferenceFileAccess = findPreference<ListPreference>("fileAccess")
            if (listPreferenceFileAccess != null) {
                val entries: ArrayList<String> =
                    ArrayList(listOf(*resources.getStringArray(R.array.file_access_entries)))
                val values: ArrayList<String> =
                    ArrayList(listOf(*resources.getStringArray(R.array.file_access_values)))
                if (Build.VERSION.SDK_INT < 30) {
                    val index = values.indexOf("mediastore")
                    entries.removeAt(index)
                    values.removeAt(index)
                }
                if (!Utils.hasSAFChooser(requireContext().packageManager)) {
                    val index = values.indexOf("saf")
                    entries.removeAt(index)
                    values.removeAt(index)
                }
                listPreferenceFileAccess.entries = entries.toTypedArray()
                listPreferenceFileAccess.entryValues = values.toTypedArray()
            }
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            if (Build.VERSION.SDK_INT >= 29) {
                recyclerView = listView
            }
        }
    }

    companion object {
        var recyclerView: RecyclerView? = null
    }
}