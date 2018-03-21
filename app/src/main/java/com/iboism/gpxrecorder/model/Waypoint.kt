package com.iboism.gpxrecorder.model

import android.os.Bundle
import com.iboism.gpxrecorder.util.DateTimeFormatHelper
import com.iboism.gpxrecorder.util.UUIDHelper
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.concurrent.ThreadLocalRandom

/**
 * Created by Brad on 11/19/2017.
 */
open class Waypoint(
        @PrimaryKey var identifier: Long = UUIDHelper.random(),
        var lat: Double = 0.0,
        var lon: Double = 0.0,
        var ele: Double = 0.0,
        var time: String = DateTimeFormatHelper.formatDate(),
        var title: String = "Waypoint",
        var desc: String = ""
) : XmlSerializable, RealmObject() {

    override fun getXmlString(): String {
        val eleXml = "<ele>$ele</ele>"
        val timeXml = "<time>$time</time>"
        val nameXml = "<name>$title</name>"
        val descXml = "<desc>$desc</desc>"
        return "<wpt lat=\"$lat\" lon=\"$lon\">$nameXml$descXml$eleXml$timeXml</wpt> "
    }

    // anonymized because I don't want to send exact user locations to analytics
    fun toAnonymizedBundle(): Bundle {
        val latOffset = ThreadLocalRandom.current().nextDouble(-2.0, 2.0)
        val lonOffset = ThreadLocalRandom.current().nextDouble(-2.0, 2.0)
        val bundle = Bundle()
        bundle.putString("title", title)
        bundle.putDouble("lat", lat+latOffset)
        bundle.putDouble("lon", lon+lonOffset)
        bundle.putDouble("ele", ele)
        bundle.putString("desc", desc)
        bundle.putString("time", time)
        return bundle
    }
}