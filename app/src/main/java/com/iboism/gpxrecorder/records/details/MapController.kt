package com.iboism.gpxrecorder.records.details

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
import android.view.ViewTreeObserver
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLngBounds
import io.realm.Realm

/**
 * Created by Brad on 2/26/2018.
 */

class MapController
(private val mapView: MapView, private val gpxId: Long): OnMapReadyCallback, ViewTreeObserver.OnGlobalLayoutListener {
    private var isMapReady = false
    private var isLayoutReady = false
    private var isMapSetup = false
    private var map: GoogleMap? = null

    init {
        mapView.viewTreeObserver.addOnGlobalLayoutListener(this)
    }

    fun onDestroy() {
        map?.clear()
    }

    private fun setupMapIfNeeded() {
        if (!isMapReady || !isLayoutReady || isMapSetup) return
        val map = map ?: return
        isMapSetup = true

        MapsInitializer.initialize(mapView.context)
        map.uiSettings.isMyLocationButtonEnabled = false
        map.uiSettings.isCompassEnabled = true
        map.mapType = GoogleMap.MAP_TYPE_TERRAIN
        val realm = Realm.getDefaultInstance()
        GpxContent.withId(gpxId, realm)?.let { map.drawContent(it) }
        realm.close()
    }

    override fun onGlobalLayout() {
        isLayoutReady = true
        mapView.viewTreeObserver.removeOnGlobalLayoutListener(this)
        setupMapIfNeeded()
    }

    override fun onMapReady(map: GoogleMap?) {
        isMapReady = true
        this.map = map
        setupMapIfNeeded()
    }

    private fun GoogleMap.drawContent(gpx: GpxContent) {
        val trackBounds = this.drawTracks(gpx.trackList.toList())
        this.drawWaypoints(gpx.waypointList.toList())

        if (trackBounds != null) {
            this.moveCamera(CameraUpdateFactory.newLatLngBounds(trackBounds, 50))
        }
    }

    private fun GoogleMap.drawTracks(tracks: List<Track>): LatLngBounds? {
        var allPoints: List<LatLng> = emptyList()
        val boundsBuilder = LatLngBounds.Builder()

        // draw track lines
        tracks.forEach { track ->
            val points = track.segments.flatMap { it.getLatLngPoints().blockingGet() }
            allPoints += points
            this.addPolyline(
                    PolylineOptions()
                            .color(ContextCompat.getColor(mapView.context, R.color.white))
                            .jointType(ROUND)
                            .width(16f)
                            .addAll(points))

            this.addPolyline(
                    PolylineOptions()
                            .color(ContextCompat.getColor(mapView.context, R.color.gLightBlue))
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

        return if (allPoints.isNotEmpty()) boundsBuilder.build() else null
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
        val vectorDrawable = ResourcesCompat.getDrawable(mapView.context.resources, id, null)
        val bitmap = Bitmap.createBitmap(vectorDrawable!!.intrinsicWidth,
                vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)
        val descriptor = BitmapDescriptorFactory.fromBitmap(bitmap)
        bitmap.recycle()
        return descriptor
    }
}