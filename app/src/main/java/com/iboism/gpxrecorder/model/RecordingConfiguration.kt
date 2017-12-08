package com.iboism.gpxrecorder.model

import android.os.Bundle
import com.google.android.gms.location.LocationRequest
import java.util.*

/**
 * Created by bradpatras on 12/2/17.
 */
class RecordingConfiguration(
        var title: String = "Unititled Recording",
        var minDisplacement: Float = 25f,
        var interval: Long = 300000
) {

    fun locationRequest(): LocationRequest {
        return LocationRequest()
                .setInterval(interval)
                .setSmallestDisplacement(minDisplacement)
                .setMaxWaitTime(interval * 3)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setFastestInterval(interval / 2)
                .setNumUpdates(10)
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