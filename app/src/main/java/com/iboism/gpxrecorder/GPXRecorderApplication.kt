package com.iboism.gpxrecorder

import android.app.Application
import io.realm.Realm

/**
 * Created by Brad on 11/18/2017.
 */
class GPXRecorderApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        Realm.init(applicationContext)
    }
}