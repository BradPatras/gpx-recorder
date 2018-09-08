package com.iboism.gpxrecorder.primary

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.*
import com.iboism.gpxrecorder.navigation.NavigationHelper
import com.iboism.gpxrecorder.recording.LocationRecorderService
import com.iboism.gpxrecorder.recording.RecordingConfiguratorModal
import com.iboism.gpxrecorder.util.Keys
import com.iboism.gpxrecorder.util.getRealmInitFailure
import io.realm.Realm
import io.realm.RealmList
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), RecordingConfiguratorModal.Listener {

    private val navigationHelper: NavigationHelper = NavigationHelper(this)

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

//            // uncomment to create random track and add it to realm
//            val lst = listOf("Catshark", "Caterpillar", "Catfish", "Cattle", "list", "Centipede", "Cephalopod", "Chameleon", "Cheetah", "Chickadee", "Chicken", "list", "Chimpanzee", "Chinchilla", "Chipmunk", "Clam", "Clownfish", "Cobra", "Cockroach", "Cod", "Condor", "Constrictor", "Coral", "Cougar", "Cow", "Coyote", "Crab", "Crane", "Crane fly", "Crawdad", "Crayfish", "Cricket", "Crocodile", "Crow", "Cuckoo", "Cicada", "Damselfly", "Deer", "Dingo", "Dinosaur", "list", "Dog", "list", "Dolphin", "Donkey", "list", "Dormouse", "Dove", "Dragonfly", "Dragon", "Duck", "list", "Dung beetle", "Eagle", "Earthworm", "Earwig", "Echidna", "Eel", "Egret", "Elephant", "Elephant seal", "Elk", "Emu", "English pointer", "Ermine", "Falcon", "Ferret", "Finch", "Firefly", "Fish", "Flamingo", "Flea", "Fly", "Flyingfish", "Fowl", "Fox", "Frog")
//            for (j in 0..25) {
//                val seg = Segment()
//                val r = Random()
//                var sLat = r.nextInt(100).toDouble()
//                var sLon = r.nextInt(100).toDouble()
//                for (i in 0..1000) {
//                    sLat += (r.nextInt(5).toDouble() / 2.0) * if(r.nextBoolean()) 1 else -1
//                    sLon += (r.nextInt(5).toDouble() / 2.0) * if(r.nextBoolean()) 1 else -1
//                    seg.addPoint(TrackPoint(lat = sLat, lon = sLon))
//                }
//
//                val trk = Track(segments = RealmList(seg))
//                val gpx = GpxContent(title = lst[j], trackList = RealmList(trk))
//
//                Realm.getDefaultInstance().executeTransaction {
//                    it.copyToRealm(gpx)
//                }
//            }
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
        intent.putExtra(Keys.GpxId, newGpx.identifier)
        intent.putExtra(RecordingConfiguration.configKey, configuration.toBundle())

        startService(intent)
    }
}
