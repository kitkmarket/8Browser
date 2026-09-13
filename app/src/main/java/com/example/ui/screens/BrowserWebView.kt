package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.HapticFeedbackConstants
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.GeolocationPermissions
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.model.BrowserTab
import com.example.viewmodel.BrowserViewModel
import com.example.viewmodel.ContextMenuTarget
import com.example.viewmodel.ContextMenuType

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserWebView(
    tab: BrowserTab,
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Web permissions state (Microphone / Camera / Protected Media)
    var pendingWebPermissionRequest by remember { mutableStateOf<PermissionRequest?>(null) }
    var pendingGeolocationCallback by remember { mutableStateOf<Pair<String, GeolocationPermissions.Callback>?>(null) }
    var pendingFileChooserCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }

    // Launcher for runtime permissions (Microphone, Camera, Geolocation)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        // Handle web permission (Audio / Video capture)
        pendingWebPermissionRequest?.let { req ->
            val audioOk = if (req.resources.contains(PermissionRequest.RESOURCE_AUDIO_CAPTURE)) {
                permissionsMap[Manifest.permission.RECORD_AUDIO] == true ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
            } else true

            val cameraOk = if (req.resources.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
                permissionsMap[Manifest.permission.CAMERA] == true ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            } else true

            if (audioOk && cameraOk) {
                req.grant(req.resources)
            } else {
                req.deny()
            }
            pendingWebPermissionRequest = null
        }

        // Handle Geolocation permission
        pendingGeolocationCallback?.let { (origin, callback) ->
            val fine = permissionsMap[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val coarse = permissionsMap[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val granted = fine || coarse
            callback.invoke(origin, granted, false)
            pendingGeolocationCallback = null
        }
    }

    // Launcher for file picker
    val fileChooserLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uris = WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
            pendingFileChooserCallback?.onReceiveValue(uris)
        } else {
            pendingFileChooserCallback?.onReceiveValue(null)
        }
        pendingFileChooserCallback = null
    }

    val currentPermissionLauncher = rememberUpdatedState(permissionLauncher)
    val currentFileLauncher = rememberUpdatedState(fileChooserLauncher)

    val webView = remember(tab.id) {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(android.graphics.Color.BLACK)

            settings.apply {
                javaScriptEnabled = viewModel.javascriptEnabled.value
                domStorageEnabled = true
                databaseEnabled = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                useWideViewPort = true
                loadWithOverviewMode = true
                cacheMode = viewModel.cacheMode.value.webSettingsMode
                allowFileAccess = true
                allowContentAccess = true
                setGeolocationEnabled(true)
                mediaPlaybackRequiresUserGesture = false
                javaScriptCanOpenWindowsAutomatically = true
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

                userAgentString = if (tab.isDesktopMode) {
                    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"
                } else {
                    WebSettings.getDefaultUserAgent(context)
                }

                if (tab.isIncognito) {
                    saveFormData = false
                    @Suppress("DEPRECATION")
                    savePassword = false
                }
            }

            if (tab.isIncognito) {
                CookieManager.getInstance().acceptThirdPartyCookies(this)
            }

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    url?.let { viewModel.onPageStarted(it) }
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    url?.let { viewModel.onPageFinished(it, view?.title) }
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val targetUrl = request?.url?.toString() ?: return false
                    if (targetUrl.startsWith("http://") || targetUrl.startsWith("https://")) {
                        return false // Allow WebView to load
                    }
                    return true
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    viewModel.onProgressChanged(newProgress)
                }

                override fun onReceivedTitle(view: WebView?, title: String?) {
                    super.onReceivedTitle(view, title)
                }

                // Web permissions: Microphone (Alice, speech), Camera, Protected Media
                override fun onPermissionRequest(request: PermissionRequest?) {
                    if (request == null) return
                    val resources = request.resources
                    val neededPermissions = mutableListOf<String>()

                    for (res in resources) {
                        if (res == PermissionRequest.RESOURCE_AUDIO_CAPTURE) {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                                neededPermissions.add(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                        if (res == PermissionRequest.RESOURCE_VIDEO_CAPTURE) {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                neededPermissions.add(Manifest.permission.CAMERA)
                            }
                        }
                    }

                    if (neededPermissions.isEmpty()) {
                        request.grant(resources)
                    } else {
                        pendingWebPermissionRequest = request
                        currentPermissionLauncher.value.launch(neededPermissions.toTypedArray())
                    }
                }

                override fun onPermissionRequestCanceled(request: PermissionRequest?) {
                    super.onPermissionRequestCanceled(request)
                    if (pendingWebPermissionRequest == request) {
                        pendingWebPermissionRequest = null
                    }
                }

                // Geolocation prompt
                override fun onGeolocationPermissionsShowPrompt(
                    origin: String?,
                    callback: GeolocationPermissions.Callback?
                ) {
                    if (origin == null || callback == null) return
                    val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

                    if (fineGranted || coarseGranted) {
                        callback.invoke(origin, true, false)
                    } else {
                        pendingGeolocationCallback = origin to callback
                        currentPermissionLauncher.value.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                }

                override fun onGeolocationPermissionsHidePrompt() {
                    super.onGeolocationPermissionsHidePrompt()
                    pendingGeolocationCallback = null
                }

                // File Chooser
                override fun onShowFileChooser(
                    webView: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    pendingFileChooserCallback?.onReceiveValue(null)
                    pendingFileChooserCallback = filePathCallback
                    val intent = try {
                        fileChooserParams?.createIntent() ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                            type = "*/*"
                            addCategory(Intent.CATEGORY_OPENABLE)
                        }
                    } catch (_: Exception) {
                        Intent(Intent.ACTION_GET_CONTENT).apply {
                            type = "*/*"
                            addCategory(Intent.CATEGORY_OPENABLE)
                        }
                    }
                    return try {
                        currentFileLauncher.value.launch(intent)
                        true
                    } catch (_: Exception) {
                        pendingFileChooserCallback?.onReceiveValue(null)
                        pendingFileChooserCallback = null
                        false
                    }
                }

                // JavaScript alert dialog
                override fun onJsAlert(
                    view: WebView?,
                    url: String?,
                    message: String?,
                    result: JsResult?
                ): Boolean {
                    val host = try { Uri.parse(url).host } catch (_: Exception) { null } ?: "Страница сообщает"
                    val ctx = view?.context ?: context
                    try {
                        AlertDialog.Builder(ctx, android.R.style.Theme_Holo_Dialog)
                            .setTitle(host)
                            .setMessage(message ?: "")
                            .setPositiveButton("OK") { dialog, _ ->
                                result?.confirm()
                                dialog.dismiss()
                            }
                            .setOnCancelListener {
                                result?.cancel()
                            }
                            .create()
                            .show()
                    } catch (_: Exception) {
                        result?.cancel()
                    }
                    return true
                }

                // JavaScript confirm dialog
                override fun onJsConfirm(
                    view: WebView?,
                    url: String?,
                    message: String?,
                    result: JsResult?
                ): Boolean {
                    val host = try { Uri.parse(url).host } catch (_: Exception) { null } ?: "Подтверждение"
                    val ctx = view?.context ?: context
                    try {
                        AlertDialog.Builder(ctx, android.R.style.Theme_Holo_Dialog)
                            .setTitle(host)
                            .setMessage(message ?: "")
                            .setPositiveButton("OK") { dialog, _ ->
                                result?.confirm()
                                dialog.dismiss()
                            }
                            .setNegativeButton("Отмена") { dialog, _ ->
                                result?.cancel()
                                dialog.dismiss()
                            }
                            .setOnCancelListener {
                                result?.cancel()
                            }
                            .create()
                            .show()
                    } catch (_: Exception) {
                        result?.cancel()
                    }
                    return true
                }

                // JavaScript prompt dialog
                override fun onJsPrompt(
                    view: WebView?,
                    url: String?,
                    message: String?,
                    defaultValue: String?,
                    result: JsPromptResult?
                ): Boolean {
                    val host = try { Uri.parse(url).host } catch (_: Exception) { null } ?: "Запрос ввода"
                    val ctx = view?.context ?: context
                    val editText = EditText(ctx).apply {
                        setText(defaultValue ?: "")
                        setSingleLine(true)
                        setTextColor(android.graphics.Color.WHITE)
                    }
                    val container = LinearLayout(ctx).apply {
                        setPadding(40, 10, 40, 10)
                        addView(editText, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    }

                    try {
                        AlertDialog.Builder(ctx, android.R.style.Theme_Holo_Dialog)
                            .setTitle(host)
                            .setMessage(message ?: "")
                            .setView(container)
                            .setPositiveButton("OK") { dialog, _ ->
                                result?.confirm(editText.text.toString())
                                dialog.dismiss()
                            }
                            .setNegativeButton("Отмена") { dialog, _ ->
                                result?.cancel()
                                dialog.dismiss()
                            }
                            .setOnCancelListener {
                                result?.cancel()
                            }
                            .create()
                            .show()
                    } catch (_: Exception) {
                        result?.cancel()
                    }
                    return true
                }
            }

            setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
                viewModel.handleDownload(url, userAgent, contentDisposition, mimetype, contentLength)
            }

            isLongClickable = true
            setOnLongClickListener { view ->
                val wv = view as? WebView ?: return@setOnLongClickListener false
                val hitResult = wv.hitTestResult ?: return@setOnLongClickListener false

                when (hitResult.type) {
                    WebView.HitTestResult.IMAGE_TYPE -> {
                        val imgUrl = hitResult.extra
                        if (!imgUrl.isNullOrBlank()) {
                            wv.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            viewModel.showContextMenu(
                                ContextMenuTarget(
                                    type = ContextMenuType.IMAGE,
                                    imageUrl = imgUrl,
                                    linkUrl = null
                                )
                            )
                            true
                        } else false
                    }
                    WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> {
                        wv.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        val mainHandler = Handler(Looper.getMainLooper())
                        val msg = mainHandler.obtainMessage()
                        msg.target = object : Handler(Looper.getMainLooper()) {
                            override fun handleMessage(m: Message) {
                                val linkUrl = m.data?.getString("url")
                                val imgSrc = m.data?.getString("src") ?: hitResult.extra
                                if (!imgSrc.isNullOrBlank() || !linkUrl.isNullOrBlank()) {
                                    val type = if (!imgSrc.isNullOrBlank() && !linkUrl.isNullOrBlank()) {
                                        ContextMenuType.IMAGE_LINK
                                    } else if (!imgSrc.isNullOrBlank()) {
                                        ContextMenuType.IMAGE
                                    } else {
                                        ContextMenuType.LINK
                                    }
                                    viewModel.showContextMenu(
                                        ContextMenuTarget(
                                            type = type,
                                            imageUrl = imgSrc,
                                            linkUrl = linkUrl
                                        )
                                    )
                                }
                            }
                        }
                        wv.requestFocusNodeHref(msg)
                        true
                    }
                    WebView.HitTestResult.SRC_ANCHOR_TYPE -> {
                        val linkUrl = hitResult.extra
                        if (!linkUrl.isNullOrBlank()) {
                            wv.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            viewModel.showContextMenu(
                                ContextMenuTarget(
                                    type = ContextMenuType.LINK,
                                    imageUrl = null,
                                    linkUrl = linkUrl
                                )
                            )
                            true
                        } else false
                    }
                    else -> false
                }
            }

            loadUrl(tab.url)
        }
    }

    DisposableEffect(tab.id) {
        viewModel.registerActiveWebView(webView)
        onDispose {
            pendingFileChooserCallback?.onReceiveValue(null)
            pendingFileChooserCallback = null
            pendingWebPermissionRequest?.deny()
            pendingWebPermissionRequest = null
            pendingGeolocationCallback?.second?.invoke(pendingGeolocationCallback?.first ?: "", false, false)
            pendingGeolocationCallback = null

            viewModel.registerActiveWebView(null)
            if (tab.isIncognito) {
                webView.clearCache(true)
                webView.clearHistory()
                webView.clearFormData()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { webView },
            modifier = Modifier.fillMaxSize()
        )
    }
}
