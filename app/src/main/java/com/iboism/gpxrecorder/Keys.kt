package com.iboism.gpxrecorder

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

        // App shortcut action
        const val ShortcutAction = "com.iboism.gpxrecorder.START_RECORDING"

        // Shared Preferences
        const val HasShownLocationJustification = "kHasShownLocationJustification"
    }
}