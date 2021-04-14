package com.cory.hourcalculator.activities

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
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
import java.lang.Exception
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

class MainActivity : AppCompatActivity() {

   // private lateinit var vibrationData: VibrationData
    val vibrationData by lazy { VibrationData(this) }
    private lateinit var darkThemeData: DarkThemeData
    private lateinit var historyToggleData: HistoryToggleData
    private lateinit var updateData: UpdateData
    private lateinit var trashAutomaticDeletion: TrashAutomaticDeletion
    var testDeviceId = listOf("8EDC43FD82F98F52B4B982B33812B1BC")
    private val dbHandler = DBHelper(this, null)
    private val permissionRequestCode = 1
    private lateinit var managePermissions: ManagePermissions

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    // Spinner lazy and lateinit inializers
    val spinner by lazy { findViewById<MaterialSpinner>(R.id.material_spinner_1) }
    val spinner1 by lazy { findViewById<MaterialSpinner>(R.id.material_spinner_2) }
    private lateinit var spinner1selecteditem: String
    private lateinit var spinner2selecteditem: String

    // Adview lazy initializer
    val adView by lazy { AdView(this) }
    val mAdView by lazy { findViewById<AdView>(R.id.adView) }
    val adRequest by lazy { AdRequest.Builder().build() }

    // Break data lazy initializer
    val breakData by lazy { BreakData(this) }

    // Input manager lazy initializer
    val imm by lazy { this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }

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

        if (breakData.loadBreakState() == false) {
            findViewById<TextView>(R.id.textView4).visibility = View.GONE
            findViewById<TextInputLayout>(R.id.textInputLayout3).visibility = View.GONE
            findViewById<TextInputEditText>(R.id.breakTime).visibility = View.GONE
        }
        else if (breakData.loadBreakState() == true) {
            findViewById<TextView>(R.id.textView4).visibility = View.VISIBLE
            findViewById<TextInputLayout>(R.id.textInputLayout3).visibility = View.VISIBLE
            findViewById<TextInputEditText>(R.id.breakTime).visibility = View.VISIBLE
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

        //spinner = findViewById<MaterialSpinner>(R.id.material_spinner_1)
        //spinner1 = findViewById<MaterialSpinner>(R.id.material_spinner_2)

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
            //val str = inTime.text.toString()
            //val str1 = outTime.text.toString()
            if (i == KeyEvent.KEYCODE_BACK && keyEvent.action == KeyEvent.ACTION_DOWN) {
                outTime.clearFocus()
            }
            if (i == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_UP) {
                vibration(vibrationData)
                //if(textInputLayout3.visibility != View.VISIBLE) {
                    //hideKeyboard()
                //}
                if (textInputLayout3.visibility == View.VISIBLE) {
                    //Toast.makeText(this, "It is visible", Toast.LENGTH_SHORT).show()
                        Log.i("Break", "It is visible")
                    //breakTime.requestFocus()
                    /*window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
                    val imm1: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm1.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)*/
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
            val str = inTime.text.toString()
            val str1 = outTime.text.toString()
            if (i == KeyEvent.KEYCODE_BACK && keyEvent.action == KeyEvent.ACTION_DOWN) {
                //outTime.clearFocus()
                breakTime.clearFocus()
            }
            if (i == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_UP) {
                vibration(vibrationData)

                hideKeyboard()
                validation(str, str1, spinner1selecteditem, spinner2selecteditem, infoTextView1)
                return@OnKeyListener true
            }
            false
        })

        calculateButton1.setOnClickListener {
            val str = inTime.text.toString()
            val str1 = outTime.text.toString()
            vibration(vibrationData)

            validation(str, str1, spinner1selecteditem, spinner2selecteditem, infoTextView1)

            hideKeyboard()
        }

        btnClear.setOnClickListener {
            vibration(vibrationData)

            clearTextBoxes(inTime, outTime, breakTime)
        }
    }

    fun clearTextBoxes(inTime: EditText, outTime: EditText, breakTime: EditText) {
        if (inTime.text.toString() == "" && outTime.text.toString() == "" && breakTime.text.toString() == "" && infoTextView1.visibility == View.INVISIBLE) {
            Toast.makeText(this, getString(R.string.everything_already_cleared), Toast.LENGTH_SHORT).show()
        } else {
            infoTextView1.visibility = View.INVISIBLE
            inTime.text?.clear()
            outTime.text?.clear()
            breakTime.text?.clear()
            inTime.requestFocus()
            val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(inTime, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    fun savingHours(totalHours3: Double, inTime: EditText, outTime: EditText, breakTime: EditText) {
        val intime = inTime.text.toString()
        val out = outTime.text.toString()
        var break1 = breakTime.text.toString()
        if (breakTime.text.toString() == "") {
            break1 = getString(R.string.break_zero)
        }
        val total = totalHours3.toString()
        val day = LocalDateTime.now()
        val day2 = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        val dayOfWeek = day.format(day2)
        dbHandler.insertRow(intime, out, break1, total, dayOfWeek)
    }

    fun savingHours2(totalHours2: Double, inTime: EditText, outTime: EditText, breakTime: EditText) {
        val intime = inTime.text.toString()
        val out = outTime.text.toString()
        var break1 = breakTime.text.toString()
        if (breakTime.text.toString() == "") {
            break1 = "0"
        }
        val total = totalHours2.toString()
        val day = LocalDateTime.now()
        val day2 = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        val dayOfWeek = day.format(day2)
        dbHandler.insertRow(intime, out, break1, total, dayOfWeek)
    }

    fun validation(str: String, str1: String, spinner1selecteditem: String, spinner2selecteditem: String, infoTextView1: TextView) {
        if (inTime.text.toString().contains(",")) {
            infoTextView1.visibility = View.VISIBLE
            infoTextView1.text = getString(R.string.theres_a_comman_in_text_box)
        } else if (outTime.text.toString().contains(",")) {
            infoTextView1.visibility = View.VISIBLE
            infoTextView1.text = getString(R.string.theres_a_comman_in_text_box)
        }
        if (spinner1selecteditem == getString(R.string.am) && spinner2selecteditem == getString(R.string.am)) {
            if (str.length == 2 || str1.length == 2) {
                infoTextView1.visibility = View.VISIBLE
                infoTextView1.text = getString(R.string.proper_input)
            }
            if (inTime.text.toString() == "" || outTime.text.toString() == "" || !inTime.hasFocus() || !outTime.hasFocus()) {
                infoTextView1.visibility = View.VISIBLE
                infoTextView1.text = getString(R.string.dont_leave_anything_blank)
            }
            if (!str.contains(":") && !str1.contains(":")) {
                if (str.length == 3 && str1.length == 3) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = str.drop(1)
                        val intimelast = str.dropLast(2)
                        val outtime = str1.drop(1)
                        val outtimelast = str1.dropLast(2)
                        outTime.setText("$outtimelast:$outtime")
                        inTime.setText("$intimelast:$intime")
                        val (separate1, separate2) = inTime.text.toString().split(":")
                        val (separate3, separate4) = outTime.text.toString().split(":")
                        if (separate2 == "" || separate4 == "") {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (separate2.toDouble() >= 60 || separate4.toDouble() >= 60) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (separate1.toDouble() >= 13 || separate3.toDouble() >= 13) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandAMandPMandPM(separate1, separate2, separate3, separate4, infoTextView1, breakTime)
                        }
                    }
                } else if (str.length == 3 && str1.length == 4) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = str.drop(2)
                        val intimelast = str.dropLast(1)
                        val outtime = str1.drop(2)
                        val outtimelast = str1.dropLast(2)
                        inTime.setText("$intime:$intimelast")
                        outTime.setText("$outtimelast:$outtime")
                        val (separate1, separate2) = inTime.text.toString().split(":")
                        val (separate3, separate4) = outTime.text.toString().split(":")
                        if (separate2 == "" || separate4 == "") {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (separate2.toDouble() >= 60 || separate4.toDouble() >= 60) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (separate1.toDouble() >= 13 || separate3.toDouble() >= 13) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandAMandPMandPM(separate1, separate2, separate3, separate4, infoTextView1, breakTime)
                        }
                    }
                } else if (str.length == 4 && str1.length == 3) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = str.drop(2)
                        val intimelast = str.dropLast(2)
                        val outtime = str1.drop(2)
                        val outtimelast = str1.dropLast(1)
                        inTime.setText("$intimelast:$intime")
                        outTime.setText("$outtime:$outtimelast")
                        val (separate1, separate2) = inTime.text.toString().split(":")
                        val (separate3, separate4) = outTime.text.toString().split(":")
                        if (separate2 == "" || separate4 == "") {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (separate2.toDouble() >= 60 || separate4.toDouble() >= 60) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (separate1.toDouble() >= 13 || separate3.toDouble() >= 13) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            val conv = separate2.toDouble()
                            val conv1 = separate1.toDouble()
                            val conv2 = separate3.toDouble()
                            val conv3 = separate4.toDouble()
                            val div = conv / 60
                            val div1 = conv3 / 60
                            val rounded = div.toBigDecimal().setScale(3, RoundingMode.HALF_EVEN).toString()
                            val rounded1 = div1.toBigDecimal().setScale(3, RoundingMode.HALF_EVEN).toString()
                            val s2 = rounded1.substring(1)
                            val s1 = rounded.substring(1)
                            val s3 = s1.toDouble()
                            val s5 = s2.toDouble()
                            val total1 = conv1 + s3
                            val total2 = conv2 + s5
                            val difference = total2 - total1
                            val totalhours = String.format("%.2f", difference).toDouble()
                            if (totalhours < 0) {
                                infoTextView1.visibility = View.VISIBLE
                                infoTextView1.text = getString(R.string.in_time_can_not_be_greater_than_out_time)
                            } else {
                                aMandAMandPMandPM(separate1, separate2, separate3, separate4, infoTextView1, breakTime)
                            }
                        }
                    }
                } else if (str.length == 4 && str1.length == 4) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = str.drop(2)
                        val intimelast = str.dropLast(2)
                        val outtime = str1.drop(2)
                        val outtimelast = str1.dropLast(2)
                        inTime.setText("$intimelast:$intime")
                        outTime.setText("$outtimelast:$outtime")
                        val (separate1, separate2) = inTime.text.toString().split(":")
                        val (separate3, separate4) = outTime.text.toString().split(":")
                        if (separate2 == "" || separate4 == "") {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (separate2.toDouble() >= 60 || separate4.toDouble() >= 60) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (separate1.toDouble() >= 13 || separate3.toDouble() >= 13) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandAMandPMandPM(separate1, separate2, separate3, separate4, infoTextView1, breakTime)
                        }
                    }
                }
            }
            if (!str.contains(":") && str1.contains(":")) {
                if (str.length == 3) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = str.drop(1)
                        val intimelast = str.dropLast(2)
                        inTime.setText("$intimelast:$intime")
                        val (separate1, separate2) = inTime.text.toString().split(":")
                        val (separate3, separate4) = outTime.text.toString().split(":")
                        if (separate2 == "" || separate4 == "") {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (separate2.toDouble() >= 60 || separate4.toDouble() >= 60) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (separate1.toDouble() >= 13 || separate3.toDouble() >= 13) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandAMandPMandPM(separate1, separate2, separate3, separate4, infoTextView1, breakTime)
                        }
                    }
                } else if (str.length == 4) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = str.drop(2)
                        val intimelast = str.dropLast(2)
                        inTime.setText("$intimelast:$intime")
                        val (separate1, separate2) = inTime.text.toString().split(":")
                        val (separate3, separate4) = outTime.text.toString().split(":")
                        if (separate2 == "" || separate4 == "") {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (separate1.toDouble() >= 13 || separate3.toDouble() >= 13) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else if (separate2.toDouble() >= 60 || separate4.toDouble() >= 60) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else {
                            aMandAMandPMandPM(separate1, separate2, separate3, separate4, infoTextView1, breakTime)
                        }
                    }
                }
            }
            if (str.contains(":") && !str1.contains(":")) {
                if (str1.length == 3) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val outtime = str1.drop(1)
                        val outtimelast = str1.dropLast(2)
                        outTime.setText("$outtimelast:$outtime")
                        val (separate1, separate2) = inTime.text.toString().split(":")
                        val (separate3, separate4) = outTime.text.toString().split(":")
                        if (separate2 == "" || separate4 == "") {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (separate2.toDouble() >= 60 || separate4.toDouble() >= 60) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (separate1.toDouble() >= 13 || separate3.toDouble() >= 13) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandAMandPMandPM(separate1, separate2, separate3, separate4, infoTextView1, breakTime)
                        }
                    }
                } else if (str1.length == 4) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val outtime = str1.drop(2)
                        val outtimelast = str1.dropLast(2)
                        outTime.setText("$outtimelast:$outtime")
                        val (separate1, separate2) = inTime.text.toString().split(":")
                        val (separate3, separate4) = outTime.text.toString().split(":")
                        if (separate2 == "" || separate4 == "") {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (separate2.toDouble() >= 60 || separate4.toDouble() >= 60) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (separate1.toDouble() >= 13 || separate3.toDouble() >= 13) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandAMandPMandPM(separate1, separate2, separate3, separate4, infoTextView1, breakTime)
                        }
                    }
                }
            } else if (str.contains(":") && str1.contains(":")) {
                val (separate1, separate2) = inTime.text.toString().split(":")
                val (separate3, separate4) = outTime.text.toString().split(":")
                if (separate2 == "" || separate4 == "") {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.proper_input)
                } else if (separate2.toDouble() >= 60 || separate4.toDouble() >= 60) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                } else if (separate1.toDouble() >= 13 || separate3.toDouble() >= 13) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                } else {
                    aMandAMandPMandPM(separate1, separate2, separate3, separate4, infoTextView1, breakTime)
                }
            }
        }
        if (spinner1selecteditem == getString(R.string.pm) && spinner2selecteditem == getString(R.string.pm)) {
            if (str.length <= 2 || str1.length <= 2) {
                infoTextView1.visibility = View.VISIBLE
                infoTextView1.text = getString(R.string.proper_input)
            }
            if (inTime.text.toString() == "" || outTime.text.toString() == "" || !inTime.hasFocus() || !outTime.hasFocus()) {
                infoTextView1.visibility = View.VISIBLE
                infoTextView1.text = getString(R.string.dont_leave_anything_blank)
            }
            if (!str.contains(":") && !str1.contains(":")) {
                if (str.length == 3 && str1.length == 3) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = str.drop(1)
                        val intimelast = str.dropLast(2)
                        val outtime = str1.drop(1)
                        val outtimelast = str1.dropLast(2)
                        outTime.setText("$outtimelast:$outtime")
                        inTime.setText("$intimelast:$intime")
                        val (separate1, separate2) = inTime.text.toString().split(":")
                        val (separate3, separate4) = outTime.text.toString().split(":")
                        if (separate2 == "" || separate4 == "") {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (separate2.toDouble() >= 60 || separate4.toDouble() >= 60) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (separate1.toDouble() >= 13 || separate3.toDouble() >= 13) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandAMandPMandPM(separate1, separate2, separate3, separate4, infoTextView1, breakTime)
                        }
                    }
                } else if (str.length == 3 && str1.length == 4) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = str.drop(2)
                        val intimelast = str.dropLast(1)
                        val outtime = str1.drop(2)
                        val outtimelast = str1.dropLast(2)
                        inTime.setText("$intime:$intimelast")
                        outTime.setText("$outtimelast:$outtime")
                        val (separate1, separate2) = inTime.text.toString().split(":")
                        val (separate3, separate4) = outTime.text.toString().split(":")
                        if (separate2 == "" || separate4 == "") {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (separate2.toDouble() >= 60 || separate4.toDouble() >= 60) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (separate1.toDouble() >= 13 || separate3.toDouble() >= 13) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandAMandPMandPM(separate1, separate2, separate3, separate4, infoTextView1, breakTime)
                        }
                    }
                } else if (str.length == 4 && str1.length == 3) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = str.drop(2)
                        val intimelast = str.dropLast(2)
                        val outtime = str1.drop(2)
                        val outtimelast = str1.dropLast(1)
                        inTime.setText("$intimelast:$intime")
                        outTime.setText("$outtime:$outtimelast")
                        val (separate1, separate2) = inTime.text.toString().split(":")
                        val (separate3, separate4) = outTime.text.toString().split(":")
                        if (separate2 == "" || separate4 == "") {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (separate2.toDouble() >= 60 || separate4.toDouble() >= 60) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (separate1.toDouble() >= 13 || separate3.toDouble() >= 13) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandAMandPMandPM(separate1, separate2, separate3, separate4, infoTextView1, breakTime)
                        }
                    }
                } else if (str.length == 4 && str1.length == 4) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = str.drop(2)
                        val intimelast = str.dropLast(2)
                        val outtime = str1.drop(2)
                        val outtimelast = str1.dropLast(2)
                        inTime.setText("$intimelast:$intime")
                        outTime.setText("$outtimelast:$outtime")
                        val (separate1, separate2) = inTime.text.toString().split(":")
                        val (separate3, separate4) = outTime.text.toString().split(":")
                        if (separate2 == "" || separate4 == "") {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (separate2.toDouble() >= 60 || separate4.toDouble() >= 60) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (separate1.toDouble() >= 13 || separate3.toDouble() >= 13) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandAMandPMandPM(separate1, separate2, separate3, separate4, infoTextView1, breakTime)
                        }
                    }
                }
            }
            if (!str.contains(":") && str1.contains(":")) {
                if (str.length == 3) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = str.drop(1)
                        val intimelast = str.dropLast(2)
                        inTime.setText("$intimelast:$intime")
                        val (separate1, separate2) = inTime.text.toString().split(":")
                        val (separate3, separate4) = outTime.text.toString().split(":")
                        if (separate2 == "" || separate4 == "") {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (separate2.toDouble() >= 60 || separate4.toDouble() >= 60) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (separate1.toDouble() >= 13 || separate3.toDouble() >= 13) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandAMandPMandPM(separate1, separate2, separate3, separate4, infoTextView1, breakTime)
                        }
                    }
                } else if (str.length == 4) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = str.drop(2)
                        val intimelast = str.dropLast(2)
                        inTime.setText("$intimelast:$intime")
                        val (separate1, separate2) = inTime.text.toString().split(":")
                        val (separate3, separate4) = outTime.text.toString().split(":")
                        if (separate2 == "" || separate4 == "") {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (separate1.toDouble() >= 13 || separate3.toDouble() >= 13) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else if (separate2.toDouble() >= 60 || separate4.toDouble() >= 60) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else {
                            aMandAMandPMandPM(separate1, separate2, separate3, separate4, infoTextView1, breakTime)
                        }
                    }
                }
            }
            if (str.contains(":") && !str1.contains(":")) {
                if (str1.length == 3) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val outtime = str1.drop(1)
                        val outtimelast = str1.dropLast(2)
                        outTime.setText("$outtimelast:$outtime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                } else if (str1.length == 4) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val outtime = str1.drop(2)
                        val outtimelast = str1.dropLast(2)
                        outTime.setText("$outtimelast:$outtime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                }
            } else if (str.contains(":") && str1.contains(":")) {
                val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                if (inTimeMinutes == "" || outTimeMinutes == "") {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.proper_input)
                } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                } else {
                    aMandAMandPMandPM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                }
            }
        }
        if (spinner1selecteditem == getString(R.string.am) && spinner2selecteditem == getString(R.string.pm)) {
            if (str.length == 2 || str1.length == 2) {
                infoTextView1.visibility = View.VISIBLE
                infoTextView1.text = getString(R.string.proper_input)
            }
            if (str == "" || str1 == "") {
                infoTextView1.visibility = View.VISIBLE
                infoTextView1.text = getString(R.string.dont_leave_anything_blank)
            }
            if (!str.contains(":") && !str1.contains(":")) {
                if (str.length == 3 && str1.length == 3) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = str.drop(1)
                        val intimelast = str.dropLast(2)
                        val outtime = str1.drop(1)
                        val outtimelast = str1.dropLast(2)
                        outTime.setText("$outtimelast:$outtime")
                        inTime.setText("$intimelast:$intime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || inTimeMinutes == "") {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else {
                            aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                } else if (str.length == 3 && str1.length == 4) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = str.drop(2)
                        val intimelast = str.dropLast(1)
                        val outtime = str1.drop(2)
                        val outtimelast = str1.dropLast(2)
                        inTime.setText("$intime:$intimelast")
                        outTime.setText("$outtimelast:$outtime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else {
                            aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                } else if (str.length == 4 && str1.length == 3) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = str.drop(2)
                        val intimelast = str.dropLast(2)
                        val outtime = str1.drop(2)
                        val outtimelast = str1.dropLast(1)
                        inTime.setText("$intimelast:$intime")
                        outTime.setText("$outtime:$outtimelast")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                } else if (str.length == 4 && str1.length == 4) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = str.drop(2)
                        val intimelast = str.dropLast(2)
                        val outtime = str1.drop(2)
                        val outtimelast = str1.dropLast(2)
                        inTime.setText("$intimelast:$intime")
                        outTime.setText("$outtimelast:$outtime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                }
            }
            if (!str.contains(":") && str1.contains(":")) {
                if (str.length == 3) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = str.drop(1)
                        val intimelast = str.dropLast(2)
                        inTime.setText("$intimelast:$intime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                } else if (str.length == 4) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = str.drop(2)
                        val intimelast = str.dropLast(2)
                        inTime.setText("$intimelast:$intime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                }
            }
            if (str.contains(":") && !str1.contains(":")) {
                if (str1.length == 3) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val outtime = str1.drop(1)
                        val outtimelast = str1.dropLast(2)
                        outTime.setText("$outtimelast:$outtime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                } else if (str1.length == 4) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val outtime = str1.drop(2)
                        val outtimelast = str1.dropLast(2)
                        outTime.setText("$outtimelast:$outtime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                }
            } else if (str.contains(":") && str1.contains(":")) {
                val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                if (inTimeMinutes == "" || outTimeMinutes == "") {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.proper_input)
                } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                } else {
                    aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                }
            }
        }
        if (spinner1selecteditem == getString(R.string.pm) && spinner2selecteditem == getString(R.string.am)) {
            if (str.length == 2 || str1.length == 2) {
                infoTextView1.visibility = View.VISIBLE
                infoTextView1.text = getString(R.string.proper_input)
            }
            if (str == "" || str1 == "") {
                infoTextView1.visibility = View.VISIBLE
                infoTextView1.text = getString(R.string.dont_leave_anything_blank)
            }
            if (!str.contains(":") && !str1.contains(":")) {
                if (str.length == 3 && str1.length == 3) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = str.drop(1)
                        val intimelast = str.dropLast(2)
                        val outtime = str1.drop(1)
                        val outtimelast = str1.dropLast(2)
                        outTime.setText("$outtimelast:$outtime")
                        inTime.setText("$intimelast:$intime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else {
                            aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                } else if (str.length == 3 && str1.length == 4) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = str.drop(2)
                        val intimelast = str.dropLast(1)
                        val outtime = str1.drop(2)
                        val outtimelast = str1.dropLast(2)
                        inTime.setText("$intime:$intimelast")
                        outTime.setText("$outtimelast:$outtime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else {
                            aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                } else if (str.length == 4 && str1.length == 3) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = str.drop(2)
                        val intimelast = str.dropLast(2)
                        val outtime = str1.drop(2)
                        val outtimelast = str1.dropLast(1)
                        inTime.setText("$intimelast:$intime")
                        outTime.setText("$outtime:$outtimelast")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                } else if (str.length == 4 && str1.length == 4) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = str.drop(2)
                        val intimelast = str.dropLast(2)
                        val outtime = str1.drop(2)
                        val outtimelast = str1.dropLast(2)
                        inTime.setText("$intimelast:$intime")
                        outTime.setText("$outtimelast:$outtime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                }
            }
            if (!str.contains(":") && str1.contains(":")) {
                if (str.length == 3) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = str.drop(1)
                        val intimelast = str.dropLast(2)
                        inTime.setText("$intimelast:$intime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                } else if (str.length == 4) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val intime = str.drop(2)
                        val intimelast = str.dropLast(2)
                        inTime.setText("$intimelast:$intime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                }
            }
            if (str.contains(":") && !str1.contains(":")) {
                if (str1.length == 3) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val outtime = str1.drop(1)
                        val outtimelast = str1.dropLast(2)
                        outTime.setText("$outtimelast:$outtime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                } else if (str1.length == 4) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.no_colon)
                    infoTextView1.setOnClickListener {
                        val outtime = str1.drop(2)
                        val outtimelast = str1.dropLast(2)
                        outTime.setText("$outtimelast:$outtime")
                        val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                        val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                        if (inTimeMinutes == "" || outTimeMinutes == "") {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.proper_input)
                        } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                        }
                    }
                }
            } else if (str.contains(":") && str1.contains(":")) {
                val (inTimeHours, inTimeMinutes) = inTime.text.toString().split(":")
                val (outTimeHours, outTimeMinutes) = outTime.text.toString().split(":")
                if (inTimeMinutes == "" || outTimeMinutes == "") {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.proper_input)
                } else if (inTimeMinutes.toDouble() >= 60 || outTimeMinutes.toDouble() >= 60) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                } else if (inTimeHours.toDouble() >= 13 || outTimeHours.toDouble() >= 13) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                } else {
                    aMandPMandPMandAM(inTimeHours, inTimeMinutes, outTimeHours, outTimeMinutes, infoTextView1, breakTime)
                }
            }
        }
    }

    fun aMandAMandPMandPM(inTimeHours: String, inTimeMinutes: String, outTimeHours: String, outTimeMinutes: String, infoTextView1: TextView, breakTime: EditText) {
        val historyToggleData = HistoryToggleData(this)
        //val separate2.toDouble() = separate2.toDouble()
        //val conv1 = inTimeHours.toDouble()
        //val conv2 = outTimeHours.toDouble()
        //val conv3 = separate4.toDouble()
        //val div = separate2.toDouble() / 60
        //val div1 = separate4.toDouble() / 60
        val inTimeMinutesRounded = (inTimeMinutes.toDouble() / 60).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toString()
        val outTimeMinutesRounded = (outTimeMinutes.toDouble() / 60).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toString()
        //val s2 = outTimeMinutesRounded.substring(1)
        //val s1 = inTimeMinutesRounded.substring(1)
        //val s3 = s1.toDouble()
        //val s5 = s2.toDouble()
        val inTimeTotal = inTimeHours.toDouble() + inTimeMinutesRounded.substring(1).toDouble()
        val outTimeTotal = outTimeHours.toDouble() + outTimeMinutesRounded.substring(1).toDouble()
        val difference = outTimeTotal - inTimeTotal
        val totalhours = String.format("%.2f", difference).toDouble()
        //val totalhours3 = String.format("%.2f", totalhours).toDouble()
        if (totalhours < 0) {
            infoTextView1.visibility = View.VISIBLE
            infoTextView1.text = getString(R.string.in_time_can_not_be_greater_than_out_time)
        } else {
            if (breakTime.text.toString() == "") {
                infoTextView1.visibility = View.VISIBLE
                infoTextView1.text = getString(R.string.total_hours, totalhours.toString())
                if (historyToggleData.loadHistoryState() == true) {
                    savingHours(totalhours, inTime, outTime, breakTime)
                }
            } else if (breakTime.text.toString() != "") {
                //val breakTimeInt = breakTime.text.toString().toDouble()
                    if(!breakTime.text.isDigitsOnly()) {
                        infoTextView1.text = getString(R.string.something_wrong_with_break_text_box)
                        infoTextView1.visibility = View.VISIBLE
                    }
                else {
                        val breakTimeDec: Double = (breakTime.text.toString().toDouble() / 60).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toString().toDouble()
                        //val break1 = breakTimeDec.toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toString().toDouble()
                        val totalHours1 = totalhours - breakTimeDec
                        val totalHoursWithBreak = String.format("%.2f", totalHours1).toDouble()
                        if (totalHoursWithBreak < 0) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.in_time_can_not_be_greater_than_out_time)
                        } else if (totalHoursWithBreak > 0) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.total_hours_with_and_without_break, totalHoursWithBreak.toString(), totalhours.toString())
                            if (historyToggleData.loadHistoryState() == true) {
                                savingHours2(totalHoursWithBreak, inTime, outTime, breakTime)
                            }
                        }
                    }
            }
        }
    }

    fun aMandPMandPMandAM(inTimeHours: String, inTimeMinutes: String, outTimeHours: String, outTimeMinutes: String, infoTextView1: TextView, breakTime: EditText) {
        val historyToggleData = HistoryToggleData(this)
        //val separate2.toDouble() = separate2.toDouble()
        //val conv1 = inTimeHours.toDouble()
        //val conv2 = outTimeHours.toDouble()
        //val conv3 = separate4.toDouble()
        //val div = separate2.toDouble() / 60
        //val div1 = separate4.toDouble() / 60
        val inTimeMinutesRounded = (inTimeMinutes.toDouble() / 60).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toString()
        val outTimeMinutesRounded = (outTimeMinutes.toDouble() / 60).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toString()
        //val s2 = outTimeMinutesRounded.substring(1)
        //val s1 = inTimeMinutesRounded.substring(1)
        //val s3 = s1.toDouble()
        //val s5 = s2.toDouble()
        val inTimeTotal = inTimeHours.toDouble() + inTimeMinutesRounded.substring(1).toDouble()
        val outTimeTotal = outTimeHours.toDouble() + outTimeMinutesRounded.substring(1).toDouble()
        val difference = outTimeTotal - inTimeTotal
        val totalhours = String.format("%.2f", difference).toDouble() + 12
        if (totalhours < 0) {
            infoTextView1.visibility = View.VISIBLE
            infoTextView1.text = getString(R.string.in_time_can_not_be_greater_than_out_time)
        } else {
            if (breakTime.text.toString() == "") {
                infoTextView1.visibility = View.VISIBLE
                infoTextView1.text = getString(R.string.total_hours, totalhours.toString())
                if (historyToggleData.loadHistoryState() == true) {
                    savingHours(totalhours, inTime, outTime, breakTime)
                }
            } else if (breakTime.text.toString() != "") {
                //val breakTimeInt = breakTime.text.toString().toDouble()
                    if(!breakTime.text.toString().isDigitsOnly()) {
                        infoTextView1.text = getString(R.string.something_wrong_with_break_text_box)
                        infoTextView1.visibility = View.VISIBLE
                    }
                else {
                        val breakTimeDec: Double = (breakTime.text.toString().toDouble() / 60).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toString().toDouble()
                        //val break1 = breakTimeDec.toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toString().toDouble()
                        val totalHours1 = totalhours - breakTimeDec
                        val totalHoursWithBreak = String.format("%.2f", totalHours1).toDouble()
                        if (totalHours1 < 0) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.in_time_can_not_be_greater_than_out_time)
                        } else if (totalHours1 > 0) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.total_hours_with_and_without_break, totalHours1.toString(), totalhours.toString())
                            if (historyToggleData.loadHistoryState() == true) {
                                savingHours2(totalHours1, inTime, outTime, breakTime)
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
            if (inTime!!.hasFocus()) {
                inTime!!.clearFocus()
            } else if (outTime!!.hasFocus()) {
                outTime!!.clearFocus()
            } else if (breakTime!!.hasFocus()) {
                breakTime!!.clearFocus()
            }
        }
    }

    fun vibration(vibrationData: VibrationData) {
        if (vibrationData.loadVibrationState() == true) {
            val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
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
        if (historyToggleData.loadHistoryState() == false) {
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