package com.iboism.gpxrecorder.model

import com.iboism.gpxrecorder.util.DateTimeFormatHelper
import com.iboism.gpxrecorder.util.UUIDHelper
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by Brad on 11/18/2017.
 */

open class TrackPoint(
        @PrimaryKey var identifier: Long = UUIDHelper.random(),
        var lat: Double = 0.0,
        var lon: Double = 0.0,
        var ele: Double? = null,
        var time: String = DateTimeFormatHelper.formatDate()
) : RealmObject(), XmlSerializable {
    override fun getXmlString(): String {
        val eleXml = ele?.let { return@let "<ele>$ele</ele>" } ?: ""
        val timeXml = "<time>$time</time>"
        return "<trkpt lat=\"$lat\" lon=\"$lon\">$eleXml $timeXml</trkpt>"
    }
}