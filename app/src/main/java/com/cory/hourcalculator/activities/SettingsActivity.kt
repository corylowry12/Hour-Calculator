package com.cory.hourcalculator.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.cory.hourcalculator.R
import com.cory.hourcalculator.classes.*
import com.cory.hourcalculator.database.DBHelper
import com.google.android.gms.ads.*
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.android.synthetic.main.activity_settings.*
import java.io.*

class SettingsActivity : AppCompatActivity() {

    // Not initialized variables
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var darkThemeData: DarkThemeData
    private val CREATE_FILE = 1
    private val dbHandler = DBHelper(this, null)
    private val permissionRequestCode = 1

    @SuppressLint("ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        // Sets theme before activity is created
        darkThemeData = DarkThemeData(this)
        if (darkThemeData.loadDarkModeState()) {
            setTheme(R.style.AMOLED)
        } else {
            setTheme(R.style.AppTheme)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        // sets the back arrow on the action bar
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        // sets background to null to prevent overdraw
        window.setBackgroundDrawable(null)

        // initializes firebase analytics
        firebaseAnalytics = Firebase.analytics

        // Initializes and displays ad view
        MobileAds.initialize(this)
        val adView = AdView(this)
        adView.adSize = AdSize.BANNER
        adView.adUnitId = "ca-app-pub-4546055219731501/5171269817"
        val mAdView = findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        mAdView.adListener = object : AdListener() {
        }

        // prevents keyboard from opening when activity is launched
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        val exportData = ExportData(this)

        // initializes the vibrationData class
        val vibrationData = VibrationData(this)
        var selectedItemIndex = exportData.loadExportFormat()

        val selection = arrayOf(getString(R.string.text_file), getString(R.string.spread_sheet))

        var selectedItem = selection[selectedItemIndex]

        textView29.setOnClickListener {
            vibration(vibrationData)
            val list = listOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val managePermissions = ManagePermissions(this, list, permissionRequestCode)
            if (dbHandler.getCount() > 0) {
                if (managePermissions.checkPermissions()) {
                    val alertDialog = AlertDialog.Builder(this)
                        .setTitle(getString(R.string.choose_format))
                        .setSingleChoiceItems(selection, selectedItemIndex) { _, which ->
                            selectedItemIndex = which
                            selectedItem = selection[which]
                        }
                        .setPositiveButton(R.string.ok) {_, _ ->
                            exportData.setExportFormat(selectedItemIndex)
                            if(exportData.loadExportFormat() == 1) {
                                createFileCSV()
                            }
                            else if(exportData.loadExportFormat() == 0) {
                                createFileTEXT()
                            }
                        }
                        .setNeutralButton(R.string.cancel, null)
                    val alert = alertDialog.create()
                    alert.show()
                }
                else {
                    managePermissions.showAlertSettings(this)
                }
            }
            else {
                Toast.makeText(this, getString(R.string.no_hours_stored), Toast.LENGTH_SHORT).show()
            }
        }
        textView30.setOnClickListener {
            vibration(vibrationData)
            val list = listOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val managePermissions = ManagePermissions(this, list, permissionRequestCode)
            if (dbHandler.getCount() > 0) {
                if (managePermissions.checkPermissions()) {
                    val alertDialog = AlertDialog.Builder(this)
                        .setTitle(getString(R.string.choose_format))
                        .setSingleChoiceItems(selection, selectedItemIndex) { _, which ->
                            selectedItemIndex = which
                            selectedItem = selection[which]
                        }
                        .setPositiveButton(R.string.ok) {_, _ ->
                            exportData.setExportFormat(selectedItemIndex)
                            if(exportData.loadExportFormat() == 1) {
                                createFileCSV()
                            }
                            else if(exportData.loadExportFormat() == 0) {
                                createFileTEXT()
                            }
                        }
                        .setNeutralButton(R.string.cancel, null)
                    val alert = alertDialog.create()
                    alert.show()
                }
                else {
                    managePermissions.showAlertSettings(this)
                }
            }
            else {
                Toast.makeText(this, getString(R.string.no_hours_stored), Toast.LENGTH_SHORT).show()
            }
        }
        txtviewExport.setOnClickListener {
            vibration(vibrationData)
            val list = listOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val managePermissions = ManagePermissions(this, list, permissionRequestCode)
            if (dbHandler.getCount() > 0) {
                if (managePermissions.checkPermissions()) {
                    val alertDialog = AlertDialog.Builder(this)
                        .setTitle(getString(R.string.choose_format))
                        .setSingleChoiceItems(selection, selectedItemIndex) { _, which ->
                            selectedItemIndex = which
                            selectedItem = selection[which]
                        }
                        .setPositiveButton(R.string.ok) {_, _ ->
                            exportData.setExportFormat(selectedItemIndex)
                            if(exportData.loadExportFormat() == 1) {
                                createFileCSV()
                            }
                            else if(exportData.loadExportFormat() == 0) {
                                createFileTEXT()
                            }
                        }
                        .setNeutralButton(R.string.cancel, null)
                    val alert = alertDialog.create()
                    alert.show()
                }
                else {
                    managePermissions.showAlertSettings(this)
                }
            }
            else {
                Toast.makeText(this, getString(R.string.no_hours_stored), Toast.LENGTH_SHORT).show()
            }
        }
        cardView12.setOnClickListener {
            vibration(vibrationData)
            val list = listOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val managePermissions = ManagePermissions(this, list, permissionRequestCode)
            if (dbHandler.getCount() > 0) {
                if (managePermissions.checkPermissions()) {
                    val alertDialog = AlertDialog.Builder(this)
                        .setTitle(getString(R.string.choose_format))
                        .setSingleChoiceItems(selection, selectedItemIndex) { _, which ->
                            selectedItemIndex = which
                            selectedItem = selection[which]
                        }
                        .setPositiveButton(R.string.ok) {_, _ ->
                            exportData.setExportFormat(selectedItemIndex)
                            if(exportData.loadExportFormat() == 1) {
                                createFileCSV()
                            }
                            else if(exportData.loadExportFormat() == 0) {
                                createFileTEXT()
                            }
                        }
                        .setNeutralButton(R.string.cancel, null)
                    val alert = alertDialog.create()
                    alert.show()
                }
                else {
                    managePermissions.showAlertSettings(this)
                }
            }
            else {
                Toast.makeText(this, getString(R.string.no_hours_stored), Toast.LENGTH_SHORT).show()
            }
        }

        // switched to enable and disable vibration
        val vibrationSwitch = findViewById<SwitchMaterial>(R.id.switch3)
        if (vibrationData.loadVibrationState()) {
            vibrationSwitch.isChecked = true
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "vibration_switch")
                param(FirebaseAnalytics.Param.ITEM_NAME, "vibration_switch_enabled")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "switch")
            }
        }
        else {
            vibrationSwitch.isChecked = false
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "vibration_switch")
                param(FirebaseAnalytics.Param.ITEM_NAME, "vibration_switch_disabled")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "switch")
            }
        }
        // listens for the switch to be changed
        vibrationSwitch.setOnCheckedChangeListener { _, isChecked ->
            vibration(vibrationData)
            if (isChecked) {
                vibrationData.setVibrationState(true)
                Snackbar.make(constraintLayout, getString(R.string.enabled_vibration), Snackbar.LENGTH_SHORT).show()
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "vibration_switch")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "vibration_switch_enabled")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "switch")
                }
            } else {
                vibrationSwitch.isChecked = false
                vibrationData.setVibrationState(false)
                Snackbar.make(constraintLayout, getString(R.string.disabled_vibration), Snackbar.LENGTH_SHORT).show()
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "vibration_switch")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "vibration_switch_disabled")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "switch")
                }
            }
        }

        val switch = findViewById<SwitchMaterial>(R.id.switch1)
        if (darkThemeData.loadDarkModeState()) {
            switch.isChecked = true
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "dark_mode_switch")
                param(FirebaseAnalytics.Param.ITEM_NAME, "dark_mode_switch_enabled")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "switch")
            }
        }
        else {
            switch.isChecked = false
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "dark_mode_switch")
                param(FirebaseAnalytics.Param.ITEM_NAME, "dark_mode_switch_disabled")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "switch")
            }
        }

        switch.setOnCheckedChangeListener { _, isChecked ->
            vibration(vibrationData)
            if (isChecked) {
                switch.isChecked = true
                darkThemeData.setDarkModeState(true)
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "dark_mode_switch")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "dark_mode_switch_enabled")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "switch")
                }
                restartApplication()
            } else {
                switch.isChecked = false
                darkThemeData.setDarkModeState(false)
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "dark_mode_switch")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "dark_mode_switch_disabled")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "switch")
                }
                restartApplication()
            }
        }

        val historySwitch = findViewById<SwitchMaterial>(R.id.switch4)
        val historyToggleData = HistoryToggleData(this)
        if (historyToggleData.loadHistoryState()) {
            historySwitch.isChecked = true
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "history_switch")
                param(FirebaseAnalytics.Param.ITEM_NAME, "history_switch_enabled")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "switch")
            }
        }
        else {
            historySwitch.isChecked = false
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "history_switch")
                param(FirebaseAnalytics.Param.ITEM_NAME, "history_switch_disabled")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "switch")
            }
        }

        historySwitch.setOnCheckedChangeListener { _, isChecked ->
            vibration(vibrationData)
            if (isChecked) {
                historySwitch.isChecked = true
                historyToggleData.setHistoryToggle(true)
                invalidateOptionsMenu()
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "history_toggle_switch")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "history_toggle_switch_enabled")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "switch")
                }
            } else {
                historySwitch.isChecked = false
                historyToggleData.setHistoryToggle(false)
                invalidateOptionsMenu()
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "history_toggle_switch")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "history_toggle_switch_disabled")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "switch")
                }
            }
        }

        val trashAutomaticDeletion = TrashAutomaticDeletion(this)
        val trashSwitch = findViewById<SwitchMaterial>(R.id.switch5)
        if (trashAutomaticDeletion.loadTrashDeletionState()) {
            trashSwitch.isChecked = true
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "trash_automatic_deletion_switch")
                param(FirebaseAnalytics.Param.ITEM_NAME, "trash_automatic_deletion_switch_enabled")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "switch")
            }
        }
        else {
            trashSwitch.isChecked = false
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "trash_automatic_deletion_switch")
                param(FirebaseAnalytics.Param.ITEM_NAME, "trash_automatic_deletion_switch_disabled")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "switch")
            }
        }
        trashSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                trashAutomaticDeletion.setTrashDeletionState(true)
                Snackbar.make(constraintLayout, getString(R.string.enable_automatic_deletion), Snackbar.LENGTH_SHORT).show()
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "trash_automatic_deletion_switch")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "trash_automatic_deletion_switch_enabled")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "switch")
                }
            } else {
                trashSwitch.isChecked = false
                trashAutomaticDeletion.setTrashDeletionState(false)
                Snackbar.make(constraintLayout, getString(R.string.disable_automatic_deletion), Snackbar.LENGTH_SHORT).show()
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "trash_automatic_deletion_switch")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "trash_automatic_deletion_switch_disabled")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "switch")
                }
            }
        }

        val updateData = UpdateData(this)
        val updateSwitch = findViewById<SwitchMaterial>(R.id.switch6)
        if(updateData.loadUpdateNotificationState()) {
            updateSwitch.isChecked = true
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "update_notifications_switch")
                param(FirebaseAnalytics.Param.ITEM_NAME, "update_notifications_switch_enabled")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "switch")
            }
        }
        else {
            updateSwitch.isChecked = false
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "update_notifications_switch")
                param(FirebaseAnalytics.Param.ITEM_NAME, "update_notifications_switch_disabled")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "switch")
            }
        }
        updateSwitch.setOnCheckedChangeListener { _, isChecked ->
            vibration(vibrationData)
            if(isChecked) {
                updateData.setUpdateNotificationState(true)
                Snackbar.make(constraintLayout, getString(R.string.enabled_update_notifications), Snackbar.LENGTH_SHORT).show()
                Firebase.messaging.subscribeToTopic("updates")
                    .addOnCompleteListener { task ->
                        var msg = "Subscribed"
                        if(!task.isSuccessful) {
                            msg = "Subscribe failed"
                            updateSwitch.isChecked = false
                        }
                        Log.d("Updates", msg)
                    }
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "update_notifications_switch")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "update_notifications_switch_enabled")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "switch")
                }
            }
            else {
                updateSwitch.isChecked = false
                updateData.setUpdateNotificationState(false)
                Snackbar.make(constraintLayout, getString(R.string.disabled_update_notifications), Snackbar.LENGTH_SHORT).show()
                Firebase.messaging.unsubscribeFromTopic("updates")
                    .addOnCompleteListener { task ->
                        var msg = "Unsubscribed"
                        if(!task.isSuccessful) {
                            msg = "Unsubscribe failed"
                        }
                        Log.d("Updates", msg)
                    }
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "update_notifications_switch")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "update_notifications_switch_disabled")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "switch")
                }
            }
        }

        val wagesData = WagesData(this)
        val wagesEditText = findViewById<TextInputEditText>(R.id.Wages)

        val editable = Editable.Factory.getInstance().newEditable(wagesData.loadWageAmount().toString())
        wagesEditText.text = editable

        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
            param(FirebaseAnalytics.Param.ITEM_ID, "wages_amount")
            param(FirebaseAnalytics.Param.ITEM_NAME, "wages_data_changed")
            param(FirebaseAnalytics.Param.CONTENT_TYPE, "wages edit text")
            param(FirebaseAnalytics.Param.CONTENT, wagesEditText.text.toString())
        }
        wagesEditText.setOnKeyListener(View.OnKeyListener { _, i, keyEvent ->
            if (i == KeyEvent.KEYCODE_BACK && keyEvent.action == KeyEvent.ACTION_DOWN) {
                wagesEditText.clearFocus()
            }
            if (i == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_UP) {
                wagesData.setWageAmount(wagesEditText.text.toString())
                //wagesEditText.clearFocus()
                hideKeyboard()
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "wages_amount")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "wages_data_changed")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "wages edit text")
                    param(FirebaseAnalytics.Param.CONTENT, wagesEditText.text.toString())
                }
                return@OnKeyListener true
            }
            false
        })

        wagesEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != "") {
                    wagesData.setWageAmount(s.toString())
                    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                        param(FirebaseAnalytics.Param.ITEM_ID, "wages_amount")
                        param(FirebaseAnalytics.Param.ITEM_NAME, "wages_data_changed")
                        param(FirebaseAnalytics.Param.CONTENT_TYPE, "wages edit text")
                        param(FirebaseAnalytics.Param.CONTENT, wagesEditText.text.toString())
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "wages_amount")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "wages_data_changed")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "wages edit text")
                    param(FirebaseAnalytics.Param.CONTENT, wagesEditText.text.toString())
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                wagesData.setWageAmount(s.toString())
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "wages_amount")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "wages_data_changed")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "wages edit text")
                    param(FirebaseAnalytics.Param.CONTENT, wagesEditText.text.toString())
                }
            }
        })

        val breakData = BreakData(this)
        val breakSwitch = findViewById<SwitchMaterial>(R.id.switch2)
        if (breakData.loadBreakState()) {
            breakSwitch.isChecked = true
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "break_switch")
                param(FirebaseAnalytics.Param.ITEM_NAME, "break_switch_enabled")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "switch")
            }
        }
        else {
            breakSwitch.isChecked = false
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "break_switch")
                param(FirebaseAnalytics.Param.ITEM_NAME, "break_switch_disabled")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "switch")
            }
        }
        breakSwitch.setOnCheckedChangeListener { _, isChecked ->
            vibration(vibrationData)
            if (isChecked) {
                breakData.setBreakState(true)
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "break_switch")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "break_switch_enabled")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "switch")
                }
            } else {
                breakData.setBreakState(false)
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "break_switch")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "break_switch_disabled")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "switch")
                }
            }
        }

        cardView5.setOnClickListener {
            vibration(vibrationData)
            val intent = Intent(Intent.ACTION_SEND)
            setTheme(R.style.AMOLED)
            intent.data = Uri.parse("mailto:")
            intent.type = "message/rfc822"
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.recipient)))
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.subject))
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.message))
            try {
                startActivity(Intent.createChooser(intent, getString(R.string.choose_email)))
            } catch (e: Exception) {
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            }
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "feature_request")
                param(FirebaseAnalytics.Param.ITEM_NAME, "feature_request_card_view_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "card view")
            }
        }
        textView19.setOnClickListener {
            vibration(vibrationData)
            val intent = Intent(Intent.ACTION_SEND)
            setTheme(R.style.AMOLED)
            intent.data = Uri.parse("mailto:")
            intent.type = "message/rfc822"
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.recipient)))
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.subject))
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.message))
            try {
                startActivity(Intent.createChooser(intent, getString(R.string.choose_email)))
            } catch (e: Exception) {
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            }
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "feature_request")
                param(FirebaseAnalytics.Param.ITEM_NAME, "feature_request_text_view_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "text view")
            }
        }
        textView21.setOnClickListener {
            vibration(vibrationData)
            val intent = Intent(Intent.ACTION_SEND)
            setTheme(R.style.AMOLED)
            intent.data = Uri.parse("mailto:")
            intent.type = "message/rfc822"
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.recipient)))
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.subject))
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.message))
            try {
                startActivity(Intent.createChooser(intent, getString(R.string.choose_email)))
            } catch (e: Exception) {
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            }
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "feature_request")
                param(FirebaseAnalytics.Param.ITEM_NAME, "feature_request_text_view_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "text view")
            }
        }

        cardView6.setOnClickListener {
            vibration(vibrationData)
            val reviewManager = ReviewManagerFactory.create(this)
            val requestReviewFlow = reviewManager.requestReviewFlow()
            requestReviewFlow.addOnCompleteListener { request ->
                if (request.isSuccessful) {
                    val reviewInfo = request.result
                    val flow = reviewManager.launchReviewFlow(this, reviewInfo)
                    flow.addOnCompleteListener {

                    }
                } else {
                    Snackbar.make(constraintLayout, getString(R.string.please_try_again), Snackbar.LENGTH_SHORT).show()
                }
            }
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "leave_a_review")
                param(FirebaseAnalytics.Param.ITEM_NAME, "leave_a_review_card_view_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "card view")
            }
        }
        textView18.setOnClickListener {
            vibration(vibrationData)
            val reviewManager = ReviewManagerFactory.create(this)
            val requestReviewFlow = reviewManager.requestReviewFlow()
            requestReviewFlow.addOnCompleteListener { request ->
                if (request.isSuccessful) {
                    val reviewInfo = request.result
                    val flow = reviewManager.launchReviewFlow(this, reviewInfo)
                    flow.addOnCompleteListener {

                    }
                } else {
                    Snackbar.make(constraintLayout, getString(R.string.please_try_again), Snackbar.LENGTH_SHORT).show()
                }
            }
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "leave_a_review")
                param(FirebaseAnalytics.Param.ITEM_NAME, "leave_a_review_text_view_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "text view")
            }
        }
        textView23.setOnClickListener {
            vibration(vibrationData)
            val reviewManager = ReviewManagerFactory.create(this)
            val requestReviewFlow = reviewManager.requestReviewFlow()
            requestReviewFlow.addOnCompleteListener { request ->
                if (request.isSuccessful) {
                    val reviewInfo = request.result
                    val flow = reviewManager.launchReviewFlow(this, reviewInfo)
                    flow.addOnCompleteListener {

                    }
                } else {
                    Snackbar.make(constraintLayout, getString(R.string.please_try_again), Snackbar.LENGTH_SHORT).show()
                }
            }
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "leave_a_review")
                param(FirebaseAnalytics.Param.ITEM_NAME, "leave_a_review_text_view_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "text view")
            }
        }

        cardView7.setOnClickListener {
            vibration(vibrationData)
            val intent = Intent(Intent.ACTION_SEND)
            intent.data = Uri.parse("mailto:")
            intent.type = "message/rfc822"
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.recipient)))
            intent.putExtra(Intent.EXTRA_SUBJECT, "Bug Report")
            intent.putExtra(
                Intent.EXTRA_TEXT, getString(R.string.bug_report)
            )
            startActivity(Intent.createChooser(intent, getString(R.string.choose_email)))
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "report_a_bug")
                param(FirebaseAnalytics.Param.ITEM_NAME, "report_a_bug_card_view_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "card view")
            }
        }
        textView31.setOnClickListener {
            vibration(vibrationData)
            val intent = Intent(Intent.ACTION_SEND)
            intent.data = Uri.parse("mailto:")
            intent.type = "message/rfc822"
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.recipient)))
            intent.putExtra(Intent.EXTRA_SUBJECT, "Bug Report")
            intent.putExtra(
                Intent.EXTRA_TEXT, getString(R.string.bug_report)
            )
            startActivity(Intent.createChooser(intent, getString(R.string.choose_email)))
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "report_a_bug")
                param(FirebaseAnalytics.Param.ITEM_NAME, "report_a_bug_text_view_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "text view")
            }
        }
        textView32.setOnClickListener {
            vibration(vibrationData)
            val intent = Intent(Intent.ACTION_SEND)
            intent.data = Uri.parse("mailto:")
            intent.type = "message/rfc822"
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.recipient)))
            intent.putExtra(Intent.EXTRA_SUBJECT, "Bug Report")
            intent.putExtra(
                Intent.EXTRA_TEXT, getString(R.string.bug_report)
            )
            startActivity(Intent.createChooser(intent, getString(R.string.choose_email)))
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "report_a_bug")
                param(FirebaseAnalytics.Param.ITEM_NAME, "report_a_bug_text_view_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "text view")
            }
        }

        cardView8.setOnClickListener {
            vibration(vibrationData)
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "other_apps")
                param(FirebaseAnalytics.Param.ITEM_NAME, "other_apps_card_view_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "card view")
            }
            val fragment = BottomSheet.newInstance()
            fragment.show(supportFragmentManager, "my_bs")
        }
        textView33.setOnClickListener {
            vibration(vibrationData)
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "other_apps")
                param(FirebaseAnalytics.Param.ITEM_NAME, "other_apps_text_view_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "text view")
            }
            val fragment = BottomSheet.newInstance()
            fragment.show(supportFragmentManager, "my_bs")
        }

        textView34.setOnClickListener {
            vibration(vibrationData)
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "other_apps")
                param(FirebaseAnalytics.Param.ITEM_NAME, "other_apps_text_view_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "text view")
            }
            val fragment = BottomSheet.newInstance()
            fragment.show(supportFragmentManager, "my_bs")
        }

        cardView9.setOnClickListener {
            vibration(vibrationData)
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle(getString(R.string.about_me)).setMessage(getString(R.string.about_me_message))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                    dialog.cancel()
                }
            val alert = alertDialog.create()
            alert.show()
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "about_me")
                param(FirebaseAnalytics.Param.ITEM_NAME, "about_me_card_view_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "card view")
            }
        }
        textView35.setOnClickListener {
            vibration(vibrationData)
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle(getString(R.string.about_me))
                .setMessage(getString(R.string.about_me_message))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                    dialog.cancel()
                }
            val alert = alertDialog.create()
            alert.show()
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "about_me")
                param(FirebaseAnalytics.Param.ITEM_NAME, "about_me_text_view_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "text view")
            }
        }
        textView36.setOnClickListener {
            vibration(vibrationData)
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle(getString(R.string.about_me))
                .setMessage(getString(R.string.about_me_message))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                    dialog.cancel()
                }
            val alert = alertDialog.create()
            alert.show()
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "about_me")
                param(FirebaseAnalytics.Param.ITEM_NAME, "about_me_text_view_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "text view")
            }
        }

        findViewById<CardView>(R.id.cardView10).setOnClickListener {
            vibration(vibrationData)
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "version_info")
                param(FirebaseAnalytics.Param.ITEM_NAME, "version_info_card_view_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "card view")
            }
            val intent = Intent(this, VersionInfoActivity::class.java)
            startActivity(intent)
        }
        textView38.setOnClickListener {
            vibration(vibrationData)
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "version_info")
                param(FirebaseAnalytics.Param.ITEM_NAME, "version_info_text_view_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "text view")
            }
            val intent = Intent(this, VersionInfoActivity::class.java)
            startActivity(intent)
        }
        textView39.setOnClickListener {
            vibration(vibrationData)
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "version_info")
                param(FirebaseAnalytics.Param.ITEM_NAME, "version_info_text_view_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "text view")
            }
            val intent = Intent(this, VersionInfoActivity::class.java)
            startActivity(intent)
        }
    }

    private fun hideKeyboard() {
        val inputManager: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val focusedView = this.currentFocus
        if (focusedView != null) {
            inputManager.hideSoftInputFromWindow(focusedView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            if (Wages!!.hasFocus()) {
                Wages!!.clearFocus()
            }
        }
    }

    private fun vibration(vibrationData: VibrationData) {
        if (vibrationData.loadVibrationState()) {
            val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    private fun restartApplication() {
        finish()
        val i = Intent(applicationContext, this::class.java)
        startActivity(i)
    }

    private fun createFileTEXT() {
        try {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/plain"
                putExtra(Intent.EXTRA_TITLE, getString(R.string.hours_file_name) + ".txt")

            }
            startActivityForResult(intent, CREATE_FILE)
        } catch (e : FileNotFoundException) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.file_not_found), Toast.LENGTH_SHORT).show()
        } catch (e : IOException) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.error_saving), Toast.LENGTH_SHORT).show()
        }
    }

    private fun createFileCSV() {
        try {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/csv"
                putExtra(Intent.EXTRA_TITLE, getString(R.string.hours_file_name) + ".csv")

            }
            startActivityForResult(intent, CREATE_FILE)
        } catch (e : FileNotFoundException) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.file_not_found), Toast.LENGTH_SHORT).show()
        } catch (e : IOException) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.error_saving), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val exportData = ExportData(this)
        if (CREATE_FILE == requestCode) {
            when(resultCode) {
                Activity.RESULT_OK -> {
                    if(data?.data != null) {
                        if(exportData.loadExportFormat() == 1) {
                            var string = ""
                            val datalist = ArrayList<HashMap<String, String>>()
                            datalist.clear()
                            val cursor = dbHandler.getAllRow(this)
                            cursor!!.moveToFirst()

                            while (!cursor.isAfterLast) {
                                val map = HashMap<String, String>()
                                map["id"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ID))
                                map["intime"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_IN))
                                map["out"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_OUT))
                                map["break"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_BREAK))
                                map["total"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TOTAL))
                                map["day"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DAY))

                                datalist.add(map)

                                string += map["id"].toString() + "," +
                                        map["intime"].toString() + "," +
                                        map["out"].toString() + "," +
                                        map["break"].toString() + "," +
                                        map["total"].toString() + "," +
                                        map["day"].toString() + "," + "\n"

                                cursor.moveToNext()
                            }
                            writeInFile(data.data!!, string)
                        }
                        else if(exportData.loadExportFormat() == 0) {
                            var string = ""
                            val datalist = ArrayList<HashMap<String, String>>()
                            datalist.clear()
                            val cursor = dbHandler.getAllRow(this)
                            cursor!!.moveToFirst()

                            while (!cursor.isAfterLast) {
                                val map = HashMap<String, String>()
                                map["id"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ID))
                                map["intime"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_IN))
                                map["out"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_OUT))
                                map["break"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_BREAK))
                                map["total"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TOTAL))
                                map["day"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DAY))

                                datalist.add(map)

                                string += "ID: " + map["id"].toString() + "\n" +
                                        "In Time: " + map["intime"].toString() + "\n" +
                                        "Out Time: " + map["out"].toString() + "\n" +
                                        "Break Time: " + map["break"].toString() + "\n" +
                                        "Total Time: " + map["total"].toString() + "\n" +
                                        "Day: " + map["day"].toString() + "\n" +
                                        "*******************************" + "\n"

                                cursor.moveToNext()
                            }
                            writeInFile(data.data!!, string)
                        }
                    }
                }
                Activity.RESULT_CANCELED -> finishActivity(requestCode)
            }
        }

    }

    private fun writeInFile(@NonNull uri : Uri, @NonNull text : String) {
        val exportData = ExportData(this)
        if(exportData.loadExportFormat() == 1) {
            val outputStream: OutputStream = contentResolver.openOutputStream(uri)!!
            val bw = BufferedWriter(OutputStreamWriter(outputStream))
            try {
                val csvHeader = "ID,In Time,Out Time,Break Time,Total,Day,Year,Time\n"
                bw.append(csvHeader)
                bw.append(text)
                bw.flush()
                bw.close()
                val intent = Intent()
                    .setType("text/csv")
                    .setAction(Intent.ACTION_SEND)
                    .putExtra(Intent.EXTRA_STREAM, Uri.parse(uri.toString()))
                startActivity(Intent.createChooser(intent, "Choose app"))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        else if(exportData.loadExportFormat() == 0) {
            val outputStream: OutputStream = contentResolver.openOutputStream(uri)!!
            val bw = BufferedWriter(OutputStreamWriter(outputStream))
            try {
                bw.write(text)
                bw.flush()
                bw.close()
                val intent = Intent()
                    .setType("text/plain")
                    .setAction(Intent.ACTION_SEND)
                    .putExtra(Intent.EXTRA_STREAM, Uri.parse(uri.toString()))
                startActivity(Intent.createChooser(intent, "Choose app"))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu_settings, menu)
        val historyToggleData = HistoryToggleData(this)
        if (!historyToggleData.loadHistoryState()) {
            val history = menu.findItem(R.id.history)
            history.isVisible = false
            val trash = menu.findItem(R.id.trash)
            trash.isVisible = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val vibrationData = VibrationData(this)
        if (vibrationData.loadVibrationState()) {
            val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        }
        return when (item.itemId) {
            R.id.home -> {
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "home_menu_item_settings")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "home_menu_item_clicked_settings")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "menu_item")
                }
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.changelog -> {
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "patch_notes_menu_item_settings")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "patch_notes_menu_item_clicked_settings")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "menu_item")
                }
                val intent = Intent(this, PatchNotesActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.history -> {
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "history_menu_item_settings")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "history_menu_item_clicked_settings")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "menu_item")
                }
                val intent = Intent(this, HistoryActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.trash -> {
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "trash_menu_item_settings")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "trash_menu_item_clicked_settings")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "menu_item")
                }
                val intent = Intent(this, TrashActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.graph -> {
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "graph_menu_item_settings")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "graph_menu_item_clicked_settings")
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