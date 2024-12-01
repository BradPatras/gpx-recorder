package com.iboism.gpxrecorder.recording.waypoint

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.iboism.gpxrecorder.Keys
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.databinding.ActivityCreateWaypointDialogBinding
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.util.Alerts
import com.iboism.gpxrecorder.util.PermissionHelper
import io.realm.Realm

private const val DRAFT_TITLE_KEY = "CreateWaypointDialog_draftTitle"
private const val DRAFT_NOTE_KEY = "CreateWaypointDialog_draftNote"

class CreateWaypointDialogActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateWaypointDialogBinding

    private val fusedLocation by lazy {
        LocationServices.getFusedLocationProviderClient(this@CreateWaypointDialogActivity)
    }

    private val locationConfiguration by lazy {
        LocationRequest.Builder(1000)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMaxUpdates(1)
            .setMaxUpdateDelayMillis(2500)
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateWaypointDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val bundle = intent.extras ?: return waypointError()
        val gpxId = checkNotNull(bundle[Keys.GpxId] as? Long) { waypointError() }

        binding.doneButton.setOnClickListener {
            startWaypointService(gpxId, binding.titleEditText.text.toString(), binding.noteEditText.text.toString())
        }

        val realm = Realm.getDefaultInstance()
        realm.executeTransaction {
            val gpxContent = GpxContent.withId(gpxId, it)
            val dist = gpxContent?.trackList?.first()?.segments?.first()?.distance?.toDouble() ?: 0.0
            binding.noteEditText.text.insert(0, "@%.2fkm".format(dist))
        }
        realm.close()

        restoreInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(DRAFT_TITLE_KEY, binding.titleEditText.text.toString())
        outState.putString(DRAFT_NOTE_KEY, binding.noteEditText.text.toString())
    }

    private fun restoreInstanceState(outState: Bundle?) {
        outState?.getString(DRAFT_TITLE_KEY)?.let { draftTitle ->
            binding.titleEditText.text.clear()
            binding.titleEditText.text.append(draftTitle)
        }

        outState?.getString(DRAFT_NOTE_KEY)?.let { draftNote ->
            binding.noteEditText.text.clear()
            binding.noteEditText.text.append(draftNote)
        }
    }

    private fun waypointError() {
        Alerts(applicationContext)
            .genericError(R.string.cannot_create_waypoint) { finish() }
            .show()
    }

    @SuppressLint("MissingPermission")
    private fun startWaypointService(gpxId: Long, title: String, note: String) {
        val intentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE
        } else {
            0
        }

        val waypointIntent = CreateWaypointService.startServiceIntent(applicationContext, gpxId, title, note)
        val waypointPendingIntent = PendingIntent.getBroadcast(applicationContext, 0, waypointIntent, intentFlags)
        PermissionHelper
            .checkLocationPermissions(this@CreateWaypointDialogActivity.applicationContext) {
                fusedLocation.requestLocationUpdates(locationConfiguration, waypointPendingIntent)
                finish()
            }
    }
}
