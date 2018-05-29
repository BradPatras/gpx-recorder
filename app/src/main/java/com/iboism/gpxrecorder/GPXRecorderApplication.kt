package com.iboism.gpxrecorder

import android.app.Application
import android.content.Intent
import com.iboism.gpxrecorder.recording.LocationRecorderService
import io.realm.Realm
import io.realm.RealmConfiguration
import android.app.NotificationManager
import android.app.NotificationChannel
import android.os.Build
import android.support.annotation.RequiresApi
import com.iboism.gpxrecorder.recording.CHANNEL_ID


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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val name = getString(R.string.channel_name)
        val description = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        channel.description = description
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(channel)
    }
}