package com.iboism.gpxrecorder.util

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.iboism.gpxrecorder.BuildConfig
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
        val permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // background location permission was introduced in version 29 and if
            // the user hasn't granted this permission
        }

        Dexter.withContext(activity)
                .withPermissions(permissions)
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

    fun checkPermission(permissionName: String): Boolean {
        return if (Build.VERSION.SDK_INT >= 23) {
            val granted =
                    ContextCompat.checkSelfPermission(activity, permissionName)
            granted == PackageManager.PERMISSION_GRANTED
        } else {
            val granted =
                    PermissionChecker.checkSelfPermission(activity, permissionName)
            granted == PermissionChecker.PERMISSION_GRANTED
        }
    }

    private fun openApplicationSettings() {
        activity.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID)))
    }

    companion object : SingletonArgHolder<PermissionHelper, Activity>(::PermissionHelper)

    private class Listener(
            val onAllowed: () -> Unit,
            val onDenied: () -> Unit,
            val onDeniedForever: () -> Unit
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
