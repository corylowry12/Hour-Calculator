package com.cory.hourcalculator.classes

import android.content.Context
import com.cory.hourcalculator.database.DBHelper

class HistoryDeletion(context: Context) {

    private val dbHandler = DBHelper(context, null)

    private val dataList = ArrayList<HashMap<String, String>>()

    private val daysWorkedPerWeek = DaysWorkedPerWeek(context)

    fun deletion(context: Context) {
        val numberToDelete = dbHandler.getCount() - daysWorkedPerWeek.loadDaysWorked().toString().toInt()
        dataList.clear()
        val cursor = dbHandler.automaticDeletion(context, numberToDelete)
        cursor!!.moveToFirst()

        val map = HashMap<String, String>()
        while (!cursor.isAfterLast) {

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
    }
}