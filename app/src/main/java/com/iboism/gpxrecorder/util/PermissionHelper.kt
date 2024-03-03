package com.iboism.gpxrecorder.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.iboism.gpxrecorder.BuildConfig
import com.iboism.gpxrecorder.Keys
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener


/**
 * Created by bradpatras on 11/24/17.
 */
class PermissionHelper private constructor(private val activity: Activity) {

    fun checkLocationPermissions(onAllowed: () -> Unit) {
        val hasShownJustification = activity
            .getPreferences(Context.MODE_PRIVATE)
            .getBoolean(Keys.HasShownLocationJustification, false)

        // If we haven't shown background location access justification yet, do that before
        // requesting permission.
        if (!hasShownJustification) {
            Alerts(activity)
                .backgroundLocationJustificationAlert { checkLocationPermissions(onAllowed) }.show()
            activity.getPreferences(Context.MODE_PRIVATE).edit()
                .putBoolean(Keys.HasShownLocationJustification, true)
                .apply()
            return
        }

        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        permissions.addAll(listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))

        Dexter.withContext(activity)
            .withPermissions(permissions)
            .withListener(
                Listener(
                    onAllowed,
                    onDenied = {
                        Alerts(activity)
                            .permissionDeniedAlert { openApplicationSettings() }
                            .show()
                    }
                )
            ).check()
    }

    fun checkPermission(permissionName: String): Boolean {
        val granted = ContextCompat.checkSelfPermission(activity, permissionName)
        return granted == PackageManager.PERMISSION_GRANTED
    }

    private fun openApplicationSettings() {
        activity.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID)))
    }

    companion object : SingletonArgHolder<PermissionHelper, Activity>(::PermissionHelper)

    private class Listener(
        val onAllowed: () -> Unit,
        val onDenied: () -> Unit,
    ) : MultiplePermissionsListener {
        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
            report?.let {
                when {
                    it.areAllPermissionsGranted() -> onAllowed.invoke()
                    else -> onDenied.invoke()
                }
            }
        }

        override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
            token?.continuePermissionRequest()
        }
    }
}
