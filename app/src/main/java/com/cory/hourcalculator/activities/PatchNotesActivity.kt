package com.cory.hourcalculator.activities

import android.annotation.SuppressLint
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
import com.cory.hourcalculator.classes.HistoryToggleData
import com.cory.hourcalculator.classes.DarkThemeData
import com.cory.hourcalculator.classes.VibrationData
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

class PatchNotesActivity : AppCompatActivity() {

    private lateinit var firebaseAnalytics : FirebaseAnalytics
    private lateinit var saveData : DarkThemeData

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        saveData = DarkThemeData(this)
        if (saveData.loadDarkModeState()) {
            setTheme(R.style.AMOLED)
        } else {
            setTheme(R.style.AppTheme)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patch_notes)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        window.setBackgroundDrawable(null)

        firebaseAnalytics = Firebase.analytics

        findViewById<TextView>(R.id.textView).text = getString(R.string.whats_new) + " " + getString(R.string.version_number)

    }

    override fun onRestart() {
        super.onRestart()
        recreate()
    }

        private fun vibration(vibrationData: VibrationData) {
            if (vibrationData.loadVibrationState()) {
                val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu_patch_notes, menu)
        val historyToggleData = HistoryToggleData(this)
        if (!historyToggleData.loadHistoryState()) {
            val history = menu.findItem(R.id.history)
            history.isVisible = false
            val trash = menu.findItem(R.id.trash)
            trash.isVisible = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val vibrationData = VibrationData(this)
        vibration(vibrationData)
        return when (item.itemId) {
            R.id.home -> {
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "home_menu_item_patch")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "home_menu_item_clicked_patch")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "menu_item")
                }
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.Settings -> {
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "settings_menu_item_patch")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "settings_menu_item_clicked_patch")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "menu_item")
                }
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)

                return true
            }
            R.id.history -> {
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "history_menu_item_patch")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "history_menu_item_clicked_patch")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "menu_item")
                }
                val intent = Intent(this, HistoryActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.trash -> {
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "trash_menu_item_patch")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "trash_menu_item_clicked_patch")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "menu_item")
                }
                val intent = Intent(this, TrashActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.graph -> {
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "graph_menu_item_patch")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "graph_menu_item_clicked_patch")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "menu_item")
                }
                val intent = Intent(this, GraphActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

   override fun onBackPressed() {
       super.onBackPressed()
       finish()
    }
}