package com.iboism.gpxrecorder.recording.waypoint

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.iboism.gpxrecorder.Keys
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.util.Alerts
import com.iboism.gpxrecorder.util.PermissionHelper
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_create_waypoint_dialog.*

private const val DRAFT_TITLE_KEY = "CreateWaypointDialog_draftTitle"
private const val DRAFT_NOTE_KEY = "CreateWaypointDialog_draftNote"

class CreateWaypointDialogActivity : AppCompatActivity() {

    private val fusedLocation by lazy {
        LocationServices.getFusedLocationProviderClient(this@CreateWaypointDialogActivity)
    }

    private val locationConfiguration by lazy {
        LocationRequest.create()
                .setInterval(1) // 1 second
                .setMaxWaitTime(5000) // 5 seconds
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setNumUpdates(1)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_waypoint_dialog)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val bundle = intent.extras ?: return waypointError()
        val gpxId = checkNotNull(bundle[Keys.GpxId] as? Long) { waypointError() }

        done_button.setOnClickListener {
            startWaypointService(gpxId, title_editText.text.toString(), note_editText.text.toString())
        }

        val realm = Realm.getDefaultInstance()
        realm.executeTransaction {
            val gpxContent = GpxContent.withId(gpxId, it)
            val dist = gpxContent?.trackList?.first()?.segments?.first()?.distance?.toDouble() ?: 0.0
            note_editText.text.insert(0, "@%.2fkm".format(dist))
        }
        realm.close()

        restoreInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(DRAFT_TITLE_KEY, title_editText.text.toString())
        outState.putString(DRAFT_NOTE_KEY, note_editText.text.toString())
    }

    private fun restoreInstanceState(outState: Bundle?) {
        outState?.getString(DRAFT_TITLE_KEY)?.let { draftTitle ->
            title_editText.text.clear()
            title_editText.text.append(draftTitle)
        }

        outState?.getString(DRAFT_NOTE_KEY)?.let {draftNote ->
            note_editText.text.clear()
            note_editText.text.append(draftNote)
        }
    }

    private fun waypointError() {
        Alerts(applicationContext)
                .genericError(R.string.cannot_create_waypoint) { finish() }
                .show()
    }

    @SuppressLint("MissingPermission")
    private fun startWaypointService(gpxId: Long, title: String, note: String) {
        val waypointIntent = CreateWaypointService.startServiceIntent(applicationContext, gpxId, title, note)
        val waypointPendingIntent = PendingIntent.getBroadcast(applicationContext, 0, waypointIntent, 0)
        PermissionHelper.getInstance(this@CreateWaypointDialogActivity)
                .checkLocationPermissions {
                    fusedLocation.requestLocationUpdates(locationConfiguration, waypointPendingIntent)
                    finish()
                }
    }
}
