package com.cory.hourcalculator.classes

import android.content.Context
import android.content.SharedPreferences

class ExportData(context: Context) {

    private var sharedPreferences: SharedPreferences = context.getSharedPreferences("file", Context.MODE_PRIVATE)

    //this saves the theme preference
    fun setExportFormat(state: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt("Export", state)
        editor.apply()
    }

    // this will load the night mode state
    fun loadExportFormat(): Int {
        val state = sharedPreferences.getInt("Export", 1)
        return (state)
    }
}