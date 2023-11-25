package com.iboism.gpxrecorder.extensions

fun Double.toElevationString(): String {
    return String.format("%.3f", this)
}