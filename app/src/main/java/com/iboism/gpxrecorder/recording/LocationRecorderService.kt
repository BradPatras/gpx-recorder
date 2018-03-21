package com.iboism.gpxrecorder.recording

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.IBinder
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.analytics.FirebaseAnalytics
import com.iboism.gpxrecorder.analytics.recordingStarted
import com.iboism.gpxrecorder.analytics.recordingStopped
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.model.RecordingConfiguration
import com.iboism.gpxrecorder.model.TrackPoint
import com.iboism.gpxrecorder.util.Keys
import io.realm.Realm

/**
 * Created by Brad on 11/19/2017.
 */
class LocationRecorderService : Service() {
    private val analytics by lazy { FirebaseAnalytics.getInstance(applicationContext) }
    private val serviceBinder = ServiceBinder()
    private var gpxId: Long? = null
    private var config = RecordingConfiguration()

    private var notificationHelper: RecordingNotification? = null

    private val Context.notificationManager: NotificationManager
        get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val fusedLocation by lazy {
        LocationServices.getFusedLocationProviderClient(this@LocationRecorderService)
    }

    private val locationCallback by lazy {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                onLocationChanged(locationResult?.lastLocation)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocation.removeLocationUpdates(locationCallback)
        stopForeground(true)
    }

    override fun onBind(intent: Intent?): IBinder {
        return serviceBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when {
            intent?.extras?.containsKey(Keys.StopService) == true -> stopRecording()
            intent?.extras?.containsKey(Keys.PauseService) == true -> pauseRecording()
            intent?.extras?.containsKey(Keys.ResumeService) == true -> resumeRecording()
            intent?.extras?.containsKey(Keys.GpxId) == true -> startRecording(intent)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun stopRecording() {
        gpxId?.let { analytics.recordingStopped(it) }
        stopSelf()
    }

    private fun pauseRecording() {
        val notificationHelper = notificationHelper ?: return
        val gpxId = gpxId ?: return

        notificationManager.notify(gpxId.toInt(), notificationHelper.setPaused(true).notification())
        fusedLocation.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission")
    private fun resumeRecording() {
        val notificationHelper = notificationHelper ?: return
        val gpxId = gpxId ?: return

        notificationManager.notify(gpxId.toInt(), notificationHelper.setPaused(false).notification())
        fusedLocation.requestLocationUpdates(config.locationRequest(),
                locationCallback,
                mainLooper)
    }

    @SuppressLint("MissingPermission")
    private fun startRecording(intent: Intent?) {
        val gpxId = intent?.extras?.get(Keys.GpxId) as? Long ?: return
        val notificationHelper = RecordingNotification(applicationContext, gpxId)
        val notification = notificationHelper.notification()

        this.gpxId = gpxId
        this.notificationHelper = notificationHelper

        startForeground(gpxId.toInt(), notification)

        config = intent.extras?.getBundle(RecordingConfiguration.configKey)?.let {
            return@let RecordingConfiguration.fromBundle(it)
        } ?: RecordingConfiguration()

        analytics.recordingStarted(gpxId, config)

        fusedLocation.requestLocationUpdates(config.locationRequest(),
                locationCallback,
                mainLooper)
    }

    private fun onLocationChanged(location: Location?) {
        location?.let { loc ->
            if (loc.accuracy > 20) return

            Realm.getDefaultInstance().executeTransaction {
                val trkpt = TrackPoint(lat = loc.latitude, lon = loc.longitude, ele = loc.altitude)
                val gpx = GpxContent.withId(gpxId)
                gpx?.trackList?.last()?.segments?.last()?.addPoint(trkpt)
            }
        }
    }

    inner class ServiceBinder : Binder() {
        internal val service: LocationRecorderService
            get() = this@LocationRecorderService
    }

}

