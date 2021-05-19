package com.cory.hourcalculator.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.cory.hourcalculator.R
import com.cory.hourcalculator.billing.BillingAgent
import com.cory.hourcalculator.billing.BillingCallback
import com.cory.hourcalculator.classes.*
import com.cory.hourcalculator.database.DBHelper
import com.google.android.gms.ads.*
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_settings.*
import java.io.*

class SettingsActivity : AppCompatActivity(), BillingCallback {

    // Not initialized variables
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var darkThemeData: DarkThemeData
    private val createFile = 1
    private val dbHandler = DBHelper(this, null)
    private val permissionRequestCode = 1

    private var billingAgent: BillingAgent? = null

    var testDeviceId = listOf(getString(R.string.oneplus_device_id))

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
        val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceId).build()
        MobileAds.setRequestConfiguration(configuration)
        val mAdView = findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        mAdView.adListener = object : AdListener() {
        }

        // initializes the vibrationData class
        val vibrationData = VibrationData(this)

        val theme = findViewById<TextView>(R.id.theme)
        theme.setOnClickListener {
            vibration(vibrationData)
            val intent = Intent(this, ThemeActivity::class.java)
            startActivity(intent)
            if(!PerformanceModeData(this).loadPerformanceMode()) {
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            else {
                overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
            }
        }
        val themeSubtitle = findViewById<TextView>(R.id.themeSubtitle)
        themeSubtitle.setOnClickListener {
            vibration(vibrationData)
            val intent = Intent(this, ThemeActivity::class.java)
            startActivity(intent)
            if(!PerformanceModeData(this).loadPerformanceMode()) {
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            else {
                overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
            }
        }
        val themeCardView = findViewById<CardView>(R.id.themeCardView)
        themeCardView.setOnClickListener {
            vibration(vibrationData)
            val intent = Intent(this, ThemeActivity::class.java)
            startActivity(intent)
            if(!PerformanceModeData(this).loadPerformanceMode()) {
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            else {
                overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
            }
        }

        val layout = findViewById<TextView>(R.id.layout)
        layout.setOnClickListener {
            vibration(vibrationData)
            val intent = Intent(this, LayoutSettings::class.java)
            startActivity(intent)
            if(!PerformanceModeData(this).loadPerformanceMode()) {
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            else {
                overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
            }
        }
        val layoutSubtitle = findViewById<TextView>(R.id.layoutSubtitle)
        layoutSubtitle.setOnClickListener {
            vibration(vibrationData)
            val intent = Intent(this, LayoutSettings::class.java)
            startActivity(intent)
            if(!PerformanceModeData(this).loadPerformanceMode()) {
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            else {
                overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
            }
        }
        val layoutCardView = findViewById<CardView>(R.id.layoutCardView)
        layoutCardView.setOnClickListener {
            vibration(vibrationData)
            val intent = Intent(this, LayoutSettings::class.java)
            startActivity(intent)
            if(!PerformanceModeData(this).loadPerformanceMode()) {
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            else {
                overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
            }
        }

        val notificationCardView = findViewById<CardView>(R.id.notificationsCardView)
        notificationCardView.setOnClickListener {
            vibration(vibrationData)
            val intent = Intent(this, NotificationSettingActivity::class.java)
            startActivity(intent)
            if(!PerformanceModeData(this).loadPerformanceMode()) {
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            else {
                overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
            }
        }
        val notificationTitle = findViewById<TextView>(R.id.notifications)
        notificationTitle.setOnClickListener {
            vibration(vibrationData)
            val intent = Intent(this, NotificationSettingActivity::class.java)
            startActivity(intent)
            if(!PerformanceModeData(this).loadPerformanceMode()) {
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            else {
                overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
            }
        }
        val notificationSubtitle = findViewById<TextView>(R.id.notificationsSubtitle)
        notificationSubtitle.setOnClickListener {
            vibration(vibrationData)
            val intent = Intent(this, NotificationSettingActivity::class.java)
            startActivity(intent)
            if(!PerformanceModeData(this).loadPerformanceMode()) {
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            else {
                overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
            }
        }

        cardViewGithub.setOnClickListener {
            vibration(vibrationData)
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
            vibration(vibrationData)
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
            vibration(vibrationData)
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

        val donateSelection = arrayOf(getString(R.string.five_dollar))
        var donateSelectedItemIndex = 0

        donateHeading.setOnClickListener {
            vibration(vibrationData)
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle(getString(R.string.please_donate))
            alertDialog.setSingleChoiceItems(donateSelection, donateSelectedItemIndex) { _, which ->
                    donateSelectedItemIndex = which
                }
            alertDialog.setPositiveButton(R.string.donate) { _, _ ->
                vibration(vibrationData)
                billingAgent?.purchaseView(0)
            }
            alertDialog.setNegativeButton(R.string.cancel) { dialog, _ ->
                vibration(vibrationData)
                dialog.dismiss()
            }
            val alert = alertDialog.create()
            alert.show()
        }

        donateSubtitle.setOnClickListener {
            vibration(vibrationData)
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle(getString(R.string.please_donate))
            alertDialog.setSingleChoiceItems(donateSelection, donateSelectedItemIndex) { _, which ->
                donateSelectedItemIndex = which
            }
            alertDialog.setPositiveButton(getString(R.string.donate)) { _, _ ->
                vibration(vibrationData)
                billingAgent?.purchaseView(0)
            }
            alertDialog.setNegativeButton(R.string.cancel) { dialog, _ ->
                vibration(vibrationData)
                dialog.dismiss()
            }
            val alert = alertDialog.create()
            alert.show()
        }

        donateCardView.setOnClickListener {
            vibration(vibrationData)
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle(getString(R.string.please_donate))
            alertDialog.setSingleChoiceItems(donateSelection, donateSelectedItemIndex) { _, which ->
                donateSelectedItemIndex = which
            }
            alertDialog.setPositiveButton(getString(R.string.donate)) { _, _ ->
                vibration(vibrationData)
                billingAgent?.purchaseView(0)
            }
            alertDialog.setNegativeButton(R.string.cancel) { dialog, _ ->
                vibration(vibrationData)
                dialog.dismiss()
            }
            val alert = alertDialog.create()
            alert.show()
        }

        val exportData = ExportData(this)

        var selectedItemIndex = exportData.loadExportFormat()

        val selection = arrayOf(getString(R.string.text_file), getString(R.string.spread_sheet))

        exportHeading.setOnClickListener {
            vibration(vibrationData)
            val list = listOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val managePermissions = ManagePermissions(this, list, permissionRequestCode)
            if (dbHandler.getCount() > 0) {
                if (managePermissions.checkPermissions()) {
                    val alertDialog = AlertDialog.Builder(this)
                        .setTitle(getString(R.string.choose_format))
                        .setSingleChoiceItems(selection, selectedItemIndex) { _, which ->
                            selectedItemIndex = which
                            exportData.setExportFormat(selectedItemIndex)
                        }
                        .setPositiveButton(R.string.ok) {_, _ ->
                            vibration(vibrationData)
                            exportData.setExportFormat(selectedItemIndex)
                            if(exportData.loadExportFormat() == 1) {
                                createFileCSV()
                            }
                            else if(exportData.loadExportFormat() == 0) {
                                createFileTEXT()
                            }
                        }
                        .setNeutralButton(R.string.cancel) { dialog, _ ->
                            vibration(vibrationData)
                            dialog.dismiss()
                        }
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

        val list = listOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        exportSubtitle.setOnClickListener {
            vibration(vibrationData)
            val managePermissions = ManagePermissions(this, list, permissionRequestCode)
            if (dbHandler.getCount() > 0) {
                if (managePermissions.checkPermissions()) {
                    val alertDialog = AlertDialog.Builder(this)
                        .setTitle(getString(R.string.choose_format))
                        .setSingleChoiceItems(selection, selectedItemIndex) { _, which ->
                            selectedItemIndex = which
                            exportData.setExportFormat(selectedItemIndex)
                        }
                        .setPositiveButton(R.string.ok) {_, _ ->
                            vibration(vibrationData)
                            exportData.setExportFormat(selectedItemIndex)
                            if(exportData.loadExportFormat() == 1) {
                                createFileCSV()
                            }
                            else if(exportData.loadExportFormat() == 0) {
                                createFileTEXT()
                            }
                        }
                        .setNeutralButton(R.string.cancel) { dialog, _ ->
                            vibration(vibrationData)
                            dialog.dismiss()
                        }
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
        ExportCardView.setOnClickListener {
            vibration(vibrationData)
            val managePermissions = ManagePermissions(this, list, permissionRequestCode)
            if (dbHandler.getCount() > 0) {
                if (managePermissions.checkPermissions()) {
                    val alertDialog = AlertDialog.Builder(this)
                        .setTitle(getString(R.string.choose_format))
                        .setSingleChoiceItems(selection, selectedItemIndex) { _, which ->
                            selectedItemIndex = which
                            exportData.setExportFormat(selectedItemIndex)
                        }
                        .setPositiveButton(R.string.ok) {_, _ ->
                            vibration(vibrationData)
                            exportData.setExportFormat(selectedItemIndex)
                            if(exportData.loadExportFormat() == 1) {
                                createFileCSV()
                            }
                            else if(exportData.loadExportFormat() == 0) {
                                createFileTEXT()
                            }
                        }
                        .setNeutralButton(R.string.cancel) { dialog, _ ->
                            vibration(vibrationData)
                            dialog.dismiss()
                        }
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

        reviewCardView.setOnClickListener {
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
        reviewHeading.setOnClickListener {
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
        reviewSubtitle.setOnClickListener {
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

        bugCardView.setOnClickListener {
            vibration(vibrationData)
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra("url", getString(R.string.github_issue_link))
            startActivity(intent)
            if(!PerformanceModeData(this).loadPerformanceMode()) {
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            else {
                overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
            }
        }
        bugHeading.setOnClickListener {
            vibration(vibrationData)
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra("url", getString(R.string.github_issue_link))
            startActivity(intent)
            if(!PerformanceModeData(this).loadPerformanceMode()) {
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            else {
                overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
            }
        }
        bugSubtitle.setOnClickListener {
            vibration(vibrationData)
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra("url", getString(R.string.github_issue_link))
            startActivity(intent)
            if(!PerformanceModeData(this).loadPerformanceMode()) {
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            else {
                overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
            }
        }

        otherAppsCardView.setOnClickListener {
            vibration(vibrationData)
            val fragment = BottomSheet.newInstance()
            fragment.show(supportFragmentManager, "my_bs")
        }
        otherAppHeading.setOnClickListener {
            vibration(vibrationData)
            val fragment = BottomSheet.newInstance()
            fragment.show(supportFragmentManager, "my_bs")
        }

        otherAppsSubtitle.setOnClickListener {
            vibration(vibrationData)
            val fragment = BottomSheet.newInstance()
            fragment.show(supportFragmentManager, "my_bs")
        }

        aboutCardView.setOnClickListener {
            vibration(vibrationData)
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle(getString(R.string.about_me))
                .setMessage(getString(R.string.about_me_message))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                    vibration(vibrationData)
                    dialog.cancel()
                }
            val alert = alertDialog.create()
            alert.show()
        }
        aboutMeHeading.setOnClickListener {
            vibration(vibrationData)
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle(getString(R.string.about_me))
                .setMessage(getString(R.string.about_me_message))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                    vibration(vibrationData)
                    dialog.cancel()
                }
            val alert = alertDialog.create()
            alert.show()
        }
        aboutMeSubtitle.setOnClickListener {
            vibration(vibrationData)
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle(getString(R.string.about_me))
                .setMessage(getString(R.string.about_me_message))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                    vibration(vibrationData)
                    dialog.cancel()
                }
            val alert = alertDialog.create()
            alert.show()
        }

        findViewById<CardView>(R.id.versionCardView).setOnClickListener {
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
        versionHeading.setOnClickListener {
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
        versionSubtitle.setOnClickListener {
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

        val deleteCardView = findViewById<CardView>(R.id.deleteDataCardView)
        deleteCardView.setOnClickListener {
            val intent = Intent(this, DeleteAppDataActivity::class.java)
            startActivity(intent)
            if(!PerformanceModeData(this).loadPerformanceMode()) {
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            else {
                overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
            }
        }
        val deleteHeading = findViewById<TextView>(R.id.deleteDataHeading)
        deleteHeading.setOnClickListener {
            val intent = Intent(this, DeleteAppDataActivity::class.java)
            startActivity(intent)
            if(!PerformanceModeData(this).loadPerformanceMode()) {
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            else {
                overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
            }
        }
        val deleteSubtitle = findViewById<TextView>(R.id.deleteDataSubtitle)
        deleteSubtitle.setOnClickListener {
            val intent = Intent(this, DeleteAppDataActivity::class.java)
            startActivity(intent)
            if(!PerformanceModeData(this).loadPerformanceMode()) {
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            else {
                overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
            }
        }
    }

    private fun vibration(vibrationData: VibrationData) {
        if (vibrationData.loadVibrationState()) {
            val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(5, VibrationEffect.DEFAULT_AMPLITUDE))
        }
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

    override fun onRestart() {
        super.onRestart()
        val intent = Intent(this, this::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        if(!PerformanceModeData(this).loadPerformanceMode()) {
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
        else {
            overridePendingTransition(0, 0)
        }
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