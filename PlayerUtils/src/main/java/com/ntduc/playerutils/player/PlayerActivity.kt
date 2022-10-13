package com.ntduc.playerutils.player

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.*
import android.content.*
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Icon
import android.hardware.display.DisplayManager
import android.media.AudioManager
import android.media.audiofx.AudioEffect
import android.media.audiofx.LoudnessEnhancer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.DocumentsContract
import android.provider.Settings
import com.ntduc.playerutils.player.Utils.toggleSystemUi
import com.ntduc.playerutils.player.Utils.setOrientation
import com.ntduc.playerutils.player.Utils.isTvBox
import com.ntduc.playerutils.player.Utils.moviesFolderUri
import com.ntduc.playerutils.player.SubtitleUtils.isSubtitle
import com.ntduc.playerutils.player.SubtitleUtils.buildSubtitle
import com.ntduc.playerutils.player.Utils.isPiPSupported
import com.ntduc.playerutils.player.Utils.showText
import com.ntduc.playerutils.player.Utils.getNextOrientation
import com.ntduc.playerutils.player.Utils.setViewParams
import com.ntduc.playerutils.player.Utils.setViewMargins
import com.ntduc.playerutils.player.Utils.adjustVolume
import com.ntduc.playerutils.player.Utils.formatMilisSign
import com.ntduc.playerutils.player.Utils.formatMilis
import com.ntduc.playerutils.player.SubtitleUtils.clearCache
import com.ntduc.playerutils.player.SubtitleUtils.convertToUTF
import com.ntduc.playerutils.player.Utils.isSupportedNetworkUri
import com.ntduc.playerutils.player.Utils.fileExists
import com.ntduc.playerutils.player.Utils.deviceLanguages
import com.ntduc.playerutils.player.Utils.getFileName
import com.ntduc.playerutils.player.Utils.markChapters
import com.ntduc.playerutils.player.Utils.isPortrait
import com.ntduc.playerutils.player.Utils.switchFrameRate
import com.ntduc.playerutils.player.Utils.alternativeChooser
import com.ntduc.playerutils.player.Utils.getSystemComponent
import com.ntduc.playerutils.player.Utils.getRational
import com.ntduc.playerutils.player.Utils.isTablet
import com.ntduc.playerutils.player.SubtitleUtils.normalizeFontScale
import com.ntduc.playerutils.player.Utils.isProgressiveContainerUri
import com.ntduc.playerutils.player.SubtitleFinder.Companion.isUriCompatible
import com.ntduc.playerutils.player.SubtitleUtils.findUriInScope
import com.ntduc.playerutils.player.SubtitleUtils.findDocInScope
import com.ntduc.playerutils.player.SubtitleUtils.findSubtitle
import com.ntduc.playerutils.player.SubtitleUtils.findNext
import com.ntduc.playerutils.player.Utils.isDeletable
import com.ntduc.playerutils.player.Utils.setButtonEnabled
import com.ntduc.playerutils.player.Utils.normalizeScaleFactor
import android.support.v4.media.session.MediaSessionCompat
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.ntduc.playerutils.player.dtpv.youtube.YouTubeOverlay
import com.google.android.exoplayer2.ui.StyledPlayerControlView
import com.google.android.exoplayer2.MediaItem.SubtitleConfiguration
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.exoplayer2.ui.TimeBar.OnScrubListener
import com.google.android.exoplayer2.ui.TimeBar
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.StyledPlayerView.ControllerVisibilityListener
import com.getkeepsafe.taptargetview.TapTargetView
import com.getkeepsafe.taptargetview.TapTarget
import com.ntduc.playerutils.player.dtpv.youtube.YouTubeOverlay.PerformListener
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.ts.DefaultTsPayloadReaderFactory
import com.google.android.exoplayer2.extractor.ts.TsExtractor
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import android.support.v4.media.MediaMetadataCompat
import android.util.Base64
import android.util.Rational
import android.view.*
import android.view.accessibility.CaptioningManager
import android.widget.*
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.exoplayer2.ui.DefaultTimeBar
import kotlin.jvm.JvmOverloads
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.source.TrackGroup
import com.google.android.exoplayer2.trackselection.TrackSelectionParameters
import com.google.android.exoplayer2.trackselection.TrackSelectionOverride
import com.google.android.exoplayer2.ui.SubtitleView
import com.google.android.material.snackbar.Snackbar
import com.google.android.exoplayer2.ui.CaptionStyleCompat
import androidx.documentfile.provider.DocumentFile
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.ntduc.playerutils.R
import com.ntduc.playerutils.player.dtpv.DoubleTapPlayerView
import java.io.File
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.lang.RuntimeException
import java.lang.reflect.InvocationTargetException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

open class PlayerActivity : Activity() {
    private var playerListener: PlayerListener? = null
    private var mReceiver: BroadcastReceiver? = null
    private var mAudioManager: AudioManager? = null
    private var mediaSession: MediaSessionCompat? = null
    private var trackSelector: DefaultTrackSelector? = null
    var playerView: CustomStyledPlayerView? = null
    private var youTubeOverlay: YouTubeOverlay? = null
    private var mPictureInPictureParamsBuilder: Any? = null
    var mPrefs: Prefs? = null
    var mBrightnessControl: BrightnessControl? = null
    private var videoLoading = false
    private var errorToShow: ExoPlaybackException? = null
    private var isScaling = false
    private var isScaleStarting = false
    private var scaleFactor = 1.0f
    private var coordinatorLayout: CoordinatorLayout? = null
    private var buttonBack: ImageButton? = null
    private var titleView: TextView? = null
    private var buttonMore: ImageButton? = null
    private var buttonOpen: ImageButton? = null
    private var buttonPiP: ImageButton? = null
    private var buttonAspectRatio: ImageButton? = null
    private var buttonRotation: ImageButton? = null
    private var exoSettings: ImageButton? = null
    private var exoPlayPause: ImageButton? = null
    private var loadingProgressBar: ProgressBar? = null
    private var controlView: StyledPlayerControlView? = null
    private var timeBar: CustomDefaultTimeBar? = null
    private var restoreOrientationLock = false
    private var restorePlayState = false
    private var restorePlayStateAllowed = false
    private var play = false
    private var subtitlesScale = 0f
    private var isScrubbing = false
    private var scrubbingNoticeable = false
    private var scrubbingStart: Long = 0
    var frameRendered = false
    private var alive = false
    private var nextUri: Uri? = null
    private var nextUriThread: Thread? = null
    var frameRateSwitchThread: Thread? = null
    var chaptersThread: Thread? = null
    private var lastScrubbingPosition: Long = 0
    val rationalLimitWide = Rational(239, 100)
    val rationalLimitTall = Rational(100, 239)
    var apiAccess = false
    var apiTitle: String? = null
    var apiSubs: MutableList<SubtitleConfiguration> = ArrayList()
    var intentReturnResult = false
    var playbackFinished = false
    var displayManager: DisplayManager? = null
    var displayListener: DisplayManager.DisplayListener? = null
    var subtitleFinder: SubtitleFinder? = null
    var barsHider = Runnable {
        if (playerView != null && !controllerVisible) {
            toggleSystemUi(this@PlayerActivity, playerView!!, false)
        }
    }

    @SuppressLint("PrivateResource")
    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Xoay màn hình càng sớm càng tốt, trước khi inflater để tránh gặp trục trặc với animation khởi tạo activity
        // Rotate ASAP, before super/inflating to avoid glitches with activity launch animation
        mPrefs = Prefs(this) //Load SharedPreferences
        setOrientation(this, mPrefs!!.orientation) //Xoay Activity
        super.onCreate(savedInstanceState)

        //Kiểm tra nhà sản xuất phần cứng để sử dụng layout phù hợp
        if (Build.VERSION.SDK_INT == 28 //Android 9
            && Build.MANUFACTURER.equals("xiaomi", ignoreCase = true) //Hãng Xiaomi
            && (Build.DEVICE.equals("oneday", ignoreCase = true) || Build.DEVICE.equals(
                "once",
                ignoreCase = true
            ))
        ) {
            setContentView(R.layout.activity_player_textureview)
        } else {
            setContentView(R.layout.activity_player)
        }

        //Đối với >= Android 12
        if (Build.VERSION.SDK_INT >= 31) {
            val window = window
            if (window != null) {
                window.setDecorFitsSystemWindows(false) //???
                val windowInsetsController = window.insetsController
                if (windowInsetsController != null) {
                    // Từ Android 12 trở lên, BEHAVIOR_DEFAULT cho phép các cử chỉ hệ thống mà không hiển thị thanh hệ thống
                    // On Android 12 BEHAVIOR_DEFAULT allows system gestures without visible system bars
                    windowInsetsController.systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_DEFAULT
                }
            }
        }

        //Bật darkmode đối với TV Box
        isTvBox = isTvBox(this)
        if (isTvBox) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        val launchIntent = intent
        val action = launchIntent.action
        val type = launchIntent.type
        if ("com.ntduc.playerutils.player.action.SHORTCUT_VIDEOS" == action) {
            openFile(moviesFolderUri)
        } else if (Intent.ACTION_SEND == action && "text/plain" == type) {
            val text = launchIntent.getStringExtra(Intent.EXTRA_TEXT)
            if (text != null) {
                val parsedUri = Uri.parse(text)
                if (parsedUri.isAbsolute) {
                    mPrefs!!.updateMedia(this, parsedUri, null)
                    focusPlay = true
                }
            }
        } else if (launchIntent.data != null) {
            resetApiAccess()
            val uri = launchIntent.data

            //Kiểm tra video có phụ đề không
            if (isSubtitle(uri, type)) {
                handleSubtitles(uri)
            } else {
                val bundle = launchIntent.extras

                //???
                if (bundle != null) {
                    apiAccess = (bundle.containsKey(API_POSITION) || bundle.containsKey(
                        API_RETURN_RESULT
                    ) || bundle.containsKey(API_TITLE)
                            || bundle.containsKey(API_SUBS) || bundle.containsKey(API_SUBS_ENABLE))
                    if (apiAccess) {
                        mPrefs!!.setPersistent(false)
                    }
                    apiTitle = bundle.getString(API_TITLE)
                }

                mPrefs!!.updateMedia(this, uri, type) //Lưu information Video

                //???
                if (bundle != null) {
                    var defaultSub: Uri? = null
                    val subsEnable = bundle.getParcelableArray(API_SUBS_ENABLE)
                    if (subsEnable != null && subsEnable.isNotEmpty()) {
                        defaultSub = subsEnable[0] as Uri
                    }
                    val subs = bundle.getParcelableArray(API_SUBS)
                    val subsName = bundle.getStringArray(API_SUBS_NAME)
                    if (subs != null && subs.isNotEmpty()) {
                        for (i in subs.indices) {
                            val sub = subs[i] as Uri
                            var name: String? = null
                            if (subsName != null && subsName.size > i) {
                                name = subsName[i]
                            }
                            apiSubs.add(buildSubtitle(this, sub, name, sub == defaultSub))
                        }
                    }
                }

                //???
                if (apiSubs.isEmpty()) {
                    searchSubtitles()
                }

                //???
                if (bundle != null) {
                    intentReturnResult = bundle.getBoolean(API_RETURN_RESULT)
                    if (bundle.containsKey(API_POSITION)) {
                        mPrefs!!.updatePosition(bundle.getInt(API_POSITION).toLong())
                    }
                }
            }
            focusPlay = true
        }

        //Xét Main View
        coordinatorLayout = findViewById(R.id.coordinatorLayout)
        mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        playerView = findViewById(R.id.video_view)

        //Xét Control View
        exoPlayPause = findViewById(R.id.exo_play_pause)
        loadingProgressBar = findViewById(R.id.loading)
        playerView!!.setShowNextButton(false) //Ẩn nút Next
        playerView!!.setShowPreviousButton(false) //Ẩn nút Previous
        playerView!!.setShowFastForwardButton(false) //Ẩn nút Fast Forward (Tua 15s)
        playerView!!.setShowRewindButton(false) //Ẩn nút Rewind  (Back 5s)
        playerView!!.setRepeatToggleModes(Player.REPEAT_MODE_ONE) //Xét chế độ Repeat
        playerView!!.controllerHideOnTouch = false //Khóa Controller Video khi giữ
        playerView!!.controllerAutoShow = true //Show Controller Video khi vừa vào?
        (playerView as DoubleTapPlayerView?)?.isDoubleTapEnabled = false //Ẩn chế độ Double Tap

        //Xét Event TimeBar
        timeBar = playerView!!.findViewById(R.id.exo_progress)
        timeBar!!.addListener(object : OnScrubListener {
            override fun onScrubStart(timeBar: TimeBar, position: Long) {
                if (player == null) {
                    return
                }
                restorePlayState = player!!.isPlaying
                if (restorePlayState) {
                    player!!.pause()
                }
                lastScrubbingPosition = position
                scrubbingNoticeable = false
                isScrubbing = true
                frameRendered = true
                playerView!!.controllerShowTimeoutMs = -1
                scrubbingStart = player!!.currentPosition
                player!!.setSeekParameters(SeekParameters.CLOSEST_SYNC)
                reportScrubbing(position)
            }

            override fun onScrubMove(timeBar: TimeBar, position: Long) {
                reportScrubbing(position)
                for (start in chapterStarts!!) {
                    if ((start in ((lastScrubbingPosition + 1)..position)) || (start in (position until lastScrubbingPosition))) {
                        playerView!!.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    }
                }
                lastScrubbingPosition = position
            }

            override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
                playerView!!.setCustomErrorMessage(null)
                isScrubbing = false
                if (restorePlayState) {
                    restorePlayState = false
                    playerView!!.controllerShowTimeoutMs = CONTROLLER_TIMEOUT
                    player!!.playWhenReady = true
                }
            }
        })

        //Tạo Btn Open file
        buttonOpen = ImageButton(this, null, 0, R.style.ExoStyledControls_Button_Bottom)
        buttonOpen!!.setImageResource(getDrawableResFolderOpen())
        buttonOpen!!.id = View.generateViewId()
        buttonOpen!!.contentDescription = getString(R.string.button_open)
        buttonOpen!!.visibility = getVisibilityFolderOpen()

        //Tải Video
        buttonOpen!!.setOnClickListener {
            openFile(
                mPrefs!!.mediaUri
            )
        }

        //Tải phụ đề
        buttonOpen?.setOnLongClickListener {
            if (!isTvBox && mPrefs!!.askScope) {
                askForScope(loadSubtitlesOnCancel = true, skipToNextOnCancel = false)
            } else {
                loadSubtitleFile(mPrefs!!.mediaUri)
            }
            true
        }

        //Thiết lập PIP cho thiết bị hỗ trợ
        if (isPiPSupported(this)) {
            // TODO: Android 12 improvements:
            // https://developer.android.com/about/versions/12/features/pip-improvements
            mPictureInPictureParamsBuilder = PictureInPictureParams.Builder()
            val success = updatePictureInPictureActions(
                getDrawableResPictureInPicturePlay(),
                R.string.exo_controls_play_description,
                CONTROL_TYPE_PLAY,
                REQUEST_PLAY
            )
            if (success) {
                buttonPiP = ImageButton(this, null, 0, R.style.ExoStyledControls_Button_Bottom)
                buttonPiP!!.contentDescription = getString(R.string.button_pip)
                buttonPiP!!.setImageResource(getDrawableResPictureInPictureAlt())
                buttonPiP!!.visibility = getVisibilityPictureInPictureAlt()
                buttonPiP!!.setOnClickListener { enterPiP() }
            }
        }

        //Thiết lập btn Zoom In/Out
        buttonAspectRatio = ImageButton(this, null, 0, R.style.ExoStyledControls_Button_Bottom)
        buttonAspectRatio!!.id = Int.MAX_VALUE - 100
        buttonAspectRatio!!.contentDescription = getString(R.string.button_crop)
        updatebuttonAspectRatioIcon()
        buttonAspectRatio!!.visibility = getVisibilityAspectRatio()
        buttonAspectRatio!!.setOnClickListener {
            playerView!!.setScale(1f)
            if (playerView!!.resizeMode == AspectRatioFrameLayout.RESIZE_MODE_FIT) {
                playerView!!.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                showText(playerView!!, getString(R.string.video_resize_crop))
            } else {
                // Default mode
                playerView!!.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                showText(playerView!!, getString(R.string.video_resize_fit))
            }
            updatebuttonAspectRatioIcon()
            resetHideCallbacks()
        }
        if (isTvBox && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            buttonAspectRatio!!.setOnLongClickListener {
                scaleStart()
                updatebuttonAspectRatioIcon()
                true
            }
        }

        //Thiết lập btn Xoay màn hình
        buttonRotation = ImageButton(this, null, 0, R.style.ExoStyledControls_Button_Bottom)
        buttonRotation!!.contentDescription = getString(R.string.button_rotate)
        updateButtonRotation()
        buttonRotation!!.visibility = getVisibilityRotation()
        buttonRotation!!.setOnClickListener {
            mPrefs!!.orientation = getNextOrientation(
                mPrefs!!.orientation
            )
            setOrientation(this@PlayerActivity, mPrefs!!.orientation)
            updateButtonRotation()
            showText(playerView!!, getString(mPrefs!!.orientation.description), 2500)
            resetHideCallbacks()
        }

        val exoControlsToolbar = playerView!!.findViewById<FrameLayout>(R.id.exo_controls_toolbar)

        buttonBack = playerView!!.findViewById(R.id.btn_back)
        buttonBack!!.visibility = getVisibilityBack()

        titleView = playerView!!.findViewById(R.id.txt_title)
        titleView!!.visibility = getVisibilityTitle()

        buttonMore = playerView!!.findViewById(R.id.btn_more)
        buttonMore!!.visibility = getVisibilityMore()

        buttonBack!!.setOnClickListener { onBackPressed() }
        buttonMore!!.setOnClickListener { enterMore(it) }

        //Gửi video
        titleView!!.setOnLongClickListener {
            // Prevent FileUriExposedException
            if (mPrefs!!.mediaUri != null && ContentResolver.SCHEME_FILE == mPrefs!!.mediaUri!!.scheme) {
                return@setOnLongClickListener false
            }
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.putExtra(Intent.EXTRA_STREAM, mPrefs!!.mediaUri)
            if (mPrefs!!.mediaType == null) shareIntent.type = "video/*" else shareIntent.type =
                mPrefs!!.mediaType
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            // Start without intent chooser to allow any target to be set as default
            startActivity(shareIntent)
            true
        }

        //???
        controlView = playerView!!.findViewById(R.id.exo_controller)
        controlView!!.setOnApplyWindowInsetsListener { view: View, windowInsets: WindowInsets? ->
            if (windowInsets != null) {
                if (Build.VERSION.SDK_INT >= 31) {
                    val visibleBars = windowInsets.isVisible(WindowInsets.Type.statusBars())
                    if (visibleBars && !controllerVisible) {
                        playerView!!.postDelayed(barsHider, 2500)
                    } else {
                        playerView!!.removeCallbacks(barsHider)
                    }
                }
                view.setPadding(
                    0, windowInsets.systemWindowInsetTop,
                    0, windowInsets.systemWindowInsetBottom
                )
                val insetLeft = windowInsets.systemWindowInsetLeft
                val insetRight = windowInsets.systemWindowInsetRight
                var paddingLeft = 0
                var marginLeft = insetLeft
                var paddingRight = 0
                var marginRight = insetRight
                if (Build.VERSION.SDK_INT >= 28 && windowInsets.displayCutout != null) {
                    if (windowInsets.displayCutout!!.safeInsetLeft == insetLeft) {
                        paddingLeft = insetLeft
                        marginLeft = 0
                    }
                    if (windowInsets.displayCutout!!.safeInsetRight == insetRight) {
                        paddingRight = insetRight
                        marginRight = 0
                    }
                }
                setViewParams(
                    exoControlsToolbar!!,
                    0,
                    0,
                    0,
                    0,
                    marginLeft,
                    0,
                    marginRight,
                    0
                )
                setViewParams(
                    findViewById(R.id.exo_bottom_bar), paddingLeft, 0, paddingRight, 0,
                    marginLeft, 0, marginRight, 0
                )
                findViewById<View>(R.id.exo_progress).setPadding(
                    windowInsets.systemWindowInsetLeft, 0,
                    windowInsets.systemWindowInsetRight, 0
                )
                setViewMargins(
                    findViewById(R.id.exo_error_message),
                    0,
                    windowInsets.systemWindowInsetTop / 2,
                    0,
                    resources.getDimensionPixelSize(
                        R.dimen.exo_error_message_margin_bottom
                    ) + windowInsets.systemWindowInsetBottom / 2
                )
                windowInsets.consumeSystemWindowInsets()
            }
            windowInsets!!
        }

        //Xét color TimeBar
        timeBar!!.setAdMarkerColor(getAdMarkerColor())
        timeBar!!.setPlayedAdMarkerColor(getPlayedAdMarkerColor())
        timeBar!!.setPlayedColor(getPlayedColor())
        timeBar!!.setUnplayedColor(getUnplayedColor())

        //???
        try {
            val customDefaultTrackNameProvider = CustomDefaultTrackNameProvider(resources)
            val field = StyledPlayerControlView::class.java.getDeclaredField("trackNameProvider")
            field.isAccessible = true
            field[controlView] = customDefaultTrackNameProvider
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }

        //Thiết lập btn xóa video
        val buttonDelete = findViewById<View>(R.id.delete)
        buttonDelete.visibility = getVisibilityDelete()
        buttonDelete.setOnClickListener { askDeleteMedia() }

        //Thiết lập btn next video
        val buttonNext = findViewById<View>(R.id.next)
        buttonNext.visibility = getVisibilityNext()
        buttonNext.setOnClickListener {
            if (!isTvBox && mPrefs!!.askScope) {
                askForScope(loadSubtitlesOnCancel = false, skipToNextOnCancel = true)
            } else {
                skipToNext()
            }
        }

        //Thiết lập btn pause/play video
        exoPlayPause!!.setOnClickListener { dispatchPlayPause() }

        //Ngăn các thao tác double tap trong controller
        // Prevent double tap actions in controller
        findViewById<View>(R.id.exo_bottom_bar).setOnTouchListener { _: View?, _: MotionEvent? -> true }
        //titleView.setOnTouchListener((v, event) -> true);
        playerListener = PlayerListener()

        //Thiết lập độ sáng
        mBrightnessControl = BrightnessControl(this)
        if (mPrefs!!.brightness >= 0) {
            mBrightnessControl!!.currentBrightnessLevel = mPrefs!!.brightness
            mBrightnessControl!!.screenBrightness = mBrightnessControl!!.levelToBrightness(
                mBrightnessControl!!.currentBrightnessLevel
            )
        }
        playerView!!.setBrightnessControl(mBrightnessControl)
        val exoBasicControls = playerView!!.findViewById<LinearLayout>(R.id.exo_basic_controls)
        val exoSubtitle = exoBasicControls.findViewById<ImageButton>(R.id.exo_subtitle)
        exoSubtitle.visibility = getVisibilitySubtitle()
        exoBasicControls.removeView(exoSubtitle)
        exoSettings = exoBasicControls.findViewById(R.id.exo_settings)
        exoSettings!!.visibility = getVisibilitySettings()
        exoBasicControls.removeView(exoSettings)
        val exoRepeat = exoBasicControls.findViewById<ImageButton>(R.id.exo_repeat_toggle)
        exoRepeat.visibility = getVisibilityRepeat()
        exoBasicControls.removeView(exoRepeat)
        //exoBasicControls.setVisibility(View.GONE);
        exoSettings!!.setOnLongClickListener {
            //askForScope(false, false);
            val intent = Intent(this, SettingsActivity::class.java)
            startActivityForResult(intent, REQUEST_SETTINGS)
            true
        }
        exoSubtitle.setOnLongClickListener {
            enableRotation()
            safelyStartActivityForResult(
                Intent(Settings.ACTION_CAPTIONING_SETTINGS),
                REQUEST_SYSTEM_CAPTIONS
            )
            true
        }
        updateButtons(false)
        val horizontalScrollView =
            layoutInflater.inflate(R.layout.controls, null) as HorizontalScrollView
        val controls = horizontalScrollView.findViewById<LinearLayout>(R.id.controls)
        controls.addView(buttonOpen)
        controls.addView(exoSubtitle)
        controls.addView(buttonAspectRatio)
        if (isPiPSupported(this) && buttonPiP != null) {
            controls.addView(buttonPiP)
        }
        if (mPrefs!!.repeatToggle) {
            controls.addView(exoRepeat)
        }
        if (!isTvBox) {
            controls.addView(buttonRotation)
        }
        controls.addView(exoSettings)
        exoBasicControls.addView(horizontalScrollView)
        if (Build.VERSION.SDK_INT > 23) {
            horizontalScrollView.setOnScrollChangeListener { view: View?, i: Int, i1: Int, i2: Int, i3: Int -> resetHideCallbacks() }
        }
        playerView!!.setControllerVisibilityListener(ControllerVisibilityListener { visibility ->
            controllerVisible = visibility == View.VISIBLE
            controllerVisibleFully = playerView!!.isControllerFullyVisible()
            if (restoreControllerTimeout) {
                restoreControllerTimeout = false
                if (player == null || !player!!.isPlaying) {
                    playerView!!.controllerShowTimeoutMs = -1
                } else {
                    playerView!!.controllerShowTimeoutMs = CONTROLLER_TIMEOUT
                }
            }

            // https://developer.android.com/training/system-ui/immersive
            toggleSystemUi(this@PlayerActivity, playerView!!, visibility == View.VISIBLE)
            if (visibility == View.VISIBLE) {
                // Because when using dpad controls, focus resets to first item in bottom controls bar
                findViewById<View>(R.id.exo_play_pause).requestFocus()
            }
            if (controllerVisible && playerView!!.isControllerFullyVisible) {
                if (mPrefs!!.firstRun) {
                    TapTargetView.showFor(this@PlayerActivity,
                        TapTarget.forView(
                            buttonOpen, getString(R.string.onboarding_open_title), getString(
                                R.string.onboarding_open_description
                            )
                        )
                            .outerCircleColor(R.color.green)
                            .targetCircleColor(R.color.white)
                            .titleTextSize(22)
                            .titleTextColor(R.color.white)
                            .descriptionTextSize(14)
                            .cancelable(true),
                        object : TapTargetView.Listener() {
                            override fun onTargetClick(view: TapTargetView) {
                                super.onTargetClick(view)
                                buttonOpen!!.performClick()
                            }
                        })
                    // TODO: Explain gestures?
                    //  "Use vertical and horizontal gestures to change brightness, volume and seek in video"
                    mPrefs!!.markFirstRun()
                }
                if (errorToShow != null) {
                    showError(errorToShow!!)
                    errorToShow = null
                }
            }
        })
        youTubeOverlay = findViewById(R.id.youtube_overlay)
        youTubeOverlay!!.performListener(object : PerformListener {
            override fun onAnimationStart() {
                youTubeOverlay!!.alpha = 1.0f
                youTubeOverlay!!.visibility = View.VISIBLE
            }

            override fun onAnimationEnd() {
                youTubeOverlay!!.animate()
                    .alpha(0.0f)
                    .setDuration(300)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            youTubeOverlay!!.visibility = View.GONE
                            youTubeOverlay!!.alpha = 1.0f
                        }
                    })
            }
        })
    }

    public override fun onStart() {
        super.onStart()
        alive = true
        if (!(isTvBox && Build.VERSION.SDK_INT >= 31)) {
            updateSubtitleStyle(this)
        }
        if (Build.VERSION.SDK_INT >= 31) {
            playerView!!.removeCallbacks(barsHider)
            toggleSystemUi(this, playerView!!, true)
        }
        initializePlayer()
        updateButtonRotation()
    }

    public override fun onResume() {
        super.onResume()
        restorePlayStateAllowed = true
        if (isTvBox && Build.VERSION.SDK_INT >= 31) {
            updateSubtitleStyle(this)
        }
    }

    override fun onPause() {
        super.onPause()
        savePlayer()
    }

    public override fun onStop() {
        super.onStop()
        alive = false
        if (Build.VERSION.SDK_INT >= 31) {
            playerView!!.removeCallbacks(barsHider)
        }
        playerView!!.setCustomErrorMessage(null)
        releasePlayer(false)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        restorePlayStateAllowed = false
        super.onBackPressed()
    }

    override fun finish() {
        if (intentReturnResult) {
            val intent = Intent("com.mxtech.intent.result.VIEW")
            intent.putExtra(API_END_BY, if (playbackFinished) "playback_completion" else "user")
            if (!playbackFinished) {
                if (player != null) {
                    val duration = player!!.duration
                    if (duration != C.TIME_UNSET) {
                        intent.putExtra(API_DURATION, player!!.duration.toInt())
                    }
                    if (player!!.isCurrentMediaItemSeekable) {
                        if (mPrefs!!.persistentMode) {
                            intent.putExtra(API_POSITION, mPrefs!!.nonPersitentPosition.toInt())
                        } else {
                            intent.putExtra(API_POSITION, player!!.currentPosition.toInt())
                        }
                    }
                }
            }
            setResult(RESULT_OK, intent)
        }
        super.finish()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            val action = intent.action
            val type = intent.type
            val uri = intent.data
            if (Intent.ACTION_VIEW == action && uri != null) {
                if (isSubtitle(uri, type)) {
                    handleSubtitles(uri)
                } else {
                    mPrefs!!.updateMedia(this, uri, type)
                    searchSubtitles()
                }
                focusPlay = true
                initializePlayer()
            } else if (Intent.ACTION_SEND == action && "text/plain" == type) {
                val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                if (text != null) {
                    val parsedUri = Uri.parse(text)
                    if (parsedUri.isAbsolute) {
                        mPrefs!!.updateMedia(this, parsedUri, null)
                        focusPlay = true
                        initializePlayer()
                    }
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY, KeyEvent.KEYCODE_MEDIA_PAUSE, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_BUTTON_SELECT -> {
                if (player == null) return super.onKeyDown(keyCode, event)
                if (keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                    player!!.pause()
                } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                    player!!.play()
                } else if (player!!.isPlaying) {
                    player!!.pause()
                } else {
                    player!!.play()
                }
                return true
            }
            KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN -> {
                adjustVolume(
                    this,
                    mAudioManager!!,
                    playerView!!,
                    keyCode == KeyEvent.KEYCODE_VOLUME_UP,
                    event.repeatCount == 0,
                    true
                )
                return true
            }
            KeyEvent.KEYCODE_BUTTON_START, KeyEvent.KEYCODE_BUTTON_A, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_NUMPAD_ENTER, KeyEvent.KEYCODE_SPACE -> {
                if (player == null) return super.onKeyDown(keyCode, event)
                if (!controllerVisibleFully) {
                    if (player!!.isPlaying) {
                        player!!.pause()
                    } else {
                        player!!.play()
                    }
                    return true
                }
            }
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_BUTTON_L2, KeyEvent.KEYCODE_MEDIA_REWIND -> if (!controllerVisibleFully || keyCode == KeyEvent.KEYCODE_MEDIA_REWIND) {
                if (player == null) return super.onKeyDown(keyCode, event)
                playerView!!.removeCallbacks(playerView!!.textClearRunnable)
                val pos = player!!.currentPosition
                if (playerView!!.keySeekStart == -1L) {
                    playerView!!.keySeekStart = pos
                }
                var seekTo = pos - 10000
                if (seekTo < 0) seekTo = 0
                player!!.setSeekParameters(SeekParameters.PREVIOUS_SYNC)
                player!!.seekTo(seekTo)
                val message = """
                    ${formatMilisSign(seekTo - playerView!!.keySeekStart)}
                    ${formatMilis(seekTo)}
                    """.trimIndent()
                playerView!!.setCustomErrorMessage(message)
                return true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_BUTTON_R2, KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> if (!controllerVisibleFully || keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD) {
                if (player == null) return super.onKeyDown(keyCode, event)
                playerView!!.removeCallbacks(playerView!!.textClearRunnable)
                val pos = player!!.currentPosition
                if (playerView!!.keySeekStart == -1L) {
                    playerView!!.keySeekStart = pos
                }
                var seekTo = pos + 10000
                val seekMax = player!!.duration
                if (seekMax != C.TIME_UNSET && seekTo > seekMax) seekTo = seekMax
                player!!.setSeekParameters(SeekParameters.NEXT_SYNC)
                player!!.seekTo(seekTo)
                val message = """
                    ${formatMilisSign(seekTo - playerView!!.keySeekStart)}
                    ${formatMilis(seekTo)}
                    """.trimIndent()
                playerView!!.setCustomErrorMessage(message)
                return true
            }
            KeyEvent.KEYCODE_BACK -> if (isTvBox) {
                if (controllerVisible && player != null && player!!.isPlaying) {
                    playerView!!.hideController()
                    return true
                } else {
                    onBackPressed()
                }
            }
            else -> if (!controllerVisibleFully) {
                playerView!!.showController()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN -> {
                playerView!!.postDelayed(
                    playerView!!.textClearRunnable,
                    CustomStyledPlayerView.MESSAGE_TIMEOUT_KEY.toLong()
                )
                return true
            }
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_BUTTON_L2, KeyEvent.KEYCODE_MEDIA_REWIND, KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_BUTTON_R2, KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> if (!isScrubbing) {
                playerView!!.postDelayed(playerView!!.textClearRunnable, 1000)
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (isScaling) {
            val keyCode = event.keyCode
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_UP -> scale(true)
                    KeyEvent.KEYCODE_DPAD_DOWN -> scale(false)
                }
            } else if (event.action == KeyEvent.ACTION_UP) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN -> {}
                    else -> if (isScaleStarting) {
                        isScaleStarting = false
                    } else {
                        scaleEnd()
                    }
                }
            }
            return true
        }
        return if (isTvBox && !controllerVisibleFully) {
            if (event.action == KeyEvent.ACTION_DOWN) {
                onKeyDown(event.keyCode, event)
            } else if (event.action == KeyEvent.ACTION_UP) {
                onKeyUp(event.keyCode, event)
            }
            true
        } else {
            super.dispatchKeyEvent(event)
        }
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        if (0 != event.source and InputDevice.SOURCE_CLASS_POINTER) {
            when (event.action) {
                MotionEvent.ACTION_SCROLL -> {
                    val value = event.getAxisValue(MotionEvent.AXIS_VSCROLL)
                    adjustVolume(
                        this,
                        mAudioManager!!,
                        playerView!!,
                        value > 0.0f,
                        Math.abs(value) > 1.0f,
                        true
                    )
                    return true
                }
            }
        } else if (event.source and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK &&
            event.action == MotionEvent.ACTION_MOVE
        ) {
            // TODO: This somehow works, but it would use better filtering
            var value = event.getAxisValue(MotionEvent.AXIS_RZ)
            for (i in 0 until event.historySize) {
                val historical = event.getHistoricalAxisValue(MotionEvent.AXIS_RZ, i)
                if (abs(historical) > value) {
                    value = historical
                }
            }
            if (abs(value) == 1.0f) {
                adjustVolume(this, mAudioManager!!, playerView!!, value < 0, true, true)
            }
        }
        return super.onGenericMotionEvent(event)
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (isInPictureInPictureMode) {
            // On Android TV it is required to hide controller in this PIP change callback
            playerView!!.hideController()
            setSubtitleTextSizePiP()
            playerView!!.setScale(1f)
            mReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent?) {
                    if (intent == null || ACTION_MEDIA_CONTROL != intent.action || player == null) {
                        return
                    }
                    when (intent.getIntExtra(EXTRA_CONTROL_TYPE, 0)) {
                        CONTROL_TYPE_PLAY -> player!!.play()
                        CONTROL_TYPE_PAUSE -> player!!.pause()
                    }
                }
            }
            registerReceiver(mReceiver, IntentFilter(ACTION_MEDIA_CONTROL))
        } else {
            setSubtitleTextSize()
            if (mPrefs!!.resizeMode == AspectRatioFrameLayout.RESIZE_MODE_ZOOM) {
                playerView!!.setScale(mPrefs!!.scale)
            }
            if (mReceiver != null) {
                unregisterReceiver(mReceiver)
                mReceiver = null
            }
            playerView!!.controllerAutoShow = true
            if (player != null) {
                if (player!!.isPlaying) toggleSystemUi(
                    this,
                    playerView!!,
                    false
                ) else playerView!!.showController()
            }
        }
    }

    fun resetApiAccess() {
        apiAccess = false
        apiTitle = null
        apiSubs.clear()
        mPrefs!!.setPersistent(true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        try {
            if (restoreOrientationLock) {
                Settings.System.putInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION, 0)
                restoreOrientationLock = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (resultCode == RESULT_OK && alive) {
            releasePlayer()
        }
        if (requestCode == REQUEST_CHOOSER_VIDEO || requestCode == REQUEST_CHOOSER_VIDEO_MEDIASTORE) {
            if (resultCode == RESULT_OK) {
                resetApiAccess()
                restorePlayState = false
                val uri = data?.data ?: return
                if (requestCode == REQUEST_CHOOSER_VIDEO) {
                    var uriAlreadyTaken = false

                    // https://commonsware.com/blog/2020/06/13/count-your-saf-uri-permission-grants.html
                    val contentResolver = contentResolver
                    for (persistedUri in contentResolver.persistedUriPermissions) {
                        if (persistedUri.uri == mPrefs!!.scopeUri) {
                            continue
                        } else if (persistedUri.uri == uri) {
                            uriAlreadyTaken = true
                        } else {
                            try {
                                contentResolver.releasePersistableUriPermission(
                                    persistedUri.uri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                                )
                            } catch (e: SecurityException) {
                                e.printStackTrace()
                            }
                        }
                    }
                    if (!uriAlreadyTaken) {
                        try {
                            contentResolver.takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                        } catch (e: SecurityException) {
                            e.printStackTrace()
                        }
                    }
                }
                mPrefs!!.setPersistent(true)
                mPrefs!!.updateMedia(this, uri, data.type)
                if (requestCode == REQUEST_CHOOSER_VIDEO) {
                    searchSubtitles()
                }
            }
        } else if (requestCode == REQUEST_CHOOSER_SUBTITLE || requestCode == REQUEST_CHOOSER_SUBTITLE_MEDIASTORE) {
            if (resultCode == RESULT_OK) {
                val uri = data?.data ?: return
                if (requestCode == REQUEST_CHOOSER_SUBTITLE) {
                    try {
                        contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    } catch (e: SecurityException) {
                        e.printStackTrace()
                    }
                }
                handleSubtitles(uri)
            }
        } else if (requestCode == REQUEST_CHOOSER_SCOPE_DIR) {
            if (resultCode == RESULT_OK) {
                val uri = data?.data ?: return
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    mPrefs!!.updateScope(uri)
                    mPrefs!!.markScopeAsked()
                    searchSubtitles()
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            }
        } else if (requestCode == REQUEST_SETTINGS) {
            mPrefs!!.loadUserPreferences()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }

        // Init here because onStart won't follow when app was only paused when file chooser was shown
        // (for example pop-up file chooser on tablets)
        if (resultCode == RESULT_OK && alive) {
            initializePlayer()
        }
    }

    private fun handleSubtitles(uri: Uri?) {
        // Convert subtitles to UTF-8 if necessary
        var uri = uri
        clearCache(this)
        uri = convertToUTF(this, uri!!)
        mPrefs!!.updateSubtitle(uri)
    }

    fun initializePlayer() {
        val isNetworkUri = mPrefs!!.mediaUri != null && isSupportedNetworkUri(
            mPrefs!!.mediaUri!!
        )
        haveMedia =
            mPrefs!!.mediaUri != null && (fileExists(this, mPrefs!!.mediaUri!!) || isNetworkUri)
        if (player != null) {
            player!!.removeListener(playerListener!!)
            player!!.clearMediaItems()
            player!!.release()
            player = null
        }
        trackSelector = DefaultTrackSelector(this)
        if (mPrefs!!.tunneling) {
            trackSelector!!.setParameters(
                trackSelector!!.buildUponParameters()
                    .setTunnelingEnabled(true)
            )
        }
        trackSelector!!.setParameters(
            trackSelector!!.buildUponParameters()
                .setPreferredAudioLanguages(*deviceLanguages)
        )
        // https://github.com/google/ExoPlayer/issues/8571
        val extractorsFactory = DefaultExtractorsFactory()
            .setTsExtractorFlags(DefaultTsPayloadReaderFactory.FLAG_ENABLE_HDMV_DTS_AUDIO_STREAMS)
            .setTsExtractorTimestampSearchBytes(1500 * TsExtractor.TS_PACKET_SIZE)
        @SuppressLint("WrongConstant") val renderersFactory: RenderersFactory =
            DefaultRenderersFactory(this)
                .setExtensionRendererMode(mPrefs!!.decoderPriority)
        val playerBuilder = ExoPlayer.Builder(this, renderersFactory)
            .setTrackSelector(trackSelector!!)
            .setMediaSourceFactory(DefaultMediaSourceFactory(this, extractorsFactory))
        if (haveMedia && isNetworkUri) {
            if (mPrefs!!.mediaUri!!.scheme!!.lowercase(Locale.getDefault()).startsWith("http")) {
                val headers = HashMap<String, String>()
                val userInfo = mPrefs!!.mediaUri!!.userInfo
                if (userInfo != null && userInfo.isNotEmpty() && userInfo.contains(":")) {
                    headers["Authorization"] =
                        "Basic " + Base64.encodeToString(userInfo.toByteArray(), Base64.NO_WRAP)
                    val defaultHttpDataSourceFactory = DefaultHttpDataSource.Factory()
                    defaultHttpDataSourceFactory.setDefaultRequestProperties(headers)
                    playerBuilder.setMediaSourceFactory(
                        DefaultMediaSourceFactory(
                            defaultHttpDataSourceFactory,
                            extractorsFactory
                        )
                    )
                }
            }
        }
        player = playerBuilder.build()
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()
        player!!.setAudioAttributes(audioAttributes, true)
        if (mPrefs!!.skipSilence) {
            player!!.skipSilenceEnabled = true
        }
        youTubeOverlay!!.player(player)
        playerView!!.player = player
        mediaSession = MediaSessionCompat(this, getString(R.string.app_name))
        val mediaSessionConnector = MediaSessionConnector(mediaSession!!)
        mediaSessionConnector.setPlayer(player)

        if (mPrefs!!.mediaUri != null) {
            val title = getFileName(this@PlayerActivity, mPrefs!!.mediaUri!!)
            if (title != null) {
                mediaSessionConnector.setMediaMetadataProvider {
                    return@setMediaMetadataProvider MediaMetadataCompat.Builder()
                        .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, title)
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                        .build()
                }
            }
        }

        playerView!!.controllerShowTimeoutMs = -1
        locked = false
        chapterStarts = LongArray(0)
        if (haveMedia) {
            if (isNetworkUri) {
                timeBar!!.setBufferedColor(DefaultTimeBar.DEFAULT_BUFFERED_COLOR)
            } else {
                // https://github.com/google/ExoPlayer/issues/5765
                timeBar!!.setBufferedColor(0x33FFFFFF)
            }
            playerView!!.resizeMode = mPrefs!!.resizeMode
            if (mPrefs!!.resizeMode == AspectRatioFrameLayout.RESIZE_MODE_ZOOM) {
                playerView!!.setScale(mPrefs!!.scale)
            } else {
                playerView!!.setScale(1f)
            }
            updatebuttonAspectRatioIcon()
            val mediaItemBuilder = MediaItem.Builder()
                .setUri(mPrefs!!.mediaUri)
                .setMimeType(mPrefs!!.mediaType)
            if (apiAccess && apiSubs.size > 0) {
                mediaItemBuilder.setSubtitleConfigurations(apiSubs)
            } else if (mPrefs!!.subtitleUri != null && fileExists(this, mPrefs!!.subtitleUri!!)) {
                val subtitle = buildSubtitle(this, mPrefs!!.subtitleUri!!, null, true)
                mediaItemBuilder.setSubtitleConfigurations(listOf(subtitle))
            }
            player!!.setMediaItem(mediaItemBuilder.build(), mPrefs!!.position)
            if (loudnessEnhancer != null) {
                loudnessEnhancer!!.release()
            }
            try {
                loudnessEnhancer = LoudnessEnhancer(
                    player!!.audioSessionId
                )
            } catch (e: RuntimeException) {
                e.printStackTrace()
            }
            notifyAudioSessionUpdate(true)
            videoLoading = true
            updateLoading(true)
            if (mPrefs!!.position == 0L || apiAccess) {
                play = true
            }
            if (apiTitle != null) {
                titleView!!.text = apiTitle
            } else {
                titleView!!.text = getFileName(this, mPrefs!!.mediaUri!!)
            }
            titleView!!.visibility = View.VISIBLE
            updateButtons(true)
            (playerView as DoubleTapPlayerView?)!!.isDoubleTapEnabled = true
            if (!apiAccess) {
                if (nextUriThread != null) {
                    nextUriThread!!.interrupt()
                }
                nextUri = null
                nextUriThread = Thread {
                    val uri = findNext()
                    if (!Thread.currentThread().isInterrupted) {
                        nextUri = uri
                    }
                }
                nextUriThread!!.start()
            }
            markChapters(this, mPrefs!!.mediaUri!!, controlView!!)
            player!!.setHandleAudioBecomingNoisy(!isTvBox)
            mediaSession!!.isActive = true
        } else {
            playerView!!.showController()
        }
        player!!.addListener(playerListener!!)
        player!!.prepare()
        if (restorePlayState) {
            restorePlayState = false
            playerView!!.showController()
            playerView!!.controllerShowTimeoutMs = CONTROLLER_TIMEOUT
            player!!.playWhenReady = true
        }
    }

    private fun savePlayer() {
        if (player != null) {
            mPrefs!!.updateBrightness(mBrightnessControl!!.currentBrightnessLevel)
            mPrefs!!.updateOrientation()
            if (haveMedia) {
                // Prevent overwriting temporarily inaccessible media position
                if (player!!.isCurrentMediaItemSeekable) {
                    mPrefs!!.updatePosition(player!!.currentPosition)
                }
                mPrefs!!.updateMeta(
                    getSelectedTrack(C.TRACK_TYPE_AUDIO),
                    getSelectedTrack(C.TRACK_TYPE_TEXT),
                    playerView!!.resizeMode,
                    playerView!!.videoSurfaceView!!.scaleX,
                    player!!.playbackParameters.speed
                )
            }
        }
    }

    @JvmOverloads
    fun releasePlayer(save: Boolean = true) {
        if (save) {
            savePlayer()
        }
        if (player != null) {
            notifyAudioSessionUpdate(false)
            mediaSession!!.isActive = false
            mediaSession!!.release()
            if (player!!.isPlaying && restorePlayStateAllowed) {
                restorePlayState = true
            }
            player!!.removeListener(playerListener!!)
            player!!.clearMediaItems()
            player!!.release()
            player = null
        }
        titleView!!.visibility = View.GONE
        updateButtons(false)
    }

    private inner class PlayerListener : Player.Listener {
        override fun onAudioSessionIdChanged(audioSessionId: Int) {
            if (loudnessEnhancer != null) {
                loudnessEnhancer!!.release()
            }
            try {
                loudnessEnhancer = LoudnessEnhancer(audioSessionId)
            } catch (e: RuntimeException) {
                e.printStackTrace()
            }
            notifyAudioSessionUpdate(true)
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            playerView!!.keepScreenOn = isPlaying
            if (isPiPSupported(this@PlayerActivity)) {
                if (isPlaying) {
                    updatePictureInPictureActions(
                        R.drawable.ic_pause_24dp,
                        R.string.exo_controls_pause_description,
                        CONTROL_TYPE_PAUSE,
                        REQUEST_PAUSE
                    )
                } else {
                    updatePictureInPictureActions(
                        R.drawable.ic_play_arrow_24dp,
                        R.string.exo_controls_play_description,
                        CONTROL_TYPE_PLAY,
                        REQUEST_PLAY
                    )
                }
            }
            if (!isScrubbing) {
                if (isPlaying) {
                    if (shortControllerTimeout) {
                        playerView!!.controllerShowTimeoutMs = CONTROLLER_TIMEOUT / 3
                        shortControllerTimeout = false
                        restoreControllerTimeout = true
                    } else {
                        playerView!!.controllerShowTimeoutMs = CONTROLLER_TIMEOUT
                    }
                } else {
                    playerView!!.controllerShowTimeoutMs = -1
                }
            }
            if (!isPlaying) {
                locked = false
            }
        }

        @SuppressLint("SourceLockedOrientationActivity")
        override fun onPlaybackStateChanged(state: Int) {
            var isNearEnd = false
            val duration = player!!.duration
            if (duration != C.TIME_UNSET) {
                val position = player!!.currentPosition
                if (position + 4000 >= duration) {
                    isNearEnd = true
                } else {
                    // Last chapter is probably "Credits" chapter
                    val chapters = chapterStarts!!.size
                    if (chapters > 1) {
                        val lastChapter = chapterStarts!![chapters - 1]
                        if (duration - lastChapter < duration / 10 && position > lastChapter) {
                            isNearEnd = true
                        }
                    }
                }
            }
            setEndControlsVisible(haveMedia && (state == Player.STATE_ENDED || isNearEnd))
            if (state == Player.STATE_READY) {
                frameRendered = true
                if (videoLoading) {
                    videoLoading = false
                    if (mPrefs!!.orientation === Utils.Orientation.UNSPECIFIED) {
                        mPrefs!!.orientation = getNextOrientation(
                            mPrefs!!.orientation
                        )
                        setOrientation(this@PlayerActivity, mPrefs!!.orientation)
                    }
                    val format = player!!.videoFormat
                    if (format != null) {
                        if (mPrefs!!.orientation === Utils.Orientation.VIDEO) {
                            if (isPortrait(format)) {
                                this@PlayerActivity.requestedOrientation =
                                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                            } else {
                                this@PlayerActivity.requestedOrientation =
                                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                            }
                            updateButtonRotation()
                        }
                        updateSubtitleViewMargin(format)
                    }
                    if (duration != C.TIME_UNSET && duration > TimeUnit.MINUTES.toMillis(20)) {
                        timeBar!!.setKeyTimeIncrement(TimeUnit.MINUTES.toMillis(1))
                    } else {
                        timeBar!!.setKeyCountIncrement(20)
                    }
                    var switched = false
                    if (mPrefs!!.frameRateMatching) {
                        if (play) {
                            if (displayManager == null) {
                                displayManager = getSystemService(DISPLAY_SERVICE) as DisplayManager
                            }
                            if (displayListener == null) {
                                displayListener = object : DisplayManager.DisplayListener {
                                    override fun onDisplayAdded(displayId: Int) {}
                                    override fun onDisplayRemoved(displayId: Int) {}
                                    override fun onDisplayChanged(displayId: Int) {
                                        if (play) {
                                            play = false
                                            displayManager!!.unregisterDisplayListener(this)
                                            if (player != null) {
                                                player!!.play()
                                            }
                                            if (playerView != null) {
                                                playerView!!.hideController()
                                            }
                                        }
                                    }
                                }
                            }
                            displayManager!!.registerDisplayListener(displayListener, null)
                        }
                        switched = switchFrameRate(this@PlayerActivity, mPrefs!!.mediaUri!!, play)
                    }
                    if (!switched) {
                        if (displayManager != null) {
                            displayManager!!.unregisterDisplayListener(displayListener)
                        }
                        if (play) {
                            play = false
                            player!!.play()
                            playerView!!.hideController()
                        }
                    }
                    updateLoading(false)
                    if (mPrefs!!.speed <= 0.99f || mPrefs!!.speed >= 1.01f) {
                        player!!.setPlaybackSpeed(mPrefs!!.speed)
                    }
                    if (!apiAccess) {
                        setSelectedTracks(mPrefs!!.subtitleTrackId, mPrefs!!.audioTrackId)
                    }
                }
            } else if (state == Player.STATE_ENDED) {
                playbackFinished = true
                if (apiAccess) {
                    finish()
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            updateLoading(false)
            if (error is ExoPlaybackException) {
                if (controllerVisible && controllerVisibleFully) {
                    showError(error)
                } else {
                    errorToShow = error
                }
            }
        }
    }

    private fun enableRotation() {
        try {
            if (Settings.System.getInt(
                    contentResolver,
                    Settings.System.ACCELEROMETER_ROTATION
                ) == 0
            ) {
                Settings.System.putInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION, 1)
                restoreOrientationLock = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openFile(pickerInitialUri: Uri?) {
        var pickerInitialUri = pickerInitialUri
        val targetSdkVersion = applicationContext.applicationInfo.targetSdkVersion
        if (isTvBox && Build.VERSION.SDK_INT >= 30 && targetSdkVersion >= 30 && mPrefs!!.fileAccess == "auto" || mPrefs!!.fileAccess == "mediastore") {
            val intent = Intent(this, MediaStoreChooserActivity::class.java)
            startActivityForResult(intent, REQUEST_CHOOSER_VIDEO_MEDIASTORE)
        } else if (isTvBox && mPrefs!!.fileAccess == "auto" || mPrefs!!.fileAccess == "legacy") {
            alternativeChooser(this, pickerInitialUri, true)
        } else {
            enableRotation()
            if (pickerInitialUri == null || isSupportedNetworkUri(pickerInitialUri) || !fileExists(
                    this,
                    pickerInitialUri
                )
            ) {
                pickerInitialUri = moviesFolderUri
            }
            val intent = createBaseFileIntent(Intent.ACTION_OPEN_DOCUMENT, pickerInitialUri)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "video/*"
            intent.putExtra(Intent.EXTRA_MIME_TYPES, Utils.supportedMimeTypesVideo)
            if (Build.VERSION.SDK_INT < 30) {
                val systemComponentName = getSystemComponent(this, intent)
                if (systemComponentName != null) {
                    intent.component = systemComponentName
                }
            }
            safelyStartActivityForResult(intent, REQUEST_CHOOSER_VIDEO)
        }
    }

    private fun loadSubtitleFile(pickerInitialUri: Uri?) {
        Toast.makeText(this@PlayerActivity, R.string.open_subtitles, Toast.LENGTH_SHORT).show()
        val targetSdkVersion = applicationContext.applicationInfo.targetSdkVersion
        if (isTvBox && Build.VERSION.SDK_INT >= 30 && targetSdkVersion >= 30 && mPrefs!!.fileAccess == "auto" || mPrefs!!.fileAccess == "mediastore") {
            val intent = Intent(this, MediaStoreChooserActivity::class.java)
            intent.putExtra(MediaStoreChooserActivity.SUBTITLES, true)
            startActivityForResult(intent, REQUEST_CHOOSER_SUBTITLE_MEDIASTORE)
        } else if (isTvBox && mPrefs!!.fileAccess == "auto" || mPrefs!!.fileAccess == "legacy") {
            alternativeChooser(this, pickerInitialUri, false)
        } else {
            enableRotation()
            val intent = createBaseFileIntent(Intent.ACTION_OPEN_DOCUMENT, pickerInitialUri)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            val supportedMimeTypes = arrayOf(
                MimeTypes.APPLICATION_SUBRIP,
                MimeTypes.TEXT_SSA,
                MimeTypes.TEXT_VTT,
                MimeTypes.APPLICATION_TTML,
                "text/*",
                "application/octet-stream"
            )
            intent.putExtra(Intent.EXTRA_MIME_TYPES, supportedMimeTypes)
            if (Build.VERSION.SDK_INT < 30) {
                val systemComponentName = getSystemComponent(this, intent)
                if (systemComponentName != null) {
                    intent.component = systemComponentName
                }
            }
            safelyStartActivityForResult(intent, REQUEST_CHOOSER_SUBTITLE)
        }
    }

    private fun requestDirectoryAccess() {
        enableRotation()
        val intent = createBaseFileIntent(Intent.ACTION_OPEN_DOCUMENT_TREE, moviesFolderUri)
        safelyStartActivityForResult(intent, REQUEST_CHOOSER_SCOPE_DIR)
    }

    private fun createBaseFileIntent(action: String, initialUri: Uri?): Intent {
        val intent = Intent(action)

        // http://stackoverflow.com/a/31334967/1615876
        intent.putExtra("android.content.extra.SHOW_ADVANCED", true)
        if (Build.VERSION.SDK_INT >= 26 && initialUri != null) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri)
        }
        return intent
    }

    @SuppressLint("QueryPermissionsNeeded")
    fun safelyStartActivityForResult(intent: Intent, code: Int) {
        if (intent.resolveActivity(packageManager) == null) showSnack(
            getText(R.string.error_files_missing).toString(),
            intent.toString()
        ) else startActivityForResult(intent, code)
    }

    private fun getTrackGroupFromFormatId(trackType: Int, id: String?): TrackGroup? {
        if (id == null && trackType == C.TRACK_TYPE_AUDIO || player == null) {
            return null
        }
        for (group in player!!.currentTracks.groups) {
            if (group.type == trackType) {
                val trackGroup = group.mediaTrackGroup
                val format = trackGroup.getFormat(0)
                if (id == format.id) {
                    return trackGroup
                }
            }
        }
        return null
    }

    fun setSelectedTracks(subtitleId: String?, audioId: String?) {
        if ("#none" == subtitleId) {
            if (trackSelector == null) {
                return
            }
            trackSelector!!.setParameters(
                trackSelector!!.buildUponParameters()
                    .setDisabledTextTrackSelectionFlags(C.SELECTION_FLAG_DEFAULT or C.SELECTION_FLAG_FORCED)
            )
        }
        val subtitleGroup = getTrackGroupFromFormatId(C.TRACK_TYPE_TEXT, subtitleId)
        val audioGroup = getTrackGroupFromFormatId(C.TRACK_TYPE_AUDIO, audioId)
        val overridesBuilder = TrackSelectionParameters.Builder(this)
        var trackSelectionOverride: TrackSelectionOverride? = null
        val tracks: MutableList<Int> = ArrayList()
        tracks.add(0)
        if (subtitleGroup != null) {
            trackSelectionOverride = TrackSelectionOverride(subtitleGroup, tracks)
            overridesBuilder.addOverride(trackSelectionOverride)
        }
        if (audioGroup != null) {
            trackSelectionOverride = TrackSelectionOverride(audioGroup, tracks)
            overridesBuilder.addOverride(trackSelectionOverride)
        }
        if (player != null) {
            val trackSelectionParametersBuilder = player!!.trackSelectionParameters.buildUpon()
            if (trackSelectionOverride != null) {
                trackSelectionParametersBuilder.setOverrideForType(trackSelectionOverride)
            }
            player!!.trackSelectionParameters = trackSelectionParametersBuilder.build()
        }
    }

    private fun hasOverrideType(trackType: Int): Boolean {
        val trackSelectionParameters = player!!.trackSelectionParameters
        for (override in trackSelectionParameters.overrides.values) {
            if (override.type == trackType) return true
        }
        return false
    }

    fun getSelectedTrack(trackType: Int): String? {
        if (player == null) {
            return null
        }
        val tracks = player!!.currentTracks

        // Disabled (e.g. selected subtitle "None" - different than default)
        if (!tracks.isTypeSelected(trackType)) {
            return "#none"
        }

        // Audio track set to "Auto"
        if (trackType == C.TRACK_TYPE_AUDIO) {
            if (!hasOverrideType(C.TRACK_TYPE_AUDIO)) {
                return null
            }
        }
        for (group in tracks.groups) {
            if (group.isSelected && group.type == trackType) {
                val format = group.mediaTrackGroup.getFormat(0)
                return format.id
            }
        }
        return null
    }

    fun setSubtitleTextSize() {
        setSubtitleTextSize(resources.configuration.orientation)
    }

    fun setSubtitleTextSize(orientation: Int) {
        // Tweak text size as fraction size doesn't work well in portrait
        val subtitleView = playerView!!.subtitleView
        if (subtitleView != null) {
            val size: Float
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                size = SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * subtitlesScale
            } else {
                val metrics = resources.displayMetrics
                var ratio = metrics.heightPixels.toFloat() / metrics.widthPixels.toFloat()
                if (ratio < 1) ratio = 1 / ratio
                size = SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * subtitlesScale / ratio
            }
            subtitleView.setFractionalTextSize(size)
        }
    }

    fun updateSubtitleViewMargin() {
        if (player == null) {
            return
        }
        updateSubtitleViewMargin(player!!.videoFormat)
    }

    // Set margins to fix PGS aspect as subtitle view is outside of content frame
    fun updateSubtitleViewMargin(format: Format?) {
        if (format == null) {
            return
        }
        val aspectVideo = getRational(format)
        val metrics = resources.displayMetrics
        val aspectDisplay = Rational(metrics.widthPixels, metrics.heightPixels)
        var marginHorizontal = 0
        val marginVertical = 0
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (aspectDisplay.toFloat() > aspectVideo.toFloat()) {
                // Left & right bars
                val videoWidth =
                    metrics.heightPixels / aspectVideo.denominator * aspectVideo.numerator
                marginHorizontal = (metrics.widthPixels - videoWidth) / 2
            }
        }
        setViewParams(
            playerView!!.subtitleView!!, 0, 0, 0, 0,
            marginHorizontal, marginVertical, marginHorizontal, marginVertical
        )
    }

    fun setSubtitleTextSizePiP() {
        val subtitleView = playerView!!.subtitleView
        subtitleView?.setFractionalTextSize(SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * 2)
    }

    @TargetApi(26)
    fun updatePictureInPictureActions(
        iconId: Int,
        resTitle: Int,
        controlType: Int,
        requestCode: Int
    ): Boolean {
        try {
            val actions = ArrayList<RemoteAction>()
            val intent = PendingIntent.getBroadcast(
                this@PlayerActivity,
                requestCode,
                Intent(ACTION_MEDIA_CONTROL).putExtra(EXTRA_CONTROL_TYPE, controlType),
                PendingIntent.FLAG_IMMUTABLE
            )
            val icon = Icon.createWithResource(this@PlayerActivity, iconId)
            val title = getString(resTitle)
            actions.add(RemoteAction(icon, title, title, intent))
            (mPictureInPictureParamsBuilder as PictureInPictureParams.Builder?)!!.setActions(actions)
            setPictureInPictureParams((mPictureInPictureParamsBuilder as PictureInPictureParams.Builder?)!!.build())
            return true
        } catch (e: IllegalStateException) {
            // On Samsung devices with Talkback active:
            // Caused by: java.lang.IllegalStateException: setPictureInPictureParams: Device doesn't support picture-in-picture mode.
            e.printStackTrace()
        }
        return false
    }

    @get:RequiresApi(api = Build.VERSION_CODES.N)
    private val isInPip: Boolean
        get() = if (!isPiPSupported(this)) false else isInPictureInPictureMode

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (!isInPip) {
            setSubtitleTextSize(newConfig.orientation)
        }
        updateSubtitleViewMargin()
        updateButtonRotation()
    }

    fun showError(error: ExoPlaybackException) {
        val errorGeneral = error.localizedMessage
        val errorDetailed: String? = when (error.type) {
            ExoPlaybackException.TYPE_SOURCE -> error.sourceException.localizedMessage
            ExoPlaybackException.TYPE_RENDERER -> error.rendererException.localizedMessage
            ExoPlaybackException.TYPE_UNEXPECTED -> error.unexpectedException.localizedMessage
            ExoPlaybackException.TYPE_REMOTE -> errorGeneral
            else -> errorGeneral
        }
        showSnack(errorGeneral, errorDetailed)
    }

    fun showSnack(textPrimary: String?, textSecondary: String?) {
        snackbar = Snackbar.make(coordinatorLayout!!, textPrimary!!, Snackbar.LENGTH_LONG)
        if (textSecondary != null) {
            snackbar!!.setAction(R.string.error_details) { v: View? ->
                val builder = AlertDialog.Builder(this@PlayerActivity)
                builder.setMessage(textSecondary)
                builder.setPositiveButton(android.R.string.ok) { dialogInterface: DialogInterface, i: Int -> dialogInterface.dismiss() }
                val dialog = builder.create()
                dialog.show()
            }
        }
        snackbar!!.setAnchorView(R.id.exo_bottom_bar)
        snackbar!!.show()
    }

    fun reportScrubbing(position: Long) {
        val diff = position - scrubbingStart
        if (abs(diff) > 1000) {
            scrubbingNoticeable = true
        }
        if (scrubbingNoticeable) {
            playerView!!.clearIcon()
            playerView!!.setCustomErrorMessage(formatMilisSign(diff))
        }
        if (frameRendered) {
            frameRendered = false
            player!!.seekTo(position)
        }
    }

    fun updateSubtitleStyle(context: Context?) {
        val captioningManager = getSystemService(CAPTIONING_SERVICE) as CaptioningManager
        val subtitleView = playerView!!.subtitleView
        val isTablet = isTablet(context!!)
        subtitlesScale = normalizeFontScale(captioningManager.fontScale, isTvBox || isTablet)
        if (subtitleView != null) {
            val userStyle = captioningManager.userStyle
            val userStyleCompat = CaptionStyleCompat.createFromCaptionStyle(userStyle)
            val captionStyle = CaptionStyleCompat(
                if (userStyle.hasForegroundColor()) userStyleCompat.foregroundColor else Color.WHITE,
                if (userStyle.hasBackgroundColor()) userStyleCompat.backgroundColor else Color.TRANSPARENT,
                if (userStyle.hasWindowColor()) userStyleCompat.windowColor else Color.TRANSPARENT,
                if (userStyle.hasEdgeType()) userStyleCompat.edgeType else CaptionStyleCompat.EDGE_TYPE_OUTLINE,
                if (userStyle.hasEdgeColor()) userStyleCompat.edgeColor else Color.BLACK,
                if (userStyleCompat.typeface != null) userStyleCompat.typeface else Typeface.DEFAULT_BOLD
            )
            subtitleView.setStyle(captionStyle)
            if (captioningManager.isEnabled) {
                // Do not apply embedded style as currently the only supported color style is PrimaryColour
                // https://github.com/google/ExoPlayer/issues/8435#issuecomment-762449001
                // This may result in poorly visible text (depending on user's selected edgeColor)
                // The same can happen with style provided using setStyle but enabling CaptioningManager should be a way to change the behavior
                subtitleView.setApplyEmbeddedStyles(false)
            } else {
                subtitleView.setApplyEmbeddedStyles(true)
            }
            subtitleView.setBottomPaddingFraction(SubtitleView.DEFAULT_BOTTOM_PADDING_FRACTION * 2f / 3f)
        }
        setSubtitleTextSize()
    }

    fun searchSubtitles() {
        if (mPrefs!!.mediaUri == null) return
        if (isSupportedNetworkUri(mPrefs!!.mediaUri!!) && isProgressiveContainerUri(
                mPrefs!!.mediaUri!!
            )
        ) {
            clearCache(this)
            if (isUriCompatible(mPrefs!!.mediaUri!!)) {
                subtitleFinder = SubtitleFinder(this@PlayerActivity, mPrefs!!.mediaUri!!)
                subtitleFinder!!.start()
            }
            return
        }
        if (mPrefs!!.scopeUri != null || isTvBox) {
            var video: DocumentFile? = null
            var videoRaw: File? = null
            val scheme = mPrefs!!.mediaUri!!.scheme
            if (mPrefs!!.scopeUri != null) {
                video =
                    if ("com.android.externalstorage.documents" == mPrefs!!.mediaUri!!.host || "org.courville.nova.provider" == mPrefs!!.mediaUri!!.host) {
                        // Fast search based on path in uri
                        findUriInScope(this, mPrefs!!.scopeUri!!, mPrefs!!.mediaUri!!)
                    } else {
                        // Slow search based on matching metadata, no path in uri
                        // Provider "com.android.providers.media.documents" when using "Videos" tab in file picker
                        val fileScope = DocumentFile.fromTreeUri(this, mPrefs!!.scopeUri!!)
                        val fileMedia = DocumentFile.fromSingleUri(this, mPrefs!!.mediaUri!!)
                        findDocInScope(fileScope, fileMedia)
                    }
            } else if (ContentResolver.SCHEME_FILE == scheme) {
                videoRaw = File(mPrefs!!.mediaUri!!.schemeSpecificPart)
                video = DocumentFile.fromFile(videoRaw)
            }
            if (video != null) {
                var subtitle: DocumentFile? = null
                if (mPrefs!!.scopeUri != null) {
                    subtitle = findSubtitle(video)
                } else if (ContentResolver.SCHEME_FILE == scheme) {
                    val parentRaw = videoRaw!!.parentFile ?: return
                    val dir = DocumentFile.fromFile(parentRaw)
                    subtitle = findSubtitle(video, dir)
                }
                if (subtitle != null) {
                    handleSubtitles(subtitle.uri)
                }
            }
        }
    }

    fun findNext(): Uri? {
        // TODO: Unify with searchSubtitles()
        if (mPrefs!!.scopeUri != null || isTvBox) {
            var video: DocumentFile? = null
            var videoRaw: File? = null
            if (!isTvBox && mPrefs!!.scopeUri != null) {
                video = if ("com.android.externalstorage.documents" == mPrefs!!.mediaUri!!.host) {
                    // Fast search based on path in uri
                    findUriInScope(this, mPrefs!!.scopeUri!!, mPrefs!!.mediaUri!!)
                } else {
                    // Slow search based on matching metadata, no path in uri
                    // Provider "com.android.providers.media.documents" when using "Videos" tab in file picker
                    val fileScope = DocumentFile.fromTreeUri(this, mPrefs!!.scopeUri!!)
                    val fileMedia = DocumentFile.fromSingleUri(this, mPrefs!!.mediaUri!!)
                    findDocInScope(fileScope, fileMedia)
                }
            } else if (isTvBox) {
                videoRaw = File(mPrefs!!.mediaUri!!.schemeSpecificPart)
                video = DocumentFile.fromFile(videoRaw)
            }
            if (video != null) {
                val next: DocumentFile? = if (!isTvBox) {
                    findNext(video)
                } else {
                    val parentRaw = videoRaw!!.parentFile ?: return null
                    val dir = DocumentFile.fromFile(parentRaw)
                    findNext(video, dir)
                }
                if (next != null) {
                    return next.uri
                }
            }
        }
        return null
    }

    fun askForScope(loadSubtitlesOnCancel: Boolean, skipToNextOnCancel: Boolean) {
        val builder = AlertDialog.Builder(this@PlayerActivity)
        builder.setMessage(
            String.format(
                getString(R.string.request_scope),
                getString(R.string.app_name)
            )
        )
        builder.setPositiveButton(
            android.R.string.ok
        ) { _: DialogInterface?, _: Int -> requestDirectoryAccess() }
        builder.setNegativeButton(android.R.string.cancel) { _: DialogInterface?, _: Int ->
            mPrefs!!.markScopeAsked()
            if (loadSubtitlesOnCancel) {
                loadSubtitleFile(mPrefs!!.mediaUri)
            }
            if (skipToNextOnCancel) {
                nextUri = findNext()
                if (nextUri != null) {
                    skipToNext()
                }
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    fun resetHideCallbacks() {
        if (haveMedia && player != null && player!!.isPlaying) {
            // Keep controller UI visible - alternative to resetHideCallbacks()
            playerView!!.controllerShowTimeoutMs = CONTROLLER_TIMEOUT
        }
    }

    private fun updateLoading(enableLoading: Boolean) {
        if (enableLoading) {
            exoPlayPause!!.visibility = View.GONE
            loadingProgressBar!!.visibility = View.VISIBLE
        } else {
            loadingProgressBar!!.visibility = View.GONE
            exoPlayPause!!.visibility = View.VISIBLE
            if (focusPlay) {
                focusPlay = false
                exoPlayPause!!.requestFocus()
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onUserLeaveHint() {
        if (mPrefs != null && mPrefs!!.autoPiP && player != null && player!!.isPlaying && isPiPSupported(
                this
            )
        ) enterPiP() else super.onUserLeaveHint()
    }

    @SuppressLint("QueryPermissionsNeeded")
    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun enterPiP() {
        val appOpsManager = getSystemService(APP_OPS_SERVICE) as AppOpsManager
        if (AppOpsManager.MODE_ALLOWED != appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                Process.myUid(),
                packageName
            )
        ) {
            val intent = Intent(
                "android.settings.PICTURE_IN_PICTURE_SETTINGS",
                Uri.fromParts("package", packageName, null)
            )
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
            return
        }
        if (player == null) {
            return
        }
        playerView!!.controllerAutoShow = false
        playerView!!.hideController()
        val format = player!!.videoFormat
        if (format != null) {
            // https://github.com/google/ExoPlayer/issues/8611
            // TODO: Test/disable on Android 11+
            val videoSurfaceView = playerView!!.videoSurfaceView
            if (videoSurfaceView is SurfaceView) {
                videoSurfaceView.holder.setFixedSize(format.width, format.height)
            }
            var rational = getRational(format)
            if (Build.VERSION.SDK_INT >= 33 &&
                packageManager.hasSystemFeature(PackageManager.FEATURE_EXPANDED_PICTURE_IN_PICTURE) &&
                (rational.toFloat() > rationalLimitWide.toFloat() || rational.toFloat() < rationalLimitTall.toFloat())
            ) {
                (mPictureInPictureParamsBuilder as PictureInPictureParams.Builder?)!!.setExpandedAspectRatio(
                    rational
                )
            }
            if (rational.toFloat() > rationalLimitWide.toFloat()) rational =
                rationalLimitWide else if (rational.toFloat() < rationalLimitTall.toFloat()) rational =
                rationalLimitTall
            (mPictureInPictureParamsBuilder as PictureInPictureParams.Builder?)!!.setAspectRatio(
                rational
            )
        }
        enterPictureInPictureMode((mPictureInPictureParamsBuilder as PictureInPictureParams.Builder?)!!.build())
    }

    fun setEndControlsVisible(visible: Boolean) {
        val deleteVisible = if (visible && haveMedia && isDeletable(
                this,
                mPrefs!!.mediaUri!!
            )
        ) View.VISIBLE else View.INVISIBLE
        val nextVisible =
            if (visible && haveMedia && (nextUri != null || mPrefs!!.askScope && !isTvBox)) View.VISIBLE else View.INVISIBLE
        findViewById<View>(R.id.delete).visibility = deleteVisible
        findViewById<View>(R.id.next).visibility = nextVisible
    }

    fun askDeleteMedia() {
        val builder = AlertDialog.Builder(this@PlayerActivity)
        builder.setMessage(getString(R.string.delete_query))
        builder.setPositiveButton(R.string.delete_confirmation) { _: DialogInterface?, _: Int ->
            releasePlayer()
            deleteMedia()
            if (nextUri == null) {
                haveMedia = false
                setEndControlsVisible(false)
                playerView!!.controllerShowTimeoutMs = -1
            } else {
                skipToNext()
            }
        }
        builder.setNegativeButton(android.R.string.cancel) { _: DialogInterface?, _: Int -> }
        val dialog = builder.create()
        dialog.show()
    }

    fun deleteMedia() {
        try {
            if (ContentResolver.SCHEME_CONTENT == mPrefs!!.mediaUri!!.scheme) {
                DocumentsContract.deleteDocument(contentResolver, mPrefs!!.mediaUri!!)
            } else if (ContentResolver.SCHEME_FILE == mPrefs!!.mediaUri!!.scheme) {
                val file = File(mPrefs!!.mediaUri!!.schemeSpecificPart)
                if (file.canWrite()) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun dispatchPlayPause() {
        if (player == null) return
        val state = player!!.playbackState
        val methodName: String
        if (state == Player.STATE_IDLE || state == Player.STATE_ENDED || !player!!.playWhenReady) {
            methodName = "dispatchPlay"
            shortControllerTimeout = true
        } else {
            methodName = "dispatchPause"
        }
        try {
            val method = StyledPlayerControlView::class.java.getDeclaredMethod(
                methodName,
                Player::class.java
            )
            method.isAccessible = true
            method.invoke(controlView, player as Player?)
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }

    fun skipToNext() {
        if (nextUri != null) {
            releasePlayer()
            mPrefs!!.updateMedia(this, nextUri, null)
            searchSubtitles()
            initializePlayer()
        }
    }

    fun notifyAudioSessionUpdate(active: Boolean) {
        val intent =
            Intent(if (active) AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION else AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION)
        intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, player!!.audioSessionId)
        intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, packageName)
        if (active) {
            intent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MOVIE)
        }
        try {
            sendBroadcast(intent)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun updateButtons(enable: Boolean) {
        if (buttonPiP != null) {
            setButtonEnabled(buttonPiP!!, enable)
        }
        setButtonEnabled(buttonAspectRatio!!, enable)
        if (isTvBox) {
            setButtonEnabled(exoSettings!!, true)
        } else {
            setButtonEnabled(exoSettings!!, enable)
        }
    }

    private fun scaleStart() {
        isScaling = true
        if (playerView!!.resizeMode != AspectRatioFrameLayout.RESIZE_MODE_ZOOM) {
            playerView!!.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        }
        scaleFactor = playerView!!.videoSurfaceView!!.scaleX
        playerView!!.removeCallbacks(playerView!!.textClearRunnable)
        playerView!!.clearIcon()
        playerView!!.setCustomErrorMessage((scaleFactor * 100).toInt().toString() + "%")
        playerView!!.hideController()
        isScaleStarting = true
    }

    private fun scale(up: Boolean) {
        if (up) {
            scaleFactor += 0.01.toFloat()
        } else {
            scaleFactor -= 0.01.toFloat()
        }
        scaleFactor = normalizeScaleFactor(scaleFactor, playerView!!.scaleFit)
        playerView!!.setScale(scaleFactor)
        playerView!!.setCustomErrorMessage((scaleFactor * 100).toInt().toString() + "%")
    }

    private fun scaleEnd() {
        isScaling = false
        playerView!!.postDelayed(playerView!!.textClearRunnable, 200)
        if (!player!!.isPlaying) {
            playerView!!.showController()
        }
        if (abs(playerView!!.scaleFit - scaleFactor) < 0.01 / 2) {
            playerView!!.setScale(1f)
            playerView!!.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        }
        updatebuttonAspectRatioIcon()
    }

    private fun updatebuttonAspectRatioIcon() {
        if (playerView!!.resizeMode == AspectRatioFrameLayout.RESIZE_MODE_ZOOM) {
            buttonAspectRatio!!.setImageResource(getDrawableResAspectRatioFill())
        } else {
            buttonAspectRatio!!.setImageResource(getDrawableResAspectRatioZoom())
        }
    }

    private fun updateButtonRotation() {
        val portrait = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        var auto = false
        try {
            auto =
                Settings.System.getInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION) == 1
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
        }
        if (mPrefs!!.orientation === Utils.Orientation.VIDEO) {
            if (auto) {
                buttonRotation!!.setImageResource(getDrawableResScreenLockRotation())
            } else if (portrait) {
                buttonRotation!!.setImageResource(getDrawableResScreenLockPortrait())
            } else {
                buttonRotation!!.setImageResource(getDrawableResScreenLockLandscape())
            }
        } else {
            if (auto) {
                buttonRotation!!.setImageResource(getDrawableResScreenRotation())
            } else if (portrait) {
                buttonRotation!!.setImageResource(getDrawableResScreenPortrait())
            } else {
                buttonRotation!!.setImageResource(getDrawableResScreenLandscape())
            }
        }
    }

    companion object {
        var loudnessEnhancer: LoudnessEnhancer? = null
        var player: ExoPlayer? = null
        var haveMedia = false
        var controllerVisible = false
        var controllerVisibleFully = false
        var snackbar: Snackbar? = null
        var boostLevel = 0
        private const val REQUEST_CHOOSER_VIDEO = 1
        private const val REQUEST_CHOOSER_SUBTITLE = 2
        private const val REQUEST_CHOOSER_SCOPE_DIR = 10
        private const val REQUEST_CHOOSER_VIDEO_MEDIASTORE = 20
        private const val REQUEST_CHOOSER_SUBTITLE_MEDIASTORE = 21
        private const val REQUEST_SETTINGS = 100
        private const val REQUEST_SYSTEM_CAPTIONS = 200
        const val CONTROLLER_TIMEOUT = 3500
        private const val ACTION_MEDIA_CONTROL = "media_control"
        private const val EXTRA_CONTROL_TYPE = "control_type"
        private const val REQUEST_PLAY = 1
        private const val REQUEST_PAUSE = 2
        private const val CONTROL_TYPE_PLAY = 1
        private const val CONTROL_TYPE_PAUSE = 2
        var focusPlay = false
        private var isTvBox = false
        var locked = false
        var chapterStarts: LongArray? = null
        var restoreControllerTimeout = false
        var shortControllerTimeout = false
        const val API_POSITION = "position"
        const val API_DURATION = "duration"
        const val API_RETURN_RESULT = "return_result"
        const val API_SUBS = "subs"
        const val API_SUBS_ENABLE = "subs.enable"
        const val API_SUBS_NAME = "subs.name"
        const val API_TITLE = "title"
        const val API_END_BY = "end_by"
    }

    //Folder Open
    @DrawableRes
    open fun getDrawableResFolderOpen(): Int {
        return R.drawable.ic_folder_open_24dp
    }

    open fun getVisibilityFolderOpen(): Int {
        return View.VISIBLE
    }

    //PIP
    @DrawableRes
    open fun getDrawableResPictureInPicturePlay(): Int {
        return R.drawable.ic_play_arrow_24dp
    }

    @DrawableRes
    open fun getDrawableResPictureInPictureAlt(): Int {
        return R.drawable.ic_picture_in_picture_alt_24dp
    }

    open fun getVisibilityPictureInPictureAlt(): Int {
        return View.VISIBLE
    }

    //Aspect Ratio
    @DrawableRes
    open fun getDrawableResAspectRatioFill(): Int {
        return R.drawable.ic_fit_screen_24dp
    }

    @DrawableRes
    open fun getDrawableResAspectRatioZoom(): Int {
        return R.drawable.ic_aspect_ratio_24dp
    }

    open fun getVisibilityAspectRatio(): Int {
        return View.VISIBLE
    }

    //Rotation
    @DrawableRes
    open fun getDrawableResScreenLockRotation(): Int {
        return R.drawable.ic_screen_lock_rotation_24dp
    }

    @DrawableRes
    open fun getDrawableResScreenLockPortrait(): Int {
        return R.drawable.ic_screen_lock_portrait_24dp
    }

    @DrawableRes
    open fun getDrawableResScreenLockLandscape(): Int {
        return R.drawable.ic_screen_lock_landscape_24dp
    }

    @DrawableRes
    open fun getDrawableResScreenRotation(): Int {
        return R.drawable.ic_screen_rotation_24dp
    }

    @DrawableRes
    open fun getDrawableResScreenPortrait(): Int {
        return R.drawable.ic_screen_portrait_24dp
    }

    @DrawableRes
    open fun getDrawableResScreenLandscape(): Int {
        return R.drawable.ic_screen_landscape_24dp
    }

    open fun getVisibilityRotation(): Int {
        return View.VISIBLE
    }

    //Back
    open fun getVisibilityBack(): Int {
        return View.VISIBLE
    }

    //Title
    open fun getVisibilityTitle(): Int {
        return View.VISIBLE
    }

    //More
    open fun getVisibilityMore(): Int {
        return View.VISIBLE
    }

    open fun enterMore(view: View) {}

    //TimeBar
    @ColorInt
    open fun getAdMarkerColor(): Int {
        return Color.argb(0x00, 0xFF, 0xFF, 0xFF)
    }

    @ColorInt
    open fun getPlayedAdMarkerColor(): Int {
        return Color.argb(0x98, 0xFF, 0xFF, 0xFF)
    }

    @ColorInt
    open fun getPlayedColor(): Int {
        return DefaultTimeBar.DEFAULT_PLAYED_COLOR
    }

    @ColorInt
    open fun getUnplayedColor(): Int {
        return DefaultTimeBar.DEFAULT_UNPLAYED_COLOR
    }

    //Delete
    open fun getVisibilityDelete(): Int {
        return View.GONE
    }

    //Next
    open fun getVisibilityNext(): Int {
        return View.GONE
    }

    //Subtitle
    open fun getVisibilitySubtitle(): Int {
        return View.VISIBLE
    }

    //Settings
    open fun getVisibilitySettings(): Int {
        return View.VISIBLE
    }

    //Repeat
    open fun getVisibilityRepeat(): Int {
        return View.VISIBLE
    }
}