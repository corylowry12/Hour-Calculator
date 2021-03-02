package com.cory.hourcalculator.classes

import android.content.Context
import android.content.SharedPreferences

class TrashAutomaticDeletion(context: Context) {

    private var sharedPreferences: SharedPreferences = context.getSharedPreferences("file", Context.MODE_PRIVATE)

    //this saves the theme preference
    fun setTrashDeletionState(state: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("Trash_Deletion", state)
        editor.apply()
    }
    // this will load the night mode state
    fun loadTrashDeletionState(): Boolean {
        val state = sharedPreferences.getBoolean("Trash_Deletion", true)
        return (state)
    }

}