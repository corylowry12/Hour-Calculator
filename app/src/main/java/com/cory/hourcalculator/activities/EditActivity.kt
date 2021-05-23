package com.cory.hourcalculator.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.isDigitsOnly
import com.cory.hourcalculator.R
import com.cory.hourcalculator.classes.DarkThemeData
import com.cory.hourcalculator.classes.HistoryToggleData
import com.cory.hourcalculator.classes.PerformanceModeData
import com.cory.hourcalculator.classes.VibrationData
import com.cory.hourcalculator.database.DBHelper
import com.google.android.gms.ads.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.jaredrummler.materialspinner.MaterialSpinner
import kotlinx.android.synthetic.main.activity_main.*
import java.math.RoundingMode

class EditActivity : AppCompatActivity() {

    private val dbHandler = DBHelper(this, null)
    private val dataList = ArrayList<HashMap<String, String>>()

    private lateinit var spinner1selecteditem: String
    private lateinit var spinner2selecteditem: String

    private lateinit var darkThemeData: DarkThemeData
    private lateinit var vibrationData: VibrationData

    val testDeviceId = listOf("5E80E48DC2282D372EAE0E3ACDE070CC", "8EE44B7B4B422D333731760574A381FE")

    override fun onCreate(savedInstanceState: Bundle?) {
        darkThemeData = DarkThemeData(this)
        if (darkThemeData.loadDarkModeState()) {
            setTheme(R.style.AMOLED)
        } else {
            setTheme(R.style.AppTheme)
        }
        super.onCreate(savedInstanceState)
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
        mAdView.adListener = object : AdListener() {
        }

        main()

        vibrationData = VibrationData(this)

    }

    fun main() {

        val inTime = findViewById<TextInputEditText>(R.id.inTime)
        val outTime = findViewById<TextInputEditText>(R.id.outTime)
        val breakTime = findViewById<TextInputEditText>(R.id.breakTime)
        val spinner1 = findViewById<MaterialSpinner>(R.id.material_spinner_1)
        val spinner2 = findViewById<MaterialSpinner>(R.id.material_spinner_2)
        val dateEditText = findViewById<TextInputEditText>(R.id.date)

        val id = intent.getStringExtra("id").toString()

        dataList.clear()
        val cursor = dbHandler.getAllRow(this)
        cursor!!.moveToPosition(id.toInt())

        val map = HashMap<String, String>()
        while (cursor.position == id.toInt()) {

            map["id"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ID))
            map["intime"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_IN))
            map["out"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_OUT))
            map["break"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_BREAK))
            map["total"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TOTAL))
            map["day"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DAY))
            dataList.add(map)

            cursor.moveToNext()

        }
        val idMap = map["id"].toString()

        inTime.setText(map["intime"].toString().replace(" ", "").replace("A", "").replace("M", "").replace("P", ""))
        outTime.setText(map["out"].toString().replace(" ", "").replace("A", "").replace("M", "").replace("P", ""))
        breakTime.setText(map["break"].toString())
        if (map["intime"].toString().contains(this.getString(R.string.am))) {
            material_spinner_1.setItems(getString(R.string.am), getString(R.string.pm))
            spinner1selecteditem = getString(R.string.am)
        } else if (map["intime"].toString().contains(this.getString(R.string.pm))) {
            material_spinner_1.setItems(getString(R.string.pm), getString(R.string.am))
            spinner1selecteditem = getString(R.string.pm)
        }
        if (map["out"].toString().contains(getString(R.string.am))) {
            material_spinner_2.setItems(getString(R.string.am), getString(R.string.pm))
            spinner2selecteditem = getString(R.string.am)
        } else if (map["out"].toString().contains(getString(R.string.pm))) {
            material_spinner_2.setItems(getString(R.string.pm), getString(R.string.am))
            spinner2selecteditem = getString(R.string.pm)
        }

        dateEditText.setText(map["day"].toString())

        inTime.setOnClickListener {
            vibration(vibrationData)
        }

        outTime.setOnClickListener {
            vibration(vibrationData)
        }

        breakTime.setOnClickListener {
            vibration(vibrationData)
        }

        spinner1.setOnClickListener {
            vibration(vibrationData)
        }

        spinner2.setOnClickListener {
            vibration(vibrationData)
        }

        spinner1.setOnItemSelectedListener { _, _, _, item ->
            spinner1selecteditem = item as String
        }

        spinner2.setOnItemSelectedListener { _, _, _, item ->
            vibration(vibrationData)
            spinner2selecteditem = item as String
        }

        spinner1.setOnNothingSelectedListener {
            vibration(vibrationData)
        }

        spinner2.setOnNothingSelectedListener {
            vibration(vibrationData)
        }

        val saveButton = findViewById<MaterialButton>(R.id.buttonSave)
        saveButton.setOnClickListener {
            vibration(vibrationData)
            validation(inTime.text.toString(), outTime.text.toString(), spinner1selecteditem, spinner2selecteditem, idMap)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun validation(inTimeString: String, outTimeString: String, spinner1selecteditem: String, spinner2selecteditem: String, id: String) {
        if (inTime.text.toString().contains(",")) {
            Toast.makeText(this, getString(R.string.theres_a_comman_in_text_box), Toast.LENGTH_LONG).show()
        } else if (outTime.text.toString().contains(",")) {
            Toast.makeText(this, getString(R.string.theres_a_comman_in_text_box), Toast.LENGTH_LONG).show()
        }
        if (spinner1selecteditem == getString(R.string.am) && spinner2selecteditem == getString(R.string.am)) {
            if (inTimeString.length == 2 || outTimeString.length == 2) {
                Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
            }
            if (inTime.text.toString() == "" || outTime.text.toString() == "") {
                Toast.makeText(this, getString(R.string.dont_leave_anything_blank), Toast.LENGTH_LONG).show()
            }
            if (!inTimeString.contains(":") && !outTimeString.contains(":")) {
                if (inTimeString.length == 3 && outTimeString.length == 3) {
                    val intime = inTimeString.drop(1)
                    val intimelast = inTimeString.dropLast(2)
                    val outtime = outTimeString.drop(1)
                    val outtimelast = outTimeString.dropLast(2)
                    outTime.setText("$outtimelast:$outtime")
                    inTime.setText("$intimelast:$intime")
                    val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                    val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                    if (inTimeMinutes == "" || outTimeMinutes == "") {
                        Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_60), Toast.LENGTH_LONG).show()
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_13), Toast.LENGTH_LONG).show()
                    } else {
                        aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, breakTime, spinner1selecteditem, spinner2selecteditem, id)
                    }
                } else if (inTimeString.length == 3 && outTimeString.length == 4) {
                    val intime = inTimeString.drop(2)
                    val intimelast = inTimeString.dropLast(1)
                    val outtime = outTimeString.drop(2)
                    val outtimelast = outTimeString.dropLast(2)
                    inTime.setText("$intime:$intimelast")
                    outTime.setText("$outtimelast:$outtime")
                    val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                    val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                    if (inTimeMinutes == "" || outTimeMinutes == "") {
                        Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_60), Toast.LENGTH_LONG).show()
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_13), Toast.LENGTH_LONG).show()
                    } else {
                        aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, breakTime, spinner1selecteditem, spinner2selecteditem, id)
                    }
                } else if (inTimeString.length == 4 && outTimeString.length == 3) {
                    val intime = inTimeString.drop(2)
                    val intimelast = inTimeString.dropLast(2)
                    val outtime = outTimeString.drop(2)
                    val outtimelast = outTimeString.dropLast(1)
                    inTime.setText("$intimelast:$intime")
                    outTime.setText("$outtime:$outtimelast")
                    val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                    val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                    if (inTimeMinutes == "" || outTimeMinutes == "") {
                        Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_60), Toast.LENGTH_LONG).show()
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_13), Toast.LENGTH_LONG).show()
                    } else {
                        val rounded = (inTimeMinutes.toDouble() / 60).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toString()
                        val rounded1 = (outTimeMinutes.toDouble() / 60).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toString()
                        val total1 = inTimeHours.toDouble() + rounded.substring(1).toDouble()
                        val total2 = outTimeHours.toDouble() + rounded1.substring(1).toDouble()
                        val difference = total2 - total1
                        val totalhours = String.format("%.2f", difference).toDouble()
                        if (totalhours < 0) {
                            Toast.makeText(this, getString(R.string.in_time_can_not_be_greater_than_out_time), Toast.LENGTH_LONG).show()
                        } else {
                            aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, breakTime, spinner1selecteditem, spinner2selecteditem, id)
                        }
                    }
                } else if (inTimeString.length == 4 && outTimeString.length == 4) {
                    val intime = inTimeString.drop(2)
                    val intimelast = inTimeString.dropLast(2)
                    val outtime = outTimeString.drop(2)
                    val outtimelast = outTimeString.dropLast(2)
                    inTime.setText("$intimelast:$intime")
                    outTime.setText("$outtimelast:$outtime")
                    val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                    val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                    if (inTimeMinutes == "" || outTimeMinutes == "") {
                        Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_60), Toast.LENGTH_LONG).show()
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_13), Toast.LENGTH_LONG).show()
                    } else {
                        aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, breakTime, spinner1selecteditem, spinner2selecteditem, id)
                    }
                }
            }
            if (!inTimeString.contains(":") && outTimeString.contains(":")) {
                if (inTimeString.length == 3) {
                    val intime = inTimeString.drop(1)
                    val intimelast = inTimeString.dropLast(2)
                    inTime.setText("$intimelast:$intime")
                    val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                    val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                    if (inTimeMinutes == "" || outTimeMinutes == "") {
                        Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_60), Toast.LENGTH_LONG).show()
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_13), Toast.LENGTH_LONG).show()
                    } else {
                        aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, breakTime, spinner1selecteditem, spinner2selecteditem, id)
                    }
                } else if (inTimeString.length == 4) {
                    val intime = inTimeString.drop(2)
                    val intimelast = inTimeString.dropLast(2)
                    inTime.setText("$intimelast:$intime")
                    val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                    val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                    if (inTimeMinutes == "" || outTimeMinutes == "") {
                        Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_13), Toast.LENGTH_LONG).show()
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_60), Toast.LENGTH_LONG).show()
                    } else {
                        aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, breakTime, spinner1selecteditem, spinner2selecteditem, id)
                    }
                }
                else if (inTimeString.length == 5) {
                    Toast.makeText(this, getString(R.string.time_cant_be_five_digits), Toast.LENGTH_LONG).show()
                }
            }
            if (inTimeString.contains(":") && !outTimeString.contains(":")) {
                if (outTimeString.length == 3) {
                    val outtime = outTimeString.drop(1)
                    val outtimelast = outTimeString.dropLast(2)
                    outTime.setText("$outtimelast:$outtime")
                    val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                    val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                    if (inTimeMinutes == "" || outTimeMinutes == "") {
                        Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_60), Toast.LENGTH_LONG).show()
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_13), Toast.LENGTH_LONG).show()
                    } else {
                        aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, breakTime, spinner1selecteditem, spinner2selecteditem, id)
                    }
                } else if (outTimeString.length == 4) {
                    val outtime = outTimeString.drop(2)
                    val outtimelast = outTimeString.dropLast(2)
                    outTime.setText("$outtimelast:$outtime")
                    val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                    val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                    if (inTimeMinutes == "" || outTimeMinutes == "") {
                        Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_60), Toast.LENGTH_LONG).show()
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_13), Toast.LENGTH_LONG).show()
                    } else {
                        aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, breakTime, spinner1selecteditem, spinner2selecteditem, id)
                    }
                }
                else if (outTimeString.length == 5) {
                    Toast.makeText(this, getString(R.string.time_cant_be_five_digits), Toast.LENGTH_LONG).show()
                }
            } else if (inTimeString.contains(":") && outTimeString.contains(":")) {
                val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                if (inTimeMinutes == "" || outTimeMinutes == "") {
                    Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
                } else if (inTimeMinutes.length == 3 || outTimeMinutes.length == 3) {
                    Toast.makeText(this, getString(R.string.minutes_cant_be_three_numbers), Toast.LENGTH_LONG).show()
                } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                    Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_60), Toast.LENGTH_LONG).show()
                } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                    Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_13), Toast.LENGTH_LONG).show()
                } else {
                    aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, breakTime, spinner1selecteditem, spinner2selecteditem, id)
                }
            }
        }
        if (spinner1selecteditem == getString(R.string.pm) && spinner2selecteditem == getString(R.string.pm)) {
            if (inTimeString.length <= 2 || outTimeString.length <= 2) {
                Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
            }
            if (inTime.text.toString() == "" || outTime.text.toString() == "") {
                Toast.makeText(this, getString(R.string.dont_leave_anything_blank), Toast.LENGTH_LONG).show()
            }
            if (!inTimeString.contains(":") && !outTimeString.contains(":")) {
                if (inTimeString.length == 3 && outTimeString.length == 3) {
                    val intime = inTimeString.drop(1)
                    val intimelast = inTimeString.dropLast(2)
                    val outtime = outTimeString.drop(1)
                    val outtimelast = outTimeString.dropLast(2)
                    outTime.setText("$outtimelast:$outtime")
                    inTime.setText("$intimelast:$intime")
                    val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                    val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                    if (inTimeMinutes == "" || outTimeMinutes == "") {
                        Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_60), Toast.LENGTH_LONG).show()
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_13), Toast.LENGTH_LONG).show()
                    } else {
                        aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, breakTime, spinner1selecteditem, spinner2selecteditem, id)
                    }
                } else if (inTimeString.length == 3 && outTimeString.length == 4) {
                    val intime = inTimeString.drop(2)
                    val intimelast = inTimeString.dropLast(1)
                    val outtime = outTimeString.drop(2)
                    val outtimelast = outTimeString.dropLast(2)
                    inTime.setText("$intime:$intimelast")
                    outTime.setText("$outtimelast:$outtime")
                    val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                    val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                    if (inTimeMinutes == "" || outTimeMinutes == "") {
                        Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_60), Toast.LENGTH_LONG).show()
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_13), Toast.LENGTH_LONG).show()
                    } else {
                        aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, breakTime, spinner1selecteditem, spinner2selecteditem, id)
                    }
                } else if (inTimeString.length == 4 && outTimeString.length == 3) {
                    val intime = inTimeString.drop(2)
                    val intimelast = inTimeString.dropLast(2)
                    val outtime = outTimeString.drop(2)
                    val outtimelast = outTimeString.dropLast(1)
                    inTime.setText("$intimelast:$intime")
                    outTime.setText("$outtime:$outtimelast")
                    val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                    val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                    if (inTimeMinutes == "" || outTimeMinutes == "") {
                        Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_60), Toast.LENGTH_LONG).show()
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_13), Toast.LENGTH_LONG).show()
                    } else {
                        aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, breakTime, spinner1selecteditem, spinner2selecteditem, id)
                    }
                } else if (inTimeString.length == 4 && outTimeString.length == 4) {
                    val intime = inTimeString.drop(2)
                    val intimelast = inTimeString.dropLast(2)
                    val outtime = outTimeString.drop(2)
                    val outtimelast = outTimeString.dropLast(2)
                    inTime.setText("$intimelast:$intime")
                    outTime.setText("$outtimelast:$outtime")
                    val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                    val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                    if (inTimeMinutes == "" || outTimeMinutes == "") {
                        Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_60), Toast.LENGTH_LONG).show()
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_13), Toast.LENGTH_LONG).show()
                    } else {
                        aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, breakTime, spinner1selecteditem, spinner2selecteditem, id)
                    }
                }
            }
            if (!inTimeString.contains(":") && outTimeString.contains(":")) {
                if (inTimeString.length == 3) {
                    val intime = inTimeString.drop(1)
                    val intimelast = inTimeString.dropLast(2)
                    inTime.setText("$intimelast:$intime")
                    val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                    val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                    if (inTimeMinutes == "" || outTimeMinutes == "") {
                        Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_60), Toast.LENGTH_LONG).show()
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_13), Toast.LENGTH_LONG).show()
                    } else {
                        aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, breakTime, spinner1selecteditem, spinner2selecteditem, id)
                    }
                } else if (inTimeString.length == 4) {
                    val intime = inTimeString.drop(2)
                    val intimelast = inTimeString.dropLast(2)
                    inTime.setText("$intimelast:$intime")
                    val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                    val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                    if (inTimeMinutes == "" || outTimeMinutes == "") {
                        Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_13), Toast.LENGTH_LONG).show()
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_60), Toast.LENGTH_LONG).show()
                    } else {
                        aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, breakTime, spinner1selecteditem, spinner2selecteditem, id)
                    }
                }
                else if (inTimeString.length == 5) {
                    Toast.makeText(this, getString(R.string.time_cant_be_five_digits), Toast.LENGTH_LONG).show()
                }
            }
            if (inTimeString.contains(":") && !outTimeString.contains(":")) {
                if (outTimeString.length == 3) {
                    val outtime = outTimeString.drop(1)
                    val outtimelast = outTimeString.dropLast(2)
                    outTime.setText("$outtimelast:$outtime")
                    val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                    val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                    if (inTimeMinutes == "" || outTimeMinutes == "") {
                        Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_60), Toast.LENGTH_LONG).show()
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_13), Toast.LENGTH_LONG).show()
                    } else {
                        aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, breakTime, spinner1selecteditem, spinner2selecteditem, id)
                    }
                } else if (outTimeString.length == 4) {
                    val outtime = outTimeString.drop(2)
                    val outtimelast = outTimeString.dropLast(2)
                    outTime.setText("$outtimelast:$outtime")
                    val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                    val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                    if (inTimeMinutes == "" || outTimeMinutes == "") {
                        Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_60), Toast.LENGTH_LONG).show()
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_13), Toast.LENGTH_LONG).show()
                    } else {
                        aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, breakTime, spinner1selecteditem, spinner2selecteditem, id)
                    }
                }
                else if (outTimeString.length == 5) {
                    Toast.makeText(this, getString(R.string.time_cant_be_five_digits), Toast.LENGTH_LONG).show()
                }
            } else if (inTimeString.contains(":") && outTimeString.contains(":")) {
                val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                if (inTimeMinutes == "" || outTimeMinutes == "") {
                    Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
                } else if (inTimeMinutes.length == 3 || outTimeMinutes.length == 3) {
                    Toast.makeText(this, getString(R.string.minutes_cant_be_three_numbers), Toast.LENGTH_LONG).show()
                } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                    Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_60), Toast.LENGTH_LONG).show()
                } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                    Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_13), Toast.LENGTH_LONG).show()
                } else {
                    aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, breakTime, spinner1selecteditem, spinner2selecteditem, id)
                }
            }
        }
        if (spinner1selecteditem == getString(R.string.am) && spinner2selecteditem == getString(R.string.pm)) {
            if (inTimeString.length == 2 || outTimeString.length == 2) {
                Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
            }
            if (inTimeString == "" || outTimeString == "") {
                Toast.makeText(this, getString(R.string.dont_leave_anything_blank), Toast.LENGTH_LONG).show()
            }
            if (!inTimeString.contains(":") && !outTimeString.contains(":")) {
                if (inTimeString.length == 3 && outTimeString.length == 3) {
                    val intime = inTimeString.drop(1)
                    val intimelast = inTimeString.dropLast(2)
                    val outtime = outTimeString.drop(1)
                    val outtimelast = outTimeString.dropLast(2)
                    outTime.setText("$outtimelast:$outtime")
                    inTime.setText("$intimelast:$intime")
                    val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                    val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                    if (inTimeMinutes == "" || inTimeMinutes == "") {
                        Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_13), Toast.LENGTH_LONG).show()
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_60), Toast.LENGTH_LONG).show()
                    } else {
                        aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, breakTime, spinner1selecteditem, spinner2selecteditem, id)
                    }
                } else if (inTimeString.length == 3 && outTimeString.length == 4) {
                    val intime = inTimeString.drop(2)
                    val intimelast = inTimeString.dropLast(1)
                    val outtime = outTimeString.drop(2)
                    val outtimelast = outTimeString.dropLast(2)
                    inTime.setText("$intime:$intimelast")
                    outTime.setText("$outtimelast:$outtime")
                    val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                    val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                    if (inTimeMinutes == "" || outTimeMinutes == "") {
                        Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_13), Toast.LENGTH_LONG).show()
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_60), Toast.LENGTH_LONG).show()
                    } else {
                        aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, breakTime, spinner1selecteditem, spinner2selecteditem, id)
                    }
                } else if (inTimeString.length == 4 && outTimeString.length == 3) {
                    val intime = inTimeString.drop(2)
                    val intimelast = inTimeString.dropLast(2)
                    val outtime = outTimeString.drop(2)
                    val outtimelast = outTimeString.dropLast(1)
                    inTime.setText("$intimelast:$intime")
                    outTime.setText("$outtime:$outtimelast")
                    val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                    val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                    if (inTimeMinutes == "" || outTimeMinutes == "") {
                        Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_60), Toast.LENGTH_LONG).show()
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_13), Toast.LENGTH_LONG).show()
                    } else {
                        aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, breakTime, spinner1selecteditem, spinner2selecteditem, id)
                    }
                } else if (inTimeString.length == 4 && outTimeString.length == 4) {
                    val intime = inTimeString.drop(2)
                    val intimelast = inTimeString.dropLast(2)
                    val outtime = outTimeString.drop(2)
                    val outtimelast = outTimeString.dropLast(2)
                    inTime.setText("$intimelast:$intime")
                    outTime.setText("$outtimelast:$outtime")
                    val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                    val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                    if (inTimeMinutes == "" || outTimeMinutes == "") {
                        Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_60), Toast.LENGTH_LONG).show()
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_13), Toast.LENGTH_LONG).show()
                    } else {
                        aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, breakTime, spinner1selecteditem, spinner2selecteditem, id)
                    }
                }
            }
            if (!inTimeString.contains(":") && outTimeString.contains(":")) {
                if (inTimeString.length == 3) {
                    val intime = inTimeString.drop(1)
                    val intimelast = inTimeString.dropLast(2)
                    inTime.setText("$intimelast:$intime")
                    val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                    val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                    if (inTimeMinutes == "" || outTimeMinutes == "") {
                        Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_60), Toast.LENGTH_LONG).show()
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_13), Toast.LENGTH_LONG).show()
                    } else {
                        aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, breakTime, spinner1selecteditem, spinner2selecteditem, id)
                    }
                } else if (inTimeString.length == 4) {
                    val intime = inTimeString.drop(2)
                    val intimelast = inTimeString.dropLast(2)
                    inTime.setText("$intimelast:$intime")
                    val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                    val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                    if (inTimeMinutes == "" || outTimeMinutes == "") {
                        Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_60), Toast.LENGTH_LONG).show()
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_13), Toast.LENGTH_LONG).show()
                    } else {
                        aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, breakTime, spinner1selecteditem, spinner2selecteditem, id)
                    }
                }
                else if (inTimeString.length == 5) {
                    Toast.makeText(this, getString(R.string.time_cant_be_five_digits), Toast.LENGTH_LONG).show()
                }
            }
            if (inTimeString.contains(":") && !outTimeString.contains(":")) {
                if (outTimeString.length == 3) {
                    val outtime = outTimeString.drop(1)
                    val outtimelast = outTimeString.dropLast(2)
                    outTime.setText("$outtimelast:$outtime")
                    val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                    val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                    if (inTimeMinutes == "" || outTimeMinutes == "") {
                        Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_60), Toast.LENGTH_LONG).show()
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_13), Toast.LENGTH_LONG).show()
                    } else {
                        aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, breakTime, spinner1selecteditem, spinner2selecteditem, id)
                    }
                } else if (outTimeString.length == 4) {
                    val outtime = outTimeString.drop(2)
                    val outtimelast = outTimeString.dropLast(2)
                    outTime.setText("$outtimelast:$outtime")
                    val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                    val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                    if (inTimeMinutes == "" || outTimeMinutes == "") {
                        Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_60), Toast.LENGTH_LONG).show()
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_13), Toast.LENGTH_LONG).show()
                    } else {
                        aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, breakTime, spinner1selecteditem, spinner2selecteditem, id)
                    }
                }
                else if (outTimeString.length == 5) {
                    Toast.makeText(this, getString(R.string.time_cant_be_five_digits), Toast.LENGTH_LONG).show()
                }
            } else if (inTimeString.contains(":") && outTimeString.contains(":")) {
                val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                if (inTimeMinutes == "" || outTimeMinutes == "") {
                    Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
                } else if (inTimeMinutes.length == 3 || outTimeMinutes.length == 3) {
                    Toast.makeText(this, getString(R.string.minutes_cant_be_three_numbers), Toast.LENGTH_LONG).show()
                } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                    Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_60), Toast.LENGTH_LONG).show()
                } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                    Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_13), Toast.LENGTH_LONG).show()
                } else {
                    aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, breakTime, spinner1selecteditem, spinner2selecteditem, id)
                }
            }
        }
        if (spinner1selecteditem == getString(R.string.pm) && spinner2selecteditem == getString(R.string.am)) {
            if (inTimeString.length == 2 || outTimeString.length == 2) {
                Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
            }
            if (inTimeString == "" || outTimeString == "") {
                Toast.makeText(this, getString(R.string.dont_leave_anything_blank), Toast.LENGTH_LONG).show()
            }
            if (!inTimeString.contains(":") && !outTimeString.contains(":")) {
                if (inTimeString.length == 3 && outTimeString.length == 3) {
                    val intime = inTimeString.drop(1)
                    val intimelast = inTimeString.dropLast(2)
                    val outtime = outTimeString.drop(1)
                    val outtimelast = outTimeString.dropLast(2)
                    outTime.setText("$outtimelast:$outtime")
                    inTime.setText("$intimelast:$intime")
                    val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                    val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                    if (inTimeMinutes == "" || outTimeMinutes == "") {
                        Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_13), Toast.LENGTH_LONG).show()
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_60), Toast.LENGTH_LONG).show()
                    } else {
                        aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, breakTime, spinner1selecteditem, spinner2selecteditem, id)
                    }
                } else if (inTimeString.length == 3 && outTimeString.length == 4) {
                    val intime = inTimeString.drop(2)
                    val intimelast = inTimeString.dropLast(1)
                    val outtime = outTimeString.drop(2)
                    val outtimelast = outTimeString.dropLast(2)
                    inTime.setText("$intime:$intimelast")
                    outTime.setText("$outtimelast:$outtime")
                    val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                    val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                    if (inTimeMinutes == "" || outTimeMinutes == "") {
                        Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_13), Toast.LENGTH_LONG).show()
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_60), Toast.LENGTH_LONG).show()
                    } else {
                        aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, breakTime, spinner1selecteditem, spinner2selecteditem, id)
                    }
                } else if (inTimeString.length == 4 && outTimeString.length == 3) {
                    val intime = inTimeString.drop(2)
                    val intimelast = inTimeString.dropLast(2)
                    val outtime = outTimeString.drop(2)
                    val outtimelast = outTimeString.dropLast(1)
                    inTime.setText("$intimelast:$intime")
                    outTime.setText("$outtime:$outtimelast")
                    val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                    val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                    if (inTimeMinutes == "" || outTimeMinutes == "") {
                        Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_60), Toast.LENGTH_LONG).show()
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_13), Toast.LENGTH_LONG).show()
                    } else {
                        aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, breakTime, spinner1selecteditem, spinner2selecteditem, id)
                    }
                } else if (inTimeString.length == 4 && outTimeString.length == 4) {
                    val intime = inTimeString.drop(2)
                    val intimelast = inTimeString.dropLast(2)
                    val outtime = outTimeString.drop(2)
                    val outtimelast = outTimeString.dropLast(2)
                    inTime.setText("$intimelast:$intime")
                    outTime.setText("$outtimelast:$outtime")
                    val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                    val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                    if (inTimeMinutes == "" || outTimeMinutes == "") {
                        Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_60), Toast.LENGTH_LONG).show()
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_13), Toast.LENGTH_LONG).show()
                    } else {
                        aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, breakTime, spinner1selecteditem, spinner2selecteditem, id)
                    }
                }
            }
            if (!inTimeString.contains(":") && outTimeString.contains(":")) {
                if (inTimeString.length == 3) {
                    val intime = inTimeString.drop(1)
                    val intimelast = inTimeString.dropLast(2)
                    inTime.setText("$intimelast:$intime")
                    val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                    val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                    if (inTimeMinutes == "" || outTimeMinutes == "") {
                        Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_60), Toast.LENGTH_LONG).show()
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_13), Toast.LENGTH_LONG).show()
                    } else {
                        aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, breakTime, spinner1selecteditem, spinner2selecteditem, id)
                    }
                } else if (inTimeString.length == 4) {
                    val intime = inTimeString.drop(2)
                    val intimelast = inTimeString.dropLast(2)
                    inTime.setText("$intimelast:$intime")
                    val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                    val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                    if (inTimeMinutes == "" || outTimeMinutes == "") {
                        Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_60), Toast.LENGTH_LONG).show()
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_13), Toast.LENGTH_LONG).show()
                    } else {
                        aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, breakTime, spinner1selecteditem, spinner2selecteditem, id)
                    }
                }
                else if (inTimeString.length == 5) {
                    Toast.makeText(this, getString(R.string.time_cant_be_five_digits), Toast.LENGTH_LONG).show()
                }
            }
            if (inTimeString.contains(":") && !outTimeString.contains(":")) {
                if (outTimeString.length == 3) {
                    val outtime = outTimeString.drop(1)
                    val outtimelast = outTimeString.dropLast(2)
                    outTime.setText("$outtimelast:$outtime")
                    val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                    val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                    if (inTimeMinutes == "" || outTimeMinutes == "") {
                        Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_60), Toast.LENGTH_LONG).show()
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_13), Toast.LENGTH_LONG).show()
                    } else {
                        aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, breakTime, spinner1selecteditem, spinner2selecteditem, id)
                    }
                } else if (outTimeString.length == 4) {
                    val outtime = outTimeString.drop(2)
                    val outtimelast = outTimeString.dropLast(2)
                    outTime.setText("$outtimelast:$outtime")
                    val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                    val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                    if (inTimeMinutes == "" || outTimeMinutes == "") {
                        Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_60), Toast.LENGTH_LONG).show()
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_13), Toast.LENGTH_LONG).show()
                    } else {
                        aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, breakTime, spinner1selecteditem, spinner2selecteditem, id)
                    }
                }
                else if (outTimeString.length == 5) {
                    Toast.makeText(this, getString(R.string.time_cant_be_five_digits), Toast.LENGTH_LONG).show()
                }
            } else if (inTimeString.contains(":") && outTimeString.contains(":")) {
                val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                if (inTimeMinutes == "" || outTimeMinutes == "") {
                    Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
                } else if (inTimeMinutes.length == 3 || outTimeMinutes.length == 3) {
                    Toast.makeText(this, getString(R.string.minutes_cant_be_three_numbers), Toast.LENGTH_LONG).show()
                } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                    Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_60), Toast.LENGTH_LONG).show()
                } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                    Toast.makeText(this, getString(R.string.cant_be_greater_than_or_equal_to_13), Toast.LENGTH_LONG).show()
                } else {
                    aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, breakTime, spinner1selecteditem, spinner2selecteditem, id)
                }
            }
        }
    }

    private fun aMandAMandPMandPM(inTimeHours: String, inTimeMinutes: String, outTimeHours: String, outTimeMinutes: String, breakTime: EditText, spinner1selecteditem: String, spinner2selecteditem: String, id: String) {
        val historyToggleData = HistoryToggleData(this)
        val inTimeMinutesRounded = (inTimeMinutes.toDouble() / 60).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toString()
        val outTimeMinutesRounded = (outTimeMinutes.toDouble() / 60).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toString()
        val inTimeTotal = inTimeHours.toDouble() + inTimeMinutesRounded.substring(1).toDouble()
        val outTimeTotal = outTimeHours.toDouble() + outTimeMinutesRounded.substring(1).toDouble()
        val difference = outTimeTotal - inTimeTotal
        val totalhours = String.format("%.2f", difference).toDouble()
        if (totalhours < 0) {
            Toast.makeText(this, getString(R.string.in_time_can_not_be_greater_than_out_time), Toast.LENGTH_LONG).show()
        } else {
            if (breakTime.text.toString() == "") {
                Toast.makeText(this, getString(R.string.total_hours, totalhours.toString()), Toast.LENGTH_LONG).show()
                if (historyToggleData.loadHistoryState()) {
                    savingHours(totalhours, inTime, outTime, breakTime, spinner1selecteditem, spinner2selecteditem, id)
                }
            } else if (breakTime.text.toString() != "") {
                if (!breakTime.text.isDigitsOnly()) {
                    Toast.makeText(this, getString(R.string.something_wrong_with_break_text_box), Toast.LENGTH_LONG).show()
                } else {
                    val breakTimeDec: Double = (breakTime.text.toString().toDouble() / 60).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toString().toDouble()
                    val totalHours1 = totalhours - breakTimeDec
                    val totalHoursWithBreak = String.format("%.2f", totalHours1).toDouble()
                    if (totalHoursWithBreak < 0) {
                        Toast.makeText(this, getString(R.string.in_time_can_not_be_greater_than_out_time), Toast.LENGTH_LONG).show()
                    } else if (totalHoursWithBreak > 0) {
                        Toast.makeText(this, getString(R.string.total_hours_with_and_without_break, totalHoursWithBreak.toString(), totalhours.toString()), Toast.LENGTH_LONG).show()
                        if (historyToggleData.loadHistoryState()) {
                            savingHours(totalHoursWithBreak, inTime, outTime, breakTime, spinner1selecteditem, spinner2selecteditem, id)
                        }
                    }
                }
            }
        }
    }

    private fun aMandPMandPMandAM(inTimeHours: String, inTimeMinutes: String, outTimeHours: String, outTimeMinutes: String, breakTime: EditText, spinner1selecteditem: String, spinner2selecteditem: String, id: String) {
        val historyToggleData = HistoryToggleData(this)
        val inTimeMinutesRounded = (inTimeMinutes.toDouble() / 60).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toString()
        val outTimeMinutesRounded = (outTimeMinutes.toDouble() / 60).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toString()
        val inTimeTotal = inTimeHours.toDouble() + inTimeMinutesRounded.substring(1).toDouble()
        val outTimeTotal = outTimeHours.toDouble() + outTimeMinutesRounded.substring(1).toDouble()
        val difference : Double = outTimeTotal - inTimeTotal
        val totalhours : Double = if(outTimeHours.toInt() == 12) {
            String.format("%.2f", difference).toDouble()
        } else {
            String.format("%.2f", difference).toDouble() + 12
        }
        if (totalhours < 0) {
            Toast.makeText(this, getString(R.string.in_time_can_not_be_greater_than_out_time), Toast.LENGTH_LONG).show()
        } else {
            if (breakTime.text.toString() == "") {
                Toast.makeText(this, getString(R.string.total_hours, totalhours.toString()), Toast.LENGTH_LONG).show()
                if (historyToggleData.loadHistoryState()) {
                    savingHours(totalhours, inTime, outTime, breakTime, spinner1selecteditem, spinner2selecteditem, id)
                }
            } else if (breakTime.text.toString() != "") {
                if (!breakTime.text.toString().isDigitsOnly()) {
                    Toast.makeText(this, getString(R.string.something_wrong_with_break_text_box), Toast.LENGTH_LONG).show()
                } else {
                    val breakTimeDec: Double = (breakTime.text.toString().toDouble() / 60).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toString().toDouble()
                    val totalHours1 = totalhours - breakTimeDec
                    val totalHoursWithBreak = String.format("%.2f", totalHours1).toDouble()
                    if (totalHours1 < 0) {
                        Toast.makeText(this, getString(R.string.in_time_can_not_be_greater_than_out_time), Toast.LENGTH_LONG).show()
                    } else if (totalHours1 > 0) {
                        Toast.makeText(this, getString(R.string.total_hours_with_and_without_break, totalHoursWithBreak.toString(), totalhours.toString()), Toast.LENGTH_LONG).show()
                        if (historyToggleData.loadHistoryState()) {
                            savingHours(totalHoursWithBreak, inTime, outTime, breakTime, spinner1selecteditem, spinner2selecteditem, id)
                        }
                    }
                }
            }
        }
    }

    private fun savingHours(totalHours3: Double, inTime: EditText, outTime: EditText, breakTime: EditText, spinner1selecteditem: String, spinner2selecteditem: String, id: String) {
        var break1 = breakTime.text.toString()
        if (breakTime.text.toString() == "") {
            break1 = getString(R.string.break_zero)
        }
        dbHandler.update(id, inTime.text.toString() + " " + spinner1selecteditem, outTime.text.toString() + " " + spinner2selecteditem, break1, totalHours3.toString(), findViewById<TextInputEditText>(R.id.date).text.toString())
        this.finish()
        if(!PerformanceModeData(this).loadPerformanceMode()) {
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
        else {
            overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
        }
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
        if(!PerformanceModeData(this).loadPerformanceMode()) {
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
        else {
            overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
        }
        Toast.makeText(this, getString(R.string.hour_was_not_saved), Toast.LENGTH_SHORT).show()
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu_edit, menu)
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
            R.id.home -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                if(!PerformanceModeData(this).loadPerformanceMode()) {
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                }
                else {
                    overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
                }
                Toast.makeText(this, getString(R.string.hour_was_not_saved), Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, getString(R.string.hour_was_not_saved), Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, getString(R.string.hour_was_not_saved), Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, getString(R.string.hour_was_not_saved), Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, getString(R.string.hour_was_not_saved), Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, getString(R.string.hour_was_not_saved), Toast.LENGTH_SHORT).show()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
