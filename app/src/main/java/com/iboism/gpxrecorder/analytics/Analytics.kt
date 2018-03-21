package com.iboism.gpxrecorder.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.iboism.gpxrecorder.model.RecordingConfiguration
import com.iboism.gpxrecorder.model.Waypoint
import com.iboism.gpxrecorder.util.Keys

/**
 * Created by Brad on 3/9/2018.
 */

fun FirebaseAnalytics.recordingStarted(gpxId: Long, configuration: RecordingConfiguration) {
    val bundle = Bundle(configuration.toBundle())
    bundle.putLong(Keys.GpxId, gpxId)
    this.logEvent(Event.START_RECORDING, bundle)
}

fun FirebaseAnalytics.recordingStopped(gpxId: Long) {
    val bundle = Bundle()
    bundle.putLong(Keys.GpxId, gpxId)
    this.logEvent(Event.STOP_RECORDING, bundle)
}

fun FirebaseAnalytics.waypointCreated(gpxId: Long, waypoint: Waypoint) {
    val bundle = Bundle(waypoint.toAnonymizedBundle())
    bundle.putLong(Keys.GpxId, gpxId)
    this.logEvent(Event.WAYPOINT_CREATED, bundle)
}

class Event {
    companion object {
        const val START_RECORDING = "startRecording"
        const val STOP_RECORDING = "stopRecording"
        const val WAYPOINT_CREATED = "waypointCreated"
    }
}