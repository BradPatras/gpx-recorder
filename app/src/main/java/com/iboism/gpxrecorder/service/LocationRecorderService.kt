package com.iboism.gpxrecorder.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.v4.app.NotificationCompat
import android.widget.Toast
import com.google.android.gms.location.*
import com.iboism.gpxrecorder.Keys
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.model.Segment
import com.iboism.gpxrecorder.model.Track
import com.iboism.gpxrecorder.model.TrackPoint
import io.realm.Realm
import io.realm.RealmList

/**
 * Created by Brad on 11/19/2017.
 */
class LocationRecorderService: Service() {
    val serviceBinder = ServiceBinder()
    var gpxId : Long? = null
    private val fusedLocation: FusedLocationProviderClient by lazy { LocationServices.getFusedLocationProviderClient(this@LocationRecorderService);}


    override fun onCreate() {
        super.onCreate()
        Toast.makeText(applicationContext, "Service created", Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
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

        val notification = NotificationCompat.Builder(this, Notification.CATEGORY_SERVICE)
                .setContentTitle("GPX Recorder")
                .setContentText("Location recording in progress")
                .setSmallIcon(R.mipmap.ic_launcher6)
                .setTicker("what is this")
                .build()
        startForeground(FOREGROUND_SERVICE_KEY, notification)

        gpxId = intent?.extras?.get(Keys.GpxId) as? Long

        val locationRequest = LocationRequest()
                .setInterval(10000)
                .setSmallestDisplacement(5f)
                .setMaxWaitTime(2000)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setFastestInterval(5000)
                .setNumUpdates(10)

        fusedLocation.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                // do work here
                onLocationChanged(locationResult?.getLastLocation());
            }
        },
                mainLooper)

        return super.onStartCommand(intent, flags, startId)
    }

    fun onLocationChanged(location: Location?) {
        // New location has now been determined
        location?.let {
            val msg = "Updated Location: " +
                    java.lang.Double.toString(it.getLatitude()) + "," +
                    java.lang.Double.toString(it.getLongitude())
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            // You can now create a LatLng Object for use with maps

            Realm.getDefaultInstance().executeTransaction {
                val trkpt = TrackPoint(lat = location.latitude, lon = location.longitude, ele = location.altitude)
                val gpx = Realm.getDefaultInstance().where(GpxContent::class.java).equalTo("identifier",gpxId).findFirst()
                gpx?.trackList?.last()?.segments?.last()?.points?.add(trkpt)
            }
            (getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(500)
            Toast.makeText(applicationContext, "Track point recorded", Toast.LENGTH_LONG).show()
        }
    }

    /*
        this class will record the current location and then create a new
        trackpoint realm object.  Possibly started via the requestlocationUpdates
        with pendingIntent method
     */

    companion object {
        const val FOREGROUND_SERVICE_KEY = 98072347
    }

    inner class ServiceBinder : Binder() {
        internal val service: LocationRecorderService
            get() = this@LocationRecorderService
    }

}

