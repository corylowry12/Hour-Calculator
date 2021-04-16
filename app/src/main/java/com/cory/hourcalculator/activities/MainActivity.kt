@file:Suppress("unused")

package com.cory.hourcalculator.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.isDigitsOnly
import com.cory.hourcalculator.R
import com.cory.hourcalculator.classes.*
import com.cory.hourcalculator.database.DBHelper
import com.google.android.gms.ads.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.jaredrummler.materialspinner.MaterialSpinner
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

class MainActivity : AppCompatActivity() {

    // private lateinit var vibrationData: VibrationData
    private val vibrationData by lazy { VibrationData(this) }
    private lateinit var darkThemeData: DarkThemeData
    private lateinit var historyToggleData: HistoryToggleData
    private lateinit var updateData: UpdateData
    private lateinit var trashAutomaticDeletion: TrashAutomaticDeletion

    //var testDeviceId = listOf("8EDC43FD82F98F52B4B982B33812B1BC")
    private val dbHandler = DBHelper(this, null)
    // private val permissionRequestCode = 1
    //private lateinit var managePermissions: ManagePermissions

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    // Spinner lazy and lateinit initializers
    private val spinner: MaterialSpinner by lazy { findViewById(R.id.material_spinner_1) }
    private val spinner1: MaterialSpinner by lazy { findViewById(R.id.material_spinner_2) }
    private lateinit var spinner1selecteditem: String
    private lateinit var spinner2selecteditem: String

    // AdView lazy initializer
    val adView by lazy { AdView(this) }
    private val mAdView by lazy { findViewById<AdView>(R.id.adView) }
    private val adRequest by lazy { AdRequest.Builder().build() }

    // Break data lazy initializer
    private val breakData by lazy { BreakData(this) }

    // Input manager lazy initializer
    //val imm by lazy { this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        firebaseAnalytics = Firebase.analytics
        darkThemeData = DarkThemeData(this)
        if (darkThemeData.loadDarkModeState()) {
            setTheme(R.style.AMOLED)
        } else {
            setTheme(R.style.AppTheme)
        }
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(null)
        setContentView(R.layout.activity_main)

        historyToggleData = HistoryToggleData(this)
        updateData = UpdateData(this)
        trashAutomaticDeletion = TrashAutomaticDeletion(this)

        MobileAds.initialize(this)
        //val adView = AdView(this)
        adView.adSize = AdSize.BANNER
        adView.adUnitId = "ca-app-pub-4546055219731501/5171269817"
        //val mAdView = findViewById<AdView>(R.id.adView)
        //val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        mAdView.adListener = object : AdListener() {
        }

        applicationContext.cacheDir.deleteRecursively()

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching fcm registration token failed", task.exception)
                return@OnCompleteListener
            }

            Log.d("FCM", task.result.toString())
        })

        requestFocus()

        main()

        val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm.isAcceptingText) {
            mAdView.visibility = View.INVISIBLE
        }

        if (!breakData.loadBreakState()) {
            findViewById<TextView>(R.id.textView4).visibility = View.GONE
            findViewById<TextInputLayout>(R.id.textInputLayout3).visibility = View.GONE
            findViewById<TextInputEditText>(R.id.breakTime).visibility = View.GONE
        }
    }

    override fun onRestart() {
        super.onRestart()
        val intent = Intent(this, this::class.java)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    override fun onResume() {
        super.onResume()
        main()

    }

    fun main() {

        findViewById<ConstraintLayout>(R.id.constraintLayout).setOnClickListener {
            if (findViewById<TextInputEditText>(R.id.inTime).hasFocus() || findViewById<TextInputEditText>(R.id.outTime).hasFocus() || findViewById<TextInputEditText>(R.id.breakTime).hasFocus()) {
                hideKeyboard()
            }
            findViewById<TextInputEditText>(R.id.inTime).clearFocus()
            findViewById<TextInputEditText>(R.id.outTime).clearFocus()
            findViewById<TextInputEditText>(R.id.breakTime).clearFocus()
        }

        //vibrationData = VibrationData(this)

        inTime.setOnClickListener {
            vibration(vibrationData)
        }

        outTime.setOnClickListener {
            vibration(vibrationData)
        }

        spinner.setItems(getString(R.string.am), getString(R.string.pm))
        var spinner1selecteditem: String = getString(R.string.am)

        spinner1.setItems(getString(R.string.pm), getString(R.string.am))
        var spinner2selecteditem: String = getString(R.string.pm)

        spinner.setOnClickListener {
            vibration(vibrationData)
        }

        spinner1.setOnClickListener {
            vibration(vibrationData)
        }

        spinner.setOnItemSelectedListener { _, _, _, item ->
            vibration(vibrationData)
            spinner1selecteditem = item as String
        }

        spinner1.setOnItemSelectedListener { _, _, _, item ->
            vibration(vibrationData)
            spinner2selecteditem = item as String
        }

        spinner.setOnNothingSelectedListener {
            vibration(vibrationData)
        }

        spinner1.setOnNothingSelectedListener {
            vibration(vibrationData)
        }

        inTime.setOnKeyListener(View.OnKeyListener { _, i, keyEvent ->
            if (i == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_UP) {
                vibration(vibrationData)

            }
            if (i == KeyEvent.KEYCODE_BACK && keyEvent.action == KeyEvent.ACTION_DOWN) {
                inTime.clearFocus()
                return@OnKeyListener true
            }
            false
        })


        outTime.setOnKeyListener(View.OnKeyListener { _, i, keyEvent ->
            if (i == KeyEvent.KEYCODE_BACK && keyEvent.action == KeyEvent.ACTION_DOWN) {
                outTime.clearFocus()
            }
            if (i == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_UP) {
                vibration(vibrationData)

                if (textInputLayout3.visibility == View.VISIBLE) {
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
                    breakTime.requestFocus()
                } else {
                    hideKeyboard()
                    validation(inTime.text.toString(), outTime.text.toString(), spinner1selecteditem, spinner2selecteditem, infoTextView1)
                }
                return@OnKeyListener true
            }
            false
        })

        breakTime.setOnKeyListener(View.OnKeyListener { _, i, keyEvent ->
            if (i == KeyEvent.KEYCODE_BACK && keyEvent.action == KeyEvent.ACTION_DOWN) {
                breakTime.clearFocus()
            }
            if (i == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_UP) {
                vibration(vibrationData)

                hideKeyboard()
                validation(inTime.text.toString(), outTime.text.toString(), spinner1selecteditem, spinner2selecteditem, infoTextView1)
                return@OnKeyListener true
            }
            false
        })

        calculateButton1.setOnClickListener {
            vibration(vibrationData)

            validation(inTime.text.toString(), outTime.text.toString(), spinner1selecteditem, spinner2selecteditem, infoTextView1)

            hideKeyboard()
        }

        btnClear.setOnClickListener {
            vibration(vibrationData)
            clearTextBoxes(inTime, outTime, breakTime)
        }
    }

    private fun clearTextBoxes(inTime: EditText, outTime: EditText, breakTime: EditText) {
        if (inTime.text.toString() == "" && outTime.text.toString() == "" && breakTime.text.toString() == "" && infoTextView1.text == "") {
            Toast.makeText(this, getString(R.string.everything_already_cleared), Toast.LENGTH_SHORT).show()
        } else {
            infoTextView1.text = ""
            inTime.text?.clear()
            outTime.text?.clear()
            breakTime.text?.clear()
            inTime.requestFocus()
            val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(inTime, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun savingHours(totalHours3: Double, inTime: EditText, outTime: EditText, breakTime: EditText) {
        var break1 = breakTime.text.toString()
        if (breakTime.text.toString() == "") {
            break1 = getString(R.string.break_zero)
        }
        val total = totalHours3.toString()
        val day = LocalDateTime.now()
        val day2 = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        val dayOfWeek = day.format(day2)
        dbHandler.insertRow(inTime.text.toString(), outTime.text.toString(), break1, total, dayOfWeek)
    }

    private fun savingHours2(totalHours2: Double, inTime: EditText, outTime: EditText, breakTime: EditText) {
        var break1 = breakTime.text.toString()
        if (breakTime.text.toString() == "") {
            break1 = "0"
        }
        val total = totalHours2.toString()
        val day = LocalDateTime.now()
        val day2 = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        val dayOfWeek = day.format(day2)
        dbHandler.insertRow(inTime.text.toString(), outTime.text.toString(), break1, total, dayOfWeek)
    }

    @SuppressLint("SetTextI18n")
    fun validation(inTimeString: String, outTimeString: String, spinner1selecteditem: String, spinner2selecteditem: String, infoTextView1: TextView) {
        if (inTime.text.toString().contains(",")) {
            infoTextView1.text = getString(R.string.theres_a_comman_in_text_box)
        } else if (outTime.text.toString().contains(",")) {
            infoTextView1.text = getString(R.string.theres_a_comman_in_text_box)
        }
        if (spinner1selecteditem == getString(R.string.am) && spinner2selecteditem == getString(R.string.am)) {
            if (inTimeString.length == 2 || outTimeString.length == 2) {
                infoTextView1.text = getString(R.string.proper_input)
            }
            if (inTime.text.toString() == "" || outTime.text.toString() == "" || !inTime.hasFocus() || !outTime.hasFocus()) {
                infoTextView1.text = getString(R.string.dont_leave_anything_blank)
            }
            if (!inTimeString.contains(":") && !outTimeString.contains(":")) {
                if (inTimeString.length == 3 && outTimeString.length == 3) {
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = inTimeString.drop(1)
                        val intimelast = inTimeString.dropLast(2)
                        val outtime = outTimeString.drop(1)
                        val outtimelast = outTimeString.dropLast(2)
                        outTime.setText("$outtimelast:$outtime")
                        inTime.setText("$intimelast:$intime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                } else if (inTimeString.length == 3 && outTimeString.length == 4) {
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = inTimeString.drop(2)
                        val intimelast = inTimeString.dropLast(1)
                        val outtime = outTimeString.drop(2)
                        val outtimelast = outTimeString.dropLast(2)
                        inTime.setText("$intime:$intimelast")
                        outTime.setText("$outtimelast:$outtime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                } else if (inTimeString.length == 4 && outTimeString.length == 3) {
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = inTimeString.drop(2)
                        val intimelast = inTimeString.dropLast(2)
                        val outtime = outTimeString.drop(2)
                        val outtimelast = outTimeString.dropLast(1)
                        inTime.setText("$intimelast:$intime")
                        outTime.setText("$outtime:$outtimelast")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            val rounded = (inTimeMinutes.toDouble() / 60).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toString()
                            val rounded1 = (outTimeMinutes.toDouble() / 60).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toString()
                            val total1 = inTimeHours.toDouble() + rounded.substring(1).toDouble()
                            val total2 = outTimeHours.toDouble() + rounded1.substring(1).toDouble()
                            val difference = total2 - total1
                            val totalhours = String.format("%.2f", difference).toDouble()
                            if (totalhours < 0) {
                                infoTextView1.text = getString(R.string.in_time_can_not_be_greater_than_out_time)
                            } else {
                                aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                            }
                        }
                    }
                } else if (inTimeString.length == 4 && outTimeString.length == 4) {
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = inTimeString.drop(2)
                        val intimelast = inTimeString.dropLast(2)
                        val outtime = outTimeString.drop(2)
                        val outtimelast = outTimeString.dropLast(2)
                        inTime.setText("$intimelast:$intime")
                        outTime.setText("$outtimelast:$outtime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                }
            }
            if (!inTimeString.contains(":") && outTimeString.contains(":")) {
                if (inTimeString.length == 3) {
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = inTimeString.drop(1)
                        val intimelast = inTimeString.dropLast(2)
                        inTime.setText("$intimelast:$intime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                } else if (inTimeString.length == 4) {
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = inTimeString.drop(2)
                        val intimelast = inTimeString.dropLast(2)
                        inTime.setText("$intimelast:$intime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else {
                            aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                }
            }
            if (inTimeString.contains(":") && !outTimeString.contains(":")) {
                if (outTimeString.length == 3) {
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val outtime = outTimeString.drop(1)
                        val outtimelast = outTimeString.dropLast(2)
                        outTime.setText("$outtimelast:$outtime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                } else if (outTimeString.length == 4) {
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val outtime = outTimeString.drop(2)
                        val outtimelast = outTimeString.dropLast(2)
                        outTime.setText("$outtimelast:$outtime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                }
            } else if (inTimeString.contains(":") && outTimeString.contains(":")) {
                val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                if (inTimeMinutes == "" || outTimeMinutes == "") {
                    infoTextView1.text = getString(R.string.proper_input)
                } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                    infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                    infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                } else {
                    aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                }
            }
        }
        if (spinner1selecteditem == getString(R.string.pm) && spinner2selecteditem == getString(R.string.pm)) {
            if (inTimeString.length <= 2 || outTimeString.length <= 2) {
                infoTextView1.text = getString(R.string.proper_input)
            }
            if (inTime.text.toString() == "" || outTime.text.toString() == "" || !inTime.hasFocus() || !outTime.hasFocus()) {
                infoTextView1.text = getString(R.string.dont_leave_anything_blank)
            }
            if (!inTimeString.contains(":") && !outTimeString.contains(":")) {
                if (inTimeString.length == 3 && outTimeString.length == 3) {
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = inTimeString.drop(1)
                        val intimelast = inTimeString.dropLast(2)
                        val outtime = outTimeString.drop(1)
                        val outtimelast = outTimeString.dropLast(2)
                        outTime.setText("$outtimelast:$outtime")
                        inTime.setText("$intimelast:$intime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                } else if (inTimeString.length == 3 && outTimeString.length == 4) {
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = inTimeString.drop(2)
                        val intimelast = inTimeString.dropLast(1)
                        val outtime = outTimeString.drop(2)
                        val outtimelast = outTimeString.dropLast(2)
                        inTime.setText("$intime:$intimelast")
                        outTime.setText("$outtimelast:$outtime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                } else if (inTimeString.length == 4 && outTimeString.length == 3) {
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = inTimeString.drop(2)
                        val intimelast = inTimeString.dropLast(2)
                        val outtime = outTimeString.drop(2)
                        val outtimelast = outTimeString.dropLast(1)
                        inTime.setText("$intimelast:$intime")
                        outTime.setText("$outtime:$outtimelast")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                } else if (inTimeString.length == 4 && outTimeString.length == 4) {
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = inTimeString.drop(2)
                        val intimelast = inTimeString.dropLast(2)
                        val outtime = outTimeString.drop(2)
                        val outtimelast = outTimeString.dropLast(2)
                        inTime.setText("$intimelast:$intime")
                        outTime.setText("$outtimelast:$outtime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                }
            }
            if (!inTimeString.contains(":") && outTimeString.contains(":")) {
                if (inTimeString.length == 3) {
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = inTimeString.drop(1)
                        val intimelast = inTimeString.dropLast(2)
                        inTime.setText("$intimelast:$intime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                } else if (inTimeString.length == 4) {
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = inTimeString.drop(2)
                        val intimelast = inTimeString.dropLast(2)
                        inTime.setText("$intimelast:$intime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else {
                            aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                }
            }
            if (inTimeString.contains(":") && !outTimeString.contains(":")) {
                if (outTimeString.length == 3) {
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val outtime = outTimeString.drop(1)
                        val outtimelast = outTimeString.dropLast(2)
                        outTime.setText("$outtimelast:$outtime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                } else if (outTimeString.length == 4) {
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val outtime = outTimeString.drop(2)
                        val outtimelast = outTimeString.dropLast(2)
                        outTime.setText("$outtimelast:$outtime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                }
            } else if (inTimeString.contains(":") && outTimeString.contains(":")) {
                val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                if (inTimeMinutes == "" || outTimeMinutes == "") {
                    infoTextView1.text = getString(R.string.proper_input)
                } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                    infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                    infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                } else {
                    aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                }
            }
        }
        if (spinner1selecteditem == getString(R.string.am) && spinner2selecteditem == getString(R.string.pm)) {
            if (inTimeString.length == 2 || outTimeString.length == 2) {
                infoTextView1.text = getString(R.string.proper_input)
            }
            if (inTimeString == "" || outTimeString == "") {
                infoTextView1.text = getString(R.string.dont_leave_anything_blank)
            }
            if (!inTimeString.contains(":") && !outTimeString.contains(":")) {
                if (inTimeString.length == 3 && outTimeString.length == 3) {
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = inTimeString.drop(1)
                        val intimelast = inTimeString.dropLast(2)
                        val outtime = outTimeString.drop(1)
                        val outtimelast = outTimeString.dropLast(2)
                        outTime.setText("$outtimelast:$outtime")
                        inTime.setText("$intimelast:$intime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || inTimeMinutes == "") {
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else {
                            aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                } else if (inTimeString.length == 3 && outTimeString.length == 4) {
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = inTimeString.drop(2)
                        val intimelast = inTimeString.dropLast(1)
                        val outtime = outTimeString.drop(2)
                        val outtimelast = outTimeString.dropLast(2)
                        inTime.setText("$intime:$intimelast")
                        outTime.setText("$outtimelast:$outtime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else {
                            aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                } else if (inTimeString.length == 4 && outTimeString.length == 3) {
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = inTimeString.drop(2)
                        val intimelast = inTimeString.dropLast(2)
                        val outtime = outTimeString.drop(2)
                        val outtimelast = outTimeString.dropLast(1)
                        inTime.setText("$intimelast:$intime")
                        outTime.setText("$outtime:$outtimelast")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                } else if (inTimeString.length == 4 && outTimeString.length == 4) {
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = inTimeString.drop(2)
                        val intimelast = inTimeString.dropLast(2)
                        val outtime = outTimeString.drop(2)
                        val outtimelast = outTimeString.dropLast(2)
                        inTime.setText("$intimelast:$intime")
                        outTime.setText("$outtimelast:$outtime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                }
            }
            if (!inTimeString.contains(":") && outTimeString.contains(":")) {
                if (inTimeString.length == 3) {
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = inTimeString.drop(1)
                        val intimelast = inTimeString.dropLast(2)
                        inTime.setText("$intimelast:$intime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                } else if (inTimeString.length == 4) {
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = inTimeString.drop(2)
                        val intimelast = inTimeString.dropLast(2)
                        inTime.setText("$intimelast:$intime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                }
            }
            if (inTimeString.contains(":") && !outTimeString.contains(":")) {
                if (outTimeString.length == 3) {
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val outtime = outTimeString.drop(1)
                        val outtimelast = outTimeString.dropLast(2)
                        outTime.setText("$outtimelast:$outtime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                } else if (outTimeString.length == 4) {
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val outtime = outTimeString.drop(2)
                        val outtimelast = outTimeString.dropLast(2)
                        outTime.setText("$outtimelast:$outtime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                }
            } else if (inTimeString.contains(":") && outTimeString.contains(":")) {
                val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                if (inTimeMinutes == "" || outTimeMinutes == "") {
                    infoTextView1.text = getString(R.string.proper_input)
                } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                    infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                    infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                } else {
                    aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                }
            }
        }
        if (spinner1selecteditem == getString(R.string.pm) && spinner2selecteditem == getString(R.string.am)) {
            if (inTimeString.length == 2 || outTimeString.length == 2) {
                infoTextView1.text = getString(R.string.proper_input)
            }
            if (inTimeString == "" || outTimeString == "") {
                infoTextView1.text = getString(R.string.dont_leave_anything_blank)
            }
            if (!inTimeString.contains(":") && !outTimeString.contains(":")) {
                if (inTimeString.length == 3 && outTimeString.length == 3) {
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = inTimeString.drop(1)
                        val intimelast = inTimeString.dropLast(2)
                        val outtime = outTimeString.drop(1)
                        val outtimelast = outTimeString.dropLast(2)
                        outTime.setText("$outtimelast:$outtime")
                        inTime.setText("$intimelast:$intime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else {
                            aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                } else if (inTimeString.length == 3 && outTimeString.length == 4) {
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = inTimeString.drop(2)
                        val intimelast = inTimeString.dropLast(1)
                        val outtime = outTimeString.drop(2)
                        val outtimelast = outTimeString.dropLast(2)
                        inTime.setText("$intime:$intimelast")
                        outTime.setText("$outtimelast:$outtime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else {
                            aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                } else if (inTimeString.length == 4 && outTimeString.length == 3) {
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = inTimeString.drop(2)
                        val intimelast = inTimeString.dropLast(2)
                        val outtime = outTimeString.drop(2)
                        val outtimelast = outTimeString.dropLast(1)
                        inTime.setText("$intimelast:$intime")
                        outTime.setText("$outtime:$outtimelast")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                } else if (inTimeString.length == 4 && outTimeString.length == 4) {
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = inTimeString.drop(2)
                        val intimelast = inTimeString.dropLast(2)
                        val outtime = outTimeString.drop(2)
                        val outtimelast = outTimeString.dropLast(2)
                        inTime.setText("$intimelast:$intime")
                        outTime.setText("$outtimelast:$outtime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                }
            }
            if (!inTimeString.contains(":") && outTimeString.contains(":")) {
                if (inTimeString.length == 3) {
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = inTimeString.drop(1)
                        val intimelast = inTimeString.dropLast(2)
                        inTime.setText("$intimelast:$intime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                } else if (inTimeString.length == 4) {
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = inTimeString.drop(2)
                        val intimelast = inTimeString.dropLast(2)
                        inTime.setText("$intimelast:$intime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                }
            }
            if (inTimeString.contains(":") && !outTimeString.contains(":")) {
                if (outTimeString.length == 3) {
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val outtime = outTimeString.drop(1)
                        val outtimelast = outTimeString.dropLast(2)
                        outTime.setText("$outtimelast:$outtime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                } else if (outTimeString.length == 4) {
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val outtime = outTimeString.drop(2)
                        val outtimelast = outTimeString.dropLast(2)
                        outTime.setText("$outtimelast:$outtime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                }
            } else if (inTimeString.contains(":") && outTimeString.contains(":")) {
                val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                if (inTimeMinutes == "" || outTimeMinutes == "") {
                    infoTextView1.text = getString(R.string.proper_input)
                } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                    infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                    infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                } else {
                    aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                }
            }
        }
    }

    private fun aMandAMandPMandPM(inTimeHours: String, inTimeMinutes: String, outTimeHours: String, outTimeMinutes: String, infoTextView1: TextView, breakTime: EditText) {
        val historyToggleData = HistoryToggleData(this)
        val inTimeMinutesRounded = (inTimeMinutes.toDouble() / 60).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toString()
        val outTimeMinutesRounded = (outTimeMinutes.toDouble() / 60).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toString()
        val inTimeTotal = inTimeHours.toDouble() + inTimeMinutesRounded.substring(1).toDouble()
        val outTimeTotal = outTimeHours.toDouble() + outTimeMinutesRounded.substring(1).toDouble()
        val difference = outTimeTotal - inTimeTotal
        val totalhours = String.format("%.2f", difference).toDouble()
        if (totalhours < 0) {
            infoTextView1.text = getString(R.string.in_time_can_not_be_greater_than_out_time)
        } else {
            if (breakTime.text.toString() == "") {
                infoTextView1.text = getString(R.string.total_hours, totalhours.toString())
                if (historyToggleData.loadHistoryState()) {
                    savingHours(totalhours, inTime, outTime, breakTime)
                }
            } else if (breakTime.text.toString() != "") {
                if (!breakTime.text.isDigitsOnly()) {
                    infoTextView1.text = getString(R.string.something_wrong_with_break_text_box)
                } else {
                    val breakTimeDec: Double = (breakTime.text.toString().toDouble() / 60).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toString().toDouble()
                    val totalHours1 = totalhours - breakTimeDec
                    val totalHoursWithBreak = String.format("%.2f", totalHours1).toDouble()
                    if (totalHoursWithBreak < 0) {
                        infoTextView1.text = getString(R.string.in_time_can_not_be_greater_than_out_time)
                    } else if (totalHoursWithBreak > 0) {
                        infoTextView1.text = getString(R.string.total_hours_with_and_without_break, totalHoursWithBreak.toString(), totalhours.toString())
                        if (historyToggleData.loadHistoryState()) {
                            savingHours2(totalHoursWithBreak, inTime, outTime, breakTime)
                        }
                    }
                }
            }
        }
    }

    private fun aMandPMandPMandAM(inTimeHours: String, inTimeMinutes: String, outTimeHours: String, outTimeMinutes: String, infoTextView1: TextView, breakTime: EditText) {
        val historyToggleData = HistoryToggleData(this)
        val inTimeMinutesRounded = (inTimeMinutes.toDouble() / 60).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toString()
        val outTimeMinutesRounded = (outTimeMinutes.toDouble() / 60).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toString()
        val inTimeTotal = inTimeHours.toDouble() + inTimeMinutesRounded.substring(1).toDouble()
        val outTimeTotal = outTimeHours.toDouble() + outTimeMinutesRounded.substring(1).toDouble()
        val difference: Double = outTimeTotal - inTimeTotal
        val totalhours = String.format("%.2f", difference).toDouble() + 12
        if (totalhours < 0) {
            infoTextView1.text = getString(R.string.in_time_can_not_be_greater_than_out_time)
        } else {
            if (breakTime.text.toString() == "") {
                infoTextView1.text = getString(R.string.total_hours, totalhours.toString())
                if (historyToggleData.loadHistoryState()) {
                    savingHours(totalhours, inTime, outTime, breakTime)
                }
            } else if (breakTime.text.toString() != "") {
                if (!breakTime.text.toString().isDigitsOnly()) {
                    infoTextView1.text = getString(R.string.something_wrong_with_break_text_box)
                } else {
                    val breakTimeDec: Double = (breakTime.text.toString().toDouble() / 60).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toString().toDouble()
                    val totalHours1 = totalhours - breakTimeDec
                    val totalHoursWithBreak = String.format("%.2f", totalHours1).toDouble()
                    if (totalHours1 < 0) {
                        infoTextView1.text = getString(R.string.in_time_can_not_be_greater_than_out_time)
                    } else if (totalHours1 > 0) {
                        infoTextView1.text = getString(R.string.total_hours_with_and_without_break, totalHoursWithBreak.toString(), totalhours.toString())
                        if (historyToggleData.loadHistoryState()) {
                            savingHours2(totalHoursWithBreak, inTime, outTime, breakTime)
                        }
                    }
                }
            }
        }
    }

    private fun hideKeyboard() {
        val inputManager: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val focusedView = this.currentFocus
        if (focusedView != null) {
            inputManager.hideSoftInputFromWindow(focusedView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            when {
                inTime!!.hasFocus() -> {
                    inTime!!.clearFocus()
                }
                outTime!!.hasFocus() -> {
                    outTime!!.clearFocus()
                }
                breakTime!!.hasFocus() -> {
                    breakTime!!.clearFocus()
                }
            }
        }
    }

    fun vibration(vibrationData: VibrationData) {
        if (vibrationData.loadVibrationState()) {
            val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(5, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    private fun requestFocus() {
        inTime.requestFocus()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
    }

    private var doubleBackToExitPressedOnce = false

    override fun onBackPressed() {
        inTime.clearFocus()
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, getString(R.string.click_back_again), Toast.LENGTH_SHORT).show()

        Looper.myLooper()?.let {
            Handler(it).postDelayed({
                doubleBackToExitPressedOnce = false
            }, 2000)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu_home, menu)
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
            R.id.changelog -> {
                val intent = Intent(this, PatchNotesActivity::class.java)
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
}