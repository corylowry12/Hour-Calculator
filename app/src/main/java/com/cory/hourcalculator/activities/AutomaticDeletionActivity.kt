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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.cory.hourcalculator.R
import com.cory.hourcalculator.classes.*
import com.cory.hourcalculator.database.DBHelper
import com.google.android.gms.ads.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_automatic_deletion.*
import kotlinx.android.synthetic.main.activity_main.*

class AutomaticDeletionActivity : AppCompatActivity() {

    private lateinit var darkThemeData: DarkThemeData
    private lateinit var accentColor: AccentColor

    private val dbHandler = DBHelper(this, null)
    //private val dbHandlerTrash = DBHelperTrash(this, null)

    //private val dataList = ArrayList<HashMap<String, String>>()

    val testDeviceId = listOf("5E80E48DC2282D372EAE0E3ACDE070CC", "8EE44B7B4B422D333731760574A381FE")

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

        if (historyAutomaticDeletion.loadHistoryDeletionState()) {
            try {
                enableHistoryDeletion.isChecked = true
                constraintlayoutTextBox.visibility = View.VISIBLE
                val editable = Editable.Factory.getInstance().newEditable(daysWorkedPerWeek.loadDaysWorked().toString())
                daysWorked.text = editable
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if (!historyAutomaticDeletion.loadHistoryDeletionState()) {
            try {
                disableHistoryDeletion.isChecked = true
                constraintlayoutTextBox.visibility = View.GONE
            } catch (e: Exception) {
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
                        //val numberToDelete = dbHandler.getCount() - daysWorkedPerWeek.loadDaysWorked().toString().toInt()
                        daysWorkedPerWeek.setDaysWorked(daysWorked.text.toString())
                        val alertDialog = AlertDialog.Builder(this)
                        alertDialog.setTitle(getString(R.string.delete_hours))
                        alertDialog.setMessage(getString(R.string.would_you_like_to_delete_all_hours, daysWorkedPerWeek.loadDaysWorked().toString()))
                        alertDialog.setPositiveButton(getString(R.string.yes)) { _, _ ->
                            historyDeletion.deletion(this)
                        }
                        alertDialog.setNegativeButton(getString(R.string.no)) { _, _ ->
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

        enableHistoryDeletion.setOnClickListener {
            if (!historyAutomaticDeletion.loadHistoryDeletionState()) {
                constraintlayoutTextBox.visibility = View.VISIBLE
                val slideAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_down_text_box)
                constraintlayoutTextBox.startAnimation(slideAnimation)
                layout_settings_warning.startAnimation(slideAnimation)
                relativeLayoutDeletionSettings.startAnimation(slideAnimation)
                historyAutomaticDeletion.setHistoryDeletionState(true)
                val editable = Editable.Factory.getInstance().newEditable(daysWorkedPerWeek.loadDaysWorked().toString())
                daysWorked.text = editable
                Snackbar.make(automaticDeletionConstraint, getString(R.string.history_automatic_deletion_enabled), Snackbar.LENGTH_SHORT).show()
            } else {
                if (daysWorkedTextInputLayout.visibility == View.GONE) {
                    constraintlayoutTextBox.visibility = View.VISIBLE
                    val slideAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_down_text_box)
                    constraintlayoutTextBox.startAnimation(slideAnimation)
                    layout_settings_warning.startAnimation(slideAnimation)
                    relativeLayoutDeletionSettings.startAnimation(slideAnimation)
                    historyAutomaticDeletion.setHistoryDeletionState(true)
                    val editable = Editable.Factory.getInstance().newEditable(daysWorkedPerWeek.loadDaysWorked().toString())
                    daysWorked.text = editable
                }
                Toast.makeText(this, getString(R.string.history_automatic_deletion_already_enabled), Toast.LENGTH_SHORT).show()
            }
        }
        disableHistoryDeletion.setOnClickListener {
            if (historyAutomaticDeletion.loadHistoryDeletionState()) {
                val slideTextBoxAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up_text_box)
                val slideUpTextBox = AnimationUtils.loadAnimation(this, R.anim.slide_up_text_view)
                constraintlayoutTextBox.startAnimation(slideTextBoxAnimation)
                layout_settings_warning.startAnimation(slideUpTextBox)
                relativeLayoutDeletionSettings.startAnimation(slideUpTextBox)
                historyAutomaticDeletion.setHistoryDeletionState(false)
                Snackbar.make(automaticDeletionConstraint, getString(R.string.history_automatic_deletion_disabled), Snackbar.LENGTH_SHORT).show()
                constraintlayoutTextBox.visibility = View.GONE
            } else {
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

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu_automatic_deletion, menu)
        val historyToggleData = HistoryToggleData(this)
        if (!historyToggleData.loadHistoryState()) {
            val history = menu.findItem(R.id.history)
            history.isVisible = false
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
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)

                return true
            }
            R.id.Settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)

                return true
            }
            R.id.changelog -> {
                val intent = Intent(this, PatchNotesActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)

                return true
            }
            R.id.history -> {
                val intent = Intent(this, HistoryActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)

                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}