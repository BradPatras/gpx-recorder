package com.iboism.gpxrecorder.util

import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AlertDialog
import com.iboism.gpxrecorder.R

/**
 * Created by Brad on 11/28/2017.
 */
class Alerts(val context: Context) {
    fun permissionDeniedAlert(action: () -> Unit): AlertDialog {
        val message = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val permissionTitle = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                context.packageManager.backgroundPermissionOptionLabel
            } else {
                context.getString(R.string.background_permission_title)
            }
            context.getString(R.string.denied_alert_message_background, permissionTitle)
        } else {
            context.getString(R.string.denied_alert_message)
        }
        return AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.denied_alert_title))
                .setMessage(message)
                .setPositiveButton(context.getString(R.string.denied_alert_button)) { _, _ -> action.invoke() }
                .setCancelable(true)
                .create()
    }

    fun genericError(messageResId: Int, onDismiss: (DialogInterface) -> Unit = {}): AlertDialog {
        return AlertDialog.Builder(context)
                .setTitle(R.string.error)
                .setMessage(messageResId)
                .setCancelable(true)
                .setOnDismissListener(onDismiss)
                .create()
    }
}