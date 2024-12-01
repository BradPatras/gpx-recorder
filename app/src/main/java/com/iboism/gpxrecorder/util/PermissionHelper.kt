package com.iboism.gpxrecorder.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
class PermissionHelper {
    companion object {
        fun checkLocationPermissions(context: Context, onAllowed: () -> Unit) {
            val hasShownJustification = Prefs
                .getDefault(context)
                .getBoolean(Keys.HasShownLocationJustification, false)

            // If we haven't shown background location access justification yet, do that before
            // requesting permission.
            if (!hasShownJustification) {
                Alerts(context)
                    .backgroundLocationJustificationAlert { checkLocationPermissions(context, onAllowed) }.show()
                Prefs.getDefault(context).edit()
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

            Dexter.withContext(context)
                .withPermissions(permissions)
                .withListener(
                    Listener(
                        onAllowed,
                        onDenied = {
                            Alerts(context)
                                .permissionDeniedAlert { openApplicationSettings(context) }
                                .show()
                        }
                    )
                ).check()
        }

        private fun openApplicationSettings(context: Context) {
            context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID)))
        }
    }

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
