package com.iboism.gpxrecorder.model

import android.os.Bundle
import com.google.android.gms.location.LocationRequest

/**
 * Created by bradpatras on 12/2/17.
 */
class RecordingConfiguration(
        var title: String = "Unititled Recording",
        var interval: Long = defaultInterval // 2.5 minutes
) {

    fun locationRequest(): LocationRequest {
        return LocationRequest()
                .setInterval(interval)
                .setSmallestDisplacement(2.5f)
                .setMaxWaitTime(interval * 3)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setFastestInterval(interval/4)
    }

    fun toBundle(): Bundle {
        return Bundle().apply {
            this.putString(titleKey, title)
            this.putLong(intervalKey, interval)
        }
    }

    companion object {
        const val titleKey = "kTitle"
        const val intervalKey = "kInterval"
        const val configKey = "kConfig"
        const val defaultInterval: Long = 150000


        fun fromBundle(bundle: Bundle): RecordingConfiguration? {
            if (!bundle.containsKey(titleKey)) return null
            if (!bundle.containsKey(intervalKey)) return null

            return RecordingConfiguration(
                    bundle.getString(titleKey),
                    bundle.getLong(intervalKey)
            )
        }
    }
}