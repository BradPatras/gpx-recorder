package com.iboism.gpxrecorder

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v4.view.GravityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import com.iboism.gpxrecorder.extensions.getRealmInitFailure
import com.iboism.gpxrecorder.model.*
import com.iboism.gpxrecorder.navigation.NavigationHelper
import com.iboism.gpxrecorder.recording.LocationRecorderService
import com.iboism.gpxrecorder.recording.configurator.RecordingConfiguratorModal
import com.iboism.gpxrecorder.records.list.GpxListFragment
import io.realm.Realm
import io.realm.RealmList
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity(), RecordingConfiguratorModal.Listener {

    private val navigationHelper: NavigationHelper = NavigationHelper(this)
    private var recorderService: LocationRecorderService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as LocationRecorderService.ServiceBinder
            recorderService = binder.getService()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            recorderService = null
        }
    }

    override fun onStart() {
        super.onStart()
        bindRecorderService()
    }

    override fun onStop() {
        super.onStop()
        unbindRecorderService()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (applicationContext.getRealmInitFailure()) {
            showSchemaFailure()
            return
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.content_container, GpxListFragment.newInstance())
                    .commit()

            // uncomment to create random track and add it to realm
//            val lst = listOf("Weekend Trip", "Windy Hike", "Forest Trail", "Creek Hike", "Mushroom Hunting", "Taking it Easy", "Hiking Out", "Hill Trace")
//            for (j in 0 until lst.count()) {
//                val seg = Segment()
//                val r = Random()
//                var sLat = 40.64777601
//                var sLon = -105.516816
//                for (i in 0..50) {
//                    sLat += (r.nextInt(5).toDouble() / 1000.0) * if(r.nextBoolean()) 1 else -1
//                    sLon += (r.nextInt(5).toDouble() / 1000.0) * if(r.nextBoolean()) 1 else -1
//                    seg.addPoint(TrackPoint(lat = sLat, lon = sLon))
//                }
//
//                val trk = Track(segments = RealmList(seg))
//                val gpx = GpxContent(title = lst[j], trackList = RealmList(trk))
//
//                val wlst = RealmList<Waypoint>()
//                for (i in 0..r.nextInt(4)) {
//                    wlst.add(Waypoint())
//                }
//                gpx.waypointList = wlst
//
//                Realm.getDefaultInstance().executeTransaction {
//                    it.copyToRealm(gpx)
//                }
//            }
        }

        if (Keys.ShortcutAction == intent.action) {
            RecordingConfiguratorModal.show(fragmentManager = supportFragmentManager)
        }

        nav_view.setNavigationItemSelectedListener(navigationHelper)
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

    private fun bindRecorderService() {
        val intent = Intent(this, LocationRecorderService::class.java)
        bindService(intent, serviceConnection, 0)
    }

    private fun unbindRecorderService() {
        unbindService(serviceConnection)
    }

    private fun showSchemaFailure() {
        AlertDialog.Builder(this)
                .setTitle(R.string.init_error_title)
                .setMessage(R.string.init_error_message)
                .setOnDismissListener {
                    this.finishAfterTransition()
                }.create()
                .show()
    }

    @SuppressLint("MissingPermission")
    private fun startRecording(configuration: RecordingConfiguration) {
        val newGpx = GpxContent(title = configuration.title, trackList = RealmList(Track(segments = RealmList(Segment()))))
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction {
            it.copyToRealm(newGpx)
        }
        realm.close()

        val intent = Intent(this@MainActivity, LocationRecorderService::class.java)
        intent.putExtra(Keys.StartService, true)
        intent.putExtra(Keys.GpxId, newGpx.identifier)
        intent.putExtra(RecordingConfiguration.configKey, configuration.toBundle())

        startService(intent)
    }
}
