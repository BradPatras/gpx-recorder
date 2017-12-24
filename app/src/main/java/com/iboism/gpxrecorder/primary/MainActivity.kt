package com.iboism.gpxrecorder.primary

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.iboism.gpxrecorder.util.Keys
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.*
import com.iboism.gpxrecorder.recording.LocationRecorderService
import com.iboism.gpxrecorder.recording.RecordingConfiguratorModal
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
                        RecordingConfiguratorModal.instance().show(supportFragmentManager, "dialog")
                    })
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.content_container, GpxList.newInstance())
                    .disallowAddToBackStack()
                    .commit()
        }
    }

    override fun configurationCreated(configuration: RecordingConfiguration) = startRecording(configuration)

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
