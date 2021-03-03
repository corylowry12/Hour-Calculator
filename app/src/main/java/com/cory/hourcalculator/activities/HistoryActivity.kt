package com.cory.hourcalculator.activities

import android.content.Context
import android.content.Intent
import android.os.*
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AbsListView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cory.hourcalculator.R
import com.cory.hourcalculator.adapters.CustomAdapter
import com.cory.hourcalculator.classes.*
import com.cory.hourcalculator.database.DBHelper
import com.google.android.gms.ads.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_history.*


class HistoryActivity : AppCompatActivity() {

    private val dbHandler = DBHelper(this, null)
    private val dataList = ArrayList<HashMap<String, String>>()
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private lateinit var saveData: DarkThemeData

    override fun onCreate(savedInstanceState: Bundle?) {
        saveData = DarkThemeData(this)
        if (saveData.loadDarkModeState()) {
            setTheme(R.style.AMOLED)
        } else {
            setTheme(R.style.AppTheme)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        window.setBackgroundDrawable(null)

        firebaseAnalytics = Firebase.analytics

        MobileAds.initialize(this)
        val adView = AdView(this)
        adView.adSize = AdSize.BANNER
        adView.adUnitId = "ca-app-pub-4546055219731501/5171269817"
        val mAdView = findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        mAdView.adListener = object : AdListener() {

        }


        floatingActionButtonHistory.setOnClickListener {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "fab_history")
                param(FirebaseAnalytics.Param.ITEM_NAME, "fab_history_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "floating_action_button")
            }
            listView.smoothScrollToPosition(0)
        }

        listView.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {

                if(firstVisibleItem > 0) {
                    floatingActionButtonHistory.show()

                }
                else {
                    floatingActionButtonHistory.hide()
                }
            }

            override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {
            }
        })
    }

    fun update() {
        val index = listView.firstVisiblePosition
        val v = listView.getChildAt(0)
        val top = if (v == null) 0 else v.top - listView.paddingTop
        loadIntoList()
        listView.setSelectionFromTop(index, top)
        if(dbHandler.getCount() == 0) {
            textViewTotalHours.visibility = View.INVISIBLE
        }
    }

    private fun loadIntoList() {

        val wagesData = WagesData(this)
        textViewWages.visibility = View.INVISIBLE

        var y = 0.0
        dataList.clear()
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
            dataList.add(map)

            val array = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TOTAL)).toString()

            y += array.toDouble()

            val output = String.format("%.2f", y)
            textViewTotalHours.text = getString(R.string.total_hours_history, output)
            textViewTotalHours.visibility = View.VISIBLE

            val wages = output.toDouble() * wagesData.loadWageAmount().toString().toDouble()
            val wagesrounded = String.format("%.2f", wages)
            textViewWages.text = getString(R.string.total_wages, wagesrounded)
            textViewWages.visibility = View.VISIBLE

            cursor.moveToNext()
        }
        textViewSize.text = getString(R.string.amount_of_hours_saved, dbHandler.getCount())
        textViewSize.visibility = View.VISIBLE
        val listView = findViewById<ListView>(R.id.listView)
        listView.adapter = CustomAdapter(this@HistoryActivity, dataList)

    }

    fun menuItem(id : String, name : String, type : String) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
            param(FirebaseAnalytics.Param.ITEM_ID, id)
            param(FirebaseAnalytics.Param.ITEM_NAME, name)
            param(FirebaseAnalytics.Param.CONTENT_TYPE, type)
        }
    }

    override fun onResume() {
        super.onResume()
        loadIntoList()
    }

    override fun onRestart() {
        super.onRestart()
        recreate()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu_history, menu)
        val historyToggleData = HistoryToggleData(this)
        if (!historyToggleData.loadHistoryState()) {
            val trash = menu.findItem(R.id.trash)
            trash.isVisible = false
        }
        val sortData = SortData(this)
        val sort = sortData.loadSortState()
        if (sort == getString(R.string.day_DESC)) {
            val item = menu.findItem(R.id.menuSortByLastEntered)
            item.title = getString(R.string.last_entered)
        }
        if (sort == getString(R.string.day_ASC)) {
            val item = menu.findItem(R.id.menuSortByFirstEntered)
            item.title = getString(R.string.first_entered)
        }
        if (sort == getString(R.string.total_ASC)) {
            val item = menu.findItem(R.id.menuSortByLeastHours)
            item.title = getString(R.string.least_hours)
        }
        if (sort == getString(R.string.total_DESC)) {
            val item = menu.findItem(R.id.menuSortByMostHours)
            item.title = getString(R.string.most_hours)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val sortData = SortData(this)
        val vibrationData = VibrationData(this)
        if (vibrationData.loadVibrationState()) {
            val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        }
        return when (item.itemId) {
            R.id.menuSortByFirstEntered -> {
                if (dbHandler.getCount() == 0) {
                    Toast.makeText(this, getString(R.string.cant_sort_data_history_empty), Toast.LENGTH_SHORT).show()
                } else {
                    sortData.setSortState(getString(R.string.day_ASC))
                    loadIntoList()
                    invalidateOptionsMenu()
                    Toast.makeText(this, getString(R.string.changed_sort_mode_first_entered), Toast.LENGTH_SHORT).show()
                }
                return true
            }
            R.id.menuSortByLastEntered -> {
                if (dbHandler.getCount() == 0) {
                    Toast.makeText(this, getString(R.string.cant_sort_data_history_empty), Toast.LENGTH_SHORT).show()
                } else {
                    sortData.setSortState(getString(R.string.day_DESC))
                    loadIntoList()
                    invalidateOptionsMenu()
                    Toast.makeText(this, getString(R.string.changed_sort_mode_last_entered), Toast.LENGTH_SHORT).show()
                }
                return true
            }
            R.id.menuSortByMostHours -> {
                if (dbHandler.getCount() == 0) {
                    Toast.makeText(this, getString(R.string.cant_sort_data_history_empty), Toast.LENGTH_SHORT).show()
                } else {
                    sortData.setSortState(getString(R.string.total_DESC))
                    loadIntoList()
                    invalidateOptionsMenu()
                    Toast.makeText(this, getString(R.string.changed_sort_mode_most_hours), Toast.LENGTH_SHORT).show()
                }
                return true
            }
            R.id.menuSortByLeastHours -> {
                if (dbHandler.getCount() == 0) {
                    Toast.makeText(this, getString(R.string.cant_sort_data_history_empty), Toast.LENGTH_SHORT).show()
                } else {
                    sortData.setSortState(getString(R.string.total_ASC))
                    loadIntoList()
                    invalidateOptionsMenu()
                    Toast.makeText(this, getString(R.string.changed_sort_mode_most_hours_last), Toast.LENGTH_SHORT).show()
                }
                return true
            }
            R.id.home -> {
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "home_menu_item_history")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "home_menu_item_clicked_history")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "menu_item")
                }
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.Settings -> {
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "settings_menu_item_history")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "settings_menu_item_clicked_history")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "menu_item")
                }
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.changelog -> {
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "patch_notes_menu_item_history")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "patch_notes_menu_item_clicked_history")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "menu_item")
                }
                val intent = Intent(this, PatchNotesActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.trash -> {
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "trash_menu_item_history")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "trash_menu_item_clicked_history")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "menu_item")
                }
                val intent = Intent(this, TrashActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.graph -> {
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "graph_menu_item_history")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "graph_menu_item_clicked_history")
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