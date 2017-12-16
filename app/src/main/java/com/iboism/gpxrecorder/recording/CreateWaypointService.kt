package com.iboism.gpxrecorder.recording

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
        val intent = intent ?: return
        val (gpxId, note) = harvestParameters(intent) ?: return

        createWaypoint(LocationResult.extractResult(intent), note)?.let { waypoint ->
            Realm.getDefaultInstance().executeTransaction {
                it.where(GpxContent::class.java)
                        .equalTo(GpxContent.primaryKey, gpxId)
                        .findFirst()?.waypointList?.add(waypoint)
            }
        }
    }

    private fun createWaypoint(locationResult: LocationResult?, note: String) : Waypoint? {
        val loc = locationResult?.lastLocation ?: return null

        return Waypoint(
                lat = loc.latitude,
                lon = loc.longitude,
                ele = loc.altitude,
                desc = note
        )
    }

    companion object {
        const val gpxIdKey = "kgpxId"
        const val waypointNoteKey = "kwaypointNote"
        fun startServiceIntent(context: Context, gpxId: Long, note: String): Intent {
            return Intent(context, CreateWaypointService::class.java)
                    .setData(serializeParameters(gpxId, note))

        }

        private fun serializeParameters(gpxId: Long, note: String): Uri {
            return Uri.Builder().scheme("http")
                    .authority("ugh.com")
                    .appendPath("extra")
                    .appendQueryParameter(gpxIdKey, gpxId.toString())
                    .appendQueryParameter(waypointNoteKey, note)
                    .build()
        }

        private fun harvestParameters(intent: Intent): Pair<Long, String>? {
            val gpxId = intent.data.getQueryParameter(gpxIdKey)?.toLong() ?: return null
            val note = intent.data.getQueryParameter(waypointNoteKey) ?: return null

            return Pair(gpxId, note)
        }
    }
}