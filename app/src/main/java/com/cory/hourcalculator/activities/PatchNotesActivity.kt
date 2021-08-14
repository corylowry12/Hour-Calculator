package com.cory.hourcalculator.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cory.hourcalculator.R
import com.cory.hourcalculator.classes.AccentColor
import com.cory.hourcalculator.classes.DarkThemeData
import com.cory.hourcalculator.classes.VibrationData
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_patch_notes.*

class PatchNotesActivity : AppCompatActivity() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private lateinit var darkThemeData: DarkThemeData
    private lateinit var accentColor: AccentColor

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
        setContentView(R.layout.activity_patch_notes)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        window.setBackgroundDrawable(null)

        bottomNav_patchNotes.menu.findItem(R.id.menu_settings).isChecked = true

        bottomNav_patchNotes.setOnItemSelectedListener {
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

        firebaseAnalytics = Firebase.analytics

        findViewById<TextView>(R.id.textView).text = getString(R.string.whats_new, getString(R.string.version_number))

        val vibrationData = VibrationData(this)

        linkTextView.setOnClickListener {
            vibration(vibrationData)
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra("url", getString(R.string.github_link))
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

        }

    }

    override fun onRestart() {
        super.onRestart()
        super.onRestart()
        val intent = Intent(this, this::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)

    }

    private fun vibration(vibrationData: VibrationData) {
        if (vibrationData.loadVibrationState()) {
            val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(5, VibrationEffect.DEFAULT_AMPLITUDE))
        }
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