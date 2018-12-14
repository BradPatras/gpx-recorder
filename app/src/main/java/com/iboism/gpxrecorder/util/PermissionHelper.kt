package com.iboism.gpxrecorder.util

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.iboism.gpxrecorder.BuildConfig
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener


/**
 * Created by bradpatras on 11/24/17.
 */
class PermissionHelper private constructor(val activity: Activity) {

    fun checkLocationPermissions(onAllowed: () -> Unit) {
        Dexter.withActivity(activity)
                .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .withListener(
                        Listener(
                                onAllowed,
                                onDenied = {
                                    Alerts(activity)
                                            .permissionDeniedAlert { checkLocationPermissions(onAllowed) }
                                            .show()
                                },
                                onDeniedForever = {
                                    Alerts(activity)
                                            .permissionDeniedForeverAlert { openApplicationSettings() }
                                            .show()
                                }
                        )
                ).check()
    }

    private fun openApplicationSettings() {
        activity.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID)))
    }

    companion object : SingletonArgHolder<PermissionHelper, Activity>(::PermissionHelper)

    private class Listener(
            val onAllowed: () -> Unit,
            val onDenied: () -> Unit,
            val onDeniedForever: () ->  Unit
    ) : MultiplePermissionsListener {
        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
            report?.let {
                when {
                    it.areAllPermissionsGranted() -> onAllowed.invoke()
                    it.isAnyPermissionPermanentlyDenied -> onDeniedForever.invoke()
                    else -> onDenied.invoke()
                }
            }
        }

        override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
            token?.continuePermissionRequest() //figure out what to do here
        }
    }
}
