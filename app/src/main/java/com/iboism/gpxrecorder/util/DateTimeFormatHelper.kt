package com.iboism.gpxrecorder.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Brad on 11/18/2017.
 */
class DateTimeFormatHelper {
    companion object {
        private const val dataPattern = "yyyy-MM-dd'T'HH:mm:ss'Z'"
        private const val readablePattern = "EEE, MMM d yyyy, hh:mm a"
        fun formatDate(date: Date = Date()): String {
            return SimpleDateFormat(dataPattern).format(date)
        }

        fun toReadableString(dateString: String): String {
            val date = SimpleDateFormat(dataPattern).parse(dateString)
            return SimpleDateFormat(readablePattern).format(date)
        }
    }
}