package com.iboism.gpxrecorder.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import com.google.android.material.navigation.NavigationView
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.recording.LocationRecorderService
import io.realm.Realm

/**
 * Created by Brad on 2/17/2018.
 */
class NavigationHelper(private val activity: Activity) : NavigationView.OnNavigationItemSelectedListener {
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_github ->
                activity.launchExternalIntent(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.github.com/BradPatras/gpx-recorder/")))

            R.id.nav_email -> {
                val intent = Intent(Intent.ACTION_SEND, Uri.parse("mailto:appdev.iboism@gmail.com"))
                intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("appdev.iboism@gmail.com"))
                intent.putExtra(Intent.EXTRA_SUBJECT, "Gpx Recorder Developer Contact")
                intent.type = "plain/text"
                activity.launchExternalIntent(intent)
            }

            R.id.nav_delete_recordings -> {
                AlertDialog.Builder(activity)
                        .setTitle(R.string.clear_all_alert_title)
                        .setMessage(R.string.clear_all_alert_message)
                        .setCancelable(true)
                        .setPositiveButton(R.string.clear_all_alert_button) { _, _ ->
                          LocationRecorderService.requestStopRecording(activity)
                            val realm = Realm.getDefaultInstance()
                            realm.executeTransaction {
                                it.delete(GpxContent::class.java)
                            }
                            realm.close()
                        }.create().show()
            }

            R.id.nav_privacy_policy ->
                activity.launchExternalIntent(Intent(Intent.ACTION_VIEW, Uri.parse("http://bradpatras.github.io/gpx_privacy_policy")))
        }

        return true
    }

    // Check if user has receiver for intent before attempting to start
    private fun Activity.launchExternalIntent(intent: Intent) {
       if (intent.resolveActivity(packageManager) != null) {
           this.startActivity(intent)
       }
    }
}