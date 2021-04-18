package com.cory.hourcalculator.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cory.hourcalculator.R
import com.cory.hourcalculator.classes.DarkThemeData
import com.cory.hourcalculator.classes.HistoryToggleData
import com.cory.hourcalculator.classes.PerformanceModeData
import com.cory.hourcalculator.classes.VibrationData
import com.google.android.gms.ads.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

class VersionInfoActivity : AppCompatActivity() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var darkThemeData : DarkThemeData

    override fun onCreate(savedInstanceState: Bundle?) {
        darkThemeData = DarkThemeData(this)
        if (darkThemeData.loadDarkModeState()) {
            setTheme(R.style.AMOLED)
        } else {
            setTheme(R.style.AppTheme)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_version_info)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        window.setBackgroundDrawable(null)

        firebaseAnalytics = Firebase.analytics

        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        MobileAds.initialize(this)
        val adView = AdView(this)
        adView.adSize = AdSize.BANNER
        adView.adUnitId = "ca-app-pub-4546055219731501/5171269817"
        val mAdView = findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        mAdView.adListener = object : AdListener() {
        }

        val txtMaterial = findViewById<TextView>(R.id.txtMaterialLink)
        txtMaterial.setOnClickListener {
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra("url", getString(R.string.materialLink))
            startActivity(intent)
            if(!PerformanceModeData(this).loadPerformanceMode()) {
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            else {
                overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
            }
        }

        val txtAds = findViewById<TextView>(R.id.txtAdLink)
        txtAds.setOnClickListener {
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra("url", getString(R.string.AdmobLink))
            startActivity(intent)
            if(!PerformanceModeData(this).loadPerformanceMode()) {
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            else {
                overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
            }
        }

        val txtSpinner = findViewById<TextView>(R.id.txtSpinnerLink)
        txtSpinner.setOnClickListener {
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra("url", getString(R.string.spinnerLink))
            startActivity(intent)
            if(!PerformanceModeData(this).loadPerformanceMode()) {
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            else {
                overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
            }
        }

        val txtPlayLink = findViewById<TextView>(R.id.txtPlayLink)
        txtPlayLink.setOnClickListener {
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra("url", getString(R.string.PlayCoreLink))
            startActivity(intent)
            if(!PerformanceModeData(this).loadPerformanceMode()) {
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            else {
                overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
            }
        }

        val txtFireBaseLink = findViewById<TextView>(R.id.txtFirebaseLink)
        txtFireBaseLink.setOnClickListener {
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra("url", getString(R.string.FirebaseLink))
            startActivity(intent)
            if(!PerformanceModeData(this).loadPerformanceMode()) {
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            else {
                overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
            }
        }

        val txtCrashlyticsLink = findViewById<TextView>(R.id.txtCrashlyticsLink)
        txtCrashlyticsLink.setOnClickListener {
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra("url", getString(R.string.CrashlyticsLink))
            startActivity(intent)
            if(!PerformanceModeData(this).loadPerformanceMode()) {
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            else {
                overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
            }
        }

        val txtPerformanceLink = findViewById<TextView>(R.id.txtPerformanceLink)
        txtPerformanceLink.setOnClickListener {
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra("url", getString(R.string.PerformanceLink))
            startActivity(intent)
            if(!PerformanceModeData(this).loadPerformanceMode()) {
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            else {
                overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
            }
        }

        val chartlink = findViewById<TextView>(R.id.txtMPANDROIDCHARTLINK)
        chartlink.setOnClickListener {
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra("url", getString(R.string.MPAndroidLink))
            startActivity(intent)
            if(!PerformanceModeData(this).loadPerformanceMode()) {
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            else {
                overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
            }
        }

        val cloudLink = findViewById<TextView>(R.id.txtCloudMessagingLink)
        cloudLink.setOnClickListener {
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra("url", getString(R.string.CloudMessagingLink))
            startActivity(intent)
            if(!PerformanceModeData(this).loadPerformanceMode()) {
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            else {
                overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
        if(!PerformanceModeData(this).loadPerformanceMode()) {
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
        else {
            overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu_version_info, menu)
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
        if (vibrationData.loadVibrationState()) {
            val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(5, VibrationEffect.DEFAULT_AMPLITUDE))
        }
        return when (item.itemId) {
            R.id.home -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                if(!PerformanceModeData(this).loadPerformanceMode()) {
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                }
                else {
                    overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
                }
                return true
            }
            R.id.Settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                if(!PerformanceModeData(this).loadPerformanceMode()) {
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                }
                else {
                    overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
                }
                return true
            }
            R.id.changelog -> {
                val intent = Intent(this, PatchNotesActivity::class.java)
                startActivity(intent)
                if(!PerformanceModeData(this).loadPerformanceMode()) {
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                }
                else {
                    overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
                }
                return true
            }
            R.id.history -> {
                val intent = Intent(this, HistoryActivity::class.java)
                startActivity(intent)
                if(!PerformanceModeData(this).loadPerformanceMode()) {
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                }
                else {
                    overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
                }
                return true
            }
            R.id.trash -> {
                val intent = Intent(this, TrashActivity::class.java)
                startActivity(intent)
                if(!PerformanceModeData(this).loadPerformanceMode()) {
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                }
                else {
                    overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
                }
                return true
            }
            R.id.graph -> {
                val intent = Intent(this, GraphActivity::class.java)
                startActivity(intent)
                if(!PerformanceModeData(this).loadPerformanceMode()) {
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                }
                else {
                    overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
                }
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}