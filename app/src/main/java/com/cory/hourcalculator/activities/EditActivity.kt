package com.cory.hourcalculator.activities

import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cory.hourcalculator.R
import com.cory.hourcalculator.classes.AccentColor
import com.cory.hourcalculator.classes.DarkThemeData
import com.cory.hourcalculator.classes.VibrationData
import com.cory.hourcalculator.database.DBHelper
import com.google.android.gms.ads.*
import kotlinx.android.synthetic.main.edit_activity.*
import java.math.RoundingMode

class EditActivity : AppCompatActivity() {

    private val dbHandler = DBHelper(this, null)
    private val dataList = ArrayList<HashMap<String, String>>()

    private lateinit var darkThemeData: DarkThemeData
    private lateinit var accentColor: AccentColor

    private lateinit var vibrationData: VibrationData

    private lateinit var date: String

    private val testDeviceId = listOf("5E80E48DC2282D372EAE0E3ACDE070CC", "8EE44B7B4B422D333731760574A381FE")

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
        setContentView(R.layout.edit_activity)
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

        main()

        vibrationData = VibrationData(this)

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
        val idMap = map["id"].toString()

        date = map["day"].toString()

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
            calculate(idMap, map["day"].toString())
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
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)

        Toast.makeText(this, getString(R.string.hour_was_not_saved), Toast.LENGTH_SHORT).show()
    }
}
