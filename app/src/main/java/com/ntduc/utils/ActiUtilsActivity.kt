package com.ntduc.utils

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import com.ntduc.activityutils.*
import com.ntduc.toastutils.shortToast
import com.ntduc.utils.databinding.ActivityActiUtilsBinding

class ActiUtilsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityActiUtilsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnHideBottomBar.setOnClickListener {
            hideBottomBar()
        }
        binding.btnShowBottomBar.setOnClickListener {
            showBottomBar()
        }
        binding.btnHideStatusBar.setOnClickListener {
            hideStatusBar()
        }
        binding.btnShowStatusBar.setOnClickListener {
            showStatusBar()
        }
        binding.btnEnterFullScreenMode.setOnClickListener {
            enterFullScreenMode()
        }
        binding.btnExitFullScreenMode.setOnClickListener {
            exitFullScreenMode()
        }
        binding.btnAddSecureFlag.setOnClickListener {
            addSecureFlag()
        }
        binding.btnClearSecureFlag.setOnClickListener {
            clearSecureFlag()
        }
        binding.btnShowKeyboard.setOnClickListener {
            showKeyboard(binding.edtBrightness)
        }
        binding.btnHideKeyboard.setOnClickListener {
            hideKeyboard()
        }
        binding.btnSupportsPictureInPicture.setOnClickListener {
            shortToast("Hỗ trợ PIP: $supportsPictureInPicture")
        }
        binding.btnBrightness.setOnClickListener {
            brightness = binding.edtBrightness.text.toString().toFloat()
        }
        binding.btnGetStatusBarHeight.setOnClickListener {
            shortToast("StatusBar Height: $getStatusBarHeight px")
        }
        binding.btnDisplaySizePixels.setOnClickListener {
            shortToast("Display Size: ${displaySizePixels.x} px, ${displaySizePixels.y} px")
        }
        binding.btnSetStatusBarColor.setOnClickListener {
            setStatusBarColor(R.color.teal_200)
        }
        binding.btnSetNavigationBarColor.setOnClickListener {
            setNavigationBarColor(R.color.teal_200)
        }
        binding.btnSetNavigationBarDividerColor.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                setNavigationBarDividerColor(R.color.teal_200)
            }
        }
        binding.btnRestart.setOnClickListener {
            restart()
        }

        binding.btnSleepDuration.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.System.canWrite(this)) {
                    sleepDuration = 5000
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

        binding.btnHideBottomBar.setOnClickListener {
            hideBottomBar()
        }

        binding.btnLockOrientation.setOnClickListener {
            lockOrientation()
        }

        binding.btnUnlockScreenOrientation.setOnClickListener {
            unlockScreenOrientation()
        }

        binding.btnLockCurrentScreenOrientation.setOnClickListener {
            lockCurrentScreenOrientation()
        }

        binding.btnShowBackButton.setOnClickListener {
            showBackButton()
        }

        binding.btnHideBackButton.setOnClickListener {
            hideBackButton()
        }

        binding.btnShowToolbar.setOnClickListener {
            showToolbar()
        }

        binding.btnHideToolbar.setOnClickListener {
            hideToolbar()
        }

        binding.btnCustomBackButton.setOnClickListener {
            customBackButton(R.drawable.ic_launcher_foreground)
        }
    }
}