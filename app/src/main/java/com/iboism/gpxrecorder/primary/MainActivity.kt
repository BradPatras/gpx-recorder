package com.iboism.gpxrecorder.primary

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.constraint.ConstraintSet
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.transition.TransitionManager
import android.view.MenuItem
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.model.RecordingConfiguration
import com.iboism.gpxrecorder.model.Segment
import com.iboism.gpxrecorder.model.Track
import com.iboism.gpxrecorder.recording.LocationRecorderService
import com.iboism.gpxrecorder.recording.RecordingConfiguratorModal
import com.iboism.gpxrecorder.util.Keys
import com.iboism.gpxrecorder.util.PermissionHelper
import io.realm.Realm
import io.realm.RealmList
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, RecordingConfiguratorModal.Listener {

    private val permissionHelper: PermissionHelper by lazy { PermissionHelper.getInstance(this@MainActivity) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { _ ->
            permissionHelper.checkLocationPermissions(
                    onAllowed = {
                        setConfigModalHidden(false)
                    })
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        // uncomment to create random 10,000 point track and add it to realm
//        val seg = Segment()
//        for (i in 0..10000) {
//            seg.addPoint(TrackPoint(lat = Math.random(), lon = Math.random()))
//        }
//
//        val trk = Track(segments = RealmList(seg))
//        val gpx = GpxContent(trackList = RealmList(trk))
//
//        Realm.getDefaultInstance().executeTransaction {
//            it.copyToRealm(gpx)
//        }
    }

    private fun setConfigModalHidden(hide: Boolean) {
        val showing = ConstraintSet()
        val hidden = ConstraintSet()

        showing.clone(layout_app_bar_main)
        hidden.clone(showing)

        showing.clear(R.id.fragment_recording_config, ConstraintSet.TOP)
        showing.connect(R.id.fragment_recording_config, ConstraintSet.BOTTOM, R.id.layout_app_bar_main, ConstraintSet.BOTTOM)

        hidden.clear(R.id.fragment_recording_config, ConstraintSet.BOTTOM)
        hidden.connect(R.id.fragment_recording_config, ConstraintSet.TOP, R.id.fab, ConstraintSet.TOP)

        (if (hide) hidden else showing).applyTo(layout_app_bar_main)
        TransitionManager.beginDelayedTransition(layout_app_bar_main)
    }

    override fun configurationCreated(configuration: RecordingConfiguration) {
        setConfigModalHidden(true)
        startRecording(configuration)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startRecording(configuration: RecordingConfiguration) {

        val newGpx = GpxContent(title = configuration.title, trackList = RealmList(Track(segments = RealmList(Segment()))))
        Realm.getDefaultInstance().executeTransaction {
            Realm.getDefaultInstance().copyToRealm(newGpx)
        }

        val intent = Intent(this@MainActivity, LocationRecorderService::class.java)
        intent.putExtra(Keys.GpxId, newGpx.identifier)
        intent.putExtra(RecordingConfiguration.configKey, configuration.toBundle())

        startService(intent)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_delete_recordings -> Realm.getDefaultInstance().executeTransaction {
                it.delete(GpxContent::class.java)
                stopService(Intent(this@MainActivity, LocationRecorderService::class.java))
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
