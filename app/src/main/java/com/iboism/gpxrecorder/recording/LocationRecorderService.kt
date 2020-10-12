package com.iboism.gpxrecorder.recording

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.iboism.gpxrecorder.Events
import com.iboism.gpxrecorder.Keys
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.model.RecordingConfiguration
import com.iboism.gpxrecorder.model.TrackPoint
import io.realm.Realm
import org.greenrobot.eventbus.EventBus

/**
 * Created by Brad on 11/19/2017.
 */
class LocationRecorderService : Service() {
    var gpxId: Long? = null
    var isPaused: Boolean = false

    private val serviceBinder = ServiceBinder()
    private var config = RecordingConfiguration()
    private var notificationHelper: RecordingNotification? = null

    private val notificationManager: NotificationManager
        get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val fusedLocation by lazy {
        LocationServices.getFusedLocationProviderClient(this@LocationRecorderService)
    }

    private val locationCallback by lazy {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                onLocationChanged(locationResult?.lastLocation)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocation.removeLocationUpdates(locationCallback)
        Realm.getDefaultConfiguration()?.let{ Realm.compactRealm(it) }
        stopForeground(true)
    }

    override fun onBind(intent: Intent?): IBinder {
        return serviceBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when {
            intent?.extras?.containsKey(Keys.StopService) == true -> stopRecording()
            intent?.extras?.containsKey(Keys.PauseService) == true -> pauseRecording()
            intent?.extras?.containsKey(Keys.ResumeService) == true -> resumeRecording()
            intent?.extras?.containsKey(Keys.StartService) == true -> startRecording(intent)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun stopRecording() {
        EventBus.getDefault().apply {
            removeStickyEvent(Events.RecordingStartedEvent::class.java)
            post(Events.RecordingStoppedEvent(gpxId))
        }
        stopSelf()
    }

    fun pauseRecording() {
        val notificationHelper = notificationHelper ?: return
        val gpxId = gpxId ?: return
        isPaused = true
        EventBus.getDefault().post(Events.RecordingPausedEvent(gpxId))
        notificationManager.notify(gpxId.toInt(), notificationHelper.setPaused(true).notification())
        fusedLocation.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission")
    fun resumeRecording() {
        val notificationHelper = notificationHelper ?: return
        val gpxId = gpxId ?: return
        isPaused = false
        EventBus.getDefault().post(Events.RecordingResumedEvent(gpxId))
        notificationManager.notify(gpxId.toInt(), notificationHelper.setPaused(false).notification())
        fusedLocation.requestLocationUpdates(config.locationRequest(),
                locationCallback,
                mainLooper)
    }

    @SuppressLint("MissingPermission")
    private fun startRecording(intent: Intent?) {
        val gpxId = intent?.extras?.get(Keys.GpxId) as? Long ?: return
        val notificationHelper = RecordingNotification(applicationContext, gpxId)
        val notification = notificationHelper.notification()
        isPaused = false

        EventBus.getDefault().postSticky(Events.RecordingStartedEvent(gpxId))
        this.gpxId = gpxId
        this.notificationHelper = notificationHelper
        startForeground(gpxId.toInt(), notification)

        config = intent.extras?.getBundle(RecordingConfiguration.configKey)?.let {
            return@let RecordingConfiguration.fromBundle(it)
        } ?: RecordingConfiguration()

        fusedLocation.requestLocationUpdates(config.locationRequest(),
                locationCallback,
                mainLooper)
    }

    private fun onLocationChanged(location: Location?) {
        location?.let { loc ->
            if (loc.accuracy > 40) return
            val realm = Realm.getDefaultInstance()
            realm.executeTransaction { r ->
                val point = TrackPoint(lat = loc.latitude, lon = loc.longitude, ele = loc.altitude)
                r.copyToRealm(point)
                GpxContent.withId(gpxId, r)?.let { gpx ->
                    gpx.trackList.last()?.segments?.last()?.addPoint(point)
                }
            }
            realm.close()
        }
    }

    inner class ServiceBinder : Binder() {
        fun getService() = this@LocationRecorderService
    }

    companion object {
        fun createStopRecordingIntent(context: Context): Intent {
            return Intent(context, LocationRecorderService::class.java).putExtra(Keys.StopService, true)
        }

        fun createPauseRecordingIntent(context: Context): Intent {
            return Intent(context, LocationRecorderService::class.java).putExtra(Keys.PauseService, true)
        }

        fun createResumeRecordingIntent(context: Context): Intent {
            return Intent(context, LocationRecorderService::class.java).putExtra(Keys.ResumeService, true)
        }

        fun requestStopRecording(context: Context) {
            context.startService(createStopRecordingIntent(context))
        }

        fun requestPauseRecording(context: Context) {
            context.startService(createPauseRecordingIntent(context))
        }

        fun requestResumeRecording(context: Context) {
            context.startService(createResumeRecordingIntent(context))
        }

        fun requestStartRecording(context: Context, gpxId: Long, configBundle: Bundle) {
            val intent = Intent(context, LocationRecorderService::class.java)
            intent.putExtra(Keys.StartService, true)
            intent.putExtra(Keys.GpxId, gpxId)
            intent.putExtra(RecordingConfiguration.configKey, configBundle)
            context.startService(intent)
        }
    }
}

