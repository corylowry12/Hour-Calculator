package com.cory.hourcalculator.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import com.cory.hourcalculator.R
import com.cory.hourcalculator.activities.EditActivity
import com.cory.hourcalculator.activities.HistoryActivity
import com.cory.hourcalculator.classes.PerformanceModeData
import com.cory.hourcalculator.classes.VibrationData
import com.cory.hourcalculator.database.DBHelper
import com.cory.hourcalculator.database.DBHelperTrash
import kotlinx.android.synthetic.main.list_row.view.*

class CustomAdapter(private val context: Context,
                    private val dataList: ArrayList<HashMap<String, String>>) : BaseAdapter() {

    private val inflater: LayoutInflater = this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    override fun getCount(): Int { return dataList.size }
    override fun getItem(position: Int): Int { return position }
    override fun getItemId(position: Int): Long { return position.toLong() }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val dbHandler = DBHelper(context, null)
        val dbHandlerTrash = DBHelperTrash(context, null)
        val dataitem = dataList[position]

        val vibrationData = VibrationData(context)

        val rowView = inflater.inflate(R.layout.list_row, parent, false)

        rowView.findViewById<TextView>(R.id.row_in).text = context.getString(R.string.in_time_adapter, dataitem["intime"])
        rowView.findViewById<TextView>(R.id.row_out).text = context.getString(R.string.out_time_adapter, dataitem["out"])
        rowView.findViewById<TextView>(R.id.row_break).text = context.getString(R.string.break_time_adapter, dataitem["break"])
        rowView.findViewById<TextView>(R.id.row_total).text = context.getString(R.string.total_time_adapter, dataitem["total"])
        rowView.findViewById<TextView>(R.id.row_day).text = context.getString(R.string.date_adapter, dataitem["day"])

        rowView.imageViewOptions.setOnClickListener {
            val popup = PopupMenu(context, rowView.imageViewOptions)
            popup.inflate(R.menu.menu_history_options)
            popup.setOnMenuItemClickListener { item ->
                vibration(vibrationData)
                when (item.itemId) {
                    R.id.menu1 -> {
                        //dataList.clear()
                        //val i = 1
                        /*val id = dataList[i]["id"].toString()
                        //val returnValue = dataitem["id"]
                        val intime = dataList[+i]["intime"].toString()
                        val outtime = dataList[+i]["out"].toString()
                        val breakTime = dataList[+i]["break"].toString()
                        val total = dataList[+i]["total"].toString()
                        val day = dataList[+i]["day"].toString()
                        val day1 = LocalDateTime.now()
                        val day2 = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                        val dayOfWeek = day1.format(day2)
                        //dbHandler.deleteRow(id)
                        //dbHandlerTrash.insertRow(intime, outtime, breakTime, total, day)
                        var idCursor : Int*/

                        dataList.clear()
                        val cursor = dbHandler.getAllRow(context)
                        cursor!!.moveToPosition(position)

                        val map = HashMap<String, String>()
                        while (cursor.position == position) {

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

                            cursor.moveToNext()

                        }

                        val runnable = Runnable {
                            (context as HistoryActivity).update()
                        }
                        HistoryActivity().runOnUiThread(runnable)


                    }
                    R.id.menu2 -> {
                        /*val i = 0
                        val id = dataList[+i]["id"].toString()
                        dbHandler.deleteRow(id)*/
                        dataList.clear()
                        val cursor = dbHandler.getAllRow(context)
                        cursor!!.moveToPosition(position)

                        val map = HashMap<String, String>()
                        while (cursor.position == position) {

                            map["id"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ID))
                            map["intime"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_IN))
                            map["out"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_OUT))
                            map["break"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_BREAK))
                            map["total"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TOTAL))
                            map["day"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DAY))
                            dataList.add(map)

                            dbHandler.deleteRow(map["id"].toString())

                            cursor.moveToNext()

                        }

                        val runnable = Runnable {
                            (context as HistoryActivity).update()

                        }
                        HistoryActivity().runOnUiThread(runnable)
                    }
                    R.id.menu3 -> {
                        val alertDialog = androidx.appcompat.app.AlertDialog.Builder(context)
                        alertDialog.setTitle(context.getString(R.string.move_all_to_trash_history_heading))
                        alertDialog.setMessage(context.getString(R.string.move_all_to_trash_history))
                        alertDialog.setPositiveButton(context.getString(R.string.yes)) { _, _ ->
                            dataList.clear()
                            val cursor1 = dbHandler.getAllRow(context)
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
                            val runnable = Runnable {
                                (context as HistoryActivity).update()
                            }
                            HistoryActivity().runOnUiThread(runnable)
                        }
                            .setNegativeButton(context.getString(R.string.no), null)
                        val alert = alertDialog.create()
                        alert.show()

                    }
                    R.id.menu4 -> {
                        val alertDialog = androidx.appcompat.app.AlertDialog.Builder(context)
                        alertDialog.setTitle(context.getString(R.string.delete_all_from_history_heading))
                        alertDialog.setMessage(context.getString(R.string.delete_all_from_history))
                        alertDialog.setPositiveButton(context.getString(R.string.yes)) { _, _ ->
                            dbHandler.deleteAll()
                            val runnable = Runnable {
                                (context as HistoryActivity).update()
                            }
                            HistoryActivity().runOnUiThread(runnable)
                        }
                            .setNegativeButton(context.getString(R.string.no), null)
                        val alert = alertDialog.create()
                        alert.show()
                    }
                    R.id.menu5 -> {
                        dataList.clear()
                        val cursor = dbHandler.getAllRow(context)
                        cursor!!.moveToPosition(position)

                        val map = HashMap<String, String>()
                        while (cursor.position == position) {

                            map["id"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ID))
                            map["intime"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_IN))
                            map["out"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_OUT))
                            map["break"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_BREAK))
                            map["total"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TOTAL))
                            map["day"] = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DAY))
                            dataList.add(map)

                            cursor.moveToNext()

                        }
                        /*if(!map["intime"].toString().contains(context.getString(R.string.am)) || !map["out"].toString().contains(context.getString(R.string.pm))
                            || !map["intime"].toString().contains(context.getString(R.string.pm)) || !map["out"].toString().contains(context.getString(R.string.am)))*/
                           if ((map["intime"].toString().contains(context.getString(R.string.am)) || map["intime"].toString().contains(context.getString(R.string.pm))) &&
                               (map["out"].toString().contains(context.getString(R.string.am)) || map["out"].toString().contains(context.getString(R.string.pm))))     {
                               val intent = Intent(context, EditActivity::class.java)
                               intent.putExtra("id", position.toString())
                               (context as HistoryActivity).startActivity(intent)
                               if(!PerformanceModeData(context).loadPerformanceMode()) {
                                   (context).overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                               }
                               else {
                                   (context).overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
                               }
                        }
                        else {
                               Toast.makeText(context, map["intime"].toString() /*context.getString(R.string.cant_edit)*/, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                true
            }
            popup.show()
        }

        (context as HistoryActivity).findViewById<ListView>(R.id.listView).setOnScrollChangeListener { _, _, _, _, _ ->
            if(!PerformanceModeData(context).loadPerformanceMode()) {
                val animation: Animation = AnimationUtils.loadAnimation(context, R.anim.list_view_scroll_animation)
                rowView.startAnimation(animation)
            }
        }

        rowView.tag = position
        return rowView
    }

    private fun vibration(vibrationData: VibrationData) {
        if (vibrationData.loadVibrationState()) {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(5, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }
}