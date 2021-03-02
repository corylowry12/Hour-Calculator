package com.cory.hourcalculator.classes

import android.content.Context
import android.content.SharedPreferences

class VibrationData(context: Context) {

    private var sharedPreferences: SharedPreferences = context.getSharedPreferences("file", Context.MODE_PRIVATE)

    //this saves the vibration preference
    fun setVibrationState(state: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("Vibration", state)
        editor.apply()
    }

    // this will load the vibration mode state
    fun loadVibrationState(): Boolean {
        val state = sharedPreferences.getBoolean("Vibration", true)
        return (state)
    }
}