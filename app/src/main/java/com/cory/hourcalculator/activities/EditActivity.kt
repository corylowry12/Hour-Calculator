package com.cory.hourcalculator.activities

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.isDigitsOnly
import com.cory.hourcalculator.R
import com.cory.hourcalculator.classes.HistoryToggleData
import com.cory.hourcalculator.database.DBHelper
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.jaredrummler.materialspinner.MaterialSpinner
import kotlinx.android.synthetic.main.activity_main.*
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class EditActivity : AppCompatActivity() {

    private val dbHandler = DBHelper(this, null)
    private val dataList = ArrayList<HashMap<String, String>>()

    //private var id : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_activity)

        main()

    }

    fun main() {

        val inTime = findViewById<TextInputEditText>(R.id.inTime)
        val outTime = findViewById<TextInputEditText>(R.id.outTime)
        val breakTime = findViewById<TextInputEditText>(R.id.breakTime)
        val spinner1 = findViewById<MaterialSpinner>(R.id.material_spinner_1)
        val spinner2 = findViewById<MaterialSpinner>(R.id.material_spinner_2)
        val dateEditText = findViewById<TextInputEditText>(R.id.date)

        val id = intent.getStringExtra("id").toString()
        //Toast.makeText(this, id.toString(), Toast.LENGTH_SHORT).show()

        dataList.clear()
        val cursor = dbHandler.getAllRow(this)
        cursor!!.moveToPosition(id.toString().toInt())

        val map = HashMap<String, String>()
        while (cursor.position == id.toString().toInt()) {

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
        //Toast.makeText(this, id, Toast.LENGTH_LONG).show()

        inTime.setText(map["intime"].toString().replace(" ", "").replace("A", "").replace("M", "").replace("P", ""))
        outTime.setText(map["out"].toString().replace(" ", "").replace("A", "").replace("M", "").replace("P", ""))
        breakTime.setText(map["break"].toString())
        if (map["intime"].toString().contains(getString(R.string.am))) {
            spinner1.setItems(getString(R.string.am), getString(R.string.pm))
        } else if (map["intime"].toString().contains(getString(R.string.pm))) {
            spinner1.setItems(getString(R.string.pm), getString(R.string.am))
        }
        if (map["out"].toString().contains(getString(R.string.am))) {
            spinner2.setItems(getString(R.string.am), getString(R.string.pm))
        } else if (map["out"].toString().contains(getString(R.string.pm))) {
            spinner2.setItems(getString(R.string.pm), getString(R.string.am))
        }

        dateEditText.setText(map["day"].toString())

        var spinner1selecteditem: String = getString(R.string.am)

        spinner1.setItems(getString(R.string.pm), getString(R.string.am))
        var spinner2selecteditem: String = getString(R.string.pm)

        spinner1.setOnClickListener {
            //vibration(vibrationData)
        }

        spinner2.setOnClickListener {
            //vibration(vibrationData)
        }

        spinner1.setOnItemSelectedListener { _, _, _, item ->
            spinner1selecteditem = item as String
        }

        spinner2.setOnItemSelectedListener { _, _, _, item ->
            //vibration(vibrationData)
            spinner2selecteditem = item as String
        }

        spinner1.setOnNothingSelectedListener {
            //vibration(vibrationData)
        }

        spinner2.setOnNothingSelectedListener {
            //vibration(vibrationData)
        }

        val fab = findViewById<ExtendedFloatingActionButton>(R.id.extendedFloatingActionButton)
        fab.setOnClickListener {
            validation(inTime.text.toString(), outTime.text.toString(), spinner1selecteditem, spinner2selecteditem, idMap)
        }
    }

    fun validation(inTimeString: String, outTimeString: String, spinner1selecteditem: String, spinner2selecteditem: String, id: String) {
        if (inTime.text.toString().contains(",")) {
            Toast.makeText(this, getString(R.string.theres_a_comman_in_text_box), Toast.LENGTH_LONG).show()
        } else if (outTime.text.toString().contains(",")) {
            Toast.makeText(this, getString(R.string.theres_a_comman_in_text_box), Toast.LENGTH_LONG).show()
        }
        if (spinner1selecteditem == getString(R.string.am) && spinner2selecteditem == getString(R.string.am)) {
            if (inTimeString.length == 2 || outTimeString.length == 2) {
                Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
            }
            if (inTime.text.toString() == "" || outTime.text.toString() == "" || !inTime.hasFocus() || !outTime.hasFocus()) {
                Toast.makeText(this, "Dont leave anything blank", Toast.LENGTH_LONG).show()
            }
            if (!inTimeString.contains(":") && !outTimeString.contains(":")) {
                if (inTimeString.length == 3 && outTimeString.length == 3) {
                    //Toast.makeText(this, getString(R.string.no_colon), Toast.LENGTH_LONG).show()
                    Toast.makeText(this, getString(R.string.no_colon), Toast.LENGTH_LONG).show()
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
                    Toast.makeText(this, getString(R.string.no_colon), Toast.LENGTH_LONG).show()
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
                    Toast.makeText(this, getString(R.string.no_colon), Toast.LENGTH_LONG).show()
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
                    Toast.makeText(this, getString(R.string.no_colon), Toast.LENGTH_LONG).show()
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
                    Toast.makeText(this, getString(R.string.no_colon), Toast.LENGTH_LONG).show()
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
                    Toast.makeText(this, getString(R.string.no_colon), Toast.LENGTH_LONG).show()
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
            }
            if (inTimeString.contains(":") && !outTimeString.contains(":")) {
                if (outTimeString.length == 3) {
                    Toast.makeText(this, getString(R.string.no_colon), Toast.LENGTH_LONG).show()
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
                    Toast.makeText(this, getString(R.string.no_colon), Toast.LENGTH_LONG).show()
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
            if (inTime.text.toString() == "" || outTime.text.toString() == "" || !inTime.hasFocus() || !outTime.hasFocus()) {
                Toast.makeText(this, getString(R.string.dont_leave_anything_blank), Toast.LENGTH_LONG).show()
            }
            if (!inTimeString.contains(":") && !outTimeString.contains(":")) {
                if (inTimeString.length == 3 && outTimeString.length == 3) {
                    Toast.makeText(this, getString(R.string.no_colon), Toast.LENGTH_LONG).show()
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
                    Toast.makeText(this, getString(R.string.no_colon), Toast.LENGTH_LONG).show()
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
                    Toast.makeText(this, getString(R.string.no_colon), Toast.LENGTH_LONG).show()
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
                    Toast.makeText(this, getString(R.string.no_colon), Toast.LENGTH_LONG).show()
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
                    Toast.makeText(this, getString(R.string.no_colon), Toast.LENGTH_LONG).show()
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
                    Toast.makeText(this, getString(R.string.no_colon), Toast.LENGTH_LONG).show()
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
            }
            if (inTimeString.contains(":") && !outTimeString.contains(":")) {
                if (outTimeString.length == 3) {
                    Toast.makeText(this, getString(R.string.no_colon), Toast.LENGTH_LONG).show()
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
                    Toast.makeText(this, getString(R.string.no_colon), Toast.LENGTH_LONG).show()
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
                    Toast.makeText(this, getString(R.string.no_colon), Toast.LENGTH_LONG).show()
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
                    Toast.makeText(this, getString(R.string.no_colon), Toast.LENGTH_LONG).show()
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
                    Toast.makeText(this, getString(R.string.no_colon), Toast.LENGTH_LONG).show()
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
                    Toast.makeText(this, getString(R.string.no_colon), Toast.LENGTH_LONG).show()
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
                    Toast.makeText(this, getString(R.string.no_colon), Toast.LENGTH_LONG).show()
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
                    Toast.makeText(this, getString(R.string.no_colon), Toast.LENGTH_LONG).show()
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
            }
            if (inTimeString.contains(":") && !outTimeString.contains(":")) {
                if (outTimeString.length == 3) {
                    Toast.makeText(this, getString(R.string.no_colon), Toast.LENGTH_LONG).show()
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
                    Toast.makeText(this, getString(R.string.no_colon), Toast.LENGTH_LONG).show()
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
                    Toast.makeText(this, getString(R.string.no_colon), Toast.LENGTH_LONG).show()
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
                    Toast.makeText(this, getString(R.string.no_colon), Toast.LENGTH_LONG).show()
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
                    Toast.makeText(this, getString(R.string.no_colon), Toast.LENGTH_LONG).show()
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
                    Toast.makeText(this, getString(R.string.no_colon), Toast.LENGTH_LONG).show()
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
                    Toast.makeText(this, getString(R.string.no_colon), Toast.LENGTH_LONG).show()
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
                    Toast.makeText(this, getString(R.string.no_colon), Toast.LENGTH_LONG).show()
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
            }
            if (inTimeString.contains(":") && !outTimeString.contains(":")) {
                if (outTimeString.length == 3) {
                    Toast.makeText(this, getString(R.string.no_colon), Toast.LENGTH_LONG).show()
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
                    Toast.makeText(this, getString(R.string.no_colon), Toast.LENGTH_LONG).show()
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
            } else if (inTimeString.contains(":") && outTimeString.contains(":")) {
                val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                if (inTimeMinutes == "" || outTimeMinutes == "") {
                    //Toast.makeText(this, getString(R.string.proper_input), Toast.LENGTH_LONG).show()
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
                            savingHours2(totalHoursWithBreak, inTime, outTime, breakTime, spinner1selecteditem, spinner2selecteditem, id)
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
        val difference: Double = outTimeTotal - inTimeTotal
        val totalhours = String.format("%.2f", difference).toDouble() + 12
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
                        //Toast.makeText(this, getString(R.string.total_no_hours_with_and_without_break, totalHoursWithBreak.toString(), totalhours.toString()), Toast.LENGTH_LONG).show()
                        Toast.makeText(this, getString(R.string.total_hours_with_and_without_break, totalHoursWithBreak.toString(), totalhours.toString()), Toast.LENGTH_LONG).show()
                        if (historyToggleData.loadHistoryState()) {
                            savingHours2(totalHoursWithBreak, inTime, outTime, breakTime, spinner1selecteditem, spinner2selecteditem, id)
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
        val total = totalHours3.toString()
        val day = LocalDateTime.now()
        val day2 = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        val dayOfWeek = day.format(day2)
        //dbHandler.insertRow(inTime.text.toString() + " " + spinner1selecteditem, outTime.text.toString() + " " + spinner2selecteditem, break1, total, dayOfWeek)
        dbHandler.update(id, inTime.text.toString() + " " + spinner1selecteditem, outTime.text.toString() + " " + spinner2selecteditem, break1, total, dayOfWeek)
        //this.finish()
        //overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        Toast.makeText(this, id, Toast.LENGTH_LONG).show()
    }

    private fun savingHours2(totalHours2: Double, inTime: EditText, outTime: EditText, breakTime: EditText, spinner1selecteditem: String, spinner2selecteditem: String, id: String) {
        var break1 = breakTime.text.toString()
        if (breakTime.text.toString() == "") {
            break1 = getString(R.string.break_zero)
        }
        val total = totalHours2.toString()
        val day = LocalDateTime.now()
        val day2 = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        val dayOfWeek = day.format(day2)
        //dbHandler.insertRow(inTime.text.toString() + " " + spinner1selecteditem, outTime.text.toString() + " " + spinner2selecteditem, break1, total, dayOfWeek)
        dbHandler.update(id, inTime.text.toString() + " " + spinner1selecteditem, outTime.text.toString() + " " + spinner2selecteditem, break1, total, dayOfWeek)
        Toast.makeText(this, id, Toast.LENGTH_LONG).show()
        //this.finish()
        //overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    //Toast.makeText(this, dataList.toString(), Toast.LENGTH_LONG).show()
}
