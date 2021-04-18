package com.cory.hourcalculator.classes

import android.content.Context
import android.content.SharedPreferences

class PerformanceModeData(context: Context) {

    private var sharedPreferences: SharedPreferences = context.getSharedPreferences("file", Context.MODE_PRIVATE)

    //this saves the break preference
    fun setPerformanceMode(state: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("Performance", state)
        editor.apply()
    }

    // this will load break state
    fun loadPerformanceMode(): Boolean {
        val state = sharedPreferences.getBoolean("Performance", false)
        return (state)
    }
}