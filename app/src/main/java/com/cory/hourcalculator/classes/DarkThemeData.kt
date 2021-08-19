package com.cory.hourcalculator.classes

import android.content.Context
import android.content.SharedPreferences

class DarkThemeData(context: Context) {

    private var sharedPreferences: SharedPreferences = context.getSharedPreferences("file", Context.MODE_PRIVATE)

    //this saves the theme preference
    fun setDarkModeState(state: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("Dark", state)
        editor.apply()
    }

    // this will load the night mode state
    fun loadDarkModeState(): Boolean {
        val state = sharedPreferences.getBoolean("Dark", true)
        return (state)
    }
}