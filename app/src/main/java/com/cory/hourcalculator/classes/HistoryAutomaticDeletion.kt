package com.cory.hourcalculator.classes

import android.content.Context
import android.content.SharedPreferences

class HistoryAutomaticDeletion(context: Context) {

    private var sharedPreferences: SharedPreferences = context.getSharedPreferences("file", Context.MODE_PRIVATE)

    //this saves the theme preference
    fun setHistoryDeletionState(state: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("History_Deletion", state)
        editor.apply()
    }
    // this will load the night mode state
    fun loadHistoryDeletionState(): Boolean {
        val state = sharedPreferences.getBoolean("History_Deletion", false)
        return (state)
    }
}