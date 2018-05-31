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
        var lat: Double = 0.0,
        var lon: Double = 0.0,
        var dist: Double = 0.0,
        var ele: Double? = null,
        var time: String = DateTimeFormatHelper.formatDate(),
        var title: String = "Waypoint",
        var desc: String = ""
) : XmlSerializable, RealmObject() {

    override fun getXmlString(): String {
        val eleXml = ele?.let { "<ele>$ele</ele>" } ?: ""
        val timeXml = "<time>$time</time>"
        val nameXml = "<name>$title</name>"
        val descXml = "<desc>$desc</desc>"
        return "<wpt lat=\"$lat\" lon=\"$lon\">$nameXml$descXml$eleXml$timeXml</wpt> "
    }
}