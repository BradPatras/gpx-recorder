package com.iboism.gpxrecorder.recording

import android.annotation.SuppressLint
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.model.TrackPoint
import com.iboism.gpxrecorder.model.Waypoint
import com.iboism.gpxrecorder.util.Keys
import com.iboism.gpxrecorder.util.PermissionHelper
import io.realm.Realm

class CreateWaypointDialogActivity : AppCompatActivity() {
    var gpxId: Long? = null

    private val fusedLocation by lazy {
        LocationServices.getFusedLocationProviderClient(this@CreateWaypointDialogActivity)
    }

    private val locationConfiguration by lazy {
        LocationRequest.create()
                .setInterval(10 * 1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_waypoint_dialog)

        gpxId = intent.extras[Keys.GpxId] as? Long

        PermissionHelper.getInstance(this@CreateWaypointDialogActivity)
                .checkLocationPermissions(onAllowed = this::requestLocation)

        // grab views

        // on done click, register for one location update

        // add this code (more or less) in the on Location received method
//            val wpt = Waypoint(lat = location.latitude, lon = location.longitude, ele = location.altitude, desc = message)
//            val trkpt = TrackPoint(lat = location.latitude, lon = location.longitude, ele = location.altitude)
//            val gpx = Realm.getDefaultInstance()
//                    .where(GpxContent::class.java)
//                    .equalTo(GpxContent.Keys.primaryKey,gpxId)
//                    .findFirst()
//
//            gpx?.trackList?.last()?.segments?.last()?.points?.add(trkpt)

    }

    // These methods will be moved to CreateWaypointService
    @SuppressLint("MissingPermission")
    private fun requestLocation() {
        fusedLocation.requestLocationUpdates(locationConfiguration,
                object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult?) {
                        createWaypoint(locationResult?.lastLocation)
                    }
                },
                mainLooper)
    }

    private fun createWaypoint(location: Location?) {
        val loc = location ?: return

        val wpt = Waypoint(
                lat = loc.latitude,
                lon = loc.longitude,
                ele = loc.altitude

        )
    }
}
