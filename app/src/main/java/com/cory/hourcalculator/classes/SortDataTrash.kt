package com.cory.hourcalculator.classes

import android.content.Context
import android.content.SharedPreferences

class SortDataTrash(context: Context) {

    private var sharedPreferences: SharedPreferences = context.getSharedPreferences("file", Context.MODE_PRIVATE)

    //this saves the sort trash preference
    fun setSortStateTrash(state: String?) {
        val editor = sharedPreferences.edit()
        editor.putString("Sort_trash", state!!)
        editor.apply()
    }

    // this will load the sort trash state
    fun loadSortStateTrash(): String? {
        val state = sharedPreferences.getString("Sort_trash", "day_trash DESC")
        return (state)
    }
}