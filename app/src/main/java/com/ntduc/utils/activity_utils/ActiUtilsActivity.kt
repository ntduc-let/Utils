package com.ntduc.utils.activity_utils

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import com.ntduc.activityutils.*
import com.ntduc.clickeffectutils.setOnClickShrinkEffectListener
import com.ntduc.toastutils.shortToast
import com.ntduc.utils.BuildConfig
import com.ntduc.utils.R
import com.ntduc.utils.databinding.ActivityActiUtilsBinding

class ActiUtilsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityActiUtilsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnHideNavigationBar.setOnClickShrinkEffectListener {
            hideNavigationBar()
        }
        binding.btnShowNavigationBar.setOnClickShrinkEffectListener {
            showNavigationBar()
        }
        binding.btnGetNavigationBarHeight.setOnClickShrinkEffectListener {
            shortToast("NavigationBar Height: $getNavigationBarHeight px")
        }
        binding.btnHideStatusBar.setOnClickShrinkEffectListener {
            hideStatusBar()
        }
        binding.btnShowStatusBar.setOnClickShrinkEffectListener {
            showStatusBar()
        }
        binding.btnEnterFullScreenMode.setOnClickShrinkEffectListener {
            enterFullScreenMode()
        }
        binding.btnExitFullScreenMode.setOnClickShrinkEffectListener {
            exitFullScreenMode()
        }
        binding.btnAddSecureFlag.setOnClickShrinkEffectListener {
            addSecureFlag()
        }
        binding.btnClearSecureFlag.setOnClickShrinkEffectListener {
            clearSecureFlag()
        }
        binding.btnShowKeyboard.setOnClickShrinkEffectListener {
            showKeyboard(binding.edtBrightness)
        }
        binding.btnHideKeyboard.setOnClickShrinkEffectListener {
            hideKeyboard()
        }
        binding.btnBrightness.setOnClickShrinkEffectListener {
            brightness = binding.edtBrightness.text.toString().toFloat()
        }
        binding.btnGetStatusBarHeight.setOnClickShrinkEffectListener {
            shortToast("StatusBar Height: $getStatusBarHeight px")
        }
        binding.btnDisplaySizePixels.setOnClickShrinkEffectListener {
            shortToast("Display Size: ${displaySizePixels.x} px, ${displaySizePixels.y} px")
        }
        binding.btnSetStatusBarColor.setOnClickShrinkEffectListener {
            setStatusBarColor(R.color.teal_200)
        }
        binding.btnSetNavigationBarColor.setOnClickShrinkEffectListener {
            setNavigationBarColor(R.color.teal_200)
        }
        binding.btnSetNavigationBarDividerColor.setOnClickShrinkEffectListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                setNavigationBarDividerColor(R.color.teal_200)
            }
        }
        binding.btnRestart.setOnClickShrinkEffectListener {
            restart()
        }

        binding.btnSleepDuration.setOnClickShrinkEffectListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.System.canWrite(this)) {
                    val sleepDuration = 5000
                    shortToast("Sleep after: ${sleepDuration}ms")
                } else {
                    try {
                        val uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, uri)
                        startActivity(intent)
                    } catch (e: Exception) {
                        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                        startActivity(intent)
                    }
                }
            }
        }

        binding.btnLockOrientation.setOnClickShrinkEffectListener {
            lockOrientation()
        }

        binding.btnUnlockScreenOrientation.setOnClickShrinkEffectListener {
            unlockScreenOrientation()
        }

        binding.btnLockCurrentScreenOrientation.setOnClickShrinkEffectListener {
            lockCurrentScreenOrientation()
        }

        binding.btnShowBackButton.setOnClickShrinkEffectListener {
            showBackButton()
        }

        binding.btnHideBackButton.setOnClickShrinkEffectListener {
            hideBackButton()
        }

        binding.btnShowToolbar.setOnClickShrinkEffectListener {
            showToolbar()
        }

        binding.btnHideToolbar.setOnClickShrinkEffectListener {
            hideToolbar()
        }

        binding.btnCustomBackButton.setOnClickShrinkEffectListener {
            customBackButton(R.drawable.ic_launcher_foreground)
        }
    }
}