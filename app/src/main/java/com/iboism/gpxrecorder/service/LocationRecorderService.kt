package com.iboism.gpxrecorder.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.os.Vibrator
import android.support.v4.app.NotificationCompat
import android.widget.Toast
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.iboism.gpxrecorder.Keys
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.model.RecordingConfiguration
import com.iboism.gpxrecorder.model.TrackPoint
import io.realm.Realm

/**
 * Created by Brad on 11/19/2017.
 */
class LocationRecorderService: Service() {
    val serviceBinder = ServiceBinder()
    private var gpxId : Long? = null

    private val fusedLocation by lazy {
        LocationServices.getFusedLocationProviderClient(this@LocationRecorderService);
    }

    private val notification by lazy {
        NotificationCompat.Builder(this, Notification.CATEGORY_SERVICE)
            .setContentTitle("GPX Recorder")
            .setContentText("Location recording in progress")
            .setSmallIcon(R.mipmap.ic_launcher6)
            .setTicker("what is this")
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        Toast.makeText(applicationContext, "Service created", Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
        Toast.makeText(applicationContext, "Service destroyed", Toast.LENGTH_LONG).show()
    }

    override fun onBind(intent: Intent?): IBinder {
        Toast.makeText(applicationContext, "Service bound", Toast.LENGTH_LONG).show()

        return serviceBinder
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(applicationContext, "Service started", Toast.LENGTH_LONG).show()
        //harvest realm id from intent to post the data to

        startForeground(FOREGROUND_SERVICE_KEY, notification)

        gpxId = intent?.extras?.get(Keys.GpxId) as? Long

        fusedLocation.requestLocationUpdates(RecordingConfiguration().locationRequest(),
                object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult?) {
                        // do work here
                        onLocationChanged(locationResult?.getLastLocation());
                    }
                },
                mainLooper)

        return super.onStartCommand(intent, flags, startId)
    }

    fun onLocationChanged(location: Location?) {
        location?.let {
            if (location.accuracy > 40) return // disregard inaccurate locations

            Realm.getDefaultInstance().executeTransaction {
                val trkpt = TrackPoint(lat = location.latitude, lon = location.longitude, ele = location.altitude)
                val gpx = Realm.getDefaultInstance().where(GpxContent::class.java).equalTo(GpxContent.Keys.primaryKey,gpxId).findFirst()
                gpx?.trackList?.last()?.segments?.last()?.points?.add(trkpt)
            }
            (getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(500)
            Toast.makeText(applicationContext, "Track point recorded", Toast.LENGTH_LONG).show()
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

