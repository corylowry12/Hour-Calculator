package com.cory.hourcalculator.activities

import android.content.Context
import android.content.Intent
import android.os.*
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.cory.hourcalculator.R
import com.cory.hourcalculator.classes.AccentColor
import com.cory.hourcalculator.classes.DarkThemeData
import com.cory.hourcalculator.classes.VibrationData
import com.cory.hourcalculator.database.DBHelper
import com.google.android.gms.ads.*

class DeleteAppDataActivity : AppCompatActivity() {

    private lateinit var darkThemeData: DarkThemeData
    private lateinit var accentColor: AccentColor
    private lateinit var vibrationData: VibrationData

    private val dbHandler = DBHelper(this, null)

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
        setContentView(R.layout.activity_delete_app_data)
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

        val historyToggleData = HistoryToggleData(this)

        bottomNav_deleteAppData.menu.findItem(R.id.menu_settings).isChecked = true
        bottomNav_deleteAppData.menu.findItem(R.id.menu_history).isVisible = historyToggleData.loadHistoryState()

        bottomNav_deleteAppData.setOnItemSelectedListener {
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

        val deleteButton = findViewById<Button>(R.id.deleteButton)
        deleteButton.setOnClickListener {
            vibration(vibrationData)
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle(getString(R.string.warning_delete))
            alertDialog.setMessage(getString(R.string.clear_app_data_delete_activity))
            alertDialog.setPositiveButton(getString(R.string.yes)) { _, _ ->
                vibration(vibrationData)
                applicationContext.getSharedPreferences("file", 0).edit().clear().apply()
                applicationContext.cacheDir.deleteRecursively()
                dbHandler.deleteAll()
                Toast.makeText(this, getString(R.string.app_data_cleared_toast), Toast.LENGTH_LONG).show()
                Handler(Looper.getMainLooper()).postDelayed({
                    val intent = applicationContext.packageManager.getLaunchIntentForPackage(applicationContext.packageName)
                    intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    this.finish()
                }, 1500)
            }
            alertDialog.setNeutralButton(getString(R.string.no)) { dialog, _ ->
                vibration(vibrationData)
                dialog.dismiss()
            }
            alertDialog.show()
        }
    }

    fun vibration(vibrationData: VibrationData) {
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
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}