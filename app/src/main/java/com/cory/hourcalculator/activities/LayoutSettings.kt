package com.cory.hourcalculator.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.cory.hourcalculator.R
import com.cory.hourcalculator.classes.*
import com.cory.hourcalculator.database.DBHelper
import com.google.android.gms.ads.*
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.activity_layout_settings.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_settings.*

class LayoutSettings : AppCompatActivity() {

    private lateinit var darkThemeData: DarkThemeData
    private lateinit var accentColor: AccentColor
    private lateinit var vibrationData : VibrationData

    private val dbHandler = DBHelper(this, null)

    private val testDeviceId = listOf("5E80E48DC2282D372EAE0E3ACDE070CC", "8EE44B7B4B422D333731760574A381FE", "C290EC36E0463AF42E6770B180892920")

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
        setContentView(R.layout.activity_layout_settings)
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
        val historyToggleData = HistoryToggleData(this)

        vibrationData = VibrationData(this)

        bottomNav_layoutSettings.menu.findItem(R.id.menu_settings).isChecked = true
        bottomNav_layoutSettings.menu.findItem(R.id.menu_history).isVisible = historyToggleData.loadHistoryState()

        bottomNav_layoutSettings.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.menu_history -> {
                    vibration(vibrationData)
                    val intent = Intent(this, HistoryActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                    true
                }
                R.id.menu_settings -> {
                    vibration(vibrationData)
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                    true
                }
                else -> false
            }
        }

        // prevents keyboard from opening when activity is launched
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        val enableVibration = findViewById<RadioButton>(R.id.enableVibration)
        val disableVibration = findViewById<RadioButton>(R.id.disableVibration)

        if (vibrationData.loadVibrationState()) {
            enableVibration.isChecked = true
        } else if (!vibrationData.loadVibrationState()) {
            disableVibration.isChecked = true
        }

        enableVibration.setOnClickListener {
            vibration(vibrationData)
            if (vibrationData.loadVibrationState()) {
                Toast.makeText(this, getString(R.string.vibration_is_already_enabled), Toast.LENGTH_SHORT).show()
            } else {
                vibrationData.setVibrationState(true)
                Snackbar.make(constraintLayoutSettings, getString(R.string.enabled_vibration), Snackbar.LENGTH_SHORT).show()
            }
        }
        disableVibration.setOnClickListener {
            vibration(vibrationData)
            if (!vibrationData.loadVibrationState()) {
                Toast.makeText(this, getString(R.string.vibration_is_already_disabled), Toast.LENGTH_SHORT).show()
            } else {
                vibrationData.setVibrationState(false)
                Snackbar.make(constraintLayoutSettings, getString(R.string.disabled_vibration), Snackbar.LENGTH_SHORT).show()
            }
        }

        val enableHistory = findViewById<RadioButton>(R.id.enableHistory)
        val disableHistory = findViewById<RadioButton>(R.id.disableHistory)

        if (historyToggleData.loadHistoryState()) {
            enableHistory.isChecked = true
        } else if (!historyToggleData.loadHistoryState()) {
            disableHistory.isChecked = true
        }

        enableHistory.setOnClickListener {
            vibration(vibrationData)
            if (historyToggleData.loadHistoryState()) {
                Toast.makeText(this, getString(R.string.history_is_already_enabled), Toast.LENGTH_SHORT).show()
            } else {
                historyToggleData.setHistoryToggle(true)
                Snackbar.make(constraintLayoutSettings, getString(R.string.enabled_history), Snackbar.LENGTH_SHORT).show()
                invalidateOptionsMenu()
            }
        }
        disableHistory.setOnClickListener {
            vibration(vibrationData)
            if (!historyToggleData.loadHistoryState()) {
                Toast.makeText(this, getString(R.string.history_is_already_disabled), Toast.LENGTH_SHORT).show()
            } else {
                historyToggleData.setHistoryToggle(false)
                val alertDialog = AlertDialog.Builder(this)
                alertDialog.setTitle(getString(R.string.history))
                alertDialog.setMessage(getString(R.string.what_would_you_like_to_do_with_history))
                alertDialog.setPositiveButton(getString(R.string.delete)) { _, _ ->
                    dbHandler.deleteAll()
                    Snackbar.make(constraintLayoutSettings, getString(R.string.deleted_all_of_hour_history), Snackbar.LENGTH_SHORT).show()
                }
                alertDialog.setNeutralButton(getString(R.string.nothing), null)
                alertDialog.create().show()
                Snackbar.make(constraintLayoutSettings, getString(R.string.disabled_history), Snackbar.LENGTH_SHORT).show()
                invalidateOptionsMenu()
            }
        }

        val wagesData = WagesData(this)
        val wagesEditText = findViewById<TextInputEditText>(R.id.Wages)

        val editable = Editable.Factory.getInstance().newEditable(wagesData.loadWageAmount().toString())
        wagesEditText.text = editable

        wagesEditText.setOnKeyListener(View.OnKeyListener { _, i, keyEvent ->
            if (i == KeyEvent.KEYCODE_BACK && keyEvent.action == KeyEvent.ACTION_DOWN) {
                hideKeyboard(wagesEditText)
                return@OnKeyListener true
            }
            if (i == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_UP) {
                wagesData.setWageAmount(wagesEditText.text.toString())
                hideKeyboard(wagesEditText)
                return@OnKeyListener true
            }
            false
        })

        wagesEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != "") {
                    wagesData.setWageAmount(s.toString())
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                wagesData.setWageAmount(s.toString())
            }
        })
    }

    private fun hideKeyboard(wagesEditText: TextInputEditText) {
        val inputManager: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val focusedView = this.currentFocus

        if (focusedView != null) {
            inputManager.hideSoftInputFromWindow(focusedView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            if (wagesEditText.hasFocus()) {
                wagesEditText.clearFocus()
            }
        }
    }

    private fun vibration(vibrationData: VibrationData) {
        if (vibrationData.loadVibrationState()) {
            val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(5, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    override fun onRestart() {
        super.onRestart()
        val intent = Intent(this, this::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun onSupportNavigateUp(): Boolean {
        vibration(vibrationData)
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}