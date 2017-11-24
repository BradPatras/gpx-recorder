package com.iboism.gpxrecorder.activity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.*
import com.iboism.gpxrecorder.util.FileHelper
import com.karumi.dexter.Dexter
import com.karumi.dexter.DexterBuilder
import io.realm.RealmList
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener
import com.karumi.dexter.listener.single.PermissionListener
import android.support.design.widget.Snackbar
import android.view.View
import com.iboism.gpxrecorder.util.PermissionHelper
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, PermissionListener {
    val permissionHelper: PermissionHelper by lazy { PermissionHelper.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            permissionHelper.checkLocationPermissions(
                    onAllowed = { startActivity(Intent(applicationContext, RecordingConfigurationActivity::class.java)) },
                    onDenied = { /* do nothing for now*/  })
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        // Begin tests
        val segment = Segment(points = RealmList(TrackPoint(lat = 59.4408327f, lon = 24.74516185f), TrackPoint(lat = 59.4408330f, lon = 24.74516179f)))
        val track = Track(name = "test track", segments = RealmList(segment))
        val gpxContent = GpxContent(trackList = RealmList(track), title = "are you still there")

        val fileHelper = FileHelper(applicationContext)
        val file = fileHelper.gpxFileWith(gpxContent)
        fileHelper.shareFile(file)

        // End tests

        nav_view.setNavigationItemSelectedListener(this)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
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
            R.id.nav_camera -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onPermissionGranted(response: PermissionGrantedResponse?) {

    }

    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {
        token?.continuePermissionRequest()
    }

    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
