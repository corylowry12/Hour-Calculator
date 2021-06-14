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
import com.google.firebase.messaging.ktx.messaging
import com.jaredrummler.materialspinner.MaterialSpinner
import kotlinx.android.synthetic.main.activity_history.*
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

class MainActivity : AppCompatActivity() {

    // private lateinit var vibrationData: VibrationData
    private lateinit var vibrationData: VibrationData
    private lateinit var darkThemeData: DarkThemeData
    private lateinit var historyToggleData: HistoryToggleData
    private lateinit var updateData: UpdateData
    private lateinit var trashAutomaticDeletion: TrashAutomaticDeletion

    val testDeviceId = listOf("5E80E48DC2282D372EAE0E3ACDE070CC", "8EE44B7B4B422D333731760574A381FE")
    private val dbHandler = DBHelper(this, null)
    // private val permissionRequestCode = 1
    //private lateinit var managePermissions: ManagePermissions

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    // Spinner lazy and lateinit initializers
    private val spinner: MaterialSpinner by lazy { findViewById(R.id.material_spinner_1) }
    private val spinner1: MaterialSpinner by lazy { findViewById(R.id.material_spinner_2) }
    private lateinit var spinner1selecteditem: String
    private lateinit var spinner2selecteditem: String

    // Break data lazy initializer
    private val breakData by lazy { BreakData(this) }

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

        val historyAutomaticDeletion = HistoryAutomaticDeletion(this)
        val historyDeletion = HistoryDeletion(this)
        val daysWorked = DaysWorkedPerWeek(this)

        if(daysWorked.loadDaysWorked() != "" && historyAutomaticDeletion.loadHistoryDeletionState() && dbHandler.getCount() > daysWorked.loadDaysWorked().toString().toInt()) {
            historyDeletion.deletion(this)
        }

        if (UpdateData(this).loadUpdateNotificationState()) {
            Firebase.messaging.subscribeToTopic("updates")
                .addOnCompleteListener { task ->
                    var msg = "Subscribed"
                    if (!task.isSuccessful) {
                        msg = "Subscribe failed"
                    }
                    Log.d("Updates", msg)
                }
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching fcm registration token failed", task.exception)
                return@OnCompleteListener
            }

            Log.d("FCM", task.result.toString())
        })

        vibrationData = VibrationData(this)

        requestFocus()

        main()

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
        if (!PerformanceModeData(this).loadPerformanceMode()) {
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        } else {
            overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
        }
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

        inTime.setOnClickListener {
            vibration(vibrationData)
        }

        outTime.setOnClickListener {
            vibration(vibrationData)
        }

        breakTime.setOnClickListener {
            vibration(vibrationData)
        }

        val spinnerState = SpinnerData(this)
        spinner1selecteditem = if (!spinnerState.loadSpinner1State()) {
            spinner.setItems(getString(R.string.am), getString(R.string.pm))
            getString(R.string.am)
        } else {
            spinner.setItems(getString(R.string.pm), getString(R.string.am))
            getString(R.string.pm)
        }

        spinner2selecteditem = if (!spinnerState.loadSpinner2State()) {
            spinner1.setItems(getString(R.string.pm), getString(R.string.am))
            getString(R.string.pm)
        } else {
            spinner1.setItems(getString(R.string.am), getString(R.string.pm))
            getString(R.string.am)
        }
        spinner.setOnClickListener {
            vibration(vibrationData)
        }

        spinner1.setOnClickListener {
            vibration(vibrationData)
        }

        spinner.setOnItemSelectedListener { _, _, _, item ->
            vibration(vibrationData)
            spinner1selecteditem = item as String
            if (item == getString(R.string.am)) {
                spinnerState.setSpinner1State(false)
            } else if (item == getString(R.string.pm)) {
                spinnerState.setSpinner1State(true)
            }
        }

        spinner1.setOnItemSelectedListener { _, _, _, item ->
            vibration(vibrationData)
            spinner2selecteditem = item as String
            if (item == getString(R.string.pm)) {
                spinnerState.setSpinner2State(false)
            } else if (item == getString(R.string.am)) {
                spinnerState.setSpinner2State(true)
            }
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
                return@OnKeyListener true
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
                return@OnKeyListener true
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
            inTime.requestFocus()
            val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(inTime, InputMethodManager.SHOW_IMPLICIT)
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

    private fun savingHours(totalHours3: Double, inTime: EditText, outTime: EditText, breakTime: EditText, spinner1selecteditem: String, spinner2selecteditem: String) {
        var break1 = breakTime.text.toString()
        if (breakTime.text.toString() == "") {
            break1 = getString(R.string.break_zero)
        }
        val day = LocalDateTime.now()
        val day2 = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        val dayOfWeek = day.format(day2)
        dbHandler.insertRow(inTime.text.toString() + " " + spinner1selecteditem, outTime.text.toString() + " " + spinner2selecteditem, break1, totalHours3.toString(), dayOfWeek)
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
            if (inTime.text.toString() == "" || outTime.text.toString() == "") {
                infoTextView1.text = getString(R.string.dont_leave_anything_blank)
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
                        infoTextView1.text = getString(R.string.proper_input)
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                    } else {
                        aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime, spinner1selecteditem, spinner2selecteditem)
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
                        infoTextView1.text = getString(R.string.proper_input)
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                    } else {
                        aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime, spinner1selecteditem, spinner2selecteditem)
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
                        infoTextView1.text = getString(R.string.proper_input)
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                    } else {
                        val rounded = (inTimeMinutes.toDouble() / 60).toBigDecimal().setScale(3, RoundingMode.HALF_EVEN).toString()
                        val rounded1 = (outTimeMinutes.toDouble() / 60).toBigDecimal().setScale(3, RoundingMode.HALF_EVEN).toString()
                        val total1 = inTimeHours.toDouble() + rounded.substring(1).toDouble()
                        val total2 = outTimeHours.toDouble() + rounded1.substring(1).toDouble()
                        val difference = total2 - total1
                        val totalhours = String.format("%.2f", difference).toDouble()
                        if (totalhours < 0) {
                            infoTextView1.text = getString(R.string.in_time_can_not_be_greater_than_out_time)
                        } else {
                            aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime, spinner1selecteditem, spinner2selecteditem)
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
                        infoTextView1.text = getString(R.string.proper_input)
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                    } else {
                        aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime, spinner1selecteditem, spinner2selecteditem)
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
                        infoTextView1.text = getString(R.string.proper_input)
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                    } else {
                        aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime, spinner1selecteditem, spinner2selecteditem)
                    }
                } else if (inTimeString.length == 4) {
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
                        aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime, spinner1selecteditem, spinner2selecteditem)
                    }
                } else if (inTimeString.length == 5) {
                    infoTextView1.text = getString(R.string.time_cant_be_five_digits)
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
                        infoTextView1.text = getString(R.string.proper_input)
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                    } else {
                        aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime, spinner1selecteditem, spinner2selecteditem)
                    }
                } else if (outTimeString.length == 4) {
                    val outtime = outTimeString.drop(2)
                    val outtimelast = outTimeString.dropLast(2)
                    outTime.setText("$outtimelast:$outtime")
                    val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                    val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                    if (inTimeMinutes == "" || outTimeMinutes == "") {
                        infoTextView1.text = getString(R.string.proper_input)
                    } else if (inTimeMinutes.length == 3 && outTimeMinutes.length == 3) {
                        infoTextView1.text = "Minutes can't be three numbers"
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                    } else {
                        aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime, spinner1selecteditem, spinner2selecteditem)
                    }
                } else if (outTimeString.length == 5) {
                    infoTextView1.text = getString(R.string.time_cant_be_five_digits)
                }
            } else if (inTimeString.contains(":") && outTimeString.contains(":")) {
                val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                if (inTimeMinutes == "" || outTimeMinutes == "") {
                    infoTextView1.text = getString(R.string.proper_input)
                } else if (inTimeMinutes.length == 3 || outTimeMinutes.length == 3) {
                    infoTextView1.text = getString(R.string.minutes_cant_be_three_numbers)
                } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                    infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                    infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                } else {
                    aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime, spinner1selecteditem, spinner2selecteditem)
                }
            }
        }
        if (spinner1selecteditem == getString(R.string.pm) && spinner2selecteditem == getString(R.string.pm)) {
            if (inTimeString.length <= 2 || outTimeString.length <= 2) {
                infoTextView1.text = getString(R.string.proper_input)
            }
            if (inTime.text.toString() == "" || outTime.text.toString() == "") {
                infoTextView1.text = getString(R.string.dont_leave_anything_blank)
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
                        infoTextView1.text = getString(R.string.proper_input)
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                    } else {
                        aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime, spinner1selecteditem, spinner2selecteditem)
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
                        infoTextView1.text = getString(R.string.proper_input)
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                    } else {
                        aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime, spinner1selecteditem, spinner2selecteditem)
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
                        infoTextView1.text = getString(R.string.proper_input)
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                    } else {
                        aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime, spinner1selecteditem, spinner2selecteditem)
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
                        infoTextView1.text = getString(R.string.proper_input)
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                    } else {
                        aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime, spinner1selecteditem, spinner2selecteditem)
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
                        infoTextView1.text = getString(R.string.proper_input)
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                    } else {
                        aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime, spinner1selecteditem, spinner2selecteditem)
                    }
                } else if (inTimeString.length == 4) {
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
                        aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime, spinner1selecteditem, spinner2selecteditem)
                    }
                } else if (inTimeString.length == 5) {
                    infoTextView1.text = getString(R.string.time_cant_be_five_digits)
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
                        infoTextView1.text = getString(R.string.proper_input)
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                    } else {
                        aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime, spinner1selecteditem, spinner2selecteditem)
                    }
                } else if (outTimeString.length == 4) {
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
                        aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime, spinner1selecteditem, spinner2selecteditem)
                    }
                } else if (outTimeString.length == 5) {
                    infoTextView1.text = getString(R.string.time_cant_be_five_digits)
                }
            } else if (inTimeString.contains(":") && outTimeString.contains(":")) {
                val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                if (inTimeMinutes == "" || outTimeMinutes == "") {
                    infoTextView1.text = getString(R.string.proper_input)
                } else if (inTimeMinutes.length == 3 || outTimeMinutes.length == 3) {
                    infoTextView1.text = getString(R.string.minutes_cant_be_three_numbers)
                } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                    infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                    infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                } else {
                    aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime, spinner1selecteditem, spinner2selecteditem)
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
                        aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime, spinner1selecteditem, spinner2selecteditem)
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
                        infoTextView1.text = getString(R.string.proper_input)
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                    } else {
                        aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime, spinner1selecteditem, spinner2selecteditem)
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
                        infoTextView1.text = getString(R.string.proper_input)
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                    } else {
                        aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime, spinner1selecteditem, spinner2selecteditem)
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
                        infoTextView1.text = getString(R.string.proper_input)
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                    } else {
                        aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime, spinner1selecteditem, spinner2selecteditem)
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
                        infoTextView1.text = getString(R.string.proper_input)
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                    } else {
                        aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime, spinner1selecteditem, spinner2selecteditem)
                    }
                } else if (inTimeString.length == 4) {
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
                        aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime, spinner1selecteditem, spinner2selecteditem)
                    }
                } else if (inTimeString.length == 5) {
                    infoTextView1.text = getString(R.string.time_cant_be_five_digits)
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
                        infoTextView1.text = getString(R.string.proper_input)
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                    } else {
                        aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime, spinner1selecteditem, spinner2selecteditem)
                    }
                } else if (outTimeString.length == 4) {
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
                        aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime, spinner1selecteditem, spinner2selecteditem)
                    }
                } else if (outTimeString.length == 5) {
                    infoTextView1.text = getString(R.string.time_cant_be_five_digits)
                }
            } else if (inTimeString.contains(":") && outTimeString.contains(":")) {
                val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                if (inTimeMinutes == "" || outTimeMinutes == "") {
                    infoTextView1.text = getString(R.string.proper_input)
                } else if (inTimeMinutes.length == 3 || outTimeMinutes.length == 3) {
                    infoTextView1.text = getString(R.string.minutes_cant_be_three_numbers)
                } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                    infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                    infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                } else {
                    aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime, spinner1selecteditem, spinner2selecteditem)
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
                        aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime, spinner1selecteditem, spinner2selecteditem)
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
                        infoTextView1.text = getString(R.string.proper_input)
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                    } else {
                        aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime, spinner1selecteditem, spinner2selecteditem)
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
                        infoTextView1.text = getString(R.string.proper_input)
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                    } else {
                        aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime, spinner1selecteditem, spinner2selecteditem)
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
                        infoTextView1.text = getString(R.string.proper_input)
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                    } else {
                        aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime, spinner1selecteditem, spinner2selecteditem)
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
                        infoTextView1.text = getString(R.string.proper_input)
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                    } else {
                        aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime, spinner1selecteditem, spinner2selecteditem)
                    }
                } else if (inTimeString.length == 4) {
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
                        aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime, spinner1selecteditem, spinner2selecteditem)
                    }
                } else if (inTimeString.length == 5) {
                    infoTextView1.text = getString(R.string.time_cant_be_five_digits)
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
                        infoTextView1.text = getString(R.string.proper_input)
                    } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                    } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                        infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                    } else {
                        aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime, spinner1selecteditem, spinner2selecteditem)
                    }
                } else if (outTimeString.length == 4) {
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
                        aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime, spinner1selecteditem, spinner2selecteditem)
                    }
                } else if (outTimeString.length == 5) {
                    infoTextView1.text = getString(R.string.time_cant_be_five_digits)
                }
            } else if (inTimeString.contains(":") && outTimeString.contains(":")) {
                val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                if (inTimeMinutes == "" || outTimeMinutes == "") {
                    infoTextView1.text = getString(R.string.proper_input)
                } else if (inTimeMinutes.length == 3 || outTimeMinutes.length == 3) {
                    infoTextView1.text = getString(R.string.minutes_cant_be_three_numbers)
                } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                    infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                    infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                } else {
                    aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime, spinner1selecteditem, spinner2selecteditem)
                }
            }
        }
    }

    private fun aMandAMandPMandPM(inTimeHours: String, inTimeMinutes: String, outTimeHours: String, outTimeMinutes: String, infoTextView1: TextView, breakTime: EditText, spinner1selecteditem: String, spinner2selecteditem: String) {
        try {
            val historyToggleData = HistoryToggleData(this)
            val inTimeMinutesRounded = (inTimeMinutes.toDouble() / 60).toBigDecimal().setScale(3, RoundingMode.HALF_EVEN).toString()
            val outTimeMinutesRounded = (outTimeMinutes.toDouble() / 60).toBigDecimal().setScale(3, RoundingMode.HALF_EVEN).toString()
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
                        savingHours(totalhours, inTime, outTime, breakTime, spinner1selecteditem, spinner2selecteditem)
                    }
                } else if (breakTime.text.toString() != "") {
                    if (!breakTime.text.isDigitsOnly()) {
                        infoTextView1.text = getString(R.string.something_wrong_with_break_text_box)
                    } else {
                        val breakTimeDec: Double = (breakTime.text.toString().toDouble() / 60).toBigDecimal().setScale(3, RoundingMode.HALF_EVEN).toString().toDouble()
                        val totalHours1 = totalhours - breakTimeDec
                        val totalHoursWithBreak = String.format("%.2f", totalHours1).toDouble()
                        if (totalHoursWithBreak < 0) {
                            infoTextView1.text = getString(R.string.in_time_can_not_be_greater_than_out_time)
                        } else if (totalHoursWithBreak > 0) {
                            infoTextView1.text = getString(R.string.total_hours_with_and_without_break, totalHoursWithBreak.toString(), totalhours.toString())
                            if (historyToggleData.loadHistoryState()) {
                                savingHours(totalHoursWithBreak, inTime, outTime, breakTime, spinner1selecteditem, spinner2selecteditem)
                            }
                        }
                    }
                }
            }
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.there_was_an_error_check_input), Toast.LENGTH_SHORT).show()
        }
    }

    private fun aMandPMandPMandAM(inTimeHours: String, inTimeMinutes: String, outTimeHours: String, outTimeMinutes: String, infoTextView1: TextView, breakTime: EditText, spinner1selecteditem: String, spinner2selecteditem: String) {
        try {
            val historyToggleData = HistoryToggleData(this)
            val inTimeMinutesRounded = (inTimeMinutes.toDouble() / 60).toBigDecimal().setScale(3, RoundingMode.HALF_EVEN).toString()
            val outTimeMinutesRounded = (outTimeMinutes.toDouble() / 60).toBigDecimal().setScale(3, RoundingMode.HALF_EVEN).toString()
            val inTimeTotal = inTimeHours.toDouble() + inTimeMinutesRounded.substring(1).toDouble()
            val outTimeTotal = outTimeHours.toDouble() + outTimeMinutesRounded.substring(1).toDouble()
            val difference: Double = outTimeTotal - inTimeTotal
            val totalhours: Double = if (outTimeHours.toInt() == 12) {
                String.format("%.2f", difference).toDouble()
            } else {
                String.format("%.2f", difference).toDouble() + 12
            }
            if (totalhours < 0) {
                infoTextView1.text = getString(R.string.in_time_can_not_be_greater_than_out_time)
            } else {
                if (breakTime.text.toString() == "") {
                    infoTextView1.text = getString(R.string.total_hours, totalhours.toString())
                    if (historyToggleData.loadHistoryState()) {
                        savingHours(totalhours, inTime, outTime, breakTime, spinner1selecteditem, spinner2selecteditem)
                    }
                } else if (breakTime.text.toString() != "") {
                    if (!breakTime.text.toString().isDigitsOnly()) {
                        infoTextView1.text = getString(R.string.something_wrong_with_break_text_box)
                    } else {
                        val breakTimeDec: Double = (breakTime.text.toString().toDouble() / 60).toBigDecimal().setScale(3, RoundingMode.HALF_EVEN).toString().toDouble()
                        val totalHours1 = totalhours - breakTimeDec
                        val totalHoursWithBreak = String.format("%.2f", totalHours1).toDouble()
                        if (totalHours1 < 0) {
                            infoTextView1.text = getString(R.string.in_time_can_not_be_greater_than_out_time)
                        } else if (totalHours1 > 0) {
                            infoTextView1.text = getString(R.string.total_hours_with_and_without_break, totalHoursWithBreak.toString(), totalhours.toString())
                            if (historyToggleData.loadHistoryState()) {
                                savingHours(totalHoursWithBreak, inTime, outTime, breakTime, spinner1selecteditem, spinner2selecteditem)
                            }
                        }
                    }
                }
            }
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.there_was_an_error_check_input), Toast.LENGTH_SHORT).show()
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
                if (!PerformanceModeData(this).loadPerformanceMode()) {
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                } else {
                    overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
                }
                return true
            }
            R.id.changelog -> {
                val intent = Intent(this, PatchNotesActivity::class.java)
                startActivity(intent)
                if (!PerformanceModeData(this).loadPerformanceMode()) {
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                } else {
                    overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
                }
                return true
            }
            R.id.history -> {
                val intent = Intent(this, HistoryActivity::class.java)
                startActivity(intent)
                if (!PerformanceModeData(this).loadPerformanceMode()) {
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                } else {
                    overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
                }
                return true
            }
            R.id.trash -> {
                val intent = Intent(this, TrashActivity::class.java)
                startActivity(intent)
                if (!PerformanceModeData(this).loadPerformanceMode()) {
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                } else {
                    overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
                }
                return true
            }
            R.id.graph -> {
                val intent = Intent(this, GraphActivity::class.java)
                startActivity(intent)
                if (!PerformanceModeData(this).loadPerformanceMode()) {
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                } else {
                    overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
                }
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}