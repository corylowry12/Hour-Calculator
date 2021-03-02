package com.cory.hourcalculator.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.cory.hourcalculator.classes.SortDataTrash

class DBHelperTrash(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME_TRASH, factory, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {

        db.execSQL(
            "CREATE TABLE $TABLE_NAME_TRASH " +
                    "($COLUMN_ID_TRASH INTEGER PRIMARY KEY, $COLUMN_IN_TRASH TEXT, $COLUMN_OUT_TRASH TEXT, $COLUMN_BREAK_TRASH TEXT, $COLUMN_TOTAL_TRASH TEXT, $COLUMN_DAY_TRASH TEXT)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_TRASH")
        onCreate(db)
    }

    fun insertRow(intime: String, outtime:String, breaktime: String, total: String, dayOfWeek: String) {
        val values = ContentValues()
        values.put(COLUMN_IN_TRASH, intime)
        values.put(COLUMN_OUT_TRASH, outtime)
        values.put(COLUMN_BREAK_TRASH, breaktime)
        values.put(COLUMN_TOTAL_TRASH, total)
        values.put(COLUMN_DAY_TRASH, dayOfWeek)


        val db = this.writableDatabase
        db.insert(TABLE_NAME_TRASH, null, values)
        db.close()
    }

    fun getCount(): Int {
        val db = this.readableDatabase
        return DatabaseUtils.longForQuery(db, "SELECT COUNT(*) FROM $TABLE_NAME_TRASH", null).toInt()

    }

    fun deleteRow(row_id: String) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME_TRASH, "$COLUMN_ID_TRASH = ?", arrayOf(row_id))
        db.close()
    }

    fun getAllRow(context: Context): Cursor? {
        val db = this.readableDatabase
        val sortData = SortDataTrash(context)
        val sorttype = sortData.loadSortStateTrash()
        return db.rawQuery("SELECT * FROM $TABLE_NAME_TRASH ORDER BY $sorttype", null)
    }

    fun deleteAll() {
        val db = this.writableDatabase
        db.delete(TABLE_NAME_TRASH, null, null)
        db.execSQL("delete from $TABLE_NAME_TRASH")
        db.close()
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME_TRASH = "trash.db"
        const val TABLE_NAME_TRASH = "trash"

        const val COLUMN_ID_TRASH = "id_trash"
        const val COLUMN_IN_TRASH = "intime_trash"
        const val COLUMN_OUT_TRASH = "out_trash"
        const val COLUMN_BREAK_TRASH = "break_trash"
        const val COLUMN_TOTAL_TRASH = "total_trash"
        const val COLUMN_DAY_TRASH = "day_trash"
    }
}