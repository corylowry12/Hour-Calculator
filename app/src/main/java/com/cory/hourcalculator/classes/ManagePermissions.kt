package com.cory.hourcalculator.classes

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cory.hourcalculator.R

class ManagePermissions(private val activity: Activity, private val list: List<String>, private val code:Int) {

    val vibrationData = VibrationData(activity)

    // Check permissions at runtime
    fun checkPermissions(): Boolean {
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
    /*private fun deniedPermission(): String {
        for (permission in list) {
            if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_DENIED) return permission
        }
        return ""
    }*/

    fun showAlertSettings(context: Context) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, activity.getString(R.string.permission))) {
            requestIfDenied()
        } else {
            val accentColor = AccentColor(context)
            val builder = AlertDialog.Builder(context, accentColor.alertTheme(activity))
            builder.setTitle(R.string.need_permissions)
            builder.setMessage(R.string.permissions_needed_caption)
            builder.setCancelable(false)
            builder.setPositiveButton(R.string.ok) { _, _ ->
                vibration(vibrationData)
                requestPermissions()
            }
            builder.setNeutralButton(R.string.cancel) { _, _ ->
                vibration(vibrationData)
                Toast.makeText(context, context.getString(R.string.permission_not_granted), Toast.LENGTH_SHORT).show()
            }
            val alert = builder.create()
            alert.show()
            when {
                accentColor.loadAccent() == 0 -> {
                    alert.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    alert.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
                }
                accentColor.loadAccent() == 1 -> {
                    alert.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(ContextCompat.getColor(context, R.color.pinkAccent))
                    alert.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.pinkAccent))
                }
                accentColor.loadAccent() == 2 -> {
                    alert.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(ContextCompat.getColor(context, R.color.orangeAccent))
                    alert.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.orangeAccent))
                }
                accentColor.loadAccent() == 3 -> {
                    alert.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(ContextCompat.getColor(context, R.color.redAccent))
                    alert.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.redAccent))
                }
            }
        }
    }

    // Request the permissions at run time
    private fun requestPermissions() {
        //val permission = deniedPermission()
        ActivityCompat.requestPermissions(activity, list.toTypedArray(), code)
    }

    private fun requestIfDenied() {
        val accentColor = AccentColor(activity)
        val builder = AlertDialog.Builder(activity, accentColor.alertTheme(activity))
        builder.setTitle(R.string.need_permissions)
        builder.setMessage(activity.getString(R.string.permissions_previously_denied))
        builder.setCancelable(false)
        builder.setPositiveButton(activity.getString(R.string.open_settings)) { _, _ ->
            vibration(vibrationData)
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri = Uri.fromParts("package", activity.packageName, null)
            intent.data = uri
            Toast.makeText(activity, activity.getString(R.string.manually_enable_permissions), Toast.LENGTH_SHORT).show()
            activity.startActivity(intent)
        }
        builder.setNeutralButton(activity.getString(R.string.cancel)) {dialog, _ ->
            vibration(vibrationData)
            Toast.makeText(activity, activity.getString(R.string.permission_not_granted), Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        val alert = builder.create()
        alert.show()
        when {
            accentColor.loadAccent() == 0 -> {
                alert.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(ContextCompat.getColor(activity, R.color.colorPrimary))
                alert.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(activity, R.color.colorPrimary))
            }
            accentColor.loadAccent() == 1 -> {
                alert.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(ContextCompat.getColor(activity, R.color.pinkAccent))
                alert.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(activity, R.color.pinkAccent))
            }
            accentColor.loadAccent() == 2 -> {
                alert.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(ContextCompat.getColor(activity, R.color.orangeAccent))
                alert.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(activity, R.color.orangeAccent))
            }
            accentColor.loadAccent() == 3 -> {
                alert.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(ContextCompat.getColor(activity, R.color.redAccent))
                alert.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(activity, R.color.redAccent))
            }
        }
    }

    private fun vibration(vibrationData: VibrationData) {
        if (vibrationData.loadVibrationState()) {
            val vibrator = activity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(5, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }
}