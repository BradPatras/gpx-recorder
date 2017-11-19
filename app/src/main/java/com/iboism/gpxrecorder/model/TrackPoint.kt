package com.iboism.gpxrecorder.model

import com.fasterxml.jackson.annotation.JsonAutoDetect
import io.realm.RealmObject
import com.iboism.gpxrecorder.util.DateTimeFormatHelper
import com.iboism.gpxrecorder.util.UUIDHelper
import io.realm.annotations.PrimaryKey
import com.fasterxml.jackson.annotation.JsonTypeName
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

/**
 * Created by Brad on 11/18/2017.
 */
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE
)
@JacksonXmlRootElement(localName = "trkpt")
open class TrackPoint(
        @PrimaryKey var identifier: Long = UUIDHelper.random(),
        @JacksonXmlProperty(isAttribute = true) var lat: Float = 0f,
        @JacksonXmlProperty(isAttribute = true) var lon: Float = 0f,
        @JacksonXmlProperty var ele: Float = 0f,
        @JacksonXmlProperty var time: String = DateTimeFormatHelper.formatDate()
) : RealmObject(), XmlSerializable {
    override fun getXmlString(): String {
        val eleXml = "<ele>$ele</ele>"
        val timeXml = "<time>$time</time>"
        return "<trkpt lat=\" $lat \" lon=\" $lon \">$eleXml $timeXml</trkpt> "
    }

}