package com.cory.hourcalculator.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Color
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.*
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.view.animation.BounceInterpolator
import android.view.animation.TranslateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.documentfile.provider.DocumentFile
import com.cory.hourcalculator.R
import com.cory.hourcalculator.classes.*
import com.cory.hourcalculator.database.DBHelper
import com.google.android.gms.ads.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.api.ResourceDescriptor
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.messaging.ktx.remoteMessage
import com.jaredrummler.materialspinner.MaterialSpinner
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okio.Utf8
import java.io.*
import java.lang.Exception
import java.math.RoundingMode
import java.net.URI
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {

   // private lateinit var vibrationData: VibrationData
    val vibrationData by lazy { VibrationData(this) }
    private lateinit var darkThemeData: DarkThemeData
    private lateinit var firebaseData: FirebaseData
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

    // Shortcut manager lazy initialization
    val shortcutManager by lazy { getSystemService(ShortcutManager::class.java) }
    val dynamicIntent by lazy { Intent(this, HistoryActivity::class.java) }
    val dynamicIntent2 by lazy { Intent(this, GraphActivity::class.java) }
    val dynamicIntent3 by lazy { Intent(this, SettingsActivity::class.java) }
    val dynamicIntent4 by lazy { Intent(this, TrashActivity::class.java) }
    val shortcut by lazy { ShortcutInfo.Builder(this, "dynamic_shortcut")
        .setLongLabel(getString(R.string.history_shortcut_long))
        .setShortLabel(getString(R.string.history_shortcut_short))
        .setIcon(Icon.createWithResource(applicationContext, R.drawable.ic_history_24px))
        .setIntent(dynamicIntent)
        .build() }
    val shortcut2 by lazy { ShortcutInfo.Builder(this, "dynamic_shortcut2")
        .setLongLabel(getString(R.string.graph_shortcut_long))
        .setShortLabel(getString(R.string.graph_shortcut_short))
        .setIcon(Icon.createWithResource(applicationContext, R.drawable.ic_bar_chart_24px))
        .setIntent(dynamicIntent2)
        .build()
    }
    val shortcut3 by lazy { ShortcutInfo.Builder(this, "dynamic_shortcut3")
        .setLongLabel(getString(R.string.settings_shortcut_long))
        .setShortLabel(getString(R.string.settings_shortcut_short))
        .setIcon(Icon.createWithResource(applicationContext, R.drawable.ic_settings_24px))
        .setIntent(dynamicIntent3)
        .build()
    }
    val shortcut4 by lazy { ShortcutInfo.Builder(this, "dynamic_shortcut4")
        .setLongLabel(getString(R.string.trash_shortcut_long))
        .setShortLabel(getString(R.string.trash_shortcut_short))
        .setIcon(Icon.createWithResource(applicationContext, R.drawable.ic_delete_24px))
        .setIntent(dynamicIntent4)
        .build()
    }

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
        setContentView(R.layout.activity_main)

        window.setBackgroundDrawable(null)

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

        requestFocus()

        GlobalScope.launch(Dispatchers.Main) {
            main()
        }

        //val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm.isAcceptingText) {
            mAdView.visibility = View.INVISIBLE
        }

        //val breakData = BreakData(this)
        if (breakData.loadBreakState() == false) {
            findViewById<TextView>(R.id.textView4).visibility = View.GONE
            findViewById<TextInputLayout>(R.id.textInputLayout3).visibility = View.GONE
            findViewById<TextInputEditText>(R.id.breakTime).visibility = View.GONE
        }
        if (breakData.loadBreakState() == true) {
            findViewById<TextView>(R.id.textView4).visibility = View.VISIBLE
            findViewById<TextInputLayout>(R.id.textInputLayout3).visibility = View.VISIBLE
            findViewById<TextInputEditText>(R.id.breakTime).visibility = View.VISIBLE
        }

        try {
            //val shortcutManager = getSystemService(ShortcutManager::class.java)

            //val dynamicIntent = Intent(this, HistoryActivity::class.java)
            dynamicIntent.setAction(Intent.ACTION_VIEW)

            //val dynamicIntent2 = Intent(this, GraphActivity::class.java)
            dynamicIntent2.setAction(Intent.ACTION_VIEW)

            //val dynamicIntent3 = Intent(this, SettingsActivity::class.java)
            dynamicIntent3.setAction(Intent.ACTION_VIEW)

           // val dynamicIntent4 = Intent(this, TrashActivity::class.java)
            dynamicIntent4.setAction(Intent.ACTION_VIEW)

            /*val shortcut = ShortcutInfo.Builder(this, "dynamic_shortcut")
                .setLongLabel(getString(R.string.history_shortcut_long))
                .setShortLabel(getString(R.string.history_shortcut_short))
                .setIcon(Icon.createWithResource(applicationContext, R.drawable.ic_history_24px))
                .setIntent(dynamicIntent)
                .build()

            val shortcut2 = ShortcutInfo.Builder(this, "dynamic_shortcut2")
                .setLongLabel(getString(R.string.graph_shortcut_long))
                .setShortLabel(getString(R.string.graph_shortcut_short))
                .setIcon(Icon.createWithResource(applicationContext, R.drawable.ic_bar_chart_24px))
                .setIntent(dynamicIntent2)
                .build()

            val shortcut3 = ShortcutInfo.Builder(this, "dynamic_shortcut3")
                .setLongLabel(getString(R.string.settings_shortcut_long))
                .setShortLabel(getString(R.string.settings_shortcut_short))
                .setIcon(Icon.createWithResource(applicationContext, R.drawable.ic_settings_24px))
                .setIntent(dynamicIntent3)
                .build()

            val shortcut4 = ShortcutInfo.Builder(this, "dynamic_shortcut4")
                .setLongLabel(getString(R.string.trash_shortcut_long))
                .setShortLabel(getString(R.string.trash_shortcut_short))
                .setIcon(Icon.createWithResource(applicationContext, R.drawable.ic_delete_24px))
                .setIntent(dynamicIntent4)
                .build()*/


            shortcutManager.dynamicShortcuts = listOf(shortcut, shortcut2, shortcut3, shortcut4)
            shortcutManager.disableShortcuts(Arrays.asList(shortcut.id, shortcut2.id, shortcut3.id, shortcut4.id))
        } catch (exception: Exception) {
            Log.i("MainActivity", "Couldnt Load Shortcuts")
        }

        //main()

        firebaseData = FirebaseData(this)
        if (firebaseData.loadFirebaseLog() == false) {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "break_enabled")
                param(FirebaseAnalytics.Param.ITEM_NAME, "is_break_enabled")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "class")
                param(FirebaseAnalytics.Param.CONTENT, breakData.loadBreakState().toString())
            }
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "dark_enabled")
                param(FirebaseAnalytics.Param.ITEM_NAME, "is_dark_enabled")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "class")
                param(FirebaseAnalytics.Param.CONTENT, darkThemeData.loadDarkModeState().toString())
            }
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "history_enabled")
                param(FirebaseAnalytics.Param.ITEM_NAME, "is_history_enabled")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "class")
                param(FirebaseAnalytics.Param.CONTENT, historyToggleData.loadHistoryState().toString())
            }
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "trash_deletion_enabled")
                param(FirebaseAnalytics.Param.ITEM_NAME, "is_deletion_enabled")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "class")
                param(FirebaseAnalytics.Param.CONTENT, trashAutomaticDeletion.loadTrashDeletionState().toString())
            }
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "update_notification_enabled")
                param(FirebaseAnalytics.Param.ITEM_NAME, "is_update_notification_enabled")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "class")
                param(FirebaseAnalytics.Param.CONTENT, updateData.loadUpdateNotificationState().toString())
            }
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "vibration_enabled")
                param(FirebaseAnalytics.Param.ITEM_NAME, "is_vibration_enabled")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "class")
                param(FirebaseAnalytics.Param.CONTENT, vibrationData.loadVibrationState().toString())
            }
            firebaseData.setFirebaseLog(true)
        }
    }

    override fun onRestart() {
        super.onRestart()
        recreate()
    }

    override fun onResume() {
        super.onResume()
        GlobalScope.launch(Dispatchers.Main) {
            main()
        }
    }

    suspend fun main() {

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
            val str = inTime.text.toString()
            val str1 = outTime.text.toString()
            if (i == KeyEvent.KEYCODE_BACK && keyEvent.action == KeyEvent.ACTION_DOWN) {
                outTime.clearFocus()
            }
            if (i == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_UP) {
                vibration(vibrationData)

                hideKeyboard()
                if (textInputLayout3.visibility == View.VISIBLE) {
                    breakTime.requestFocus()
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
                    val imm1: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm1.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
                } else {
                    validation(str, str1, spinner1selecteditem, spinner2selecteditem, infoTextView1)
                }
                return@OnKeyListener true
            }
            false
        })

        breakTime.setOnKeyListener(View.OnKeyListener { _, i, keyEvent ->
            val str = inTime.text.toString()
            val str1 = outTime.text.toString()
            if (i == KeyEvent.KEYCODE_BACK && keyEvent.action == KeyEvent.ACTION_DOWN) {
                outTime.clearFocus()
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
                            aMandPMandPMandAM(separate1, separate2, separate3, separate4, infoTextView1, breakTime)
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
                        } else if (separate1.toDouble() >= 13 || separate3.toDouble() >= 13) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else if (separate2.toDouble() >= 60 || separate4.toDouble() >= 60) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else {
                            aMandPMandPMandAM(separate1, separate2, separate3, separate4, infoTextView1, breakTime)
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
                            aMandPMandPMandAM(separate1, separate2, separate3, separate4, infoTextView1, breakTime)
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
                            aMandPMandPMandAM(separate1, separate2, separate3, separate4, infoTextView1, breakTime)
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
                            aMandPMandPMandAM(separate1, separate2, separate3, separate4, infoTextView1, breakTime)
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
                        } else if (separate2.toDouble() >= 60 || separate4.toDouble() >= 60) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (separate1.toDouble() >= 13 || separate3.toDouble() >= 13) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandPMandPMandAM(separate1, separate2, separate3, separate4, infoTextView1, breakTime)
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
                            aMandPMandPMandAM(separate1, separate2, separate3, separate4, infoTextView1, breakTime)
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
                            aMandPMandPMandAM(separate1, separate2, separate3, separate4, infoTextView1, breakTime)
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
                    aMandPMandPMandAM(separate1, separate2, separate3, separate4, infoTextView1, breakTime)
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
                            aMandPMandPMandAM(separate1, separate2, separate3, separate4, infoTextView1, breakTime)
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
                        } else if (separate1.toDouble() >= 13 || separate3.toDouble() >= 13) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else if (separate2.toDouble() >= 60 || separate4.toDouble() >= 60) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else {
                            aMandPMandPMandAM(separate1, separate2, separate3, separate4, infoTextView1, breakTime)
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
                            aMandPMandPMandAM(separate1, separate2, separate3, separate4, infoTextView1, breakTime)
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
                            aMandPMandPMandAM(separate1, separate2, separate3, separate4, infoTextView1, breakTime)
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
                            aMandPMandPMandAM(separate1, separate2, separate3, separate4, infoTextView1, breakTime)
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
                        } else if (separate2.toDouble() >= 60 || separate4.toDouble() >= 60) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_60)
                        } else if (separate1.toDouble() >= 13 || separate3.toDouble() >= 13) {
                            infoTextView1.visibility = View.VISIBLE
                            infoTextView1.text = getString(R.string.cant_be_greater_than_or_equal_to_13)
                        } else {
                            aMandPMandPMandAM(separate1, separate2, separate3, separate4, infoTextView1, breakTime)
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
                            aMandPMandPMandAM(separate1, separate2, separate3, separate4, infoTextView1, breakTime)
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
                            aMandPMandPMandAM(separate1, separate2, separate3, separate4, infoTextView1, breakTime)
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
                    aMandPMandPMandAM(separate1, separate2, separate3, separate4, infoTextView1, breakTime)
                }
            }
        }
    }

    fun vibration(vibrationData: VibrationData) {
        if (vibrationData.loadVibrationState() == true) {
            val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    fun aMandAMandPMandPM(separate1: String, separate2: String, separate3: String, separate4: String, infoTextView1: TextView, breakTime: EditText) {
        val historyToggleData = HistoryToggleData(this)
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
        val totalhours3 = String.format("%.2f", totalhours).toDouble()
        if (totalhours < 0) {
            infoTextView1.visibility = View.VISIBLE
            infoTextView1.text = getString(R.string.in_time_can_not_be_greater_than_out_time)
        } else {
            if (breakTime.text.toString() == "") {
                infoTextView1.visibility = View.VISIBLE
                infoTextView1.text = getString(R.string.total_hours, totalhours3.toString())
                if (historyToggleData.loadHistoryState() == true) {
                    savingHours(totalhours3, inTime, outTime, breakTime)
                }
            } else if (breakTime.text.toString() != "") {
                val breakTimeInt = breakTime.text.toString().toDouble()
                val breakTimeDec: Double = breakTimeInt / 60
                val break1 = breakTimeDec.toBigDecimal().setScale(3, RoundingMode.HALF_EVEN).toString().toDouble()
                val totalHours1 = totalhours - break1
                val totalhours2 = String.format("%.2f", totalHours1).toDouble()
                if (totalhours2 < 0) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.in_time_can_not_be_greater_than_out_time)
                } else if (totalhours2 > 0) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.total_hours_with_and_without_break, totalhours2.toString(), totalhours3.toString())
                    if (historyToggleData.loadHistoryState() == true) {
                        savingHours2(totalhours2, inTime, outTime, breakTime)
                    }
                }
            }
        }
    }

    fun aMandPMandPMandAM(separate1: String, separate2: String, separate3: String, separate4: String, infoTextView1: TextView, breakTime: EditText) {
        val historyToggleData = HistoryToggleData(this)
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
        val totalhours = String.format("%.2f", difference).toDouble() + 12
        val totalhours3 = String.format("%.2f", totalhours).toDouble()
        if (totalhours < 0) {
            infoTextView1.visibility = View.VISIBLE
            infoTextView1.text = getString(R.string.in_time_can_not_be_greater_than_out_time)
        } else {
            if (breakTime.text.toString() == "") {
                infoTextView1.visibility = View.VISIBLE
                infoTextView1.text = getString(R.string.total_hours, totalhours3.toString())
                if (historyToggleData.loadHistoryState() == true) {
                    savingHours(totalhours3, inTime, outTime, breakTime)
                }
            } else if (breakTime.text.toString() != "") {
                val breakTimeInt = breakTime.text.toString().toDouble()
                val breakTimeDec: Double = breakTimeInt / 60
                val break1 = breakTimeDec.toBigDecimal().setScale(3, RoundingMode.HALF_EVEN).toString().toDouble()
                val totalHours1 = totalhours - break1
                val totalhours2 = String.format("%.2f", totalHours1).toDouble()
                if (totalhours2 < 0) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.in_time_can_not_be_greater_than_out_time)
                } else if (totalhours2 > 0) {
                    infoTextView1.visibility = View.VISIBLE
                    infoTextView1.text = getString(R.string.total_hours_with_and_without_break, totalhours2.toString(), totalhours3.toString())
                    if (historyToggleData.loadHistoryState() == true) {
                        savingHours2(totalhours2, inTime, outTime, breakTime)
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
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "settings_menu_item_main")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "settings_menu_item_clicked_main")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "menu_item")
                }
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.changelog -> {
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "patch_notes_menu_item_main")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "patch_notes_menu_item_clicked_main")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "menu_item")
                }
                val intent = Intent(this, PatchNotesActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.history -> {
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "history_menu_item_main")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "history_menu_item_clicked_main")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "menu_item")
                }
                val intent = Intent(this, HistoryActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.trash -> {
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "trash_menu_item_main")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "trash_menu_item_clicked_main")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "menu_item")
                }
                val intent = Intent(this, TrashActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.graph -> {
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "graph_menu_item_main")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "graph_menu_item_clicked_main")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "menu_item")
                }
                val intent = Intent(this, GraphActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}