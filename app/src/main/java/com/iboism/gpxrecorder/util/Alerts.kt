package com.iboism.gpxrecorder.util

import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import com.iboism.gpxrecorder.R

/**
 * Created by Brad on 11/28/2017.
 */
class Alerts(val context: Context) {
    fun permissionDeniedAlert(action: () -> Unit): AlertDialog {
        return AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.denied_alert_title))
                .setMessage(context.getString(R.string.denied_alert_message))
                .setPositiveButton(context.getString(R.string.denied_alert_button)) { _, _ -> action.invoke() }
                .setCancelable(true)
                .create()
    }

    fun permissionDeniedForeverAlert(action: () -> Unit): AlertDialog {
        return AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.denied_alert_title))
                .setMessage(context.getString(R.string.denied_forever_alert_message))
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