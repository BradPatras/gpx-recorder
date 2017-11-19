package com.iboism.gpxrecorder.util

import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

/**
 * Created by Brad on 11/18/2017.
 */
class DateTimeFormatHelper {
    companion object {
        fun formatDate(date: Date = Date()): String {
            return SimpleDateFormat.getDateTimeInstance().format(date)
        }
    }
}