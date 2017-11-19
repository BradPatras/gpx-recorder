package com.iboism.gpxrecorder.model

import io.realm.RealmObject
import com.iboism.gpxrecorder.util.DateTimeFormatHelper
import com.iboism.gpxrecorder.util.UUIDHelper
import io.realm.annotations.PrimaryKey

/**
 * Created by Brad on 11/18/2017.
 */

open class TrackPoint(
        @PrimaryKey var identifier: Long = UUIDHelper.random(),
        var lat: Float = 0f,
        var lon: Float = 0f,
        var ele: Float = 0f,
        var time: String = DateTimeFormatHelper.formatDate()
) : RealmObject(), XmlSerializable {
    override fun getXmlString(): String {
        val eleXml = "<ele>$ele</ele>"
        val timeXml = "<time>$time</time>"
        return "<trkpt lat=\"$lat\" lon=\"$lon\">$eleXml $timeXml</trkpt> "
    }
}