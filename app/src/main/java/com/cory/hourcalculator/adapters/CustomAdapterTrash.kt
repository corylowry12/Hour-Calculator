package com.cory.hourcalculator.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import com.cory.hourcalculator.R
import com.cory.hourcalculator.activities.TrashActivity
import com.cory.hourcalculator.classes.PerformanceModeData
import com.cory.hourcalculator.classes.VibrationData
import com.cory.hourcalculator.database.DBHelper
import com.cory.hourcalculator.database.DBHelperTrash
import kotlinx.android.synthetic.main.list_row_trash.view.*

class CustomAdapterTrash(private val context: Context, private val dataList: ArrayList<HashMap<String, String>>) : BaseAdapter() {

    private val inflater: LayoutInflater = this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    override fun getCount(): Int {
        return dataList.size
    }

    override fun getItem(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val dbHandler = DBHelper(context, null)
        val dbHandlerTrash = DBHelperTrash(context, null)
        val dataitem = dataList[position]

        val vibrationData = VibrationData(context)

        val rowView = inflater.inflate(R.layout.list_row_trash, parent, false)

        rowView.findViewById<TextView>(R.id.row_in_trash).text = context.getString(R.string.in_time_adapter, dataitem["intime_trash"])
        rowView.findViewById<TextView>(R.id.row_out_trash).text = context.getString(R.string.out_time_adapter, dataitem["out_trash"])
        rowView.findViewById<TextView>(R.id.row_break_trash).text = context.getString(R.string.break_time_adapter, dataitem["break_trash"])
        rowView.findViewById<TextView>(R.id.row_total_trash).text = context.getString(R.string.total_time_adapter, dataitem["total_trash"])
        rowView.findViewById<TextView>(R.id.row_day_trash).text = context.getString(R.string.date_adapter, dataitem["day_trash"])

        rowView.imageViewOptions_trash.setOnClickListener {
            vibration(vibrationData)
            val popup = PopupMenu(context, rowView.imageViewOptions_trash)
            popup.inflate(R.menu.menu_trash_options)
            popup.setOnMenuItemClickListener { item ->
                vibration(vibrationData)
                when (item.itemId) {
                    R.id.menu1 -> {
                        dataList.clear()
                        val cursor = dbHandlerTrash.getAllRow(context)
                        cursor!!.moveToPosition(position)

                        val map = HashMap<String, String>()
                        while (cursor.position == position) {

                            map["id"] = cursor.getString(cursor.getColumnIndex(DBHelperTrash.COLUMN_ID_TRASH))
                            map["intime"] = cursor.getString(cursor.getColumnIndex(DBHelperTrash.COLUMN_IN_TRASH))
                            map["out"] = cursor.getString(cursor.getColumnIndex(DBHelperTrash.COLUMN_OUT_TRASH))
                            map["break"] = cursor.getString(cursor.getColumnIndex(DBHelperTrash.COLUMN_BREAK_TRASH))
                            map["total"] = cursor.getString(cursor.getColumnIndex(DBHelperTrash.COLUMN_TOTAL_TRASH))
                            map["day"] = cursor.getString(cursor.getColumnIndex(DBHelperTrash.COLUMN_DAY_TRASH))
                            dataList.add(map)

                            dbHandler.insertRow(
                                map["intime"].toString(), map["out"].toString(),
                                map["total"].toString(), map["day"].toString()
                            )
                            dbHandlerTrash.deleteRow(map["id"].toString())

                            cursor.moveToNext()

                        }
                        val runnable = Runnable {
                            (context as TrashActivity).update()
                            Toast.makeText(context, context.getString(R.string.item_restored), Toast.LENGTH_SHORT).show()
                        }
                        TrashActivity().runOnUiThread(runnable)
                    }
                    R.id.menu2 -> {
                        dataList.clear()
                        val cursor = dbHandlerTrash.getAllRow(context)
                        cursor!!.moveToPosition(position)

                        val map = HashMap<String, String>()
                        while (cursor.position == position) {

                            map["id"] = cursor.getString(cursor.getColumnIndex(DBHelperTrash.COLUMN_ID_TRASH))
                            map["intime"] = cursor.getString(cursor.getColumnIndex(DBHelperTrash.COLUMN_IN_TRASH))
                            map["out"] = cursor.getString(cursor.getColumnIndex(DBHelperTrash.COLUMN_OUT_TRASH))
                            map["break"] = cursor.getString(cursor.getColumnIndex(DBHelperTrash.COLUMN_BREAK_TRASH))
                            map["total"] = cursor.getString(cursor.getColumnIndex(DBHelperTrash.COLUMN_TOTAL_TRASH))
                            map["day"] = cursor.getString(cursor.getColumnIndex(DBHelperTrash.COLUMN_DAY_TRASH))
                            dataList.add(map)

                            dbHandlerTrash.deleteRow(map["id"].toString())

                            cursor.moveToNext()

                        }
                        val runnable = Runnable {
                            (context as TrashActivity).update()
                            Toast.makeText(context, context.getString(R.string.item_deleted), Toast.LENGTH_SHORT).show()
                        }
                        TrashActivity().runOnUiThread(runnable)
                    }
                    R.id.menu3 -> {
                        val alertDialog = androidx.appcompat.app.AlertDialog.Builder(context)
                        alertDialog.setTitle(context.getString(R.string.restore_all))
                        alertDialog.setMessage(context.getString(R.string.restore_all_items_trash))
                        alertDialog.setPositiveButton(context.getString(R.string.yes)) { _, _ ->
                            if (dbHandlerTrash.getCount() > 0) {
                                dataList.clear()
                                val cursor1 = dbHandlerTrash.getAllRow(context)
                                cursor1!!.moveToFirst()

                                while (!cursor1.isAfterLast) {
                                    val intime = cursor1.getString(cursor1.getColumnIndex(DBHelperTrash.COLUMN_IN_TRASH))
                                    val outtime = cursor1.getString(cursor1.getColumnIndex(DBHelperTrash.COLUMN_OUT_TRASH))
                                    val breaktime = cursor1.getString(cursor1.getColumnIndex(DBHelperTrash.COLUMN_BREAK_TRASH))
                                    val totaltime = cursor1.getString(cursor1.getColumnIndex(DBHelperTrash.COLUMN_TOTAL_TRASH))
                                    val day = cursor1.getString(cursor1.getColumnIndex(DBHelperTrash.COLUMN_DAY_TRASH))

                                    dbHandler.insertRow(intime, outtime, totaltime, day)

                                    cursor1.moveToNext()
                                }
                                dbHandlerTrash.deleteAll()
                                val runnable = Runnable {
                                    (context as TrashActivity).update()
                                    Toast.makeText(context, context.getString(R.string.all_items_restored), Toast.LENGTH_SHORT).show()
                                }
                                TrashActivity().runOnUiThread(runnable)
                            }
                        }
                        alertDialog.setNegativeButton(context.getString(R.string.no), null)
                        alertDialog.show()
                    }
                    R.id.menu4 -> {
                        val alertDialog = androidx.appcompat.app.AlertDialog.Builder(context)
                        alertDialog.setTitle(context.getString(R.string.delete_all_from_trash_heading))
                        alertDialog.setMessage(context.getString(R.string.delete_all_from_trash))
                        alertDialog.setPositiveButton(context.getString(R.string.yes)) { _, _ ->
                            dbHandlerTrash.deleteAll()
                            val runnable = Runnable {
                                (context as TrashActivity).update()
                                Toast.makeText(context, context.getString(R.string.all_items_deleted), Toast.LENGTH_SHORT).show()
                            }
                            TrashActivity().runOnUiThread(runnable)
                        }
                            .setNegativeButton(context.getString(R.string.no), null)
                        val alert = alertDialog.create()
                        alert.show()
                    }
                }
                true
            }
            popup.show()
        }

        (context as TrashActivity).findViewById<ListView>(R.id.listViewTrash).setOnScrollChangeListener { _, _, _, _, _ ->
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