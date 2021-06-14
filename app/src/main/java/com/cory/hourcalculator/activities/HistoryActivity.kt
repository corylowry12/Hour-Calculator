package com.cory.hourcalculator.activities

import android.content.Context
import android.content.Intent
import android.os.*
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.cory.hourcalculator.R
import com.cory.hourcalculator.adapters.CustomAdapter
import com.cory.hourcalculator.classes.*
import com.cory.hourcalculator.database.DBHelper
import com.google.android.gms.ads.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_history.*
import kotlinx.android.synthetic.main.activity_trash.*
import kotlinx.android.synthetic.main.list_row.*


class HistoryActivity : AppCompatActivity() {

    private val dbHandler = DBHelper(this, null)
    private val dataList = ArrayList<HashMap<String, String>>()
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private lateinit var darkThemeData: DarkThemeData

    val testDeviceId = listOf("5E80E48DC2282D372EAE0E3ACDE070CC", "8EE44B7B4B422D333731760574A381FE")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        darkThemeData = DarkThemeData(this)
        if (darkThemeData.loadDarkModeState()) {
            setTheme(R.style.AMOLED)
        } else {
            setTheme(R.style.AppTheme)
        }
        setContentView(R.layout.activity_history)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        window.setBackgroundDrawable(null)

        firebaseAnalytics = Firebase.analytics

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

        val historyAutomaticDeletion = HistoryAutomaticDeletion(this)
        val historyDeletion = HistoryDeletion(this)
        val daysWorked = DaysWorkedPerWeek(this)

        if(daysWorked.loadDaysWorked() != "" && historyAutomaticDeletion.loadHistoryDeletionState() && dbHandler.getCount() > daysWorked.loadDaysWorked().toString().toInt()) {
            historyDeletion.deletion(this)
        }

        loadIntoList()

        val slideTopToBottom = AnimationUtils.loadAnimation(this, R.anim.list_view_load_animation)
        if (!PerformanceModeData(this).loadPerformanceMode() && dbHandler.getCount() > 0) {
            listView.startAnimation(slideTopToBottom)
            textViewTotalHours.startAnimation(slideTopToBottom)
            textViewSize.startAnimation(slideTopToBottom)
            textViewWages.startAnimation(slideTopToBottom)
        }

        floatingActionButtonHistory.setOnClickListener { listView.smoothScrollToPosition(0) }

        listView.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {

                if (firstVisibleItem > 0) {
                    floatingActionButtonHistory.show()
                } else {

                    floatingActionButtonHistory.hide()
                }
            }

            override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {}
        })
    }

    fun update() {
        val index = listView.firstVisiblePosition
        val v = listView.getChildAt(0)
        val top = if (v == null) 0 else v.top - listView.paddingTop
        val slideTopToBottom = AnimationUtils.loadAnimation(this, R.anim.slide_top_to_bottom)
        if (!PerformanceModeData(this).loadPerformanceMode()) {
            listView.startAnimation(slideTopToBottom)
        }
        loadIntoList()
        listView.setSelectionFromTop(index, top)
        if (dbHandler.getCount() == 0) {
            textViewTotalHours.text = ""
        }
    }

    private fun loadIntoList() {

        val wagesData = WagesData(this)
        textViewWages.text = ""

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

            if (wagesData.loadWageAmount() != "") {
                try {
                    val wages = output.toDouble() * wagesData.loadWageAmount().toString().toDouble()
                    val wagesrounded = String.format("%.2f", wages)
                    textViewWages.text = getString(R.string.total_wages, wagesrounded)
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                    textViewWages.text = getString(R.string.there_is_a_problem_calculating_wages)
                }
            }
            cursor.moveToNext()

        }
        textViewSize.text = getString(R.string.amount_of_hours_saved, dbHandler.getCount())
        val listView = findViewById<ListView>(R.id.listView)
        listView.adapter = CustomAdapter(this@HistoryActivity, dataList)

    }

    fun retrieveItems(query: String) {
        dataList.clear()
        val cursor = dbHandler.retrieve(query)
        cursor.moveToFirst()

        while (!cursor.isAfterLast) {
            val map = HashMap<String, String>()
            map["id"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ID))
            map["intime"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_IN))
            map["out"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_OUT))
            map["break"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_BREAK))
            map["total"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TOTAL))
            map["day"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DAY))
            dataList.add(map)

            cursor.moveToNext()
        }

        val listView = findViewById<ListView>(R.id.listView)
        listView.adapter = CustomAdapter(this@HistoryActivity, dataList)

    }

    override fun onResume() {
        super.onResume()
        val slideTopToBottom = AnimationUtils.loadAnimation(this, R.anim.list_view_load_animation)
        if (!PerformanceModeData(this).loadPerformanceMode() && dbHandler.getCount() > 0) {
            listView.startAnimation(slideTopToBottom)
            textViewTotalHours.startAnimation(slideTopToBottom)
            textViewSize.startAnimation(slideTopToBottom)
            textViewWages.startAnimation(slideTopToBottom)
        }
        loadIntoList()
    }

    override fun onRestart() {
        super.onRestart()
        val intent = Intent(this, this::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        if (!PerformanceModeData(this).loadPerformanceMode()) {
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        } else {
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
        menuInflater.inflate(R.menu.main_menu_history, menu)
        val historyToggleData = HistoryToggleData(this)
        if (!historyToggleData.loadHistoryState()) {
            val trash = menu.findItem(R.id.trash)
            trash.isVisible = false
            val graph = menu.findItem(R.id.graph)
            graph.isVisible = false
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
        val search = menu.findItem(R.id.app_bar_search)
        val searchView = search.actionView as SearchView
        searchView.queryHint = getString(R.string.search)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if (query != "") {
                    retrieveItems(query)
                    textViewTotalHours.text = ""
                    textViewSize.text = ""
                    textViewWages.text = ""
                } else if (query == "") {
                    loadIntoList()
                }
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText != "") {
                    retrieveItems(newText)
                    textViewTotalHours.text = ""
                    textViewSize.text = ""
                    textViewWages.text = ""
                } else if (newText == "") {
                    loadIntoList()
                }
                return false
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val sortData = SortData(this)
        val vibrationData = VibrationData(this)
        if (vibrationData.loadVibrationState()) {
            val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(5, VibrationEffect.DEFAULT_AMPLITUDE))

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
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                if (!PerformanceModeData(this).loadPerformanceMode()) {
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                } else {
                    overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
                }
                return true
            }
            R.id.Settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                if (!PerformanceModeData(this).loadPerformanceMode()) {
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                } else {
                    overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
                }
                return true
            }
            R.id.changelog -> {
                val intent = Intent(this, PatchNotesActivity::class.java)
                startActivity(intent)
                if (!PerformanceModeData(this).loadPerformanceMode()) {
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                } else {
                    overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
                }
                return true
            }
            R.id.trash -> {
                val intent = Intent(this, TrashActivity::class.java)
                startActivity(intent)
                if (!PerformanceModeData(this).loadPerformanceMode()) {
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                } else {
                    overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
                }
                return true
            }
            R.id.graph -> {
                val intent = Intent(this, GraphActivity::class.java)
                startActivity(intent)
                if (!PerformanceModeData(this).loadPerformanceMode()) {
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                } else {
                    overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
                }
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}