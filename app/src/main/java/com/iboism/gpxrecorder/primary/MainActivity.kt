package com.iboism.gpxrecorder.primary

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.iboism.gpxrecorder.util.Keys
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.model.RecordingConfiguration
import com.iboism.gpxrecorder.model.Segment
import com.iboism.gpxrecorder.model.Track
import com.iboism.gpxrecorder.service.LocationRecorderService
import com.iboism.gpxrecorder.util.PermissionHelper
import io.realm.Realm
import io.realm.RealmList
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private val permissionHelper: PermissionHelper by lazy { PermissionHelper.getInstance(this@MainActivity) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { _ ->
            permissionHelper.checkLocationPermissions(
                onAllowed = {
                    RecordingConfiguratorDialog.instance(this@MainActivity::startRecording)
                            .show(fragmentManager,"fric")
                })
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        //uncomment to share sample gpx
//        val segment = Segment(points = RealmList(TrackPoint(lat = 59.4408327, lon = 24.74516185), TrackPoint(lat = 59.4408330, lon = 24.74516179)))
//        val track = Track(name = "test track", segments = RealmList(segment))
//        val gpxContent = GpxContent(trackList = RealmList(track), title = "are you still there")
//
//        val fileHelper = FileHelper(applicationContext)
//
//        val file = fileHelper.gpxFileWith(gpxContent)
//        fileHelper.shareFile(file)

        nav_view.setNavigationItemSelectedListener(this)

        supportFragmentManager.beginTransaction()
                .add(R.id.content_container, GpxList())
                .disallowAddToBackStack()
                .commit()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    @SuppressLint("MissingPermission")
    fun startRecording(configuration: RecordingConfiguration) {

        val newGpx = GpxContent(title = configuration.title, trackList = RealmList(Track(segments = RealmList(Segment()))))
        Realm.getDefaultInstance().executeTransaction {
            Realm.getDefaultInstance().copyToRealm(newGpx)
        }

        val intent = Intent(this@MainActivity, LocationRecorderService::class.java)
        intent.putExtra(Keys.GpxId, newGpx.identifier)
        intent.putExtra(RecordingConfiguration.configKey, configuration.toBundle())

        startService(intent)

        LocationRecorderService
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
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
                Realm.getDefaultInstance().executeTransaction {
                    it.delete(GpxContent::class.java)
                }
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this@MainActivity, LocationRecorderService::class.java))
    }
}
