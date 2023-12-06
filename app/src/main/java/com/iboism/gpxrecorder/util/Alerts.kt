package com.iboism.gpxrecorder.util

import android.content.Context
import android.content.DialogInterface
import android.os.Build
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.iboism.gpxrecorder.R

/**
 * Created by Brad on 11/28/2017.
 */
class Alerts(val context: Context) {
    fun permissionDeniedAlert(action: () -> Unit): AlertDialog {
        val message = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val permissionTitle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.packageManager.backgroundPermissionOptionLabel
            } else {
                context.getString(R.string.background_permission_title)
            }
            context.getString(R.string.denied_alert_message_background, permissionTitle)
        } else {
            context.getString(R.string.denied_alert_message)
        }
        return MaterialAlertDialogBuilder(context)
                .setTitle(context.getString(R.string.denied_alert_title))
                .setMessage(message)
                .setPositiveButton(context.getString(R.string.denied_alert_button)) { _, _ -> action.invoke() }
                .setCancelable(true)
                .create()
    }

    fun genericError(messageResId: Int, onDismiss: (DialogInterface) -> Unit = {}): AlertDialog {
        return MaterialAlertDialogBuilder(context)
                .setTitle(R.string.error)
                .setMessage(messageResId)
                .setCancelable(true)
                .setOnDismissListener(onDismiss)
                .create()
    }

    fun backgroundLocationJustificationAlert(onDismiss: (DialogInterface) -> Unit): AlertDialog {
        return MaterialAlertDialogBuilder(context)
                .setTitle(context.getString(R.string.background_location_justification_title))
                .setMessage(context.getString(R.string.background_location_justification_desc))
                .setPositiveButton(R.string.okay) {_, _ -> }
                .setOnDismissListener(onDismiss)
                .create()
    }
}