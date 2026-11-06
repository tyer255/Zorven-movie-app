package com.example

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.os.Message
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.data.remote.AnymovieApiBridge
import com.example.ui.components.RefractedTextSplashScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.Transparent.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(Color(0xFF1D1C2E).toArgb())
        )
        // Lock portrait initially; automatically rotates to landscape when the video player opens
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Hide Android system status/navigation bars so the HTML's built-in status bar (9:41)
        // and fixed bottom nav render edge-to-edge without any overlapping system bar.
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())

        setContent {
            MyApplicationTheme {
                ZorvenExactHtmlApp()
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ZorvenExactHtmlApp() {
    val context = LocalContext.current
    val activity = context as? Activity
    var showSplash by remember { mutableStateOf(true) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var fullscreenContainerRef by remember { mutableStateOf<FrameLayout?>(null) }
    var customFullscreenView by remember { mutableStateOf<View?>(null) }
    var customViewCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }

    val apiBridge = remember {
        AnymovieApiBridge(
            context = context.applicationContext,
            webViewProvider = { webViewRef },
            onPlayerModeChanged = { active, _ ->
                val wv = webViewRef
                val window = activity?.window
                if (window != null) {
                    val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                    insetsController.systemBarsBehavior =
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    insetsController.hide(WindowInsetsCompat.Type.systemBars())
                }
                if (active) {
                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                } else {
                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    wv?.postInvalidate()
                }
            }
        )
    }

    BackHandler(enabled = !showSplash) {
        // 1. If native HTML5 video fullscreen is active, exit fullscreen first
        if (customFullscreenView != null) {
            customViewCallback?.onCustomViewHidden()
            fullscreenContainerRef?.removeAllViews()
            fullscreenContainerRef?.visibility = View.GONE
            customFullscreenView = null
            customViewCallback = null
            return@BackHandler
        }

        val wv = webViewRef ?: return@BackHandler
        // 2. Navigate back through the HTML's player (#pl), bottom sheet (#sh / #sheet), detail page (#zd), subpage (.bk), or tabs
        wv.evaluateJavascript(
            """
            (function() {
                var pl = document.getElementById('pl');
                if (pl && pl.style.display === 'block') {
                    if (typeof window.pclose === 'function') {
                        window.pclose();
                        return 'handled';
                    }
                    var pclose = document.getElementById('pclose') || document.querySelector('[data-a="pclose"]');
                    if (pclose) { pclose.click(); return 'handled'; }
                }
                var sheet = document.getElementById('sheet');
                if (sheet && sheet.style.display === 'block') {
                    var sbg1 = document.getElementById('sbg');
                    if (sbg1) { sbg1.click(); return 'handled'; }
                }
                var sh = document.getElementById('sh');
                if (sh && sh.style.display === 'block') {
                    var sbg2 = document.getElementById('sbg');
                    if (sbg2) { sbg2.click(); return 'handled'; }
                }
                var closeBtn = document.querySelector('[data-a="close"]');
                if (closeBtn) {
                    closeBtn.click();
                    return 'handled';
                }
                var bk = document.querySelector('[data-a="back"]');
                if (bk) {
                    bk.click();
                    return 'handled';
                }
                var navLinks = document.querySelectorAll('.nav a');
                if (navLinks.length > 0 && !navLinks[0].classList.contains('on')) {
                    navLinks[0].click();
                    return 'handled';
                }
                return 'root';
            })();
            """.trimIndent(),
            null
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050505))
    ) {
        // Preload and display the Z Home & Z Detail UI connected to https://anymovie.ai.studio/api/v1
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .testTag("z_home_exact_webview"),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(AndroidColor.parseColor("#1D1C2E"))
                    isVerticalScrollBarEnabled = false
                    isHorizontalScrollBarEnabled = false
                    overScrollMode = View.OVER_SCROLL_NEVER

                    @Suppress("DEPRECATION")
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        allowFileAccess = true
                        allowContentAccess = true
                        allowFileAccessFromFileURLs = true
                        allowUniversalAccessFromFileURLs = true
                        loadsImagesAutomatically = true
                        blockNetworkImage = false
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        builtInZoomControls = false
                        displayZoomControls = false
                        setSupportZoom(false)
                        textZoom = 100
                        cacheMode = WebSettings.LOAD_DEFAULT
                        mediaPlaybackRequiresUserGesture = false
                        setSupportMultipleWindows(true)
                        javaScriptCanOpenWindowsAutomatically = false
                    }

                    setOnTouchListener { _, event ->
                        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_DOWN) {
                            evaluateJavascript(
                                "window.__onPlayerTouchDetected && window.__onPlayerTouchDetected();",
                                null
                            )
                        }
                        false // Pass touch event through without consuming
                    }

                    addJavascriptInterface(apiBridge, "AnymovieApiBridge")
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val reqUrl = request?.url?.toString() ?: return false
                            // Allow player iframe video/source embeds, but block navigating to the player's external root home page
                            if (request.isForMainFrame == false) {
                                if (reqUrl == "https://embed.theogpiratebot.online/" || reqUrl == "https://embed.theogpiratebot.online" ||
                                    reqUrl == "https://zxcstream-mirror.vercel.app/" || reqUrl == "https://zxcstream-mirror.vercel.app") {
                                    return true // Block navigating away to player home page
                                }
                                return false
                            }
                            // Keep top-level main frame strictly inside local app assets
                            return !reqUrl.startsWith("file:///android_asset/")
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            view?.postInvalidate()
                        }
                    }
                    webChromeClient = object : WebChromeClient() {
                        override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                            if (view == null) return
                            if (customFullscreenView != null) {
                                callback?.onCustomViewHidden()
                                return
                            }
                            customFullscreenView = view
                            customViewCallback = callback
                            fullscreenContainerRef?.apply {
                                removeAllViews()
                                addView(
                                    view,
                                    FrameLayout.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                )
                                visibility = View.VISIBLE
                            }
                            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                        }

                        override fun onHideCustomView() {
                            fullscreenContainerRef?.apply {
                                removeAllViews()
                                visibility = View.GONE
                            }
                            customFullscreenView = null
                            customViewCallback?.onCustomViewHidden()
                            customViewCallback = null
                        }

                        override fun onCreateWindow(
                            view: WebView?,
                            isDialog: Boolean,
                            isUserGesture: Boolean,
                            resultMsg: Message?
                        ): Boolean {
                            // Block any popup windows from opening an external browser or ad tab
                            return false
                        }
                    }

                    loadUrl("file:///android_asset/Z_Home_UI.html")
                    webViewRef = this
                }
            },
            update = { webView ->
                webViewRef = webView
            }
        )

        // Native HTML5 video fullscreen container for iframe fullscreen requests
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                FrameLayout(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(AndroidColor.BLACK)
                    visibility = View.GONE
                    fullscreenContainerRef = this
                }
            },
            update = { container ->
                fullscreenContainerRef = container
            }
        )

        // Keep the exact ZORVÉN single-shine splash screen on top during app launch
        AnimatedVisibility(
            visible = showSplash,
            exit = fadeOut(animationSpec = tween(durationMillis = 180))
        ) {
            RefractedTextSplashScreen(
                text = "ZORVÉN",
                onSplashFinished = { showSplash = false }
            )
        }
    }
}

