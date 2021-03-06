package com.cory.hourcalculator.activities

import android.content.Context
import android.content.Intent
import android.os.*
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AbsListView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cory.hourcalculator.R
import com.cory.hourcalculator.adapters.CustomAdapterTrash
import com.cory.hourcalculator.classes.*
import com.cory.hourcalculator.database.DBHelperTrash
import com.google.android.gms.ads.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_trash.*
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.FormatStyle

class TrashActivity : AppCompatActivity() {

    private val dbHandlerTrash = DBHelperTrash(this, null)
    private val dataListTrash = ArrayList<HashMap<String, String>>()

    private lateinit var saveData: DarkThemeData
    private lateinit var vibrationData: VibrationData

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        saveData = DarkThemeData(this)
        if (saveData.loadDarkModeState()) {
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
        val mAdView = findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        mAdView.adListener = object : AdListener() {

        }

        val textView8 = findViewById<TextView>(R.id.textView8)

        textView8.text = getString(R.string.amount_of_hours_in_trash, dbHandlerTrash.getCount())

        floatingActionButtonTrash.setOnClickListener {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "fab_trash")
                param(FirebaseAnalytics.Param.ITEM_NAME, "fab_trash_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "floating_action_button")
            }
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

    fun update() {
        val index = listViewTrash.firstVisiblePosition
        val v = listViewTrash.getChildAt(0)
        val top = if (v == null) 0 else v.top - listViewTrash.paddingTop
        loadIntoListTrash()
        listViewTrash.setSelectionFromTop(index, top)
        if(dbHandlerTrash.getCount() == 0) {
             textView8.visibility = View.INVISIBLE
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
                //Toast.makeText(this, trashdate, Toast.LENGTH_SHORT).show()

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

    fun menuItem(id : String, name : String, type : String) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
            param(FirebaseAnalytics.Param.ITEM_ID, id)
            param(FirebaseAnalytics.Param.ITEM_NAME, name)
            param(FirebaseAnalytics.Param.CONTENT_TYPE, type)
        }
    }

    private fun vibration(vibrationData: VibrationData) {
        if (vibrationData.loadVibrationState()) {
            val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    override fun onResume() {
        super.onResume()
        loadIntoListTrash()
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
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "home_menu_item")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "home_menu_item_clicked")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "menu_item")
                }
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.Settings -> {
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "settings_menu_item_trash")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "settings_menu_item_clicked_trash")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "menu_item")
                }
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.changelog -> {
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "patch_notes_menu_item_trash")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "patch_notes_menu_item_clicked_trash")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "menu_item")
                }
                val intent = Intent(this, PatchNotesActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.history -> {
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "history_menu_item_trash")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "history_menu_item_clicked_trash")
                    param(FirebaseAnalytics.Param.CONTENT_TYPE, "menu_item")
                }
                val intent = Intent(this, HistoryActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.graph -> {
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(FirebaseAnalytics.Param.ITEM_ID, "graph_menu_item_trash")
                    param(FirebaseAnalytics.Param.ITEM_NAME, "graph_menu_item_clicked_trash")
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