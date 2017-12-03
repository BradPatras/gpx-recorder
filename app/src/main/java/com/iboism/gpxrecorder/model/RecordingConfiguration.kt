package com.iboism.gpxrecorder.model

import com.google.android.gms.location.LocationRequest
import com.iboism.gpxrecorder.service.LocationRecorderService

/**
 * Created by bradpatras on 12/2/17.
 */
class RecordingConfiguration(
        var title: String = "Unititled Recording",
        var minDisplacement: Float = 25f,
        var interval: Long = 300000,
        var maxWait: Long = interval * 2,
        var fastestInteral: Long = interval / 2
) {

    fun locationRequest(): LocationRequest {
        return LocationRequest()
                .setInterval(interval)
                .setSmallestDisplacement(minDisplacement)
                .setMaxWaitTime(maxWait)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setFastestInterval(fastestInteral)
                .setNumUpdates(10)
    }

    inner class Builder() {
        var config = RecordingConfiguration()

        fun setMinDisplacement(displacement: Float): RecordingConfiguration {
            config.minDisplacement = displacement
            return config
        }

        fun setInterval(interval: Long): RecordingConfiguration {
            config.interval = interval
            return config
        }

        fun setTitle(title: String): RecordingConfiguration {
            config.title = title
            return config
        }
    }
}