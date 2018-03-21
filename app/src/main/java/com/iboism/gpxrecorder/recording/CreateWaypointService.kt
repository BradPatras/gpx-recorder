package com.iboism.gpxrecorder.recording

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.android.gms.location.LocationResult
import com.google.firebase.analytics.FirebaseAnalytics
import com.iboism.gpxrecorder.analytics.waypointCreated
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.model.Waypoint
import io.realm.Realm


/**
 * Created by Brad on 12/14/2017.
 */
class CreateWaypointService : BroadcastReceiver()  {
    override fun onReceive(context: Context?, intent: Intent?) {
        val intent = intent ?: return
        val (gpxId, title, note) = harvestParameters(intent) ?: return

        createWaypoint(LocationResult.extractResult(intent), title, note)?.let { waypoint ->

            context?.let { FirebaseAnalytics.getInstance(it).waypointCreated(gpxId, waypoint) }

            Realm.getDefaultInstance().executeTransaction {
                it.where(GpxContent::class.java)
                        .equalTo(GpxContent.primaryKey, gpxId)
                        .findFirst()?.waypointList?.add(waypoint)
            }
        }
    }

    private fun createWaypoint(locationResult: LocationResult?, title: String, note: String) : Waypoint? {
        val loc = locationResult?.lastLocation ?: return null

        return Waypoint(
                lat = loc.latitude,
                lon = loc.longitude,
                ele = loc.altitude,
                title = title,
                desc = note
        )
    }

    companion object {
        const val gpxIdKey = "kgpxId"
        const val waypointNoteKey = "kwaypointNote"
        const val waypointTitleKey = "kwaypointTitle"
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
            val gpxId = intent.data.getQueryParameter(gpxIdKey)?.toLong() ?: return null
            val note = intent.data.getQueryParameter(waypointNoteKey) ?: return null
            val title = intent.data.getQueryParameter(waypointTitleKey) ?: return null

            return Triple(gpxId, title, note)
        }
    }
}