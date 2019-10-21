package com.iboism.gpxrecorder.records.details

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.util.Log
import android.view.ViewTreeObserver
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.JointType.ROUND
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.model.Track
import com.iboism.gpxrecorder.model.Waypoint
import com.iboism.gpxrecorder.util.DateTimeFormatHelper
import io.realm.Realm

/**
 * Created by Brad on 2/26/2018.
 */

class MapController(private val mapView: MapView, private val gpxId: Long): OnMapReadyCallback, ViewTreeObserver.OnGlobalLayoutListener {
    private var isMapReady = false
    private var isLayoutReady = false
    private var isMapSetup = false
    private var map: GoogleMap? = null

    var shouldDrawEnd = true

    init {
        mapView.viewTreeObserver.addOnGlobalLayoutListener(this)
    }

    fun redraw() {
        if (!isMapReady || !isLayoutReady || !isMapSetup) return
        val realm = Realm.getDefaultInstance()
        GpxContent.withId(gpxId, realm)?.let { map?.drawContent(realm.copyFromRealm(it), false) }
        realm.close()
    }

    fun onDestroy() {
        map?.clear()
    }

    fun toggleMapType() {
        map?.let {
            it.mapType = if (it.mapType == GoogleMap.MAP_TYPE_SATELLITE) GoogleMap.MAP_TYPE_TERRAIN else GoogleMap.MAP_TYPE_SATELLITE
        }
    }

    private fun setupMapIfNeeded() {
        if (!isMapReady || !isLayoutReady || isMapSetup) return
        val map = map ?: return
        isMapSetup = true

        MapsInitializer.initialize(mapView.context)
        map.uiSettings.isCompassEnabled = true
        map.uiSettings.isMapToolbarEnabled = true
        map.mapType = GoogleMap.MAP_TYPE_TERRAIN
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (mapView.context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                map.isMyLocationEnabled = true
            }
        } else {
            map.isMyLocationEnabled = true
        }

        val realm = Realm.getDefaultInstance()
        GpxContent.withId(gpxId, realm)?.let { map.drawContent(realm.copyFromRealm(it), true) }
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

    private fun GoogleMap.drawContent(gpx: GpxContent, shouldCenter: Boolean) {
        val trackBounds = this.drawTracks(gpx.trackList.toList())
        this.drawWaypoints(gpx.waypointList.toList())

        if (trackBounds != null && shouldCenter) {
            this.moveCamera(CameraUpdateFactory.newLatLngBounds(trackBounds, 50))
        }
    }

    private fun GoogleMap.drawTracks(tracks: List<Track>): LatLngBounds? {
        var allPoints: MutableList<LatLng> = mutableListOf()
        val boundsBuilder = LatLngBounds.Builder()
        clear()
        // draw track lines
        tracks.forEach { track ->
            val points = track.segments.flatMap { it.getLatLngPoints().blockingGet() }
            allPoints.addAll(points)

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
        Log.i("MapBug", "Drawing ${allPoints.count()} points")
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
        if (shouldDrawEnd) {
            tracks.lastOrNull()?.segments?.lastOrNull()?.points?.lastOrNull()?.let {
                this.addMarker(MarkerOptions().position(LatLng(it.lat, it.lon))
                        .flat(true)
                        .title("End")
                        .snippet(DateTimeFormatHelper.toReadableString(it.time))
                        .icon(getBitmapDescriptor(R.drawable.ic_stop_pt))
                        .anchor(.5f, .5f))
            }
        }

        allPoints.forEach {
            boundsBuilder.include(it)
        }

        return if (allPoints.isNotEmpty()) boundsBuilder.build() else null
    }

    private fun GoogleMap.drawWaypoints(waypoints: List<Waypoint>) {
        waypoints.forEach {
            this.addMarker(MarkerOptions().position(LatLng(it.lat,it.lon))
                    .flat(true)
                    .title(it.title)
                    .snippet(it.desc)
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