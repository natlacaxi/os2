package fr.minios

import android.Manifest
import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebViewAssetLoader
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class MainActivity : Activity() {
    private lateinit var web: WebView
    private var filePicker: ValueCallback<Array<Uri>>? = null

    /** Pont JS -> Android : window.AndroidBridge */
    inner class Bridge {
        private fun go(i: Intent) {
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try { startActivity(i) } catch (e: Exception) {}
        }
        @JavascriptInterface fun wifi() = go(Intent(if (Build.VERSION.SDK_INT >= 29) Settings.Panel.ACTION_INTERNET_CONNECTIVITY else Settings.ACTION_WIFI_SETTINGS))
        @JavascriptInterface fun bluetooth() = go(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
        @JavascriptInterface fun airplane() = go(Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS))
        @JavascriptInterface fun settings() = go(Intent(Settings.ACTION_SETTINGS))
        @JavascriptInterface fun homeSettings() = go(Intent(Settings.ACTION_HOME_SETTINGS))
        @JavascriptInterface fun launch(p: String) { packageManager.getLaunchIntentForPackage(p)?.let { go(it) } }

        @JavascriptInterface fun setVolume(p: Int) {
            val a = getSystemService(AUDIO_SERVICE) as AudioManager
            val m = a.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            a.setStreamVolume(AudioManager.STREAM_MUSIC, p.coerceIn(0, 100) * m / 100, 0)
        }
        @JavascriptInterface fun getVolume(): Int {
            val a = getSystemService(AUDIO_SERVICE) as AudioManager
            return a.getStreamVolume(AudioManager.STREAM_MUSIC) * 100 / a.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        }
        @JavascriptInterface fun setBrightness(p: Int) {
            runOnUiThread { window.attributes = window.attributes.also { it.screenBrightness = p.coerceIn(5, 100) / 100f } }
        }
        @JavascriptInterface fun keepAwake(on: Boolean) {
            runOnUiThread { if (on) window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) else window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
        }
        @JavascriptInterface fun torch(on: Boolean) {
            try { val c = getSystemService(CAMERA_SERVICE) as CameraManager; c.setTorchMode(c.cameraIdList[0], on) } catch (e: Exception) {}
        }
        /** Toutes les apps Android installées : [{n, p, i(png base64)}] */
        @JavascriptInterface fun apps(): String {
            val pm = packageManager
            val q = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            val arr = JSONArray()
            pm.queryIntentActivities(q, 0)
                .filter { it.activityInfo.packageName != packageName }
                .sortedBy { it.loadLabel(pm).toString().lowercase() }
                .forEach { r ->
                    val d = r.loadIcon(pm)
                    val bmp = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888)
                    d.setBounds(0, 0, 96, 96); d.draw(Canvas(bmp))
                    val out = ByteArrayOutputStream(); bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
                    arr.put(JSONObject().put("n", r.loadLabel(pm).toString()).put("p", r.activityInfo.packageName)
                        .put("i", Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)))
                }
            return arr.toString()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val loader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this)).build()
        web = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            addJavascriptInterface(Bridge(), "AndroidBridge")
            webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(v: WebView, r: WebResourceRequest): WebResourceResponse? =
                    loader.shouldInterceptRequest(r.url)
            }
            webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest) {
                    runOnUiThread { request.grant(request.resources) }
                }
                override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
                    callback.invoke(origin, true, false)
                }
                override fun onShowFileChooser(view: WebView, cb: ValueCallback<Array<Uri>>, params: FileChooserParams): Boolean {
                    filePicker?.onReceiveValue(null)
                    filePicker = cb
                    return try {
                        startActivityForResult(params.createIntent(), 9)
                        true
                    } catch (e: Exception) {
                        filePicker = null
                        false
                    }
                }
            }
            loadUrl("https://appassets.androidplatform.net/assets/index.html")
        }
        setContentView(web)
        immersive()
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        }
    }

    private fun immersive() {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULL_STACK or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 9) {
            filePicker?.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data))
            filePicker = null
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) { super.onWindowFocusChanged(hasFocus); if (hasFocus) immersive() }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() { web.evaluateJavascript("closeApp()", null) }
}
