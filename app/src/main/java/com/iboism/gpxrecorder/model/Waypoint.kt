package com.iboism.gpxrecorder.model

import com.iboism.gpxrecorder.util.DateTimeFormatHelper
import com.iboism.gpxrecorder.util.UUIDHelper
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by Brad on 11/19/2017.
 */
open class Waypoint(
        @PrimaryKey var identifier: Long = UUIDHelper.random(),
        var lat: Float = 0f,
        var lon: Float = 0f,
        var ele: Float = 0f,
        var time: String = DateTimeFormatHelper.formatDate(),
        var title: String = "Waypoint",
        var desc: String = ""
) : XmlSerializable, RealmObject() {

    override fun getXmlString(): String {
        val eleXml = "<ele>$ele</ele>"
        val timeXml = "<time>$time</time>"
        val nameXml = "<name>$title</name>"
        val descXml = "<desc>$desc</desc>"
        return "<trkpt lat=\"$lat\" lon=\"$lon\">$eleXml$timeXml$nameXml$descXml</trkpt> "
    }
}