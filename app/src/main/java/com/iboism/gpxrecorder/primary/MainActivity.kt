package com.iboism.gpxrecorder.primary

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v7.app.AppCompatActivity
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.model.RecordingConfiguration
import com.iboism.gpxrecorder.model.Segment
import com.iboism.gpxrecorder.model.Track
import com.iboism.gpxrecorder.navigation.NavigationHelper
import com.iboism.gpxrecorder.recording.LocationRecorderService
import com.iboism.gpxrecorder.recording.RecordingConfiguratorModal
import com.iboism.gpxrecorder.util.Keys
import io.realm.Realm
import io.realm.RealmList
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), RecordingConfiguratorModal.Listener {

    private val navigationHelper: NavigationHelper = NavigationHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.content_container, GpxListFragment.newInstance())
                    .commit()
        }

        nav_view.setNavigationItemSelectedListener(navigationHelper)


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

    override fun configurationCreated(configuration: RecordingConfiguration) {
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
}
