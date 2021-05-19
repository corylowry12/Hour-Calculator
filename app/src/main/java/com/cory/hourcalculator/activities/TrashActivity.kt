package com.cory.hourcalculator.activities

import android.content.Context
import android.content.Intent
import android.os.*
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.cory.hourcalculator.R
import com.cory.hourcalculator.adapters.CustomAdapterTrash
import com.cory.hourcalculator.classes.*
import com.cory.hourcalculator.database.DBHelperTrash
import com.google.android.gms.ads.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_history.*
import kotlinx.android.synthetic.main.activity_trash.*
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.FormatStyle

class TrashActivity : AppCompatActivity() {

    private val dbHandlerTrash = DBHelperTrash(this, null)
    private val dataListTrash = ArrayList<HashMap<String, String>>()

    var testDeviceId = listOf(getString(R.string.oneplus_device_id))

    private lateinit var darkThemeData: DarkThemeData
    private lateinit var vibrationData: VibrationData

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        darkThemeData = DarkThemeData(this)
        if (darkThemeData.loadDarkModeState()) {
            setTheme(R.style.AMOLED)
        } else {
            setTheme(R.style.AppTheme)
        }
        setContentView(R.layout.activity_trash)
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

        val slideTopToBottom = AnimationUtils.loadAnimation(this, R.anim.list_view_load_animation_trash)
        if(!PerformanceModeData(this).loadPerformanceMode() && dbHandlerTrash.getCount() > 0) {
            listViewTrash.startAnimation(slideTopToBottom)
            textViewWarning.startAnimation(slideTopToBottom)
            textView8.startAnimation(slideTopToBottom)
        }

        floatingActionButtonTrash.setOnClickListener {
            listViewTrash.smoothScrollToPosition(0)
        }

       listViewTrash.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {

                if(firstVisibleItem > 0) {
                        floatingActionButtonTrash.show()
                }
                else {

                        floatingActionButtonTrash.hide()
                }
            }

            override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {
            }
        })
    }
    fun retrieveItems(query: String) {
        dataListTrash.clear()
        val cursor = dbHandlerTrash.retrieve(query)
        cursor.moveToFirst()

        while (!cursor.isAfterLast) {
            val map = HashMap<String, String>()
            map["id_trash"] = cursor.getString(cursor.getColumnIndex(DBHelperTrash.COLUMN_ID_TRASH))
            map["intime_trash"] = cursor.getString(cursor.getColumnIndex(DBHelperTrash.COLUMN_IN_TRASH))
            map["out_trash"] = cursor.getString(cursor.getColumnIndex(DBHelperTrash.COLUMN_OUT_TRASH))
            map["break_trash"] = cursor.getString(cursor.getColumnIndex(DBHelperTrash.COLUMN_BREAK_TRASH))
            map["total_trash"] = cursor.getString(cursor.getColumnIndex(DBHelperTrash.COLUMN_TOTAL_TRASH))
            map["day_trash"] = cursor.getString(cursor.getColumnIndex(DBHelperTrash.COLUMN_DAY_TRASH))
            dataListTrash.add(map)

            cursor.moveToNext()
        }

        val listViewTrash = findViewById<ListView>(R.id.listViewTrash)
        listViewTrash.adapter = CustomAdapterTrash(this@TrashActivity, dataListTrash)

    }

    fun update() {
        val index = listViewTrash.firstVisiblePosition
        val v = listViewTrash.getChildAt(0)
        val top = if (v == null) 0 else v.top - listViewTrash.paddingTop
        val slideTopToBottom = AnimationUtils.loadAnimation(this, R.anim.slide_top_to_bottom)
        if(!PerformanceModeData(this).loadPerformanceMode()) {
            listViewTrash.startAnimation(slideTopToBottom)
        }
        loadIntoListTrash()
        listViewTrash.setSelectionFromTop(index, top)
        if(dbHandlerTrash.getCount() == 0) {
             textView8.text = ""
        }
    }

    private fun loadIntoListTrash() {
        dataListTrash.clear()
        val cursor = dbHandlerTrash.getAllRow(this)
        cursor!!.moveToFirst()
        if(cursor.count > 0) {

            val x = 0
            while (!cursor.isAfterLast) {

                val map = HashMap<String, String>()
                map["id_trash"] = cursor.getString(cursor.getColumnIndex(DBHelperTrash.COLUMN_ID_TRASH))
                map["intime_trash"] = cursor.getString(cursor.getColumnIndex(DBHelperTrash.COLUMN_IN_TRASH))
                map["out_trash"] = cursor.getString(cursor.getColumnIndex(DBHelperTrash.COLUMN_OUT_TRASH))
                map["break_trash"] = cursor.getString(cursor.getColumnIndex(DBHelperTrash.COLUMN_BREAK_TRASH))
                map["total_trash"] = cursor.getString(cursor.getColumnIndex(DBHelperTrash.COLUMN_TOTAL_TRASH))
                map["day_trash"] = cursor.getString(cursor.getColumnIndex(DBHelperTrash.COLUMN_DAY_TRASH))
                dataListTrash.add(map)

                delete(dataListTrash[+x]["day_trash"].toString(), dataListTrash[+x]["id_trash"].toString())

                cursor.moveToNext()
            }
        }

        val count = dbHandlerTrash.getCount()
        textView8.text = getString(R.string.amount_of_hours_in_trash, count)

        findViewById<ListView>(R.id.listViewTrash).adapter = CustomAdapterTrash(this@TrashActivity, dataListTrash)
    }

    private fun delete(trashdate: String, id_trash: String) {
        val trashAutomaticDeletion = TrashAutomaticDeletion(this)
        if(trashAutomaticDeletion.loadTrashDeletionState()) {
            val date = LocalDateTime.now()

            try {
                val localDateTime = LocalDateTime.parse(trashdate, DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))
                val difference = Period.between(date.toLocalDate(), localDateTime.toLocalDate()).toString().replace("P", "").replace("-", "").replace("D", "").trim().toInt()
                if (difference.toString().toInt() >= 7) {
                    dbHandlerTrash.deleteRow(id_trash)
                    loadIntoListTrash()
                }
            }
            catch (e: DateTimeParseException) {
                Toast.makeText(this, getString(R.string.there_was_an_error_graph), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun vibration(vibrationData: VibrationData) {
        if (vibrationData.loadVibrationState()) {
            val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(VibrationEffect.createOneShot(5, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    override fun onResume() {
        super.onResume()
        val slideTopToBottom = AnimationUtils.loadAnimation(this, R.anim.list_view_load_animation_trash)
        if(!PerformanceModeData(this).loadPerformanceMode() && dbHandlerTrash.getCount() > 0) {
            textViewWarning.startAnimation(slideTopToBottom)
            textView8.startAnimation(slideTopToBottom)
            listViewTrash.startAnimation(slideTopToBottom)
        }
        loadIntoListTrash()
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
            overridePendingTransition(0, 0)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finishAndRemoveTask()
        if(!PerformanceModeData(this).loadPerformanceMode()) {
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
        else {
            overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu_trash, menu)
        val historyToggleData = HistoryToggleData(this)
        if (!historyToggleData.loadHistoryState()) {
            val history = menu.findItem(R.id.history)
            history.isVisible = false
            val trash = menu.findItem(R.id.trash)
            trash.isVisible = false
            val graph = menu.findItem(R.id.graph)
            graph.isVisible = false
        }
        val sortDataTrash = SortDataTrash(this)
        val sort = sortDataTrash.loadSortStateTrash()
        if(sort == getString(R.string.day_trash_DESC)) {
            val item = menu.findItem(R.id.menuSortByLastEntered)
            item.title = getString(R.string.last_entered)
        }
        if(sort == getString(R.string.day_trash_ASC)) {
            val item = menu.findItem(R.id.menuSortByFirstEntered)
            item.title = getString(R.string.first_entered)
        }
        if(sort == getString(R.string.total_trash_ASC)) {
            val item = menu.findItem(R.id.menuSortByLeastHours)
            item.title = getString(R.string.least_hours)
        }
        if(sort == getString(R.string.total_trash_DESC)) {
            val item = menu.findItem(R.id.menuSortByMostHours)
            item.title = getString(R.string.most_hours)
        }
        val search = menu.findItem(R.id.app_bar_search)
        val searchView = search.actionView as SearchView
        searchView.queryHint = getString(R.string.search)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if(query != "") {
                    retrieveItems(query)
                    textView8.visibility = View.INVISIBLE
                    textViewWarning.visibility = View.INVISIBLE
                }
                else if (query == "") {
                    loadIntoListTrash()
                }
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if(newText != "") {
                    retrieveItems(newText)
                    textView8.visibility = View.INVISIBLE
                    textViewWarning.visibility = View.INVISIBLE
                }
                else if (newText == "") {
                    loadIntoListTrash()
                }
                return false
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        vibrationData = VibrationData(this)
        vibration(vibrationData)
        val sortDataTrash = SortDataTrash(this)
        return when (item.itemId) {
            R.id.menuSortByFirstEntered -> {
                if(dbHandlerTrash.getCount() == 0) {
                    Toast.makeText(this, getString(R.string.cant_sort_data_trash_empty), Toast.LENGTH_SHORT).show()
                }
                else {
                    sortDataTrash.setSortStateTrash(getString(R.string.day_trash_ASC))
                    loadIntoListTrash()
                    invalidateOptionsMenu()
                    Toast.makeText(this, getString(R.string.changed_sort_mode_first_entered), Toast.LENGTH_SHORT).show()
                }
                return true
            }
            R.id.menuSortByLastEntered -> {
                if(dbHandlerTrash.getCount() == 0) {
                    Toast.makeText(this, getString(R.string.cant_sort_data_trash_empty), Toast.LENGTH_SHORT).show()
                }
                else {
                    sortDataTrash.setSortStateTrash(getString(R.string.day_trash_DESC))
                    loadIntoListTrash()
                    invalidateOptionsMenu()
                    Toast.makeText(this, getString(R.string.changed_sort_mode_last_entered), Toast.LENGTH_SHORT).show()
                }
                return true
            }
            R.id.menuSortByMostHours -> {
                if(dbHandlerTrash.getCount() == 0) {
                    Toast.makeText(this, getString(R.string.cant_sort_data_trash_empty), Toast.LENGTH_SHORT).show()
                }
                else {
                    sortDataTrash.setSortStateTrash(getString(R.string.total_trash_DESC))
                    loadIntoListTrash()
                    invalidateOptionsMenu()
                    Toast.makeText(this, getString(R.string.changed_sort_mode_most_hours), Toast.LENGTH_SHORT).show()
                }
                return true
            }
            R.id.menuSortByLeastHours -> {
                if(dbHandlerTrash.getCount() == 0) {
                    Toast.makeText(this, getString(R.string.cant_sort_data_trash_empty), Toast.LENGTH_SHORT).show()
                }
                else {
                    sortDataTrash.setSortStateTrash(getString(R.string.total_trash_ASC))
                    loadIntoListTrash()
                    invalidateOptionsMenu()
                    Toast.makeText(this, getString(R.string.changed_sort_mode_most_hours_last), Toast.LENGTH_SHORT).show()
                }
                return true
            }
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