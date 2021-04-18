package com.cory.hourcalculator.classes

import android.content.Context
import android.content.SharedPreferences

class SpinnerData(context: Context) {

    private var sharedPreferences: SharedPreferences = context.getSharedPreferences("file", Context.MODE_PRIVATE)

    //this saves spinner1 preference
    fun setSpinner1State(state: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("Spinner1", state)
        editor.apply()
    }

    // this will load spinner1 preference
    fun loadSpinner1State(): Boolean {
        val state = sharedPreferences.getBoolean("Spinner1", false)
        return (state)
    }

    //this saves spinner1 preference
    fun setSpinner2State(state: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("Spinner2", state)
        editor.apply()
    }

    // this will load spinner1 preference
    fun loadSpinner2State(): Boolean {
        val state = sharedPreferences.getBoolean("Spinner2", false)
        return (state)
    }

}