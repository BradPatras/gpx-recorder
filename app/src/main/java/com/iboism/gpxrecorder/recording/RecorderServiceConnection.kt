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
            service = null
        }
    }

    fun requestConnection(context: Context) {
        if (service != null) {
            delegate.onServiceConnected(this)
        } else {
            val intent = Intent(context, LocationRecorderService::class.java)
            context.bindService(intent, serviceConnection, 0)
        }
    }

    fun disconnect(context: Context) {
        try {
            context.unbindService(serviceConnection)
            delegate.onServiceDisconnected()
            service = null
        } catch (e: IllegalArgumentException) {
            // no op
            // The api gives no way of checking whether or not a service is bound and will throw
            // this exception if you try to unbind to a service that is not bound.
            // This is a workaround for that weird behavior.
        }
    }
}