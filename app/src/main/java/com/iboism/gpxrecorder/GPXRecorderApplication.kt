package com.iboism.gpxrecorder

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.getkeepsafe.relinker.MissingLibraryException
import com.iboism.gpxrecorder.extensions.setRealmInitFailure
import com.iboism.gpxrecorder.model.Schema
import com.iboism.gpxrecorder.recording.CHANNEL_ID
import io.realm.Realm
import io.realm.exceptions.RealmException


/**
 * Created by Brad on 11/18/2017.
 */
class GPXRecorderApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        try {
            initializeRealm()
            applicationContext.setRealmInitFailure(false)
        } catch (e: RealmException) {
            applicationContext.setRealmInitFailure(true)
        } catch (e: MissingLibraryException) {
            applicationContext.setRealmInitFailure(true) //todo
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
    }

    private fun initializeRealm() {
        Realm.init(applicationContext)
        Realm.setDefaultConfiguration(Schema.configuration())
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