package com.cory.hourcalculator.classes

import android.content.Context
import android.content.SharedPreferences

class AlertDialogData(context: Context) {

    private var sharedPreferences: SharedPreferences = context.getSharedPreferences("file", Context.MODE_PRIVATE)

    //this saves the break preference
    fun setAlertState(state: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("Alert", state)
        editor.apply()
    }

    // this will load break state
    fun loadAlertState(): Boolean {
        val state = sharedPreferences.getBoolean("Alert", false)
        return (state)
    }
}