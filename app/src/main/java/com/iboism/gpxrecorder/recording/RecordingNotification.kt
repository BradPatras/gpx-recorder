package com.iboism.gpxrecorder.recording

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.primary.MainActivity
import com.iboism.gpxrecorder.util.Keys

/**
 * Created by bradpatras on 12/13/17.
 */
class RecordingNotification(val context: Context) {
    fun forGpxId(id: Long): Notification {
        val openAppIntent = Intent(context, MainActivity::class.java)
        val openAppPendingIntent = PendingIntent.getActivity(context, 0, openAppIntent, 0)

        val setWaypointIntent = Intent(context, CreateWaypointDialogActivity::class.java)
        setWaypointIntent.putExtra(Keys.GpxId, id)
        val setWaypointPendingIntent = PendingIntent.getActivity(context, 0, setWaypointIntent, 0)

        return NotificationCompat.Builder(context, Notification.CATEGORY_SERVICE)
                .setContentTitle("GPX Recorder")
                .setContentIntent(openAppPendingIntent)
                .setContentText("Location recording in progress")
                .setSmallIcon(R.drawable.gpx_notification)
                .setStyle(NotificationCompat.BigTextStyle().bigText("Location recording in progress"))
                .addAction(R.drawable.ic_add_location, "Add Waypoint", setWaypointPendingIntent)
                .build()
    }
}