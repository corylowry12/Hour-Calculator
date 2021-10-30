package com.cory.hourcalculator.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.cory.hourcalculator.R
import com.cory.hourcalculator.classes.AccentColor
import com.cory.hourcalculator.classes.DarkThemeData
import com.cory.hourcalculator.classes.VibrationData
import com.cory.hourcalculator.database.DBHelper
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.edit_activity.*
import java.math.RoundingMode

class EditActivity : AppCompatActivity() {

    private val dbHandler = DBHelper(this, null)
    private val dataList = ArrayList<HashMap<String, String>>()

    private lateinit var darkThemeData: DarkThemeData
    private lateinit var accentColor: AccentColor

    private lateinit var vibrationData: VibrationData

    private lateinit var inTime: String
    private lateinit var outTime: String

    private var inTimeBool = false
    private var outTimeBool = false

    private lateinit var idMap: String
    private lateinit var day: String

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
            accentColor.loadAccent() == 4 -> {
                theme.applyStyle(R.style.system_accent, true)
            }
        }
        setContentView(R.layout.edit_activity)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        MobileAds.initialize(this)
        val adView = AdView(this)
        adView.adSize = AdSize.BANNER
        adView.adUnitId = "ca-app-pub-4546055219731501/5171269817"
        val mAdView = findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        main()

        bottomNav.menu.findItem(R.id.menu_history).isChecked = true

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.menu_home -> {
                    vibration(vibrationData)
                    if (inTimeBool || outTimeBool) {
                        val alert = AlertDialog.Builder(this, accentColor.alertTheme(this))
                        alert.setTitle(getString(R.string.pending_changes))
                        alert.setMessage(getString(R.string.pending_changes_would_you_like_to_save))
                        alert.setPositiveButton(getString(R.string.yes)) { _, _ ->
                            vibration(vibrationData)
                            calculate(idMap, day)
                            Toast.makeText(this, getString(R.string.hour_is_saved), Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                        }
                        alert.setNegativeButton(getString(R.string.no)) { _, _ ->
                            vibration(vibrationData)
                            this.finish()
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                            Toast.makeText(this, getString(R.string.hour_was_not_saved), Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                        }
                        alert.show()
                    }
                    else {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                    }
                    true
                }
                R.id.menu_history -> {
                    vibration(vibrationData)
                    if (inTimeBool || outTimeBool) {
                        val alert = AlertDialog.Builder(this, accentColor.alertTheme(this))
                        alert.setTitle(getString(R.string.pending_changes))
                        alert.setMessage(getString(R.string.pending_changes_would_you_like_to_save))
                        alert.setPositiveButton(getString(R.string.yes)) { _, _ ->
                            vibration(vibrationData)
                            calculate(idMap, day)
                            Toast.makeText(this, getString(R.string.hour_is_saved), Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, HistoryActivity::class.java)
                            startActivity(intent)
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                        }
                        alert.setNegativeButton(getString(R.string.no)) { _, _ ->
                            vibration(vibrationData)
                            this.finish()
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                            Toast.makeText(this, getString(R.string.hour_was_not_saved), Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, HistoryActivity::class.java)
                            startActivity(intent)
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                        }
                        alert.show()
                    }
                    else {
                        val intent = Intent(this, HistoryActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                    }
                    true
                }
                R.id.menu_settings -> {
                    vibration(vibrationData)
                    if (inTimeBool || outTimeBool) {
                        val alert = AlertDialog.Builder(this, accentColor.alertTheme(this))
                        alert.setTitle(getString(R.string.pending_changes))
                        alert.setMessage(getString(R.string.pending_changes_would_you_like_to_save))
                        alert.setPositiveButton(getString(R.string.yes)) { _, _ ->
                            vibration(vibrationData)
                            calculate(idMap, day)
                            Toast.makeText(this, getString(R.string.hour_is_saved), Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, SettingsActivity::class.java)
                            startActivity(intent)
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                        }
                        alert.setNegativeButton(getString(R.string.no)) { _, _ ->
                            vibration(vibrationData)
                            this.finish()
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                            Toast.makeText(this, getString(R.string.hour_was_not_saved), Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, SettingsActivity::class.java)
                            startActivity(intent)
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                        }
                        alert.show()
                    }
                    else {
                        val intent = Intent(this, SettingsActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                    }
                    true
                }
                else -> false
            }
        }

        vibrationData = VibrationData(this)

        timePickerInTime.setOnTimeChangedListener { view, hourOfDay, minute ->
            vibration(vibrationData)
            val inTimeMinutesNumbers: Int

            val (inTimeHours, inTimeMinutes) = inTime.split(":")

            var inTimeHoursInteger: Int = inTimeHours.toInt()

            if (inTime.contains(getString(R.string.pm))) {
                inTimeMinutesNumbers = inTimeMinutes.replace(getString(R.string.pm), "").trim().toInt()
                inTimeHoursInteger += 12
            } else {
                inTimeMinutesNumbers = inTimeMinutes.replace(getString(R.string.am), "").trim().toInt()
                if (inTimeHours.toInt() == 12) {
                    inTimeHoursInteger -= 12
                }
            }

            inTimeBool = inTimeHoursInteger != hourOfDay || inTimeMinutesNumbers != minute
        }
        timePickerOutTime.setOnTimeChangedListener { view, hourOfDay, minute ->
            vibration(vibrationData)
            val outTimeMinutesNumbers: Int
            val (outTimeHours, outTimeMinutes) = outTime.split(":")
            var outTimeHoursInteger: Int = outTimeHours.toInt()

            if (outTime.contains(getString(R.string.pm))) {
                outTimeMinutesNumbers = outTimeMinutes.replace(getString(R.string.pm), "").trim().toInt()
                outTimeHoursInteger += 12
            } else {
                outTimeMinutesNumbers = outTimeMinutes.replace(getString(R.string.am), "").trim().toInt()
                if (outTimeHours.toInt() == 12) {
                    outTimeHoursInteger -= 12
                }
            }
            outTimeBool = outTimeHoursInteger != hourOfDay || outTimeMinutesNumbers != minute
        }
    }

    fun main() {

        val id = intent.getStringExtra("id").toString()

        dataList.clear()
        val cursor = dbHandler.getAllRow(this)
        cursor!!.moveToPosition(id.toInt())

        val map = HashMap<String, String>()
        while (cursor.position == id.toInt()) {

            map["id"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ID))
            map["intime"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_IN))
            map["out"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_OUT))
            map["total"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TOTAL))
            map["day"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DAY))
            dataList.add(map)

            cursor.moveToNext()

        }
        idMap = map["id"].toString()
        inTime = map["intime"].toString()
        outTime = map["out"].toString()
        day = map["day"].toString()

        val (inTimeHours, inTimeMinutes) = map["intime"].toString().split(":")
        val (outTimeHours, outTimeMinutes) = map["out"].toString().split(":")

        var inTimeHoursInteger: Int = inTimeHours.toInt()
        var outTimeHoursInteger: Int = outTimeHours.toInt()

        val inTimeMinutesNumbers: Int
        val outTimeMinutesNumbers: Int

        if (map["intime"].toString().contains(getString(R.string.pm))) {
            inTimeMinutesNumbers = inTimeMinutes.replace(getString(R.string.pm), "").trim().toInt()
            inTimeHoursInteger += 12
        } else {
            inTimeMinutesNumbers = inTimeMinutes.replace(getString(R.string.am), "").trim().toInt()
            if (inTimeHours.toInt() == 12) {
                inTimeHoursInteger -= 12
            }
        }

        if (map["out"].toString().contains(getString(R.string.pm))) {
            outTimeMinutesNumbers = outTimeMinutes.replace(getString(R.string.pm), "").trim().toInt()
            outTimeHoursInteger += 12
        } else {
            outTimeMinutesNumbers = outTimeMinutes.replace(getString(R.string.am), "").trim().toInt()
            if (outTimeHours.toInt() == 12) {
                outTimeHoursInteger -= 12
            }
        }

        timePickerInTime.hour = inTimeHoursInteger
        timePickerInTime.minute = inTimeMinutesNumbers
        timePickerOutTime.hour = outTimeHoursInteger
        timePickerOutTime.minute = outTimeMinutesNumbers

        saveButton.setOnClickListener {
            vibration(vibrationData)
            calculate(idMap, day)
        }
    }

    private fun calculate(id: String, dayOfWeek: String) {
        var inTimeMinutes = timePickerInTime.minute
        val inTimeHours = timePickerInTime.hour
        var outTimeMinutes = timePickerOutTime.minute
        val outTimeHours = timePickerOutTime.hour

        if (inTimeMinutes.toString().length == 1) {
            inTimeMinutes = "0$inTimeMinutes".toInt()
        }

        if (outTimeMinutes.toString().length == 1) {
            outTimeMinutes = "0$outTimeMinutes".toInt()
        }

        val inTimeTotal: String
        val outTimeTotal: String

        var minutesDecimal: Double = (outTimeMinutes - inTimeMinutes) / 60.0
        minutesDecimal = minutesDecimal.toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toDouble()
        var minutesWithoutFirstDecimal = minutesDecimal.toString().substring(2)
        if (minutesDecimal < 0) {
            minutesWithoutFirstDecimal = (1.0 - minutesWithoutFirstDecimal.toDouble()).toString()
            minutesWithoutFirstDecimal = minutesWithoutFirstDecimal.toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toString()
            minutesWithoutFirstDecimal = minutesWithoutFirstDecimal.substring(2)
        }
        var hoursDifference = outTimeHours - inTimeHours
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

            when {
                inTimeHours > 12 -> {
                    val inTime = inTimeHours - 12
                    val amOrPm = getString(R.string.pm)
                    inTimeTotal = "$inTime:$inTimeMinutes $amOrPm"
                }
                inTimeHours == 0 -> {
                    val inTime = inTimeHours + 12
                    val amOrPm = getString(R.string.am)
                    inTimeTotal = "$inTime:$inTimeMinutes $amOrPm"
                }
                else -> {
                    val amOrPm = getString(R.string.am)
                    inTimeTotal = "$inTimeHours:$inTimeMinutes $amOrPm"
                }
            }
            when {
                outTimeHours > 12 -> {
                    val outTime = outTimeHours - 12
                    val amOrPm = getString(R.string.pm)
                    outTimeTotal = "$outTime:$outTimeMinutes $amOrPm"
                }
                outTimeHours == 0 -> {
                    val outTime = outTimeHours + 12
                    val amOrPm = getString(R.string.am)
                    outTimeTotal = "$outTime:$outTimeMinutes $amOrPm"
                }
                else -> {
                    val amOrPm = getString(R.string.am)
                    outTimeTotal = "$outTimeHours:$outTimeMinutes $amOrPm"
                }
            }
            val totalHours = "$hoursDifference.$minutesWithoutFirstDecimal".toDouble()
            savingHours(totalHours, inTimeTotal, outTimeTotal, id, dayOfWeek)
            infoTextView1.text = getString(R.string.total_hours, "$hoursDifference.$minutesWithoutFirstDecimal")
        }
    }

    private fun savingHours(totalHours3: Double, inTime: String, outTime: String, id: String, dayOfWeek: String) {
        dbHandler.update(id, inTime, outTime, totalHours3.toString(), dayOfWeek)
        this.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        Toast.makeText(this, getString(R.string.hour_is_saved), Toast.LENGTH_LONG).show()
    }

    fun vibration(vibrationData: VibrationData) {
        if (vibrationData.loadVibrationState()) {
            val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(5, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    override fun onResume() {
        super.onResume()
        main()
    }

    override fun onRestart() {
        super.onRestart()
        recreate()
    }

    override fun onSupportNavigateUp(): Boolean {
        vibration(vibrationData)
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        if (inTimeBool || outTimeBool) {
            val alert = AlertDialog.Builder(this, accentColor.alertTheme(this))
            alert.setTitle(getString(R.string.pending_changes))
            alert.setMessage(getString(R.string.pending_changes_would_you_like_to_save))
            alert.setPositiveButton(getString(R.string.yes)) { _, _ ->
                vibration(vibrationData)
                calculate(idMap, day)
                Toast.makeText(this, getString(R.string.hour_is_saved), Toast.LENGTH_SHORT).show()
            }
            alert.setNegativeButton(getString(R.string.no)) { _, _ ->
                vibration(vibrationData)
                this.finish()
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                Toast.makeText(this, getString(R.string.hour_was_not_saved), Toast.LENGTH_SHORT).show()
            }
            alert.show()
        } else {
            this.finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }
}
