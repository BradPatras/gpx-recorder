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
class RecordingNotification(val context: Context, val id: Long) {
    private var isPaused = false

    private val openAppIntent = Intent(context, MainActivity::class.java)
    private val openAppPendingIntent = PendingIntent.getActivity(context, 0, openAppIntent, 0)

    private val setWaypointIntent = Intent(context, CreateWaypointDialogActivity::class.java).putExtra(Keys.GpxId, id)
    private val setWaypointPendingIntent = PendingIntent.getActivity(context, id.toInt(), setWaypointIntent, 0)

    private val pauseRecordingIntent = Intent(context, LocationRecorderService::class.java).putExtra(Keys.PauseService, true)
    private val pauseRecordingPendingIntent = PendingIntent.getService(context, 2, pauseRecordingIntent, PendingIntent.FLAG_UPDATE_CURRENT)

    private val resumeRecordingIntent = Intent(context, LocationRecorderService::class.java).putExtra(Keys.ResumeService, true)
    private val resumeRecordingPendingIntent = PendingIntent.getService(context, 3, resumeRecordingIntent, PendingIntent.FLAG_UPDATE_CURRENT)

    private val stopRecordingIntent = Intent(context, LocationRecorderService::class.java).putExtra(Keys.StopService, true)
    private val stopRecordingPendingIntent = PendingIntent.getService(context, 4, stopRecordingIntent, PendingIntent.FLAG_UPDATE_CURRENT)

    fun notification(): Notification {

        val builder = NotificationCompat.Builder(context, Notification.CATEGORY_SERVICE)
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentIntent(openAppPendingIntent)
                .setContentText(context.getString(R.string.notification_recording_in_progress))
                .setSmallIcon(R.drawable.ic_gpx_notification)
                .setStyle(NotificationCompat.BigTextStyle().bigText(context.getString(R.string.notification_recording_in_progress)))
                .addAction(R.drawable.ic_add_location, context.getString(R.string.add_waypoint), setWaypointPendingIntent)
                .addAction(R.drawable.ic_cancel, context.getString(R.string.stop_recording), stopRecordingPendingIntent)

        when {
            isPaused -> builder.addAction(R.drawable.ic_play, context.getString(R.string.resume_recording), resumeRecordingPendingIntent)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(context.getString(R.string.notification_recording_paused)))
            !isPaused -> builder.addAction(R.drawable.ic_pause, context.getString(R.string.pause_recording), pauseRecordingPendingIntent)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(context.getString(R.string.notification_recording_in_progress)))
        }
        return builder.build()
    }

    fun setPaused(isPaused: Boolean): RecordingNotification {
        this.isPaused = isPaused
        return this
    }
}