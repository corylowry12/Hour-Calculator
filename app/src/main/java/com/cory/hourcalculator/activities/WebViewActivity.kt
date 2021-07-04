package com.cory.hourcalculator.activities

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cory.hourcalculator.R
import com.cory.hourcalculator.classes.*
import kotlinx.android.synthetic.main.activity_web_view.*

class WebViewActivity : AppCompatActivity() {

    private lateinit var darkThemeData: DarkThemeData
    private lateinit var accentColor: AccentColor

    private lateinit var url: String

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        darkThemeData = DarkThemeData(this)
        if (darkThemeData.loadDarkModeState()) {
            setTheme(R.style.AMOLED)
        } else {
            setTheme(R.style.AppTheme)
        }
        accentColor = AccentColor(this)
        when {
            accentColor.loadAccent() == 0 -> {
                theme.applyStyle(R.style.teal_accent, true)
            }
            accentColor.loadAccent() == 1 -> {
                theme.applyStyle(R.style.pink_accent, true)
            }
            accentColor.loadAccent() == 2 -> {
                theme.applyStyle(R.style.orange_accent, true)
            }
            accentColor.loadAccent() == 3 -> {
                theme.applyStyle(R.style.red_accent, true)
            }
        }
        setContentView(R.layout.activity_web_view)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        val webView = findViewById<WebView>(R.id.webView)

        url = intent.getStringExtra("url").toString()

        Log.i("Link", url)
        when {
            url.contains("material") -> {
                supportActionBar!!.subtitle = getString(R.string.material)
            }
            url.contains("github") -> {
                supportActionBar!!.subtitle = getString(R.string.github)
            }
            url.contains("firebase") -> {
                supportActionBar!!.subtitle = getString(R.string.firebase)
            }
            url.contains("admob") -> {
                supportActionBar!!.subtitle = getString(R.string.admob)
            }
            url.contains("playcore") -> {
                supportActionBar!!.subtitle = getString(R.string.android_developers)
            }
        }

        webView.onResume()

        webView.loadUrl(url)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.visibility = View.VISIBLE
                swipeRefreshLayout.isRefreshing = false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                webView.loadUrl(url)
                return false
            }
        }
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && darkThemeData.loadDarkModeState()) {
            webView.settings.forceDark = WebSettings.FORCE_DARK_ON
        }
        swipeRefreshLayout.setOnRefreshListener {
            webView.reload()
        }

    }

    override fun onRestart() {
        super.onRestart()
        webView.onResume()
        recreate()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
            this.finish()
            if (!PerformanceModeData(this).loadPerformanceMode()) {
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            } else {
                overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu_webview, menu)
        val historyToggleData = HistoryToggleData(this)
        if (!historyToggleData.loadHistoryState()) {
            val history = menu.findItem(R.id.history)
            history.isVisible = false
            val trash = menu.findItem(R.id.trash)
            trash.isVisible = false
            val graph = menu.findItem(R.id.graph)
            graph.isVisible = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val vibrationData = VibrationData(this)
        return when (item.itemId) {
            R.id.refresh -> {
                vibration(vibrationData)
                webView.reload()
                return true
            }
            R.id.copyMenu -> {
                vibration(vibrationData)
                val clipBoard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("URL", intent.getStringExtra("url")) //intent.getStringExtra("url")
                clipBoard.setPrimaryClip(clip)
                Toast.makeText(this, getString(R.string.text_copied_to_clipboard), Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.shareMenu -> {
                vibration(vibrationData)
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.putExtra(Intent.EXTRA_TEXT, intent.getStringExtra("url"))
                shareIntent.type = "text/plain"
                Intent.createChooser(shareIntent, getString(R.string.share_via))
                startActivity(shareIntent)
                return true
            }
            R.id.home -> {
                vibration(vibrationData)
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                if (!PerformanceModeData(this).loadPerformanceMode()) {
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                } else {
                    overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
                }
                return true
            }
            R.id.Settings -> {
                vibration(vibrationData)
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                if (!PerformanceModeData(this).loadPerformanceMode()) {
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                } else {
                    overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
                }
                return true
            }
            R.id.changelog -> {
                vibration(vibrationData)
                val intent = Intent(this, PatchNotesActivity::class.java)
                startActivity(intent)
                if (!PerformanceModeData(this).loadPerformanceMode()) {
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                } else {
                    overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
                }
                return true
            }
            R.id.history -> {
                vibration(vibrationData)
                val intent = Intent(this, HistoryActivity::class.java)
                startActivity(intent)
                if (!PerformanceModeData(this).loadPerformanceMode()) {
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                } else {
                    overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
                }
                return true
            }
            R.id.trash -> {
                vibration(vibrationData)
                val intent = Intent(this, TrashActivity::class.java)
                startActivity(intent)
                if (!PerformanceModeData(this).loadPerformanceMode()) {
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                } else {
                    overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
                }
                return true
            }
            R.id.graph -> {
                vibration(vibrationData)
                val intent = Intent(this, GraphActivity::class.java)
                startActivity(intent)
                if (!PerformanceModeData(this).loadPerformanceMode()) {
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                } else {
                    overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
                }
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun vibration(vibrationData: VibrationData) {
        if (vibrationData.loadVibrationState()) {
            val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(5, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }
}