package com.iboism.gpxrecorder.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Brad on 11/18/2017.
 */
class DateTimeFormatHelper {
    companion object {
        private const val datePattern = "yyyy-MM-dd'T'HH:mm:ss'Z'"

        private val dateFormatter: SimpleDateFormat
            get() {
                val dateFormatter = SimpleDateFormat(datePattern, Locale.US)
                dateFormatter.timeZone = TimeZone.getTimeZone("UTC")
                return dateFormatter
            }

        fun formatDate(date: Date = Date()): String {
            return dateFormatter.format(date)
        }

        fun toReadableString(dateString: String): String {
            val date = dateFormatter.parse(dateString) ?: return ""
            return SimpleDateFormat
                    .getDateTimeInstance(SimpleDateFormat.MEDIUM, SimpleDateFormat.SHORT)
                    .format(date)
        }
    }
}