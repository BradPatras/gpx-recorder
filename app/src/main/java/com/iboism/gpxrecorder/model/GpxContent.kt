package com.iboism.gpxrecorder.model

import com.iboism.gpxrecorder.util.DateTimeFormatHelper
import com.iboism.gpxrecorder.util.UUIDHelper
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.json.JSONObject

/**
 * Created by Brad on 11/19/2017.
 */
open class GpxContent(
        @PrimaryKey var identifier: Long = UUIDHelper.random(),
        var trackList: RealmList<Track> = RealmList(),
        var waypointList: RealmList<Waypoint> = RealmList(),
        var title: String = "",
        var date: String = DateTimeFormatHelper.formatDate()
        ) : JsonSerializable, XmlSerializable, RealmObject() {

    override fun getXmlString(): String {
        val titleXml = "<name>$title</name>"
        val descXml = "<desc>Recorded with GPX Recorder for Android</desc>"
        val metaDataXml = "<metadata>$titleXml$descXml</metadata>"
        val contentXml = listOf(trackList, waypointList)
                .flatten()
                .asSequence()
                .filterIsInstance(XmlSerializable::class.java)
                .map { it.getXmlString() }
                .fold("") { content, entity -> content + entity }

        return "$metaDataXml$contentXml"
    }

    override fun getJsonString(): String {
        val map: HashMap<Any?, Any?> = HashMap()
        map["title"] = title
        map["type"] = "FeatureCollection"

        val waypointsMapList = waypointList
            .asIterable()
            .map { waypoint ->
                val waypointMap = HashMap<String, Any>()

                val propertiesMap = HashMap<String, Any>()
                propertiesMap["title"] = waypoint.title
                propertiesMap["description"] = waypoint.desc

                val geometryMap = HashMap<String, Any>()
                geometryMap["type"] = "Point"
                geometryMap["coordinates"] = arrayOf(waypoint.lon, waypoint.lat)

                waypointMap["type"] = "Feature"
                waypointMap["properties"] = propertiesMap
                waypointMap["geometry"] = geometryMap

                waypointMap
            }

        val coordinates = trackList
            .flatMap { it.segments }
            .flatMap { it.points }
            .map { trackPoint ->
                listOf(trackPoint.lon, trackPoint.lat)
            }

        val lineGeometryMap = HashMap<String, Any>()
        lineGeometryMap["type"] = "LineString"
        lineGeometryMap["coordinates"] = coordinates

        val linesMap = HashMap<String, Any>()
        linesMap["type"] = "Feature"
        linesMap["properties"] = HashMap<String, Any>()
        linesMap["geometry"] = lineGeometryMap

        map["features"] = listOf(linesMap) + waypointsMapList
        return JSONObject(map).toString()
    }

    companion object Keys {
        const val primaryKey = "identifier"

        fun withId(identifier: Long?, realm: Realm): GpxContent? {
            if (identifier == null) return null

            return realm.where(GpxContent::class.java)
                    .equalTo(primaryKey, identifier)
                    .findFirst()
        }
    }
}