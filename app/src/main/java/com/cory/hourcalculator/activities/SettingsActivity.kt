@file:Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")

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
import com.android.billingclient.api.*
import com.cory.hourcalculator.R
import com.cory.hourcalculator.billing.BillingAgent
import com.cory.hourcalculator.billing.BillingCallback
import com.cory.hourcalculator.classes.*
import com.cory.hourcalculator.database.DBHelper
import com.cory.hourcalculator.database.DBHelperTrash
import com.google.android.gms.ads.*
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.activity_settings.constraintLayout
import java.io.*

class SettingsActivity : AppCompatActivity(), BillingCallback {

    // Not initialized variables
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var darkThemeData: DarkThemeData
    private val createFile = 1
    private val dbHandler = DBHelper(this, null)
    private val permissionRequestCode = 1

    private val dbHandlerTrash = DBHelperTrash(this, null)
    private val dataList = ArrayList<HashMap<String, String>>()

    private var billingAgent: BillingAgent? = null

    private var position : Int = 0


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

        billingAgent = BillingAgent(this, this)

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

        cardViewGithub.setOnClickListener {
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra("url", getString(R.string.github_link))
            startActivity(intent)
            if(!PerformanceModeData(this).loadPerformanceMode()) {
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            else {
                overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
            }
        }
        textViewGithubHeading.setOnClickListener {
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra("url", getString(R.string.github_link))
            startActivity(intent)
            if(!PerformanceModeData(this).loadPerformanceMode()) {
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            else {
                overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
            }
        }
        textViewGithubCaption.setOnClickListener {
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra("url", getString(R.string.github_link))
            startActivity(intent)
            if(!PerformanceModeData(this).loadPerformanceMode()) {
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            else {
                overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
            }
        }

        val performanceSwitch = findViewById<SwitchMaterial>(R.id.switchPerformance)
        performanceSwitch.isChecked = PerformanceModeData(this).loadPerformanceMode()
        performanceSwitch.setOnCheckedChangeListener { _, isChecked ->
            vibration(VibrationData(this))
            if (isChecked) {
                PerformanceModeData(this).setPerformanceMode(true)
                Snackbar.make(constraintLayout, getString(R.string.enabled_performance_mode), Snackbar.LENGTH_SHORT).show()
            } else {
                performanceSwitch.isChecked = false
                PerformanceModeData(this).setPerformanceMode(false)
                Snackbar.make(constraintLayout, getString(R.string.disabled_performance_mode), Snackbar.LENGTH_SHORT).show()
            }
        }

        val donateSelection = arrayOf(getString(R.string.five_dollar))
        var donateSelectedItemIndex = 0
        var donateSelectedItem = donateSelection[donateSelectedItemIndex]

        textView42.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle(getString(R.string.please_donate))
            alertDialog.setSingleChoiceItems(donateSelection, donateSelectedItemIndex) { _, which ->
                    donateSelectedItemIndex = which
                    donateSelectedItem = donateSelection[which]
                }
            alertDialog.setPositiveButton(R.string.donate) { _, _ ->
                billingAgent?.purchaseView(0)
            }
            val alert = alertDialog.create()
            alert.show()
        }

        txtDonateSettings.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle(getString(R.string.please_donate))
            alertDialog.setSingleChoiceItems(donateSelection, donateSelectedItemIndex) { _, which ->
                donateSelectedItemIndex = which
                donateSelectedItem = donateSelection[which]
            }
            alertDialog.setPositiveButton(getString(R.string.donate)) { _, _ ->
                billingAgent?.purchaseView(0)
            }
            val alert = alertDialog.create()
            alert.show()
        }

        textView41.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle(getString(R.string.please_donate))
            alertDialog.setSingleChoiceItems(donateSelection, donateSelectedItemIndex) { _, which ->
                donateSelectedItemIndex = which
                donateSelectedItem = donateSelection[which]
            }
            alertDialog.setPositiveButton(getString(R.string.donate)) { _, _ ->
                billingAgent?.purchaseView(0)
            }
            val alert = alertDialog.create()
            alert.show()
        }

        cardView15.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle(getString(R.string.please_donate))
            alertDialog.setSingleChoiceItems(donateSelection, donateSelectedItemIndex) { _, which ->
                donateSelectedItemIndex = which
                donateSelectedItem = donateSelection[which]
            }
            alertDialog.setPositiveButton(getString(R.string.donate)) { _, _ ->
                billingAgent?.purchaseView(0)
            }
            val alert = alertDialog.create()
            alert.show()
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
                            exportData.setExportFormat(selectedItemIndex)
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
                            exportData.setExportFormat(selectedItemIndex)
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
                            exportData.setExportFormat(selectedItemIndex)
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
                            exportData.setExportFormat(selectedItemIndex)
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
        vibrationSwitch.isChecked = vibrationData.loadVibrationState()
        // listens for the switch to be changed
        vibrationSwitch.setOnCheckedChangeListener { _, isChecked ->
            vibration(vibrationData)
            if (isChecked) {
                vibrationData.setVibrationState(true)
                Snackbar.make(constraintLayout, getString(R.string.enabled_vibration), Snackbar.LENGTH_SHORT).show()
            } else {
                vibrationSwitch.isChecked = false
                vibrationData.setVibrationState(false)
                Snackbar.make(constraintLayout, getString(R.string.disabled_vibration), Snackbar.LENGTH_SHORT).show()
            }
        }

        val switch = findViewById<SwitchMaterial>(R.id.switch1)
        switch.isChecked = darkThemeData.loadDarkModeState()

        switch.setOnCheckedChangeListener { _, isChecked ->
            vibration(vibrationData)
            if (isChecked) {
                switch.isChecked = true
                darkThemeData.setDarkModeState(true)
                Handler(Looper.getMainLooper()).postDelayed({
                    restartApplication()
                }, 200)
            } else {
                switch.isChecked = false
                darkThemeData.setDarkModeState(false)
                Handler(Looper.getMainLooper()).postDelayed({
                    restartApplication()
                }, 200)
            }
        }

        val historySwitch = findViewById<SwitchMaterial>(R.id.switch4)
        val historyToggleData = HistoryToggleData(this)
        historySwitch.isChecked = historyToggleData.loadHistoryState()

        historySwitch.setOnCheckedChangeListener { _, isChecked ->
            vibration(vibrationData)
            if (isChecked) {
                historySwitch.isChecked = true
                historyToggleData.setHistoryToggle(true)
                Snackbar.make(constraintLayout, getString(R.string.enabled_history), Snackbar.LENGTH_SHORT).show()
                invalidateOptionsMenu()
            } else {
                historySwitch.isChecked = false
                historyToggleData.setHistoryToggle(false)
                Snackbar.make(constraintLayout, getString(R.string.disabled_history), Snackbar.LENGTH_SHORT).show()
                invalidateOptionsMenu()
                val alertDialog = AlertDialog.Builder(this)
                alertDialog.setTitle(getString(R.string.history))
                alertDialog.setMessage(getString(R.string.what_would_you_like_to_do_with_history))
                alertDialog.setPositiveButton(getString(R.string.delete)) { _, _ ->
                    dbHandler.deleteAll()
                    Snackbar.make(constraintLayout, getString(R.string.deleted_all_of_hour_history), Snackbar.LENGTH_SHORT).show()
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
                    Snackbar.make(constraintLayout, getString(R.string.moved_all_hours_to_trash), Snackbar.LENGTH_SHORT).show()
                }
                alertDialog.setNeutralButton(getString(R.string.nothing), null)
                alertDialog.create().show()
            }
        }

        val trashAutomaticDeletion = TrashAutomaticDeletion(this)
        val trashSwitch = findViewById<SwitchMaterial>(R.id.switch5)
        trashSwitch.isChecked = trashAutomaticDeletion.loadTrashDeletionState()
        trashSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                vibration(vibrationData)
                trashAutomaticDeletion.setTrashDeletionState(true)
                Snackbar.make(constraintLayout, getString(R.string.enable_automatic_deletion), Snackbar.LENGTH_SHORT).show()
            } else {
                vibration(vibrationData)
                trashSwitch.isChecked = false
                trashAutomaticDeletion.setTrashDeletionState(false)
                Snackbar.make(constraintLayout, getString(R.string.disable_automatic_deletion), Snackbar.LENGTH_SHORT).show()
            }
        }

        val updateData = UpdateData(this)
        val updateSwitch = findViewById<SwitchMaterial>(R.id.switch6)
        updateSwitch.isChecked = updateData.loadUpdateNotificationState()
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
            }
        }

        val wagesData = WagesData(this)
        val wagesEditText = findViewById<TextInputEditText>(R.id.Wages)

        val editable = Editable.Factory.getInstance().newEditable(wagesData.loadWageAmount().toString())
        wagesEditText.text = editable

        wagesEditText.setOnKeyListener(View.OnKeyListener { _, i, keyEvent ->
            if (i == KeyEvent.KEYCODE_BACK && keyEvent.action == KeyEvent.ACTION_DOWN) {
                hideKeyboard(wagesEditText)
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

        val breakData = BreakData(this)
        val breakSwitch = findViewById<SwitchMaterial>(R.id.switch2)
        breakSwitch.isChecked = breakData.loadBreakState()
        breakSwitch.setOnCheckedChangeListener { _, isChecked ->
            vibration(vibrationData)
            if (isChecked) {
                breakData.setBreakState(true)
                Snackbar.make(constraintLayout, getString(R.string.enabled_break_text_box), Snackbar.LENGTH_SHORT).show()
            } else {
                breakData.setBreakState(false)
                Snackbar.make(constraintLayout, getString(R.string.disabled_break_text_box), Snackbar.LENGTH_SHORT).show()
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
                e.printStackTrace()
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
                e.printStackTrace()
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
                e.printStackTrace()
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
        }

        cardView7.setOnClickListener {
            vibration(vibrationData)
            val intent = Intent(Intent.ACTION_SEND)
            intent.data = Uri.parse("mailto:")
            intent.type = "message/rfc822"
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.recipient)))
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.bug_report))
            intent.putExtra(
                Intent.EXTRA_TEXT, getString(R.string.bug_report)
            )
            startActivity(Intent.createChooser(intent, getString(R.string.choose_email)))
        }
        textView31.setOnClickListener {
            vibration(vibrationData)
            val intent = Intent(Intent.ACTION_SEND)
            intent.data = Uri.parse("mailto:")
            intent.type = "message/rfc822"
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.recipient)))
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.bug_report))
            intent.putExtra(
                Intent.EXTRA_TEXT, getString(R.string.bug_report)
            )
            startActivity(Intent.createChooser(intent, getString(R.string.choose_email)))
        }
        textView32.setOnClickListener {
            vibration(vibrationData)
            val intent = Intent(Intent.ACTION_SEND)
            intent.data = Uri.parse("mailto:")
            intent.type = "message/rfc822"
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.recipient)))
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.bug_report))
            intent.putExtra(
                Intent.EXTRA_TEXT, getString(R.string.bug_report)
            )
            startActivity(Intent.createChooser(intent, getString(R.string.choose_email)))
        }

        cardView8.setOnClickListener {
            vibration(vibrationData)
            val fragment = BottomSheet.newInstance()
            fragment.show(supportFragmentManager, "my_bs")
        }
        textView33.setOnClickListener {
            vibration(vibrationData)
            val fragment = BottomSheet.newInstance()
            fragment.show(supportFragmentManager, "my_bs")
        }

        textView34.setOnClickListener {
            vibration(vibrationData)
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
        }

        findViewById<CardView>(R.id.cardView10).setOnClickListener {
            vibration(vibrationData)
            val intent = Intent(this, VersionInfoActivity::class.java)
            startActivity(intent)
            if(!PerformanceModeData(this).loadPerformanceMode()) {
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            else {
                overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
            }
        }
        textView38.setOnClickListener {
            vibration(vibrationData)
            val intent = Intent(this, VersionInfoActivity::class.java)
            startActivity(intent)
            if(!PerformanceModeData(this).loadPerformanceMode()) {
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            else {
                overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
            }
        }
        textView39.setOnClickListener {
            vibration(vibrationData)
            val intent = Intent(this, VersionInfoActivity::class.java)
            startActivity(intent)
            if(!PerformanceModeData(this).loadPerformanceMode()) {
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            else {
                overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
            }
        }
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
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    private fun restartApplication() {
        val intent = this.intent
        finish()
        overridePendingTransition(R.anim.no_animation,R.anim.no_animation)
        startActivity(intent)
        overridePendingTransition(R.anim.no_animation,R.anim.no_animation)
    }

    private fun createFileTEXT() {
        try {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/plain"
                putExtra(Intent.EXTRA_TITLE, getString(R.string.hours_file_name) + ".txt")

            }
            startActivityForResult(intent, createFile)
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
            startActivityForResult(intent, createFile)
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
        if (createFile == requestCode) {
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

                                string += getString(R.string.id_text) + map["id"].toString() + "\n" +
                                        getString(R.string.in_time_text) + map["intime"].toString() + "\n" +
                                        getString(R.string.out_time_text) + map["out"].toString() + "\n" +
                                        getString(R.string.break_time_text) + map["break"].toString() + "\n" +
                                        getString(R.string.total_time_text) + map["total"].toString() + "\n" +
                                        getString(R.string.day_text) + map["day"].toString() + "\n" +
                                        getString(R.string.asterisks_text) + "\n"

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

                                string += getString(R.string.id_text) + map["id"].toString() + "\n" +
                                        getString(R.string.in_time_text) + map["intime"].toString() + "\n" +
                                        getString(R.string.out_time_text) + map["out"].toString() + "\n" +
                                        getString(R.string.break_time_text) + map["break"].toString() + "\n" +
                                        getString(R.string.total_time_text) + map["total"].toString() + "\n" +
                                        getString(R.string.day_text) + map["day"].toString() + "\n" +
                                        getString(R.string.asterisks_text) + "\n"

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
                val csvHeader = getString(R.string.csv_heading)
                bw.append(csvHeader)
                bw.append(text)
                bw.flush()
                bw.close()
                val intent = Intent()
                    .setType("text/csv")
                    .setAction(Intent.ACTION_SEND)
                    .putExtra(Intent.EXTRA_STREAM, Uri.parse(uri.toString()))
                startActivity(Intent.createChooser(intent, getString(R.string.choose_app)))
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
                startActivity(Intent.createChooser(intent, getString(R.string.choose_app)))
            } catch (e: IOException) {
                e.printStackTrace()
            }
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
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
        else {
            overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
        }
    }

    override fun onDestroy() {
        billingAgent?.onDestroy()
        billingAgent = null
        super.onDestroy()

    }

    override fun onTokenConsumed() {
        Toast.makeText(this, getString(R.string.thanks_for_donation), Toast.LENGTH_LONG).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu_settings, menu)
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