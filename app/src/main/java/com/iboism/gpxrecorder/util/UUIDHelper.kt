package com.iboism.gpxrecorder.util

import java.util.*

/**
 * Created by Brad on 11/18/2017.
 */
class UUIDHelper {
    companion object {
        fun random(): Long {
            return UUID.randomUUID().leastSignificantBits
        }
    }
}