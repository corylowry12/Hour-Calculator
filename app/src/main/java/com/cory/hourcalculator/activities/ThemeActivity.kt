package com.cory.hourcalculator.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cory.hourcalculator.R
import com.cory.hourcalculator.classes.AccentColor
import com.cory.hourcalculator.classes.DarkThemeData
import com.cory.hourcalculator.classes.VibrationData
import com.google.android.gms.ads.*
import kotlinx.android.synthetic.main.activity_theme.*

class ThemeActivity : AppCompatActivity() {

    private lateinit var darkThemeData: DarkThemeData
    private lateinit var accentColor: AccentColor
    private lateinit var vibrationData: VibrationData

    val testDeviceId = listOf("5E80E48DC2282D372EAE0E3ACDE070CC", "8EE44B7B4B422D333731760574A381FE")

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
        setContentView(R.layout.activity_theme)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        MobileAds.initialize(this)
        val adView = AdView(this)
        adView.adSize = AdSize.BANNER
        adView.adUnitId = "ca-app-pub-4546055219731501/5171269817"
        val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceId).build()
        MobileAds.setRequestConfiguration(configuration)
        val mAdView = findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        mAdView.adListener = object : AdListener() {
        }

        bottomNav_theme.menu.findItem(R.id.menu_settings).isChecked = true

        bottomNav_theme.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.menu_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                    true
                }
                R.id.menu_history -> {
                    val intent = Intent(this, HistoryActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                    true
                }
                R.id.menu_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                    true
                }
                else -> false
            }
        }

        vibrationData = VibrationData(this)

        val lightThemeButton = findViewById<RadioButton>(R.id.lightTheme)
        val darkThemeButton = findViewById<RadioButton>(R.id.darkTheme)

        if (darkThemeData.loadDarkModeState()) {
            darkThemeButton.isChecked = true
        } else if (!darkThemeData.loadDarkModeState()) {
            lightThemeButton.isChecked = true
        }

        lightThemeButton.setOnClickListener {
            vibration(vibrationData)
            if(!darkThemeData.loadDarkModeState()) {
                Toast.makeText(this, getString(R.string.light_theme_is_already_enabled), Toast.LENGTH_SHORT).show()
            }
            else {
                darkThemeData.setDarkModeState(false)
                restartApplication()
            }
        }
        darkThemeButton.setOnClickListener {
            vibration(vibrationData)
            if (darkThemeData.loadDarkModeState()) {
                Toast.makeText(this, getString(R.string.dark_mode_is_already_enabled), Toast.LENGTH_SHORT).show()
            } else {
                darkThemeData.setDarkModeState(true)
                restartApplication()
            }
        }

        val tealAccentButton = findViewById<RadioButton>(R.id.Teal)
        val pinkAccentButton = findViewById<RadioButton>(R.id.Pink)
        val orangeAccentButton = findViewById<RadioButton>(R.id.Orange)
        val redAccentButton = findViewById<RadioButton>(R.id.Red)

        if (accentColor.loadAccent() == 0) {
            tealAccentButton.isChecked = true
        }
        else if (accentColor.loadAccent() == 1) {
            pinkAccentButton.isChecked = true
        }
        else if (accentColor.loadAccent() == 2) {
            orangeAccentButton.isChecked = true
        }
        else if (accentColor.loadAccent() == 3) {
            redAccentButton.isChecked = true
        }

        tealAccentButton.setOnClickListener {
            vibration(vibrationData)
            accentColor.setAccentState(0)
            restartApplication()
        }
        pinkAccentButton.setOnClickListener {
            vibration(vibrationData)
            accentColor.setAccentState(1)
            restartApplication()
        }
        orangeAccentButton.setOnClickListener {
            vibration(vibrationData)
            accentColor.setAccentState(2)
            restartApplication()
        }
        redAccentButton.setOnClickListener {
            vibration(vibrationData)
            accentColor.setAccentState(3)
            restartApplication()
        }
    }

    private fun vibration(vibrationData: VibrationData) {
        if (vibrationData.loadVibrationState()) {
            val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(5, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    private fun restartApplication() {
        val intent = this.intent
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun onRestart() {
        super.onRestart()
        val intent = Intent(this, this::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}