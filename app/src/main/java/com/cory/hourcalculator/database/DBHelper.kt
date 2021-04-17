@file:Suppress("CanBeVal", "CanBeVal")

package com.cory.hourcalculator.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.cory.hourcalculator.classes.SortData

class DBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {

        db.execSQL(
            "CREATE TABLE $TABLE_NAME " +
                    "($COLUMN_ID INTEGER PRIMARY KEY, $COLUMN_IN TEXT, $COLUMN_OUT TEXT, $COLUMN_BREAK TEXT, $COLUMN_TOTAL TEXT, $COLUMN_DAY TEXT)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertRow(intime: String, outtime:String, breaktime: String, total: String, dayOfWeek: String) {
        val values = ContentValues()
        values.put(COLUMN_IN, intime)
        values.put(COLUMN_OUT, outtime)
        values.put(COLUMN_BREAK, breaktime)
        values.put(COLUMN_TOTAL, total)
        values.put(COLUMN_DAY, dayOfWeek)


        val db = this.writableDatabase
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun update(id : String, intime: String, outtime: String, breaktime: String, total: String, dayOfWeek: String) {
        val values = ContentValues()
        //values.put(COLUMN_ID, id)
        values.put(COLUMN_IN, intime)
        values.put(COLUMN_OUT, outtime)
        values.put(COLUMN_BREAK, breaktime)
        values.put(COLUMN_TOTAL, total)
        values.put(COLUMN_DAY, dayOfWeek)

        val db = this.writableDatabase
        /*db.update(TABLE_NAME, values, "$COLUMN_ID=?", arrayOf(id))
        db.close()*/
        db.update(TABLE_NAME, values,"$COLUMN_ID=?", arrayOf(id))

    }

    fun getCount(): Int {
        val db = this.readableDatabase
        return DatabaseUtils.longForQuery(db, "SELECT COUNT(*) FROM $TABLE_NAME", null).toInt()

    }

    fun deleteRow(row_id: String) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(row_id))
        db.close()

    }

    fun getAllRow(context: Context): Cursor? {
        val db = this.writableDatabase
        val sortData = SortData(context)
        val sorttype = sortData.loadSortState()
        return db.rawQuery("SELECT * FROM $TABLE_NAME ORDER BY $sorttype", null)
    }

    fun returntop7(): Cursor? {
        val db = this.readableDatabase

        return db.rawQuery("SELECT * FROM $TABLE_NAME ORDER BY day DESC LIMIT 7", null)
    }

    fun deleteAll() {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, null, null)
        db.execSQL("delete from $TABLE_NAME")
        db.close()
    }

    fun retrieve(query: String): Cursor {

        var cursor : Cursor
        val db = this.writableDatabase
        val columns = listOf(COLUMN_ID, COLUMN_IN, COLUMN_OUT, COLUMN_BREAK, COLUMN_TOTAL, COLUMN_DAY)

        if(query != "" && query.isNotEmpty()) {
            val sql = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_IN LIKE '%$query%' " +
                    " OR $COLUMN_OUT LIKE '%$query%' " +
                    " OR $COLUMN_BREAK LIKE '%$query%' " +
                    " OR $COLUMN_TOTAL LIKE '%$query%' " +
                    " OR $COLUMN_DAY LIKE '%$query%' "

           cursor = db.rawQuery(sql, null)
            return cursor
        }

            cursor = db.query(TABLE_NAME, columns.toTypedArray(), null, null, null, null, null)
            return cursor


    }

    fun itemClicked(index : Int): Cursor {

        val db = this.readableDatabase

        return db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID=$index", null)
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "myDBfile.db"
        const val TABLE_NAME = "users"

        const val COLUMN_ID = "id"
        const val COLUMN_IN = "intime"
        const val COLUMN_OUT = "out"
        const val COLUMN_BREAK = "break"
        const val COLUMN_TOTAL = "total"
        const val COLUMN_DAY = "day"
    }
}
