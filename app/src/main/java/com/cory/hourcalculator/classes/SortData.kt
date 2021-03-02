package com.cory.hourcalculator.classes

import android.content.Context
import android.content.SharedPreferences

class SortData(context: Context) {

    private var sharedPreferences: SharedPreferences = context.getSharedPreferences("file", Context.MODE_PRIVATE)

    //this saves the name preference
    fun setSortState(state: String?) {
        val editor = sharedPreferences.edit()
        editor.putString("Sort", state!!)
        editor.apply()
    }

    // this will load the name state
    fun loadSortState(): String? {
        val state = sharedPreferences.getString("Sort", "day DESC")
        return (state)
    }
}