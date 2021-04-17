package com.cory.hourcalculator.activities

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cory.hourcalculator.R
import com.cory.hourcalculator.classes.DarkThemeData
import com.cory.hourcalculator.classes.HistoryToggleData
import kotlinx.android.synthetic.main.activity_web_view.*

class WebViewActivity : AppCompatActivity() {

    private lateinit var darkThemeData : DarkThemeData

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        darkThemeData = DarkThemeData(this)
        if (darkThemeData.loadDarkModeState()) {
            setTheme(R.style.AMOLED)
        } else {
            setTheme(R.style.AppTheme)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        val webView = findViewById<WebView>(R.id.webView)

        val url = intent.getStringExtra("url")
        webView.loadUrl(url.toString())
        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true

    }

    override fun onNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        }
        else {
            super.onBackPressed()
            this.finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
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
        return when (item.itemId) {
            R.id.copyMenu -> {
                val clipBoard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("URL", intent.getStringExtra("url")) //intent.getStringExtra("url")
                clipBoard.setPrimaryClip(clip)
                Toast.makeText(this, getString(R.string.text_copied_to_clipboard), Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.shareMenu -> {
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.putExtra(Intent.EXTRA_TEXT, intent.getStringExtra("url"))
                shareIntent.type = "text/plain"
                Intent.createChooser(shareIntent, getString(R.string.share_via))
                startActivity(shareIntent)
                return true
            }
            R.id.Settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                return true
            }
            R.id.changelog -> {
                val intent = Intent(this, PatchNotesActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                return true
            }
            R.id.history -> {
                val intent = Intent(this, HistoryActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                return true
            }
            R.id.trash -> {
                val intent = Intent(this, TrashActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                return true
            }
            R.id.graph -> {
                val intent = Intent(this, GraphActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}