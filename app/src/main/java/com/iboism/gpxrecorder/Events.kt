package com.iboism.gpxrecorder

class Events {
    data class RecordingStartedEvent(val gpxId: Long?)
    data class RecordingStoppedEvent(val gpxId: Long?)
}