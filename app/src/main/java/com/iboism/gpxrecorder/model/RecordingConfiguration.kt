package com.iboism.gpxrecorder.model

import android.os.Bundle
import com.google.android.gms.location.LocationRequest

/**
 * Created by bradpatras on 12/2/17.
 */
class RecordingConfiguration(
        var title: String = "Unititled Recording",
        var minDisplacement: Float = 5f, // 5 meters
        var interval: Long = 300000 // 5 minues
) {

    fun locationRequest(): LocationRequest {
        return LocationRequest()
                .setInterval(interval)
                .setSmallestDisplacement(minDisplacement)
                .setMaxWaitTime(interval * 3)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setFastestInterval(interval/4)
    }

    fun toBundle(): Bundle {
        return Bundle().apply {
            this.putString(titleKey, title)
            this.putLong(intervalKey, interval)
            this.putFloat(displacementKey, minDisplacement)
        }
    }

    companion object {
        var titleKey = "kTitle"
        var intervalKey = "kInterval"
        var displacementKey = "kDisplacement"
        var configKey = "kConfig"

        fun fromBundle(bundle: Bundle): RecordingConfiguration? {
            if (!bundle.containsKey(titleKey)) return null
            if (!bundle.containsKey(intervalKey)) return null
            if (!bundle.containsKey(displacementKey)) return null

            return RecordingConfiguration(
                    bundle.getString(titleKey),
                    bundle.getFloat(displacementKey),
                    bundle.getLong(intervalKey)
            )
        }
    }
}