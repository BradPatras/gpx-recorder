package com.iboism.gpxrecorder.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.view.MenuItem
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.GpxContent
import com.iboism.gpxrecorder.recording.LocationRecorderService
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created by Brad on 2/17/2018.
 */
class NavigationHelper(val activity: Activity) : NavigationView.OnNavigationItemSelectedListener {
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.nav_github ->
                activity.launchExternalIntent(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.github.com/BradPatras/gpx-recorder/")))
            R.id.nav_twitter ->
                activity.launchExternalIntent(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.twitter.com/iboism")))
            R.id.nav_email -> {
                val intent = Intent(Intent.ACTION_SEND, Uri.parse("mailto:"))
                intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("appdev.iboism@gmail.com"))
                intent.type = "plain/text"
                activity.launchExternalIntent(intent)
            }
            R.id.nav_delete_recordings -> {
                Realm.getDefaultInstance().executeTransaction {
                    it.delete(GpxContent::class.java)
                    activity.stopService(Intent(activity, LocationRecorderService::class.java))
                }
            }
        }

        activity.drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    // Check if user has receiver for intent before attempting to start
    private fun Activity.launchExternalIntent(intent: Intent) {
       if (intent.resolveActivity(packageManager) != null) {
           this.startActivity(intent)
       }
    }
}