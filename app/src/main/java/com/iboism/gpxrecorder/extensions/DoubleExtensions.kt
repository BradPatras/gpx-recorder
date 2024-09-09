package com.iboism.gpxrecorder.extensions

import java.util.Locale

fun Double.toElevationString(): String {
    return String.format(Locale.ROOT, "%.3f", this)
}