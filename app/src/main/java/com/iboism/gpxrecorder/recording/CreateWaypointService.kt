package com.iboism.gpxrecorder.recording

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Created by Brad on 12/14/2017.
 */
class CreateWaypointService(
        gpxId: Long,
        waypointMessage: String
) : Service()  {

    override fun onBind(intent: Intent?): IBinder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}