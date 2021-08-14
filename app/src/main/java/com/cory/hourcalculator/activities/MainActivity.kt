@file:Suppress("unused")

package com.cory.hourcalculator.activities

import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cory.hourcalculator.R
import com.cory.hourcalculator.classes.*
import com.cory.hourcalculator.database.DBHelper
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.Display
import com.github.javiersantos.appupdater.enums.UpdateFrom
import com.google.android.gms.ads.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class MainActivity : AppCompatActivity() {

    // private lateinit var vibrationData: VibrationData
    private lateinit var vibrationData: VibrationData
    private lateinit var darkThemeData: DarkThemeData
    private lateinit var accentColor: AccentColor
    private lateinit var historyToggleData: HistoryToggleData
    private lateinit var trashAutomaticDeletion: TrashAutomaticDeletion

    val testDeviceId = listOf("5E80E48DC2282D372EAE0E3ACDE070CC", "8EE44B7B4B422D333731760574A381FE")
    private val dbHandler = DBHelper(this, null)

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    // Spinner lazy and lateinit initializers
    private lateinit var spinner1selecteditem: String
    private lateinit var spinner2selecteditem: String

    override fun onCreate(savedInstanceState: Bundle?) {
        firebaseAnalytics = Firebase.analytics
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
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(null)
        setContentView(R.layout.activity_main)

        timePickerInTime.setOnTimeChangedListener { view, hourOfDay, minute ->
            Log.i("Hour of day", hourOfDay.toString())
        }

        val appUpdater = AppUpdater(this)
            .setDisplay(Display.DIALOG)
            .setCancelable(false)
            .showEvery(5)
            .setUpdateFrom(UpdateFrom.GOOGLE_PLAY)
            .setTitleOnUpdateAvailable(getString(R.string.update_available))
            .setContentOnUpdateAvailable(getString(R.string.check_out_latest_version))
            .setButtonUpdate(getString(R.string.update))
            .setButtonDismiss(getString(R.string.maybe_later))
        appUpdater.start()

        historyToggleData = HistoryToggleData(this)
        trashAutomaticDeletion = TrashAutomaticDeletion(this)

        MobileAds.initialize(this)
        val adView = AdView(this)
        adView.adSize = AdSize.BANNER
        adView.adUnitId = "ca-app-pub-4546055219731501/5171269817"
        val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceId).build()
        MobileAds.setRequestConfiguration(configuration)
        val mAdView = findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        //mAdView.loadAd(adRequest)
        //mAdView.adListener = object : AdListener() {
        //}

        val historyAutomaticDeletion = HistoryAutomaticDeletion(this)
        val historyDeletion = HistoryDeletion(this)
        val daysWorked = DaysWorkedPerWeek(this)

        if (daysWorked.loadDaysWorked() != "" && historyAutomaticDeletion.loadHistoryDeletionState() && dbHandler.getCount() > daysWorked.loadDaysWorked().toString().toInt()) {
            historyDeletion.deletion(this)
        }

        bottomNav.menu.findItem(R.id.menu_home).isChecked = true

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
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

        main()

    }

    override fun onRestart() {
        super.onRestart()
        val intent = Intent(this, this::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun onResume() {
        super.onResume()
        main()
    }

    fun main() {

        calculateButton1.setOnClickListener {
            vibration(vibrationData)
            calculate()
        }
    }

    private fun calculate() {
        val inTimeMinutes = timePickerInTime.minute
        val inTimeHours = timePickerInTime.hour
        val outTimeMinutes = timePickerOutTime.minute
        val outTimeHours = timePickerOutTime.hour

        var inTimeTotal = ""
        var outTimeTotal  = ""

        var minutesDecimal: Double = (outTimeMinutes - inTimeMinutes) / 60.0
        minutesDecimal = minutesDecimal.toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toDouble()
        var minutesWithoutFirstDecimal = minutesDecimal.toString().substring(2)
        if (minutesDecimal < 0) {
            minutesWithoutFirstDecimal = (1.0 - minutesWithoutFirstDecimal.toDouble()).toString()
            minutesWithoutFirstDecimal = minutesWithoutFirstDecimal.toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toString()
            minutesWithoutFirstDecimal = minutesWithoutFirstDecimal.substring(2)
        }
        var hoursDifference = outTimeHours - inTimeHours
        if (minutesDecimal < 0) {
            hoursDifference -= 1
        }
        if (hoursDifference < 0) {
            hoursDifference += 24
        }

        if (inTimeHours > 12) {
            val inTime = inTimeHours - 12
            val amOrPm = getString(R.string.pm)
            inTimeTotal = "$inTime:$inTimeMinutes $amOrPm"
        } else if (inTimeHours == 0) {
            val inTime = inTimeHours + 12
            val amOrPm = getString(R.string.am)
            inTimeTotal = "$inTime:$inTimeMinutes $amOrPm"
        } else {
            val amOrPm = getString(R.string.am)
            inTimeTotal = "$inTimeHours:$inTimeMinutes $amOrPm"
        }
        if (outTimeHours > 12) {
            val outTime = outTimeHours - 12
            val amOrPm = getString(R.string.pm)
            outTimeTotal = "$outTime:$outTimeMinutes $amOrPm"
        } else if (outTimeHours == 0) {
            val outTime = outTimeHours + 12
            val amOrPm = getString(R.string.am)
            outTimeTotal = "$outTime:$outTimeMinutes $amOrPm"
        } else {
            val amOrPm = getString(R.string.am)
            outTimeTotal = "$outTimeHours:$outTimeMinutes $amOrPm"
        }
        val totalHours = "$hoursDifference.$minutesWithoutFirstDecimal".toDouble()
        savingHours(totalHours, inTimeTotal, outTimeTotal)
        infoTextView1.text = getString(R.string.total_hours, "$hoursDifference.$minutesWithoutFirstDecimal")
    }

    private fun savingHours(totalHours3: Double, inTime: String, outTime: String) {
        val day = LocalDateTime.now()
        val day2 = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        val dayOfWeek = day.format(day2)
        dbHandler.insertRow(inTime, outTime, totalHours3.toString(), dayOfWeek)
    }

    fun vibration(vibrationData: VibrationData) {
        if (vibrationData.loadVibrationState()) {
            val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(5, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    private var doubleBackToExitPressedOnce = false

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finishAffinity()
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, getString(R.string.click_back_again), Toast.LENGTH_SHORT).show()

        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu_home, menu)
        val historyToggleData = HistoryToggleData(this)
        if (!historyToggleData.loadHistoryState()) {
            val history = menu.findItem(R.id.history)
            history.isVisible = false
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
            else -> super.onOptionsItemSelected(item)
        }
    }
}