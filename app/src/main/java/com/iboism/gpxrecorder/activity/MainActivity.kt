package com.iboism.gpxrecorder.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.location.LocationManager
import android.location.LocationProvider
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationSettingsRequest
import com.iboism.gpxrecorder.BuildConfig
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.model.Segment
import com.iboism.gpxrecorder.model.Track
import com.iboism.gpxrecorder.model.TrackPoint
import com.iboism.gpxrecorder.util.Alerts
import com.iboism.gpxrecorder.util.FileHelper
import com.iboism.gpxrecorder.util.PermissionHelper
import io.realm.RealmList
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import android.app.PendingIntent
import android.support.annotation.RequiresPermission
import com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
import com.google.android.gms.location.LocationServices
import com.iboism.gpxrecorder.Keys
import com.iboism.gpxrecorder.service.LocationRecorderService
import java.util.jar.Manifest


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private val permissionHelper: PermissionHelper by lazy { PermissionHelper.getInstance(this@MainActivity) }
    private val fusedLocation: FusedLocationProviderClient by lazy { LocationServices.getFusedLocationProviderClient(this@MainActivity);}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { _ ->
            permissionHelper.checkLocationPermissions(
                onAllowed = {
                    // open recording creator
                    startRecording()
                })
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        val segment = Segment(points = RealmList(TrackPoint(lat = 59.4408327f, lon = 24.74516185f), TrackPoint(lat = 59.4408330f, lon = 24.74516179f)))
        val track = Track(name = "test track", segments = RealmList(segment))
        val gpxContent = GpxContent(trackList = RealmList(track), title = "are you still there")

        val fileHelper = FileHelper(applicationContext)

        val file = fileHelper.gpxFileWith(gpxContent)
        fileHelper.shareFile(file)

        nav_view.setNavigationItemSelectedListener(this)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    @SuppressLint("MissingPermission")
    fun startRecording() {

        val locationRequest = LocationRequest()
                .setInterval(10000)
                .setSmallestDisplacement(5f)
                .setMaxWaitTime(2000)
                .setPriority(PRIORITY_BALANCED_POWER_ACCURACY)
                .setFastestInterval(5000)
                .setNumUpdates(10)

        val intent = Intent(this@MainActivity, LocationRecorderService::class.java)
        intent.putExtra(Keys.GpxId, GpxContent().identifier)
        val pi = PendingIntent.getService(this@MainActivity, 0, intent, 0)

        fusedLocation.requestLocationUpdates(locationRequest, pi)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
