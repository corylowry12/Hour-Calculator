package com.cory.hourcalculator.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import com.cory.hourcalculator.R
import com.cory.hourcalculator.classes.HistoryToggleData
import com.cory.hourcalculator.classes.DarkThemeData
import com.cory.hourcalculator.classes.VibrationData
import com.google.android.gms.ads.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

class VersionInfoActivity : AppCompatActivity() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var saveData : DarkThemeData

    override fun onCreate(savedInstanceState: Bundle?) {
        saveData = DarkThemeData(this)
        if (saveData.loadDarkModeState()) {
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
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "material_text_view")
                param(FirebaseAnalytics.Param.ITEM_NAME, "material_text_view_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "text_view")
            }
            txtMaterial.movementMethod = LinkMovementMethod.getInstance()
        }

        val txtAds = findViewById<TextView>(R.id.txtAdLink)
        txtAds.setOnClickListener {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "ad_text_view")
                param(FirebaseAnalytics.Param.ITEM_NAME, "ad_text_view_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "text_view")
            }
            txtAds.movementMethod = LinkMovementMethod.getInstance()
        }

        val txtSpinner = findViewById<TextView>(R.id.txtSpinnerLink)
        txtSpinner.setOnClickListener {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "spinner_text_view")
                param(FirebaseAnalytics.Param.ITEM_NAME, "spinner_text_view_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "text_view")
            }
            txtSpinner.movementMethod = LinkMovementMethod.getInstance()
        }

        val txtPlayLink = findViewById<TextView>(R.id.txtPlayLink)
        txtPlayLink.setOnClickListener {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "play_core_text_view")
                param(FirebaseAnalytics.Param.ITEM_NAME, "play_core_text_view_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "text_view")
            }
            txtPlayLink.movementMethod = LinkMovementMethod.getInstance()
        }

        val txtFireBaseLink = findViewById<TextView>(R.id.txtFirebaseLink)
        txtFireBaseLink.setOnClickListener {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "firebase_text_view")
                param(FirebaseAnalytics.Param.ITEM_NAME, "firebase_text_view_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "text_view")
            }
            txtFireBaseLink.movementMethod = LinkMovementMethod.getInstance()
        }

        val txtCrashlyticsLink = findViewById<TextView>(R.id.txtCrashlyticsLink)
        txtCrashlyticsLink.setOnClickListener {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "crashlytics_text_view")
                param(FirebaseAnalytics.Param.ITEM_NAME, "crashlytics_text_view_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "text_view")
            }
            txtCrashlyticsLink.movementMethod = LinkMovementMethod.getInstance()
        }

        val txtPerformanceLink = findViewById<TextView>(R.id.txtPerformanceLink)
        txtPerformanceLink.setOnClickListener {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "performance_text_view")
                param(FirebaseAnalytics.Param.ITEM_NAME, "performance_text_view_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "text_view")
            }
            txtPerformanceLink.movementMethod = LinkMovementMethod.getInstance()
        }

        val chartlink = findViewById<TextView>(R.id.txtMPANDROIDCHARTLINK)
        chartlink.setOnClickListener {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "chart_text_view")
                param(FirebaseAnalytics.Param.ITEM_NAME, "chart_text_view_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "text_view")
            }
            chartlink.movementMethod = LinkMovementMethod.getInstance()
        }

        val cloudLink = findViewById<TextView>(R.id.txtCloudMessagingLink)
        cloudLink.setOnClickListener {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "cloud_link_text_view")
                param(FirebaseAnalytics.Param.ITEM_NAME, "cloud_link_text_view_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "text_view")
            }
            cloudLink.movementMethod = LinkMovementMethod.getInstance()
        }
    }

    override fun onRestart() {
        super.onRestart()
        recreate()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
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
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        }
        return when (item.itemId) {
            R.id.home -> {
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "home_menu_item")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "home_menu_item_clicked")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "menu_item")
                }
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.Settings -> {
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "settings_menu_item")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "settings_menu_item_clicked")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "menu_item")
                }
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.changelog -> {
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "patch_notes_menu_item")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "patch_notes_menu_item_clicked")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "menu_item")
                }
                val intent = Intent(this, PatchNotesActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.history -> {
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "history_menu_item")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "history_menu_item_clicked")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "menu_item")
                }
                val intent = Intent(this, HistoryActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.trash -> {
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "trash_menu_item")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "trash_menu_item_clicked")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "menu_item")
                }
                val intent = Intent(this, TrashActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.graph -> {
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "graph_menu_item")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "graph_menu_item_clicked")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "menu_item")
                }
                val intent = Intent(this, GraphActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}