package com.cory.hourcalculator.classes

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cory.hourcalculator.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ManagePermissions(private val activity: Activity, private val list: List<String>, private val code:Int) {

    // Check permissions at runtime
    fun checkPermissions() : Boolean {
        return isPermissionsGranted() == PackageManager.PERMISSION_GRANTED
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
    private fun deniedPermission(): String {
        for (permission in list) {
            if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_DENIED) return permission
        }
        return ""
    }

    fun showAlertSettings(context : Context) {
            val builder = MaterialAlertDialogBuilder(context )
            builder.setTitle(R.string.need_permissions)
            builder.setMessage(R.string.permissions_needed_caption)
            builder.setCancelable(false)
            builder.setPositiveButton(R.string.ok) { _, _ ->
                requestPermissions()
            }
            builder.setNeutralButton(R.string.cancel) { _, _ ->
                Toast.makeText(context, context.getString(R.string.permission_not_granted), Toast.LENGTH_SHORT)
                    .show()
            }
            val dialog = builder.create()
            dialog.show()
    }

    // Request the permissions at run time
    private fun requestPermissions() {
        val permission = deniedPermission()
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            //Toast.makeText(context, "Go into settings and clear app data", Toast.LENGTH_SHORT)
               // .show()
            ActivityCompat.requestPermissions(activity, list.toTypedArray(), code)
        } else {
            ActivityCompat.requestPermissions(activity, list.toTypedArray(), code)
        }
    }
}