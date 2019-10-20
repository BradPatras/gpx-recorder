package com.iboism.gpxrecorder.model

import android.os.Bundle
import com.google.android.gms.location.LocationRequest

/**
 * Created by bradpatras on 12/2/17.
 */

class RecordingConfiguration(
        var title: String = "Unititled Recording",
        var interval: Long = REQUEST_INTERVAL,
        var maxWait: Long = (interval * 1.25).toLong(),
        var fastestInterval: Long = interval / 2
) {

    fun locationRequest(): LocationRequest {
        return LocationRequest()
                .setInterval(interval)
                .setSmallestDisplacement(2.5f)
                .setMaxWaitTime(maxWait)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setFastestInterval(fastestInterval)
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
        const val REQUEST_INTERVAL: Long = 150000

        fun fromBundle(bundle: Bundle): RecordingConfiguration? {
            val title = bundle.getString(titleKey) ?: return null
            val interval = bundle.getLong(intervalKey)

            return RecordingConfiguration(
                    title,
                    interval
            )
        }
    }
}