package com.iboism.gpxrecorder

import android.app.Application
import android.content.Intent
import com.iboism.gpxrecorder.recording.LocationRecorderService
import io.realm.Realm
import io.realm.RealmConfiguration

/**
 * Created by Brad on 11/18/2017.
 */
class GPXRecorderApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        Realm.init(applicationContext)

        val config = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build()

        Realm.setDefaultConfiguration(config)
        Realm.getDefaultInstance()
    }
}