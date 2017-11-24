package com.iboism.gpxrecorder.util

import android.Manifest
import android.content.Context
import android.app.Activity
import com.iboism.gpxrecorder.R
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener
import com.karumi.dexter.listener.single.PermissionListener
import io.reactivex.Single
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest


/**
 * Created by bradpatras on 11/24/17.
 */
class PermissionHelper private constructor(val activity: Activity) {

    init {


    }

    fun checkLocationPermissions(onAllowed: () -> Unit, onDenied: () -> Unit) {
        Dexter.withActivity(activity)
                .withPermission(Manifest.permission_group.LOCATION)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {/* ... */
                        onAllowed.invoke()
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {/* ... */
                        onDenied.invoke()
                    }

                    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest, token: PermissionToken) {/* ... */
                        token.continuePermissionRequest()
                    }
                }).check()
    }

    companion object : SingletonHolder<PermissionHelper, Activity>(::PermissionHelper)
}