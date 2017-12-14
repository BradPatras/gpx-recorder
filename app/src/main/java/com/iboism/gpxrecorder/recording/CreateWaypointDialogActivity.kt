package com.iboism.gpxrecorder.recording

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.model.TrackPoint
import com.iboism.gpxrecorder.model.Waypoint
import com.iboism.gpxrecorder.util.Keys
import io.realm.Realm

class CreateWaypointDialogActivity : AppCompatActivity() {
    var gpxId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_waypoint_dialog)

        gpxId = intent.extras[Keys.GpxId] as? Long

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
}
