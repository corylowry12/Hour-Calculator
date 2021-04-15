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
import kotlinx.android.synthetic.main.activity_patch_notes.*

class PatchNotesActivity : AppCompatActivity() {

    private lateinit var firebaseAnalytics : FirebaseAnalytics
    private lateinit var darkThemeData : DarkThemeData

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        darkThemeData = DarkThemeData(this)
        if (darkThemeData.loadDarkModeState()) {
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

        linkTextView.setOnClickListener {
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra("url", getString(R.string.github_link))
            startActivity(intent)
        }

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
            val graph = menu.findItem(R.id.graph)
            graph.isVisible = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val vibrationData = VibrationData(this)
        vibration(vibrationData)
        return when (item.itemId) {
            R.id.Settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.history -> {
                val intent = Intent(this, HistoryActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.trash -> {
                val intent = Intent(this, TrashActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.graph -> {
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