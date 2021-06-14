package com.cory.hourcalculator.classes

import android.content.Context
import android.content.SharedPreferences

class DaysWorkedPerWeek(context: Context) {

    private var sharedPreferences: SharedPreferences = context.getSharedPreferences("file", Context.MODE_PRIVATE)

    //this saves the wage amount
    fun setDaysWorked(state: String?) {
        val editor = sharedPreferences.edit()
        editor.putString("Days", state!!)
        editor.apply()
    }

    // this will load wage amount
    fun loadDaysWorked(): String? {
        val state = sharedPreferences.getString("Days", "5")
        return (state)
    }
}