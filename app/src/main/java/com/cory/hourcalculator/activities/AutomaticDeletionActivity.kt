package com.cory.hourcalculator.activities

import android.content.Context
import android.content.Intent
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.cory.hourcalculator.R
import com.cory.hourcalculator.classes.*
import com.cory.hourcalculator.database.DBHelper
import com.cory.hourcalculator.database.DBHelperTrash
import com.google.android.gms.ads.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_automatic_deletion.*
import kotlinx.android.synthetic.main.activity_main.*

class AutomaticDeletionActivity : AppCompatActivity() {

    private lateinit var darkThemeData : DarkThemeData

    private val dbHandler = DBHelper(this, null)
    private val dbHandlerTrash = DBHelperTrash(this, null)

    private val dataList = ArrayList<HashMap<String, String>>()

    val testDeviceId = listOf("5E80E48DC2282D372EAE0E3ACDE070CC", "8EE44B7B4B422D333731760574A381FE")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        darkThemeData = DarkThemeData(this)
        if (darkThemeData.loadDarkModeState()) {
            setTheme(R.style.AMOLED)
        } else {
            setTheme(R.style.AppTheme)
        }
        setContentView(R.layout.activity_automatic_deletion)
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

        val trashAutomaticDeletion = TrashAutomaticDeletion(this)
        val vibrationData = VibrationData(this)
        val historyAutomaticDeletion = HistoryAutomaticDeletion(this)
        val daysWorkedPerWeek = DaysWorkedPerWeek(this)
        val historyDeletion = HistoryDeletion(this)

        if(historyAutomaticDeletion.loadHistoryDeletionState()) {
            try {
                enableHistoryDeletion.isChecked = true
                constraintlayoutTextBox.visibility = View.VISIBLE
                val editable = Editable.Factory.getInstance().newEditable(daysWorkedPerWeek.loadDaysWorked().toString())
                daysWorked.text = editable
            }
            catch (e:Exception) {
                e.printStackTrace()
            }
        }
        else if (!historyAutomaticDeletion.loadHistoryDeletionState()) {
            try {
                disableHistoryDeletion.isChecked = true
                constraintlayoutTextBox.visibility = View.GONE
            }
            catch (e:Exception) {
                e.printStackTrace()
            }
        }

        daysWorked.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != "") {
                    daysWorkedPerWeek.setDaysWorked(s.toString())
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                daysWorkedPerWeek.setDaysWorked(s.toString())
            }
        })

        daysWorked.setOnKeyListener(View.OnKeyListener { _, i, keyEvent ->
            if (i == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_UP) {
                vibration(vibrationData)
                if (daysWorked.toString() != "") {
                    if (dbHandler.getCount() > daysWorkedPerWeek.loadDaysWorked().toString().toInt()) {
                        val numberToDelete = dbHandler.getCount() - daysWorkedPerWeek.loadDaysWorked().toString().toInt()
                        daysWorkedPerWeek.setDaysWorked(daysWorked.text.toString())
                        val alertDialog = AlertDialog.Builder(this)
                        alertDialog.setTitle(getString(R.string.delete_hours))
                        alertDialog.setMessage(getString(R.string.would_you_like_to_delete_all_hours, daysWorkedPerWeek.loadDaysWorked().toString()))
                        alertDialog.setPositiveButton(getString(R.string.yes)) { dialog, which ->
                            /*dataList.clear()
                            val cursor = dbHandler.automaticDeletion(this, numberToDelete)
                            Toast.makeText(this, cursor.toString(), Toast.LENGTH_SHORT).show()
                            cursor!!.moveToFirst()

                            val map = HashMap<String, String>()
                            while (!cursor.isAfterLast) {

                                map["id"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ID))
                                map["intime"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_IN))
                                map["out"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_OUT))
                                map["break"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_BREAK))
                                map["total"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TOTAL))
                                map["day"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DAY))
                                dataList.add(map)

                                dbHandlerTrash.insertRow(
                                    map["intime"].toString(), map["out"].toString(),
                                    map["break"].toString(), map["total"].toString(), map["day"].toString()
                                )
                                dbHandler.deleteRow(map["id"].toString())
                                Toast.makeText(this, cursor.position.toString(), Toast.LENGTH_SHORT).show()

                                cursor.moveToNext()
                            }*/
                            historyDeletion.deletion(this)
                        }
                        alertDialog.setNegativeButton(getString(R.string.no)) {dialog, which ->
                            disableHistoryDeletion.isChecked = true
                            val slideTextBoxAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up_text_box)
                            val slideUpTextBox = AnimationUtils.loadAnimation(this, R.anim.slide_up_text_view)
                            constraintlayoutTextBox.startAnimation(slideTextBoxAnimation)
                            layout_settings_warning.startAnimation(slideUpTextBox)
                            constraintlayoutTextBox.visibility = View.GONE
                            historyAutomaticDeletion.setHistoryDeletionState(false)
                            Toast.makeText(this, getString(R.string.history_deletion_disabled_because_you_chose_no), Toast.LENGTH_SHORT).show()
                        }
                        alertDialog.show()
                    }
                }
                daysWorked.clearFocus()
            }
            if (i == KeyEvent.KEYCODE_BACK && keyEvent.action == KeyEvent.ACTION_DOWN) {
                daysWorked.clearFocus()
                return@OnKeyListener true
            }
            false
        })

        val enableTrashAutomaticDeletion = findViewById<RadioButton>(R.id.enableTrashDeletion)
        val disableTrashAutomaticDeletion = findViewById<RadioButton>(R.id.disableTrashDeletion)

        if (trashAutomaticDeletion.loadTrashDeletionState()) {
            enableTrashAutomaticDeletion.isChecked = true
        }
        else if (!trashAutomaticDeletion.loadTrashDeletionState()) {
            disableTrashAutomaticDeletion.isChecked = true
        }

        enableTrashAutomaticDeletion.setOnClickListener {
            vibration(vibrationData)
            if(trashAutomaticDeletion.loadTrashDeletionState()) {
                Toast.makeText(this, getString(R.string.trash_automatic_deletion_is_already_enabled), Toast.LENGTH_SHORT).show()
            }
            else {
                trashAutomaticDeletion.setTrashDeletionState(true)
                Snackbar.make(automaticDeletionConstraint, getString(R.string.enable_automatic_deletion), Snackbar.LENGTH_SHORT).show()
            }
        }
        disableTrashAutomaticDeletion.setOnClickListener {
            vibration(vibrationData)
            if(!trashAutomaticDeletion.loadTrashDeletionState()) {
                Toast.makeText(this, getString(R.string.trash_automatic_deletion_is_already_disabled), Toast.LENGTH_SHORT).show()
            }
            else {
                trashAutomaticDeletion.setTrashDeletionState(false)
                Snackbar.make(automaticDeletionConstraint, getString(R.string.disable_automatic_deletion), Snackbar.LENGTH_SHORT).show()
            }
        }

        enableHistoryDeletion.setOnClickListener {
            if(!historyAutomaticDeletion.loadHistoryDeletionState()) {
                constraintlayoutTextBox.visibility = View.VISIBLE
                val slideAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_down_text_box)
                constraintlayoutTextBox.startAnimation(slideAnimation)
                layout_settings_warning.startAnimation(slideAnimation)
                historyAutomaticDeletion.setHistoryDeletionState(true)
                val editable = Editable.Factory.getInstance().newEditable(daysWorkedPerWeek.loadDaysWorked().toString())
                daysWorked.text = editable
                Snackbar.make(automaticDeletionConstraint, getString(R.string.history_automatic_deletion_enabled), Snackbar.LENGTH_SHORT).show()
            }
            else {
                if(daysWorkedTextInputLayout.visibility == View.GONE) {
                    constraintlayoutTextBox.visibility = View.VISIBLE
                    val slideAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_down_text_box)
                    constraintlayoutTextBox.startAnimation(slideAnimation)
                    layout_settings_warning.startAnimation(slideAnimation)
                    historyAutomaticDeletion.setHistoryDeletionState(true)
                    val editable = Editable.Factory.getInstance().newEditable(daysWorkedPerWeek.loadDaysWorked().toString())
                    daysWorked.text = editable
                }
                Toast.makeText(this, getString(R.string.history_automatic_deletion_already_enabled), Toast.LENGTH_SHORT).show()
            }
        }
        disableHistoryDeletion.setOnClickListener {
            if(historyAutomaticDeletion.loadHistoryDeletionState()) {
                val slideTextBoxAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up_text_box)
                val slideUpTextBox = AnimationUtils.loadAnimation(this, R.anim.slide_up_text_view)
                constraintlayoutTextBox.startAnimation(slideTextBoxAnimation)
                layout_settings_warning.startAnimation(slideUpTextBox)
                /*val animateTextView = TranslateAnimation(0f, 0f, layout_settings_warning.height.toFloat(), 0f)
                animateTextView.duration = 600
                animateTextView.fillAfter = true
                layout_settings_warning.startAnimation(animateTextView)*/
                historyAutomaticDeletion.setHistoryDeletionState(false)
                Snackbar.make(automaticDeletionConstraint, getString(R.string.history_automatic_deletion_disabled), Snackbar.LENGTH_SHORT).show()
                //constraintlayoutTextBox.visibility = View.GONE
                constraintlayoutTextBox.visibility = View.GONE
            }
            else {
                Toast.makeText(this, getString(R.string.history_automatic_deletion_already_disabled), Toast.LENGTH_SHORT).show()
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
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
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
        if(!PerformanceModeData(this).loadPerformanceMode()) {
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
        else {
            overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu_automatic_deletion, menu)
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