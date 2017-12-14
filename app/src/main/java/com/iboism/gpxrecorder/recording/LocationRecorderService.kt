package com.iboism.gpxrecorder.recording

import android.annotation.SuppressLint
import android.app.Notification
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.widget.Toast
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.iboism.gpxrecorder.util.Keys
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.model.RecordingConfiguration
import com.iboism.gpxrecorder.model.TrackPoint
import io.realm.Realm
import android.app.PendingIntent
import com.iboism.gpxrecorder.primary.MainActivity

/**
 * Created by Brad on 11/19/2017.
 */
class LocationRecorderService: Service() {
    val serviceBinder = ServiceBinder()
    private var gpxId : Long? = null

    private val fusedLocation by lazy {
        LocationServices.getFusedLocationProviderClient(this@LocationRecorderService)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
    }

    override fun onBind(intent: Intent?): IBinder {
        return serviceBinder
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val gpxId = intent?.extras?.get(Keys.GpxId) as? Long ?: return super.onStartCommand(intent, flags, startId)
        this.gpxId = gpxId

        startForeground(FOREGROUND_SERVICE_KEY, RecordingNotification(applicationContext).forGpxId(gpxId))

        val config = intent.extras?.getBundle(RecordingConfiguration.configKey)?.let {
            return@let RecordingConfiguration.fromBundle(it)
        } ?: RecordingConfiguration()

        fusedLocation.requestLocationUpdates(config.locationRequest(),
                object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult?) {
                        onLocationChanged(locationResult?.getLastLocation());
                    }
                },
                mainLooper)

        return super.onStartCommand(intent, flags, startId)
    }

    fun onLocationChanged(location: Location?) {
        location?.let {
            if (location.accuracy > 20) return

            Realm.getDefaultInstance().executeTransaction {
                val trkpt = TrackPoint(lat = location.latitude, lon = location.longitude, ele = location.altitude)
                val gpx = Realm.getDefaultInstance()
                        .where(GpxContent::class.java)
                        .equalTo(GpxContent.Keys.primaryKey,gpxId)
                        .findFirst()

                gpx?.trackList?.last()?.segments?.last()?.points?.add(trkpt)
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

