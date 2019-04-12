package com.iboism.gpxrecorder

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.iboism.gpxrecorder.recording.LocationRecorderService

class RecorderServiceConnection(val delegate: OnServiceConnectedDelegate) {
    interface OnServiceConnectedDelegate {
        fun onServiceConnected(service: LocationRecorderService)
        fun onServiceDisconnected()
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as LocationRecorderService.ServiceBinder
            delegate.onServiceConnected(binder.getService())
        }

        override fun onServiceDisconnected(className: ComponentName) {
            delegate.onServiceDisconnected()
        }
    }

    fun requestConnection(context: Context) {
        val intent = Intent(context, LocationRecorderService::class.java)
        context.bindService(intent, serviceConnection, 0)
    }

    fun disconnect(context: Context) {
        context.unbindService(serviceConnection)
    }
}