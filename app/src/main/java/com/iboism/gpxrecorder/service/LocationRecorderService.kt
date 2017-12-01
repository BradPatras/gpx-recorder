package com.iboism.gpxrecorder.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.widget.Toast
import com.iboism.gpxrecorder.Keys

/**
 * Created by Brad on 11/19/2017.
 */
class LocationRecorderService: Service() {
    val serviceBinder = ServiceBinder()
    var gpxId : Long? = null

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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(applicationContext, "Service started", Toast.LENGTH_LONG).show()
        //harvest realm id from intent to post the data to
        gpxId = intent?.extras?.get(Keys.GpxId) as? Long
        return super.onStartCommand(intent, flags, startId)
    }

    /*
        this class will record the current location and then create a new
        trackpoint realm object.  Possibly started via the requestlocationUpdates
        with pendingIntent method
     */

    inner class ServiceBinder : Binder() {
        internal val service: LocationRecorderService
            get() = this@LocationRecorderService
    }

}

