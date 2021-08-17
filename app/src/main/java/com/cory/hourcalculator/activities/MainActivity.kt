@file:Suppress("unused")

package com.cory.hourcalculator.activities

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cory.hourcalculator.BuildConfig
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

    private lateinit var vibrationData: VibrationData
    private lateinit var darkThemeData: DarkThemeData
    private lateinit var accentColor: AccentColor
    private lateinit var historyToggleData: HistoryToggleData

    private val testDeviceId = listOf("5E80E48DC2282D372EAE0E3ACDE070CC", "8EE44B7B4B422D333731760574A381FE")
    private val dbHandler = DBHelper(this, null)

    private lateinit var firebaseAnalytics: FirebaseAnalytics

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

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(constraintLayout.windowToken, 0)
        imm.hideSoftInputFromWindow(timePickerInTime.windowToken, 0)
        imm.hideSoftInputFromWindow(timePickerOutTime.windowToken, 0)

        val historyAutomaticDeletion = HistoryAutomaticDeletion(this)
        val historyDeletion = HistoryDeletion(this)
        val daysWorked = DaysWorkedPerWeek(this)

        if (daysWorked.loadDaysWorked() != "" && historyAutomaticDeletion.loadHistoryDeletionState() && dbHandler.getCount() > daysWorked.loadDaysWorked().toString().toInt()) {
            historyDeletion.deletion(this)
        }
        val dateData = DateData(this)
        if (dateData.loadMinutes1() != "") {
            timePickerInTime.minute = dateData.loadMinutes1()!!.toInt()
        } else {
            dateData.setMinutes1(timePickerInTime.minute.toString())
        }
        if (dateData.loadHours1() != "") {
            timePickerInTime.hour = dateData.loadHours1()!!.toInt()
        } else {
            dateData.setHours1(timePickerInTime.hour.toString())
        }
        if (dateData.loadMinutes2() != "") {
            timePickerOutTime.minute = dateData.loadMinutes2()!!.toInt()
        } else {
            dateData.setMinutes2(timePickerOutTime.minute.toString())
        }
        if (dateData.loadHours2() != "") {
            timePickerOutTime.hour = dateData.loadHours2()!!.toInt()
        } else {
            dateData.setHours2(timePickerOutTime.hour.toString())
        }

        timePickerInTime.setOnTimeChangedListener { _, hourOfDay, minute ->
            dateData.setMinutes1(minute.toString())
            dateData.setHours1(hourOfDay.toString())
        }

        timePickerOutTime.setOnTimeChangedListener { _, hourOfDay, minute ->
            dateData.setMinutes2(minute.toString())
            dateData.setHours2(hourOfDay.toString())
        }

        bottomNav.menu.findItem(R.id.menu_home).isChecked = true
        bottomNav.menu.findItem(R.id.menu_history).isVisible = historyToggleData.loadHistoryState()

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
        var inTimeMinutes = timePickerInTime.minute.toString()
        val inTimeHours = timePickerInTime.hour.toString()
        var outTimeMinutes = timePickerOutTime.minute.toString()
        val outTimeHours = timePickerOutTime.hour.toString()

        if (inTimeMinutes.length == 1) {
            inTimeMinutes = "0$inTimeMinutes"
        }

        if (outTimeMinutes.length == 1) {
            outTimeMinutes = "0$outTimeMinutes"
        }
        val inTimeTotal: String
        val outTimeTotal: String

        var minutesDecimal: Double = (outTimeMinutes.toInt() - inTimeMinutes.toInt()) / 60.0
        minutesDecimal = minutesDecimal.toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toDouble()
        var minutesWithoutFirstDecimal = minutesDecimal.toString().substring(2)
        if (minutesDecimal < 0) {
            minutesWithoutFirstDecimal = (1.0 - minutesWithoutFirstDecimal.toDouble()).toString()
            minutesWithoutFirstDecimal = minutesWithoutFirstDecimal.toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toString()
            minutesWithoutFirstDecimal = minutesWithoutFirstDecimal.substring(2)
        }
        var hoursDifference = outTimeHours.toInt() - inTimeHours.toInt()
        if ("$hoursDifference.$minutesWithoutFirstDecimal".toDouble() == 0.0) {
            infoTextView1.text = getString(R.string.in_time_and_out_time_can_not_be_the_same)
        } else if (timePickerInTime.hour >= 0 && timePickerOutTime.hour <= 12 && hoursDifference < 0) {
            infoTextView1.text = getString(R.string.in_time_can_not_be_greater_than_out_time)
        } else if (timePickerInTime.hour >= 12 && timePickerOutTime.hour <= 24 && hoursDifference < 0) {
            infoTextView1.text = getString(R.string.in_time_can_not_be_greater_than_out_time)
        } else {
            if (minutesDecimal < 0) {
                hoursDifference -= 1
            }
            if (hoursDifference < 0) {
                hoursDifference += 24
            }
            if (inTimeHours.toInt() > 12) {
                val inTime = inTimeHours.toInt() - 12
                val amOrPm = getString(R.string.pm)
                inTimeTotal = "$inTime:$inTimeMinutes $amOrPm"
            } else if (inTimeHours.toInt() == 0) {
                val inTime = inTimeHours + 12
                val amOrPm = getString(R.string.am)
                inTimeTotal = "$inTime:$inTimeMinutes $amOrPm"
            } else {
                val amOrPm = getString(R.string.am)
                inTimeTotal = "$inTimeHours:$inTimeMinutes $amOrPm"
            }
            if (outTimeHours.toInt() > 12) {
                val outTime = outTimeHours.toInt() - 12
                val amOrPm = getString(R.string.pm)
                outTimeTotal = "$outTime:$outTimeMinutes $amOrPm"
            } else if (outTimeHours.toInt() == 0) {
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

    override fun onDestroy() {
        super.onDestroy()
        if (accentColor.loadAccent() == 0) {
            packageManager.setComponentEnabledSetting(
                ComponentName(BuildConfig.APPLICATION_ID, "com.cory.hourcalculator.SplashOrange"),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
            )
            packageManager.setComponentEnabledSetting(
                ComponentName(BuildConfig.APPLICATION_ID, "com.cory.hourcalculator.SplashPink"),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
            )
            packageManager.setComponentEnabledSetting(
                ComponentName(BuildConfig.APPLICATION_ID, "com.cory.hourcalculator.SplashScreenNoIcon"),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
            )
            packageManager.setComponentEnabledSetting(
                ComponentName(BuildConfig.APPLICATION_ID, "com.cory.hourcalculator.SplashRed"),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
            )
        } else if (accentColor.loadAccent() == 1) {
            packageManager.setComponentEnabledSetting(
                ComponentName(BuildConfig.APPLICATION_ID, "com.cory.hourcalculator.SplashOrange"),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
            )
            packageManager.setComponentEnabledSetting(
                ComponentName(BuildConfig.APPLICATION_ID, "com.cory.hourcalculator.SplashPink"),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
            )
            packageManager.setComponentEnabledSetting(
                ComponentName(BuildConfig.APPLICATION_ID, "com.cory.hourcalculator.SplashScreenNoIcon"),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
            )
            packageManager.setComponentEnabledSetting(
                ComponentName(BuildConfig.APPLICATION_ID, "com.cory.hourcalculator.SplashRed"),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
            )
        } else if (accentColor.loadAccent() == 2) {
            packageManager.setComponentEnabledSetting(
                ComponentName(BuildConfig.APPLICATION_ID, "com.cory.hourcalculator.SplashOrange"),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
            )
            packageManager.setComponentEnabledSetting(
                ComponentName(BuildConfig.APPLICATION_ID, "com.cory.hourcalculator.SplashPink"),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
            )
            packageManager.setComponentEnabledSetting(
                ComponentName(BuildConfig.APPLICATION_ID, "com.cory.hourcalculator.SplashScreenNoIcon"),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
            )
            packageManager.setComponentEnabledSetting(
                ComponentName(BuildConfig.APPLICATION_ID, "com.cory.hourcalculator.SplashRed"),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
            )
        } else if (accentColor.loadAccent() == 3) {
            packageManager.setComponentEnabledSetting(
                ComponentName(BuildConfig.APPLICATION_ID, "com.cory.hourcalculator.SplashOrange"),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
            )
            packageManager.setComponentEnabledSetting(
                ComponentName(BuildConfig.APPLICATION_ID, "com.cory.hourcalculator.SplashRed"),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
            )
            packageManager.setComponentEnabledSetting(
                ComponentName(BuildConfig.APPLICATION_ID, "com.cory.hourcalculator.SplashPink"),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
            )
            packageManager.setComponentEnabledSetting(
                ComponentName(BuildConfig.APPLICATION_ID, "com.cory.hourcalculator.SplashScreenNoIcon"),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
            )
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
}