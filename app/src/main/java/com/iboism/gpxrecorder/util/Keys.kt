package com.iboism.gpxrecorder.util

/**
 * Created by Brad on 11/22/2017.
 */

class Keys {
    companion object {
        const val GpxId = "kgpxid"

        // Service Commands
        const val StopService = "kStopService"
        const val PauseService = "kPauseService"
        const val ResumeService = "kResumeService"
        const val StartService = "kStartService"

        // Analytics
        const val ServiceReceivedCommand = "service_received_command"

        // App shortcut action
        const val ShortcutAction = "com.iboism.gpxrecorder.START_RECORDING"
    }
}