package com.cory.hourcalculator.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cory.hourcalculator.R
import com.cory.hourcalculator.classes.DarkThemeData
import com.cory.hourcalculator.classes.HistoryToggleData
import com.cory.hourcalculator.classes.PerformanceModeData
import com.cory.hourcalculator.classes.VibrationData
import com.cory.hourcalculator.database.DBHelper
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.gms.ads.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_graph.*
import java.util.*
import kotlin.collections.ArrayList

class GraphActivity : AppCompatActivity() {

    private val dbHandler = DBHelper(this, null)
    private val dataList = ArrayList<HashMap<String, String>>()
    private lateinit var vibrationData: VibrationData
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var darkThemeData: DarkThemeData

    private lateinit var textViewItemClicked: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        darkThemeData = DarkThemeData(this)
        if (darkThemeData.loadDarkModeState()) {
            setTheme(R.style.AMOLED)
        } else {
            setTheme(R.style.AppTheme)
        }
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(null)
        setContentView(R.layout.activity_graph)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        firebaseAnalytics = Firebase.analytics

        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        MobileAds.initialize(this)
        val adView = AdView(this)
        adView.adSize = AdSize.BANNER
        adView.adUnitId = "ca-app-pub-4546055219731501/5171269817"
        val mAdView = findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        mAdView.adListener = object : AdListener() {
        }

        vibrationData = VibrationData(this)

        textViewItemClicked = findViewById(R.id.textView37)

        setBarChart()
    }

    private fun setBarChart() {

        // Customizes bar chart
        barChart.setVisibleXRangeMaximum(7f)
        barChart.moveViewToX(0f)
        barChart.setTouchEnabled(true)
        barChart.isDoubleTapToZoomEnabled = false
        if(!PerformanceModeData(this).loadPerformanceMode()) {
            barChart.animateY(400)
        }

        // Makes all X Axis labels fit on one page
        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.spaceBetweenLabels = 0
        xAxis.labelRotationAngle = 35f
        xAxis.textSize = 8f

        if (darkThemeData.loadDarkModeState()) {
            xAxis.textColor = getColor(android.R.color.white)
            barChart.axisLeft.textColor = getColor(android.R.color.white)
            barChart.axisLeft.textColor = getColor(android.R.color.white)
            barChart.axisRight.textColor = getColor(android.R.color.white)
            barChart.legend.textColor = getColor(android.R.color.white)
        } else {
            xAxis.textColor = getColor(android.R.color.black)
            barChart.axisLeft.textColor = getColor(android.R.color.black)
            barChart.axisRight.textColor = getColor(android.R.color.black)
            barChart.legend.textColor = getColor(android.R.color.black)
        }

        // Initializes cursor object
        dataList.clear()
        val cursor = dbHandler.returntop7()
        val count = dbHandler.getCount()
        cursor!!.moveToFirst()
        try {
            while (!cursor.isAfterLast) {

                // Maps data in the position to the map variable
                val map = HashMap<String, String>()
                map["id"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ID))
                map["intime"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_IN))
                map["out"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_OUT))
                map["break"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_BREAK))
                map["total"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TOTAL))
                map["day"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DAY))
                dataList.add(map)

                when {
                    // Initializes array to store xAxis values in
                    count >= 7 -> {
                        val splitstring = ArrayList<Int>()
                        val hours = ArrayList<String>()
                        val dropLast = ArrayList<String>()
                        dropLast.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DAY)))
                        hours.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TOTAL))).toString().replace("[", "").replace("]", "")
                        cursor.moveToNext()
                        dropLast.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DAY)))
                        hours.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TOTAL))).toString().replace("[", "").replace("]", "")
                        cursor.moveToNext()
                        dropLast.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DAY)))
                        hours.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TOTAL))).toString().replace("[", "").replace("]", "")
                        cursor.moveToNext()
                        dropLast.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DAY)))
                        hours.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TOTAL))).toString().replace("[", "").replace("]", "")
                        cursor.moveToNext()
                        dropLast.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DAY)))
                        hours.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TOTAL))).toString().replace("[", "").replace("]", "")
                        cursor.moveToNext()
                        dropLast.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DAY)))
                        hours.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TOTAL))).toString().replace("[", "").replace("]", "")
                        cursor.moveToNext()
                        dropLast.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DAY)))
                        hours.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TOTAL))).toString().replace("[", "").replace("]", "")

                        //val splitstring = dropLast.elementAt(0).toString().indexOf(",", dropLast.elementAt(0).toString().indexOf(",") + 1)
                        for (i in 0 until 7) {

                            val matchIndex = dropLast.elementAt(i).lastIndexOf(",")
                            val difference = dropLast.elementAt(i).length - matchIndex
                            splitstring.add(difference)
                        }

                        // Initializes array to store split string drop last array in
                        val drop = ArrayList<String>()
                        drop.add(dropLast.elementAt(0).dropLast(splitstring.elementAt(0)))
                        drop.add(dropLast.elementAt(1).dropLast(splitstring.elementAt(1)))
                        drop.add(dropLast.elementAt(2).dropLast(splitstring.elementAt(2)))
                        drop.add(dropLast.elementAt(3).dropLast(splitstring.elementAt(3)))
                        drop.add(dropLast.elementAt(4).dropLast(splitstring.elementAt(4)))
                        drop.add(dropLast.elementAt(5).dropLast(splitstring.elementAt(5)))
                        drop.add(dropLast.elementAt(6).dropLast(splitstring.elementAt(6)))

                        // Initializes array to store xAxis values in after they have been trimmed down
                        val labels = ArrayList<String>()
                        labels.add(drop.elementAt(0).toString())
                        labels.add(drop.elementAt(1).toString())
                        labels.add(drop.elementAt(2).toString())
                        labels.add(drop.elementAt(3).toString())
                        labels.add(drop.elementAt(4).toString())
                        labels.add(drop.elementAt(5).toString())
                        labels.add(drop.elementAt(6).toString())

                        val entries = ArrayList<BarEntry>()
                        entries.add(BarEntry(hours.elementAt(0).toString().toFloat(), 0))
                        entries.add(BarEntry(hours.elementAt(1).toString().toFloat(), 1))
                        entries.add(BarEntry(hours.elementAt(2).toString().toFloat(), 2))
                        entries.add(BarEntry(hours.elementAt(3).toString().toFloat(), 3))
                        entries.add(BarEntry(hours.elementAt(4).toString().toFloat(), 4))
                        entries.add(BarEntry(hours.elementAt(5).toString().toFloat(), 5))
                        entries.add(BarEntry(hours.elementAt(6).toString().toFloat(), 6))

                        // Sets data in bar chart
                        val barDataSet = BarDataSet(entries, getString(R.string.hours))
                        barDataSet.valueTextSize = 9f

                        // Adds data sets to the bar chart
                        val data = BarData(labels, barDataSet)
                        barChart.data = data

                        // Customizes bar chart
                        barChart.setDescription("")
                        barChart.setDrawGridBackground(true)

                        // Sets accent color to bar chart
                        //barDataSet.color = resources.getColor(R.color.colorAccent)
                        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS)

                        // Moves cursor to next to map in data
                        barChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                            override fun onValueSelected(e: Entry?, dataSetIndex: Int, h: Highlight) {

                                vibration(vibrationData)
                                val cursorIndex = dbHandler.itemClicked(h.xIndex + 1)

                                cursorIndex.moveToFirst()

                                val mapIndex = HashMap<String, String>()
                                mapIndex["id"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_ID))
                                mapIndex["intime"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_IN))
                                mapIndex["out"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_OUT))
                                mapIndex["break"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_BREAK))
                                mapIndex["total"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_TOTAL))
                                mapIndex["day"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_DAY))

                                val inTime = mapIndex["intime"]
                                val outTime = mapIndex["out"]
                                val breakTime = mapIndex["break"]
                                val total = mapIndex["total"]
                                val day = mapIndex["day"]

                                textViewItemClicked.text = getString(R.string.bar_chart_item_clicked, inTime, outTime, breakTime, total, day)

                            }

                            override fun onNothingSelected() {
                                vibration(vibrationData)
                                textViewItemClicked.text = ""
                            }
                        })

                    }
                    count == 6 -> {
                        // Initializes array to store X Axis values in
                        val splitstring = ArrayList<Int>()
                        val hours = ArrayList<String>()
                        val dropLast = ArrayList<String>()
                        dropLast.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DAY)))
                        hours.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TOTAL))).toString().replace("[", "").replace("]", "")
                        cursor.moveToNext()
                        dropLast.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DAY)))
                        hours.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TOTAL))).toString().replace("[", "").replace("]", "")
                        cursor.moveToNext()
                        dropLast.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DAY)))
                        hours.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TOTAL))).toString().replace("[", "").replace("]", "")
                        cursor.moveToNext()
                        dropLast.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DAY)))
                        hours.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TOTAL))).toString().replace("[", "").replace("]", "")
                        cursor.moveToNext()
                        dropLast.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DAY)))
                        hours.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TOTAL))).toString().replace("[", "").replace("]", "")
                        cursor.moveToNext()
                        dropLast.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DAY)))
                        hours.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TOTAL))).toString().replace("[", "").replace("]", "")

                        for (i in 0 until 6) {

                            val matchIndex = dropLast.elementAt(i).lastIndexOf(",")
                            val difference = dropLast.elementAt(i).length - matchIndex
                            splitstring.add(difference)
                        }

                        // Initializes array to store split string drop last array in
                        val drop = ArrayList<String>()
                        drop.add(dropLast.elementAt(0).dropLast(splitstring.elementAt(0)))
                        drop.add(dropLast.elementAt(1).dropLast(splitstring.elementAt(1)))
                        drop.add(dropLast.elementAt(2).dropLast(splitstring.elementAt(2)))
                        drop.add(dropLast.elementAt(3).dropLast(splitstring.elementAt(3)))
                        drop.add(dropLast.elementAt(4).dropLast(splitstring.elementAt(4)))
                        drop.add(dropLast.elementAt(5).dropLast(splitstring.elementAt(5)))

                        // Initializes array to store X Axis values in after they have been trimmed down
                        val labels = ArrayList<String>()
                        labels.add(drop.elementAt(0).toString())
                        labels.add(drop.elementAt(1).toString())
                        labels.add(drop.elementAt(2).toString())
                        labels.add(drop.elementAt(3).toString())
                        labels.add(drop.elementAt(4).toString())
                        labels.add(drop.elementAt(5).toString())

                        val entries = ArrayList<BarEntry>()
                        entries.add(BarEntry(hours.elementAt(0).toString().toFloat(), 0))
                        entries.add(BarEntry(hours.elementAt(1).toString().toFloat(), 1))
                        entries.add(BarEntry(hours.elementAt(2).toString().toFloat(), 2))
                        entries.add(BarEntry(hours.elementAt(3).toString().toFloat(), 3))
                        entries.add(BarEntry(hours.elementAt(4).toString().toFloat(), 4))
                        entries.add(BarEntry(hours.elementAt(5).toString().toFloat(), 5))

                        // Sets data in bar chart
                        val barDataSet = BarDataSet(entries, getString(R.string.hours))
                        barDataSet.valueTextSize = 9f
                        // Adds data sets to the bar chart
                        val data = BarData(labels, barDataSet)
                        barChart.data = data

                        // Customizes bar chart
                        barChart.setDescription("")
                        barChart.setDrawGridBackground(true)

                        // Sets accent color to bar chart
                        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS)

                        barChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                            override fun onValueSelected(e: Entry?, dataSetIndex: Int, h: Highlight) {

                                vibration(vibrationData)
                                val cursorIndex = dbHandler.itemClicked(h.xIndex + 1)

                                cursorIndex.moveToFirst()

                                val mapIndex = HashMap<String, String>()
                                mapIndex["id"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_ID))
                                Toast.makeText(applicationContext, mapIndex["id"], Toast.LENGTH_SHORT).show()
                                mapIndex["intime"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_IN))
                                mapIndex["out"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_OUT))
                                mapIndex["break"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_BREAK))
                                mapIndex["total"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_TOTAL))
                                mapIndex["day"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_DAY))

                                val inTime = mapIndex["intime"]
                                val outTime = mapIndex["out"]
                                val breakTime = mapIndex["break"]
                                val total = mapIndex["total"]
                                val day = mapIndex["day"]

                                textViewItemClicked.text = getString(R.string.bar_chart_item_clicked, inTime, outTime, breakTime, total, day)

                            }

                            override fun onNothingSelected() {
                                vibration(vibrationData)
                                textViewItemClicked.text = ""
                            }
                        })

                    }
                    count == 5 -> {
                        // Initializes array to store xaxis values in
                        val splitstring = ArrayList<Int>()
                        val hours = ArrayList<String>()
                        val dropLast = ArrayList<String>()
                        dropLast.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DAY)))
                        hours.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TOTAL))).toString().replace("[", "").replace("]", "")
                        cursor.moveToNext()
                        dropLast.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DAY)))
                        hours.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TOTAL))).toString().replace("[", "").replace("]", "")
                        cursor.moveToNext()
                        dropLast.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DAY)))
                        hours.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TOTAL))).toString().replace("[", "").replace("]", "")
                        cursor.moveToNext()
                        dropLast.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DAY)))
                        hours.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TOTAL))).toString().replace("[", "").replace("]", "")
                        cursor.moveToNext()
                        dropLast.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DAY)))
                        hours.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TOTAL))).toString().replace("[", "").replace("]", "")

                        for (i in 0 until 5) {

                            val matchIndex = dropLast.elementAt(i).lastIndexOf(",")
                            val difference = dropLast.elementAt(i).length - matchIndex
                            splitstring.add(difference)
                        }

                        // Initializes array to store split string drop last array in
                        val drop = ArrayList<String>()
                        drop.add(dropLast.elementAt(0).dropLast(splitstring.elementAt(0)))
                        drop.add(dropLast.elementAt(1).dropLast(splitstring.elementAt(1)))
                        drop.add(dropLast.elementAt(2).dropLast(splitstring.elementAt(2)))
                        drop.add(dropLast.elementAt(3).dropLast(splitstring.elementAt(3)))
                        drop.add(dropLast.elementAt(4).dropLast(splitstring.elementAt(4)))

                        // Initializes array to store xaxis values in after they have been trimmed down
                        val labels = ArrayList<String>()
                        labels.add(drop.elementAt(0).toString())
                        labels.add(drop.elementAt(1).toString())
                        labels.add(drop.elementAt(2).toString())
                        labels.add(drop.elementAt(3).toString())
                        labels.add(drop.elementAt(4).toString())

                        val entries = ArrayList<BarEntry>()
                        entries.add(BarEntry(hours.elementAt(0).toString().toFloat(), 0))
                        entries.add(BarEntry(hours.elementAt(1).toString().toFloat(), 1))
                        entries.add(BarEntry(hours.elementAt(2).toString().toFloat(), 2))
                        entries.add(BarEntry(hours.elementAt(3).toString().toFloat(), 3))
                        entries.add(BarEntry(hours.elementAt(4).toString().toFloat(), 4))

                        // Sets data in bar chart
                        val barDataSet = BarDataSet(entries, getString(R.string.hours))
                        barDataSet.valueTextSize = 9f

                        // Adds data sets to the bar chart
                        val data = BarData(labels, barDataSet)
                        barChart.data = data

                        // Customizes bar chart
                        barChart.setDescription("")
                        barChart.setDrawGridBackground(true)

                        // Sets accent color to bar chart
                        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS)

                        barChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                            override fun onValueSelected(e: Entry?, dataSetIndex: Int, h: Highlight) {

                                vibration(vibrationData)
                                val cursorIndex = dbHandler.itemClicked(h.xIndex + 1)

                                cursorIndex.moveToFirst()

                                val mapIndex = HashMap<String, String>()
                                mapIndex["id"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_ID))
                                mapIndex["intime"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_IN))
                                mapIndex["out"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_OUT))
                                mapIndex["break"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_BREAK))
                                mapIndex["total"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_TOTAL))
                                mapIndex["day"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_DAY))

                                val inTime = mapIndex["intime"]
                                val outTime = mapIndex["out"]
                                val breakTime = mapIndex["break"]
                                val total = mapIndex["total"]
                                val day = mapIndex["day"]

                                textViewItemClicked.text = getString(R.string.bar_chart_item_clicked, inTime, outTime, breakTime, total, day)

                            }

                            override fun onNothingSelected() {
                                vibration(vibrationData)
                                textViewItemClicked.text = ""
                            }
                        })

                    }
                    count == 4 -> {
                        // Initializes array to store xaxis values in
                        val splitstring = ArrayList<Int>()
                        val hours = ArrayList<String>()
                        val dropLast = ArrayList<String>()
                        dropLast.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DAY)))
                        hours.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TOTAL))).toString().replace("[", "").replace("]", "")
                        cursor.moveToNext()
                        dropLast.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DAY)))
                        hours.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TOTAL))).toString().replace("[", "").replace("]", "")
                        cursor.moveToNext()
                        dropLast.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DAY)))
                        hours.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TOTAL))).toString().replace("[", "").replace("]", "")
                        cursor.moveToNext()
                        dropLast.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DAY)))
                        hours.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TOTAL))).toString().replace("[", "").replace("]", "")

                        for (i in 0 until 4) {

                            val matchIndex = dropLast.elementAt(i).lastIndexOf(",")
                            val difference = dropLast.elementAt(i).length - matchIndex
                            splitstring.add(difference)
                            //Toast.makeText(this, difference.toString(), Toast.LENGTH_SHORT).show()
                        }

                        // Initializes array to store split string drop last array in
                        val drop = ArrayList<String>()
                        drop.add(dropLast.elementAt(0).dropLast(splitstring.elementAt(0)))
                        drop.add(dropLast.elementAt(1).dropLast(splitstring.elementAt(1)))
                        drop.add(dropLast.elementAt(2).dropLast(splitstring.elementAt(2)))
                        drop.add(dropLast.elementAt(3).dropLast(splitstring.elementAt(3)))

                        // Initializes array to store xaxis values in after they have been trimmed down
                        val labels = ArrayList<String>()
                        labels.add(drop.elementAt(0).toString())
                        labels.add(drop.elementAt(1).toString())
                        labels.add(drop.elementAt(2).toString())
                        labels.add(drop.elementAt(3).toString())

                        val entries = ArrayList<BarEntry>()
                        entries.add(BarEntry(hours.elementAt(0).toString().toFloat(), 0))
                        entries.add(BarEntry(hours.elementAt(1).toString().toFloat(), 1))
                        entries.add(BarEntry(hours.elementAt(2).toString().toFloat(), 2))
                        entries.add(BarEntry(hours.elementAt(3).toString().toFloat(), 3))

                        // Sets data in bar chart
                        val barDataSet = BarDataSet(entries, getString(R.string.hours))
                        barDataSet.valueTextSize = 9f

                        // Adds data sets to the bar chart
                        val data = BarData(labels, barDataSet)
                        barChart.data = data

                        // Customizes bar chart
                        barChart.setDescription("")
                        barChart.setDrawGridBackground(true)

                        // Sets accent color to bar chart
                        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS)

                        barChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                            override fun onValueSelected(e: Entry?, dataSetIndex: Int, h: Highlight) {

                                vibration(vibrationData)
                                val cursorIndex = dbHandler.itemClicked(h.xIndex + 1)

                                cursorIndex.moveToFirst()

                                val mapIndex = HashMap<String, String>()
                                mapIndex["id"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_ID))
                                mapIndex["intime"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_IN))
                                mapIndex["out"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_OUT))
                                mapIndex["break"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_BREAK))
                                mapIndex["total"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_TOTAL))
                                mapIndex["day"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_DAY))

                                val inTime = mapIndex["intime"]
                                val outTime = mapIndex["out"]
                                val breakTime = mapIndex["break"]
                                val total = mapIndex["total"]
                                val day = mapIndex["day"]

                                textViewItemClicked.text = getString(R.string.bar_chart_item_clicked, inTime, outTime, breakTime, total, day)

                            }

                            override fun onNothingSelected() {
                                vibration(vibrationData)
                                textViewItemClicked.text = ""
                            }
                        })

                    }
                    count == 3 -> {
                        // Initializes array to store xaxis values in
                        val splitstring = ArrayList<Int>()
                        val hours = ArrayList<String>()
                        val dropLast = ArrayList<String>()
                        dropLast.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DAY)))
                        hours.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TOTAL))).toString().replace("[", "").replace("]", "")
                        cursor.moveToNext()
                        dropLast.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DAY)))
                        hours.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TOTAL))).toString().replace("[", "").replace("]", "")
                        cursor.moveToNext()
                        dropLast.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DAY)))
                        hours.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TOTAL))).toString().replace("[", "").replace("]", "")

                        //val splitstring = dropLast.elementAt(0).toString().indexOf(",", dropLast.elementAt(0).toString().indexOf(",") + 1)
                        for (i in 0 until 3) {

                            val matchIndex = dropLast.elementAt(i).lastIndexOf(",")
                            val difference = dropLast.elementAt(i).length - matchIndex
                            splitstring.add(difference)
                        }

                        // Initializes array to store split string drop last array in
                        val drop = ArrayList<String>()
                        drop.add(dropLast.elementAt(0).dropLast(splitstring.elementAt(0)))
                        drop.add(dropLast.elementAt(1).dropLast(splitstring.elementAt(1)))
                        drop.add(dropLast.elementAt(2).dropLast(splitstring.elementAt(2)))

                        // Initializes array to store xaxis values in after they have been trimmed down
                        val labels = ArrayList<String>()
                        labels.add(drop.elementAt(0).toString())
                        labels.add(drop.elementAt(1).toString())
                        labels.add(drop.elementAt(2).toString())

                        val entries = ArrayList<BarEntry>()
                        entries.add(BarEntry(hours.elementAt(0).toString().toFloat(), 0))
                        entries.add(BarEntry(hours.elementAt(1).toString().toFloat(), 1))
                        entries.add(BarEntry(hours.elementAt(2).toString().toFloat(), 2))

                        // Sets data in bar chart
                        val barDataSet = BarDataSet(entries, getString(R.string.hours))
                        barDataSet.valueTextSize = 9f

                        // Adds data sets to the bar chart
                        val data = BarData(labels, barDataSet)
                        barChart.data = data

                        // Customizes bar chart
                        barChart.setDescription("")
                        barChart.setDrawGridBackground(true)

                        // Sets accent color to bar chart
                        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS)

                        barChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                            override fun onValueSelected(e: Entry?, dataSetIndex: Int, h: Highlight) {

                                vibration(vibrationData)
                                val cursorIndex = dbHandler.itemClicked(h.xIndex + 1)

                                cursorIndex.moveToFirst()

                                val mapIndex = HashMap<String, String>()
                                mapIndex["id"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_ID))
                                mapIndex["intime"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_IN))
                                mapIndex["out"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_OUT))
                                mapIndex["break"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_BREAK))
                                mapIndex["total"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_TOTAL))
                                mapIndex["day"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_DAY))

                                val inTime = mapIndex["intime"]
                                val outTime = mapIndex["out"]
                                val breakTime = mapIndex["break"]
                                val total = mapIndex["total"]
                                val day = mapIndex["day"]

                                textViewItemClicked.text = getString(R.string.bar_chart_item_clicked, inTime, outTime, breakTime, total, day)

                            }

                            override fun onNothingSelected() {
                                vibration(vibrationData)
                                textViewItemClicked.text = ""
                            }
                        })

                    }
                    count == 2 -> {
                        // Initializes array to store xaxis values in
                        val splitstring = ArrayList<Int>()
                        val hours = ArrayList<String>()
                        val dropLast = ArrayList<String>()
                        dropLast.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DAY)))
                        hours.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TOTAL))).toString().replace("[", "").replace("]", "")
                        cursor.moveToNext()
                        dropLast.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DAY)))
                        hours.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TOTAL))).toString().replace("[", "").replace("]", "")

                        for (i in 0 until 2) {

                            val matchIndex = dropLast.elementAt(i).lastIndexOf(",")
                            val difference = dropLast.elementAt(i).length - matchIndex
                            splitstring.add(difference)
                        }

                        // Initializes array to store split string drop last array in
                        val drop = ArrayList<String>()
                        drop.add(dropLast.elementAt(0).dropLast(splitstring.elementAt(0)))
                        drop.add(dropLast.elementAt(1).dropLast(splitstring.elementAt(1)))

                        // Initializes array to store xaxis values in after they have been trimmed down
                        val labels = ArrayList<String>()
                        labels.add(drop.elementAt(0).toString())
                        labels.add(drop.elementAt(1).toString())

                        val entries = ArrayList<BarEntry>()
                        entries.add(BarEntry(hours.elementAt(0).toString().toFloat(), 0))
                        entries.add(BarEntry(hours.elementAt(1).toString().toFloat(), 1))

                        // Sets data in bar chart
                        val barDataSet = BarDataSet(entries, getString(R.string.hours))
                        barDataSet.valueTextSize = 9f

                        // Adds data sets to the bar chart
                        val data = BarData(labels, barDataSet)
                        barChart.data = data

                        // Customizes bar chart
                        barChart.setDescription("")
                        barChart.setDrawGridBackground(true)

                        // Sets accent color to bar chart
                        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS)

                        barChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                            override fun onValueSelected(e: Entry?, dataSetIndex: Int, h: Highlight) {

                                vibration(vibrationData)
                                val cursorIndex = dbHandler.itemClicked(h.xIndex + 1)

                                cursorIndex.moveToFirst()

                                val mapIndex = HashMap<String, String>()
                                mapIndex["id"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_ID))
                                mapIndex["intime"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_IN))
                                mapIndex["out"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_OUT))
                                mapIndex["break"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_BREAK))
                                mapIndex["total"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_TOTAL))
                                mapIndex["day"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_DAY))

                                val inTime = mapIndex["intime"]
                                val outTime = mapIndex["out"]
                                val breakTime = mapIndex["break"]
                                val total = mapIndex["total"]
                                val day = mapIndex["day"]

                                textViewItemClicked.text = getString(R.string.bar_chart_item_clicked, inTime, outTime, breakTime, total, day)

                            }

                            override fun onNothingSelected() {
                                vibration(vibrationData)
                                textViewItemClicked.text = ""
                            }
                        })

                    }
                    count == 1 -> {
                        // Initializes array to store xaxis values in
                        val splitstring = ArrayList<Int>()
                        val hours = ArrayList<String>()
                        val dropLast = ArrayList<String>()
                        dropLast.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DAY)))
                        hours.add(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TOTAL))).toString().replace("[", "").replace("]", "")

                        //val splitstring = dropLast.elementAt(0).toString().indexOf(",", dropLast.elementAt(0).toString().indexOf(",") + 1)
                        for (i in 0 until 1) {

                            val matchIndex = dropLast.elementAt(i).lastIndexOf(",")
                            val difference = dropLast.elementAt(i).length - matchIndex
                            splitstring.add(difference)
                            //Toast.makeText(this, difference.toString(), Toast.LENGTH_SHORT).show()
                        }

                        // Initializes array to store split string drop last array in
                        val drop = ArrayList<String>()
                        drop.add(dropLast.elementAt(0).dropLast(splitstring.elementAt(0)))

                        // Initializes array to store xaxis values in after they have been trimmed down
                        val labels = ArrayList<String>()
                        labels.add(drop.elementAt(0).toString())

                        val entries = ArrayList<BarEntry>()
                        entries.add(BarEntry(hours.elementAt(0).toString().toFloat(), 0))

                        // Sets data in bar chart
                        val barDataSet = BarDataSet(entries, getString(R.string.hours))
                        barDataSet.valueTextSize = 9f

                        // Adds data sets to the bar chart
                        val data = BarData(labels, barDataSet)
                        //data.setValueTextSize(10f)
                        barChart.data = data

                        // Customizes bar chart
                        barChart.setDescription("")
                        barChart.setDrawGridBackground(true)

                        // Sets accent color to bar chart
                        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS)

                        barChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                            override fun onValueSelected(e: Entry?, dataSetIndex: Int, h: Highlight) {

                                vibration(vibrationData)
                                val cursorIndex = dbHandler.itemClicked(h.xIndex + 1)

                                cursorIndex.moveToFirst()

                                val mapIndex = HashMap<String, String>()
                                mapIndex["id"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_ID))
                                mapIndex["intime"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_IN))
                                mapIndex["out"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_OUT))
                                mapIndex["break"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_BREAK))
                                mapIndex["total"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_TOTAL))
                                mapIndex["day"] = cursorIndex.getString(cursorIndex.getColumnIndex(DBHelper.COLUMN_DAY))

                                val inTime = mapIndex["intime"]
                                val outTime = mapIndex["out"]
                                val breakTime = mapIndex["break"]
                                val total = mapIndex["total"]
                                val day = mapIndex["day"]

                                textViewItemClicked.text = getString(R.string.bar_chart_item_clicked, inTime, outTime, breakTime, total, day)

                            }

                            override fun onNothingSelected() {
                                vibration(vibrationData)
                                textViewItemClicked.text = ""
                            }
                        })

                    }

                    count == 0 -> {
                        Toast.makeText(this, getString(R.string.no_hours_stored_for_graph), Toast.LENGTH_SHORT).show()
                    }
                }
                // Moves cursor to next to map in data
                cursor.moveToNext()
            }
        } catch (e: IllegalArgumentException) {
            Toast.makeText(this, getString(R.string.there_was_an_error_graph), Toast.LENGTH_LONG).show()
        }
    }

    override fun onRestart() {
        super.onRestart()
        recreate()
    }

    private fun vibration(vibrationData: VibrationData) {
        if (vibrationData.loadVibrationState()) {
            val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(5, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu_graph, menu)
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
        vibration(vibrationData)
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
            else -> super.onOptionsItemSelected(item)
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

}