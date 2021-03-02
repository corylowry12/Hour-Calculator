package com.cory.hourcalculator.classes

import android.content.Context
import android.content.SharedPreferences

class UpdateData(context: Context) {

    private var sharedPreferences: SharedPreferences = context.getSharedPreferences("file", Context.MODE_PRIVATE)

    //this saves the vibration preference
    fun setUpdateNotificationState(state: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("UpdateNotification", state)
        editor.apply()
    }

    // this will load the vibration mode state
    fun loadUpdateNotificationState(): Boolean {
        val state = sharedPreferences.getBoolean("UpdateNotification", true)
        return (state)
    }
}