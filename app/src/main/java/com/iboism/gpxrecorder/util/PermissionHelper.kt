package com.iboism.gpxrecorder.util

import android.Manifest
import android.app.Activity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener


/**
 * Created by bradpatras on 11/24/17.
 */
class PermissionHelper private constructor(val activity: Activity) {

    fun checkLocationPermissions(onAllowed: () -> Unit, onDenied: () -> Unit) {
        Dexter.withActivity(activity)
                .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        report?.areAllPermissionsGranted()?.apply {
                            if (this) onAllowed.invoke() else onDenied.invoke()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                        token?.continuePermissionRequest() //figure out what to do here
                    }

                }).check()
    }

    companion object : SingletonHolder<PermissionHelper, Activity>(::PermissionHelper)
}
