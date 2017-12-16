package com.iboism.gpxrecorder.recording

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.util.Alerts
import com.iboism.gpxrecorder.util.Keys
import com.iboism.gpxrecorder.util.PermissionHelper
import kotlinx.android.synthetic.main.activity_create_waypoint_dialog.*

class CreateWaypointDialogActivity : AppCompatActivity() {

    private val fusedLocation by lazy {
        LocationServices.getFusedLocationProviderClient(this@CreateWaypointDialogActivity)
    }

    private val locationConfiguration by lazy {
        LocationRequest.create()
                .setInterval(5 * 1000) // 5 seconds
                .setMaxWaitTime(20 * 1000) // 20 seconds
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setNumUpdates(1)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_waypoint_dialog)

        val gpxId = checkNotNull(intent.extras[Keys.GpxId] as? Long) { waypointError() }

        done_button.setOnClickListener {
            startWaypointService(gpxId, note_editText.text.toString())
        }
    }

    private fun waypointError() {
        Alerts(applicationContext)
                .genericError(R.string.cannot_create_waypoint) { _ -> finish() }
                .show()
    }

    @SuppressLint("MissingPermission")
    private fun startWaypointService(gpxId: Long, note: String) {
        val waypointIntent = CreateWaypointService.startServiceIntent(applicationContext, gpxId, note)
        val waypointPendingIntent = PendingIntent.getBroadcast(applicationContext, 0, waypointIntent, 0)
        PermissionHelper.getInstance(this@CreateWaypointDialogActivity)
                .checkLocationPermissions {
                    fusedLocation.requestLocationUpdates(locationConfiguration, waypointPendingIntent)
                    this@CreateWaypointDialogActivity.finish()
                }
    }
}
