package com.iboism.gpxrecorder.recording

import android.annotation.SuppressLint
import android.app.Notification
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.IBinder
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.model.RecordingConfiguration
import com.iboism.gpxrecorder.model.Segment
import com.iboism.gpxrecorder.model.TrackPoint
import com.iboism.gpxrecorder.util.Keys
import io.realm.Realm

/**
 * Created by Brad on 11/19/2017.
 */
class LocationRecorderService : Service() {
    private val serviceBinder = ServiceBinder()
    private var gpxId: Long? = null

    private var notification: Notification? = null

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
            intent?.extras?.containsKey(Keys.StopService) == true -> stopSelf()
            intent?.extras?.containsKey(Keys.PauseService) == true -> pauseRecording()
            intent?.extras?.containsKey(Keys.ResumeService) == true -> resumeRecording()
            intent?.extras?.containsKey(Keys.GpxId) == true -> startRecording(intent)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun pauseRecording() {
        // todo
    }

    private fun resumeRecording() {
       // todo
    }

    @SuppressLint("MissingPermission")
    private fun startRecording(intent: Intent?) {
        val gpxId = intent?.extras?.get(Keys.GpxId) as? Long ?: return
        this.gpxId = gpxId
        val notification = RecordingNotification(applicationContext).forGpxId(gpxId)
        this.notification = notification

        startForeground(FOREGROUND_SERVICE_KEY, notification)

        val config = intent.extras?.getBundle(RecordingConfiguration.configKey)?.let {
            return@let RecordingConfiguration.fromBundle(it)
        } ?: RecordingConfiguration()

        fusedLocation.requestLocationUpdates(config.locationRequest(),
                locationCallback,
                mainLooper)
    }

    private fun onLocationChanged(location: Location?) {
        location?.let { loc ->
            if (loc.accuracy > 20) return

            Realm.getDefaultInstance().executeTransaction {
                val trkpt = TrackPoint(lat = loc.latitude, lon = loc.longitude, ele = loc.altitude)
                val gpx = Realm.getDefaultInstance()
                        .where(GpxContent::class.java)
                        .equalTo(GpxContent.Keys.primaryKey,gpxId)
                        .findFirst()

                gpx?.trackList?.last()?.segments?.last()?.addPoint(trkpt)
            }
        }
    }

    companion object {
        const val FOREGROUND_SERVICE_KEY = 98072347
    }

    inner class ServiceBinder : Binder() {
        internal val service: LocationRecorderService
            get() = this@LocationRecorderService
    }

}

