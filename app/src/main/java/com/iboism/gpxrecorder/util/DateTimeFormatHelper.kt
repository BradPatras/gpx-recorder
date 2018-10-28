package com.iboism.gpxrecorder.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Brad on 11/18/2017.
 */
class DateTimeFormatHelper {
    companion object {
        private const val dataPattern = "yyyy-MM-dd'T'HH:mm:ss'Z'"
        fun formatDate(date: Date = Date()): String {
            return SimpleDateFormat(dataPattern, Locale.US).format(date)
        }

        fun toReadableString(dateString: String): String {
            val date = SimpleDateFormat(dataPattern, Locale.US).parse(dateString)
            return SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.MEDIUM, SimpleDateFormat.SHORT).format(date)
        }
    }
}