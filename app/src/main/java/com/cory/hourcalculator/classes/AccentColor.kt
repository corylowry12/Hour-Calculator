package com.cory.hourcalculator.classes

import android.content.Context
import android.content.SharedPreferences
import com.cory.hourcalculator.R

class AccentColor(context: Context) {

    private var sharedPreferences: SharedPreferences = context.getSharedPreferences("file", Context.MODE_PRIVATE)

    //this saves the theme preference
    fun setAccentState(state: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt("Accent", state)
        editor.apply()
    }
    // this will load the night mode state
    fun loadAccent(): Int{
        val state = sharedPreferences.getInt("Accent", 0)
        return (state)
    }

    fun alertTheme(context: Context) : Int {
        val state = sharedPreferences.getInt("Accent", 0)

        val darkThemeData = DarkThemeData(context)

        if (state == 0 && !darkThemeData.loadDarkModeState()) {
            return R.style.AlertDialogStyleLight
        }
        else if (state == 0 && darkThemeData.loadDarkModeState()) {
            return R.style.AlertDialogStyleDark
        }
        else if (state == 1 && !darkThemeData.loadDarkModeState()) {
            return R.style.AlertDialogStyleLight_pink
        }
        else if (state == 1 && darkThemeData.loadDarkModeState()) {
            return R.style.AlertDialogStyleDark_pink
        }
        else if (state == 2 && !darkThemeData.loadDarkModeState()) {
            return R.style.AlertDialogStyleLight_orange
        }
        else if (state == 2 && darkThemeData.loadDarkModeState()) {
            return R.style.AlertDialogStyleDark_orange
        }
        else if (state == 3 && !darkThemeData.loadDarkModeState()) {
            return R.style.AlertDialogStyleLight_red
        }
        else if (state == 3 && darkThemeData.loadDarkModeState()) {
            return R.style.AlertDialogStyleDark_red
        }

        return R.style.AlertDialogStyleDark
    }
}