package com.cory.hourcalculator.classes

import android.content.Context
import android.content.SharedPreferences

class BreakData (context: Context) {

    private var sharedPreferences: SharedPreferences = context.getSharedPreferences("file", Context.MODE_PRIVATE)

    //this saves the break preference
    fun setBreakState(state: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("Break", state)
        editor.apply()
    }

    // this will load break state
    fun loadBreakState(): Boolean {
        val state = sharedPreferences.getBoolean("Break", true)
        return (state)
    }
}