package com.cory.hourcalculator.classes

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cory.hourcalculator.R
import com.google.android.material.snackbar.Snackbar

class ManageFilePermissions(private val activity: Activity, private val list: List<String>, private val code:Int) {

    // Check permissions at runtime
    fun checkPermissions(context: Context) : Boolean {
        return if (isPermissionsGranted() != PackageManager.PERMISSION_GRANTED) {
            showAlert(context)
            false
        } else {
            //Toast.makeText(activity, "Permission Granted", Toast.LENGTH_SHORT).show()
            true
        }
    }

    // Check permissions status
    private fun isPermissionsGranted(): Int {
        // PERMISSION_GRANTED : Constant Value: 0
        // PERMISSION_DENIED : Constant Value: -1
        var counter = 0
        for (permission in list) {
            counter += ContextCompat.checkSelfPermission(activity, permission)
        }
        return counter
    }


    // Find the first denied permission
    private fun deniedPermission(context : Context): String {
        for (permission in list) {
            if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_DENIED) return permission
        }
        return ""
    }

    // Show alert dialog to request permissions
    private fun showAlert(context: Context) {
        val alertDialogData = AlertDialogData(context)
        if(!alertDialogData.loadAlertState()) {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle("Need permissions")
            builder.setMessage("Need some permissions")
            builder.setCancelable(false)
            builder.setPositiveButton(context.getString(R.string.ok)) { _, _ ->
                requestPermissions(context)
                alertDialogData.setAlertState(true)
            }
            builder.setNeutralButton("Cancel") { _, _ ->
                alertDialogData.setAlertState(true)
                Snackbar.make(activity.findViewById(R.id.constraintLayout), "Permissions not granted", Snackbar.LENGTH_LONG)
                    .show()}
            val dialog = builder.create()
            dialog.show()
        }
    }

    // Request the permissions at run time
    private fun requestPermissions(context: Context) {
        val permission = deniedPermission(context)
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            Snackbar.make(activity.findViewById(R.id.constraintLayout), "Go into settings and enable file permissions", Snackbar.LENGTH_LONG)
                .show()
        } else {
            ActivityCompat.requestPermissions(activity, list.toTypedArray(), code)
        }
    }

    fun processPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray): Boolean {
        var result = 0
        if (grantResults.isNotEmpty()) {
            for (item in grantResults) {
                result += item
            }
        }
        if (result == PackageManager.PERMISSION_GRANTED) return true
        return false
    }
}