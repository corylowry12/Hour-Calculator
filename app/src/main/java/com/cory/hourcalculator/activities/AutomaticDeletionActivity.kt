package com.cory.hourcalculator.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Menu
import android.view.MenuItem
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.cory.hourcalculator.R
import com.cory.hourcalculator.classes.*
import com.google.android.gms.ads.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_automatic_deletion.*

class AutomaticDeletionActivity : AppCompatActivity() {

    private lateinit var darkThemeData : DarkThemeData

    val testDeviceId = listOf("5E80E48DC2282D372EAE0E3ACDE070CC", "8EE44B7B4B422D333731760574A381FE")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        darkThemeData = DarkThemeData(this)
        if (darkThemeData.loadDarkModeState()) {
            setTheme(R.style.AMOLED)
        } else {
            setTheme(R.style.AppTheme)
        }
        setContentView(R.layout.activity_automatic_deletion)
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

        val trashAutomaticDeletion = TrashAutomaticDeletion(this)
        val vibrationData = VibrationData(this)

        val enableTrashAutomaticDeletion = findViewById<RadioButton>(R.id.enableTrashDeletion)
        val disableTrashAutomaticDeletion = findViewById<RadioButton>(R.id.disableTrashDeletion)

        if (trashAutomaticDeletion.loadTrashDeletionState()) {
            enableTrashAutomaticDeletion.isChecked = true
        }
        else if (!trashAutomaticDeletion.loadTrashDeletionState()) {
            disableTrashAutomaticDeletion.isChecked = true
        }

        enableTrashAutomaticDeletion.setOnClickListener {
            vibration(vibrationData)
            if(trashAutomaticDeletion.loadTrashDeletionState()) {
                Toast.makeText(this, getString(R.string.trash_automatic_deletion_is_already_enabled), Toast.LENGTH_SHORT).show()
            }
            else {
                trashAutomaticDeletion.setTrashDeletionState(true)
                Snackbar.make(automaticDeletionConstraint, getString(R.string.enable_automatic_deletion), Snackbar.LENGTH_SHORT).show()
            }
        }
        disableTrashAutomaticDeletion.setOnClickListener {
            vibration(vibrationData)
            if(!trashAutomaticDeletion.loadTrashDeletionState()) {
                Toast.makeText(this, getString(R.string.trash_automatic_deletion_is_already_disabled), Toast.LENGTH_SHORT).show()
            }
            else {
                trashAutomaticDeletion.setTrashDeletionState(false)
                Snackbar.make(automaticDeletionConstraint, getString(R.string.disable_automatic_deletion), Snackbar.LENGTH_SHORT).show()
            }
        }

        val historyDeletionCardView = findViewById<CardView>(R.id.historyDeletionCardView)
        historyDeletionCardView.setOnClickListener {
            Toast.makeText(this, getString(R.string.feature_coming_soon), Toast.LENGTH_SHORT).show()
        }
    }

    private fun vibration(vibrationData: VibrationData) {
        if (vibrationData.loadVibrationState()) {
            val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(5, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    override fun onRestart() {
        super.onRestart()
        val intent = Intent(this, this::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        if(!PerformanceModeData(this).loadPerformanceMode()) {
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
        else {
            overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
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
        menuInflater.inflate(R.menu.main_menu_automatic_deletion, menu)
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