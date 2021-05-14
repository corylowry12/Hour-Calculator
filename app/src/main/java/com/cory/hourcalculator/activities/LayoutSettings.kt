package com.cory.hourcalculator.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.cory.hourcalculator.R
import com.cory.hourcalculator.classes.*
import com.cory.hourcalculator.database.DBHelper
import com.cory.hourcalculator.database.DBHelperTrash
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.activity_layout_settings.*
import kotlinx.android.synthetic.main.activity_settings.*

class LayoutSettings : AppCompatActivity() {

    private lateinit var darkThemeData : DarkThemeData
    private val dbHandler = DBHelper(this, null)
    private val dbHandlerTrash = DBHelperTrash(this, null)
    private val dataList = ArrayList<HashMap<String, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        darkThemeData = DarkThemeData(this)
        if (darkThemeData.loadDarkModeState()) {
            setTheme(R.style.AMOLED)
        } else {
            setTheme(R.style.AppTheme)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layout_settings)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        val performanceModeData = PerformanceModeData(this)
        val breakTextBoxData = BreakData(this)
        val vibrationData = VibrationData(this)
        val historyToggleData = HistoryToggleData(this)
        val trashAutomaticDeletion = TrashAutomaticDeletion(this)

        val enablePerformanceModeButton = findViewById<RadioButton>(R.id.enablePerformanceMode)
        val disablePerformanceModeButton = findViewById<RadioButton>(R.id.disablePerformanceMode)

        if (performanceModeData.loadPerformanceMode()) {
            enablePerformanceModeButton.isChecked = true
        }
        else if (!performanceModeData.loadPerformanceMode()) {
            disablePerformanceModeButton.isChecked = true
        }

        enablePerformanceModeButton.setOnClickListener {
            performanceModeData.setPerformanceMode(true)
            Snackbar.make(constraintLayoutSettings, getString(R.string.enabled_performance_mode), Snackbar.LENGTH_SHORT).show()
            vibration(vibrationData)
        }
        disablePerformanceModeButton.setOnClickListener {
            performanceModeData.setPerformanceMode(false)
            Snackbar.make(constraintLayoutSettings, getString(R.string.disabled_performance_mode), Snackbar.LENGTH_SHORT).show()
            vibration(vibrationData)
        }

        val enableBreakTextBox = findViewById<RadioButton>(R.id.enableBreakTextBox)
        val disableBreakTextBox = findViewById<RadioButton>(R.id.disableBreakTextBox)

        if (breakTextBoxData.loadBreakState()) {
            enableBreakTextBox.isChecked = true
        }
        else if (!breakTextBoxData.loadBreakState()) {
            disableBreakTextBox.isChecked = true
        }

        enableBreakTextBox.setOnClickListener {
            breakTextBoxData.setBreakState(true)
            Snackbar.make(constraintLayoutSettings, getString(R.string.enabled_break_text_box), Snackbar.LENGTH_SHORT).show()
            vibration(vibrationData)
        }
        disableBreakTextBox.setOnClickListener {
            breakTextBoxData.setBreakState(false)
            Snackbar.make(constraintLayoutSettings, getString(R.string.disabled_break_text_box), Snackbar.LENGTH_SHORT).show()
            vibration(vibrationData)
        }

        val enableVibration = findViewById<RadioButton>(R.id.enableVibration)
        val disableVibration = findViewById<RadioButton>(R.id.disableVibration)

        if (vibrationData.loadVibrationState()) {
            enableVibration.isChecked = true
        }
        else if (!vibrationData.loadVibrationState()) {
            disableVibration.isChecked = true
        }

        enableVibration.setOnClickListener {
            vibrationData.setVibrationState(true)
            Snackbar.make(constraintLayoutSettings, getString(R.string.enabled_vibration), Snackbar.LENGTH_SHORT).show()
            vibration(vibrationData)
        }
        disableVibration.setOnClickListener {
            vibrationData.setVibrationState(false)
            Snackbar.make(constraintLayoutSettings, getString(R.string.disabled_vibration), Snackbar.LENGTH_SHORT).show()
            vibration(vibrationData)
        }

        val enableHistory = findViewById<RadioButton>(R.id.enableHistory)
        val disableHistory = findViewById<RadioButton>(R.id.disableHistory)

        if (historyToggleData.loadHistoryState()) {
            enableHistory.isChecked = true
        }
        else if (!historyToggleData.loadHistoryState()) {
            disableHistory.isChecked = false
        }

        enableHistory.setOnClickListener {
            historyToggleData.setHistoryToggle(true)
            Snackbar.make(constraintLayoutSettings, getString(R.string.enabled_history), Snackbar.LENGTH_SHORT).show()
            vibration(vibrationData)
        }
        disableHistory.setOnClickListener {
            historyToggleData.setHistoryToggle(false)
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle(getString(R.string.history))
            alertDialog.setMessage(getString(R.string.what_would_you_like_to_do_with_history))
            alertDialog.setPositiveButton(getString(R.string.delete)) { _, _ ->
                dbHandler.deleteAll()
                Snackbar.make(constraintLayoutSettings, getString(R.string.deleted_all_of_hour_history), Snackbar.LENGTH_SHORT).show()
            }
            alertDialog.setNegativeButton(getString(R.string.trash)) { _, _ ->
                dataList.clear()
                val cursor1 = dbHandler.getAllRow(this)
                cursor1!!.moveToFirst()

                while (!cursor1.isAfterLast) {
                    val intime = cursor1.getString(cursor1.getColumnIndex(DBHelper.COLUMN_IN))
                    val outtime = cursor1.getString(cursor1.getColumnIndex(DBHelper.COLUMN_OUT))
                    val breaktime = cursor1.getString(cursor1.getColumnIndex(DBHelper.COLUMN_BREAK))
                    val totaltime = cursor1.getString(cursor1.getColumnIndex(DBHelper.COLUMN_TOTAL))
                    val day = cursor1.getString(cursor1.getColumnIndex(DBHelper.COLUMN_DAY))

                    dbHandlerTrash.insertRow(intime, outtime, breaktime, totaltime, day)

                    cursor1.moveToNext()
                }
                dbHandler.deleteAll()
                Snackbar.make(constraintLayoutSettings, getString(R.string.moved_all_hours_to_trash), Snackbar.LENGTH_SHORT).show()
            }
            alertDialog.setNeutralButton(getString(R.string.nothing), null)
            alertDialog.create().show()
            vibration(vibrationData)
            Snackbar.make(constraintLayoutSettings, getString(R.string.disabled_history), Snackbar.LENGTH_SHORT).show()
        }

        val enableTrashAutomaticDeletion = findViewById<RadioButton>(R.id.enableTrashDeletion)
        val disableTrashAutomaticDeletion = findViewById<RadioButton>(R.id.disableTrashDeletion)

        if (trashAutomaticDeletion.loadTrashDeletionState()) {
            enableTrashAutomaticDeletion.isChecked = true
        }
        else if (!trashAutomaticDeletion.loadTrashDeletionState()) {
            disableTrashAutomaticDeletion.isChecked = true
        }

        enableTrashAutomaticDeletion.setOnClickListener {
            trashAutomaticDeletion.setTrashDeletionState(true)
            Snackbar.make(constraintLayoutSettings, getString(R.string.enable_automatic_deletion), Snackbar.LENGTH_SHORT).show()
            vibration(vibrationData)
        }
        disableTrashAutomaticDeletion.setOnClickListener {
            trashAutomaticDeletion.setTrashDeletionState(false)
            Snackbar.make(constraintLayoutSettings, getString(R.string.disable_automatic_deletion), Snackbar.LENGTH_SHORT).show()
            vibration(vibrationData)
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
        if(!PerformanceModeData(this).loadPerformanceMode()) {
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
        else {
            overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
        if (!PerformanceModeData(this).loadPerformanceMode()) {
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        } else {
            overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu_layout_settings, menu)
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
        if (vibrationData.loadVibrationState()) {
            val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(5, VibrationEffect.DEFAULT_AMPLITUDE))
        }
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
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}