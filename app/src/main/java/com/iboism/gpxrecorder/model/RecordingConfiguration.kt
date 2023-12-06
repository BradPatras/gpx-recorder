package com.iboism.gpxrecorder.model

import android.os.Bundle
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority

/**
 * Created by bradpatras on 12/2/17.
 */

class RecordingConfiguration(
    var title: String = "Unititled Recording",
    var interval: Long = REQUEST_INTERVAL,
    private var maxWait: Long = (interval * 1.25).toLong(),
    private var fastestInterval: Long = interval / 2
) {

    fun locationRequest(): LocationRequest {
        return LocationRequest.Builder(interval)
            .setMinUpdateDistanceMeters(2f)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMinUpdateIntervalMillis(fastestInterval)
            .setMaxUpdateDelayMillis(maxWait)
            .build()
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