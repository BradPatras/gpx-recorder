package com.iboism.gpxrecorder.recording

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder

class RecorderServiceConnection(private val delegate: OnServiceConnectedDelegate) {
    interface OnServiceConnectedDelegate {
        fun onServiceConnected(serviceConnection: RecorderServiceConnection)
        fun onServiceDisconnected()
    }

    var service: LocationRecorderService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as LocationRecorderService.ServiceBinder
            this@RecorderServiceConnection.service = binder.getService()
            delegate.onServiceConnected(this@RecorderServiceConnection)
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