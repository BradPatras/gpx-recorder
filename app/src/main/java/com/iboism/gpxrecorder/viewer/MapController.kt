package com.iboism.gpxrecorder.viewer

import android.content.Context
import android.graphics.Color.MAGENTA
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.JointType.ROUND
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.model.Track
import com.iboism.gpxrecorder.model.Waypoint
import com.iboism.gpxrecorder.util.DateTimeFormatHelper
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import android.graphics.Bitmap
import android.graphics.Canvas
import android.support.v4.content.res.ResourcesCompat
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLngBounds


/**
 * Created by Brad on 2/26/2018.
 */

class MapController(private val context: Context, private val gpxId: Long): OnMapReadyCallback {

    override fun onMapReady(map: GoogleMap?) {
        if (map == null) return // take no action if a map was not created

        MapsInitializer.initialize(context)
        map.uiSettings.isMyLocationButtonEnabled = false
        map.uiSettings.isCompassEnabled = true
        map.mapType = GoogleMap.MAP_TYPE_TERRAIN
        GpxContent.withId(gpxId)?.let { map.drawContent(it) }
    }

    private fun GoogleMap.drawContent(gpx: GpxContent) {
        val trackBounds = this.drawTracks(gpx.trackList.toList())
        this.drawWaypoints(gpx.waypointList.toList())
        this.moveCamera(CameraUpdateFactory.newLatLngBounds(trackBounds, 50))
    }

    private fun GoogleMap.drawTracks(tracks: List<Track>): LatLngBounds {
        var allPoints: List<LatLng> = emptyList()
        val boundsBuilder = LatLngBounds.Builder()

        // draw track lines
        tracks.forEach {
            val points = it.segments.flatMap { it.getLatLngPoints().blockingGet() }
            allPoints += points
            this.addPolyline(
                    PolylineOptions()
                            .color(ContextCompat.getColor(context, R.color.white))
                            .jointType(ROUND)
                            .width(16f)
                            .addAll(points))

            this.addPolyline(
                    PolylineOptions()
                            .color(ContextCompat.getColor(context, R.color.gLightBlue))
                            .jointType(ROUND)
                            .width(12f)
                            .addAll(points))
            }

        // draw marker at start
        tracks.firstOrNull()?.segments?.firstOrNull()?.points?.firstOrNull()?.let {
            this.addMarker(MarkerOptions().position(LatLng(it.lat, it.lon))
                    .flat(true)
                    .title("Start")
                    .snippet(DateTimeFormatHelper.toReadableString(it.time))
                    .icon(getBitmapDescriptor(R.drawable.ic_start_pt))
                    .anchor(.5f, .5f))
        }
        // draw marker at end
        tracks.lastOrNull()?.segments?.lastOrNull()?.points?.lastOrNull()?.let {
            this.addMarker(MarkerOptions().position(LatLng(it.lat, it.lon))
                    .flat(true)
                    .title("End")
                    .snippet(DateTimeFormatHelper.toReadableString(it.time))
                    .icon(getBitmapDescriptor(R.drawable.ic_stop_pt))
                    .anchor(.5f, .5f))
        }

        allPoints.forEach {
            boundsBuilder.include(it)
        }

        return boundsBuilder.build()
    }

    private fun GoogleMap.drawWaypoints(waypoints: List<Waypoint>) {
        waypoints.forEach {
            val dist = if (it.dist > 0.0) "@%.2fkm".format(it.dist) else ""
            val snippet = if (it.desc.isBlank()) dist else "$dist: ${it.desc}"
            this.addMarker(MarkerOptions().position(LatLng(it.lat,it.lon))
                    .flat(true)
                    .title(it.title)
                    .snippet(snippet)
                    .icon(getBitmapDescriptor(R.drawable.ic_waypoint_pt))
                    .anchor(.5f, .5f))
        }
    }

    private fun getBitmapDescriptor(@DrawableRes id: Int): BitmapDescriptor {
        val vectorDrawable = ResourcesCompat.getDrawable(context.resources, id, null)
        val bitmap = Bitmap.createBitmap(vectorDrawable!!.intrinsicWidth,
                vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}