package com.cory.hourcalculator.classes

import android.content.Context
import android.content.SharedPreferences

class FirebaseData(context: Context) {

    private var sharedPreferences: SharedPreferences = context.getSharedPreferences("file", Context.MODE_PRIVATE)

    //this saves the firebase logs
    fun setFirebaseLog(state: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("Firebase", state)
        editor.apply()
    }
    // this will load firebase logs
    fun loadFirebaseLog(): Boolean {
        val state = sharedPreferences.getBoolean("Firebase", false)
        return (state)
    }
}