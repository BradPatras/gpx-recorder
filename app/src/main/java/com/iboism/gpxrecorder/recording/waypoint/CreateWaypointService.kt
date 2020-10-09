package com.iboism.gpxrecorder.recording.waypoint

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.android.gms.location.LocationResult
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.model.Waypoint
import io.realm.Realm


/**
 * Created by Brad on 12/14/2017.
 */
class CreateWaypointService : BroadcastReceiver()  {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return
        val (gpxId, title, note) = harvestParameters(intent) ?: return

        createWaypoint(LocationResult.extractResult(intent), title, note)?.let { waypoint ->
            val realm = Realm.getDefaultInstance()
            realm.executeTransaction {
                val gpxContent = GpxContent.withId(gpxId, it)
                waypoint.dist = gpxContent?.trackList?.first()?.segments?.first()?.distance?.toDouble() ?: 0.0
                gpxContent?.waypointList?.add(waypoint)
            }
            realm.close()
        }
    }

    private fun createWaypoint(locationResult: LocationResult?, title: String, note: String) : Waypoint? {
        val loc = locationResult?.lastLocation ?: return null

        return Waypoint(
                lat = loc.latitude,
                lon = loc.longitude,
                ele = loc.altitude.takeIf { loc.hasAltitude() },
                title = title,
                desc = note
        )
    }

    companion object {
        private const val gpxIdKey = "kgpxId"
        private const val waypointNoteKey = "kwaypointNote"
        private const val waypointTitleKey = "kwaypointTitle"
        fun startServiceIntent(context: Context, gpxId: Long, title: String, note: String): Intent {
            return Intent(context, CreateWaypointService::class.java)
                    .setData(serializeParameters(gpxId, title, note))
        }

        private fun serializeParameters(gpxId: Long, title: String, note: String): Uri {
            return Uri.Builder().scheme("http")
                    .authority("ugh.com")
                    .appendPath("extra")
                    .appendQueryParameter(gpxIdKey, gpxId.toString())
                    .appendQueryParameter(waypointNoteKey, note)
                    .appendQueryParameter(waypointTitleKey, title)
                    .build()
        }

        private fun harvestParameters(intent: Intent): Triple<Long, String, String>? {
            val gpxId = intent.data?.getQueryParameter(gpxIdKey)?.toLong() ?: return null
            val note = intent.data?.getQueryParameter(waypointNoteKey) ?: return null
            val title = intent.data?.getQueryParameter(waypointTitleKey) ?: return null

            return Triple(gpxId, title, note)
        }
    }
}