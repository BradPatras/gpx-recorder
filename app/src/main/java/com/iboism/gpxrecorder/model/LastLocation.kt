package com.iboism.gpxrecorder.model

import io.realm.Realm
import io.realm.RealmObject
import io.realm.kotlin.delete

open class LastLocation(
    var lat: Double = 0.0,
    var lon: Double = 0.0
): RealmObject() {
    companion object {
        private val default = LastLocation(lat = 42.139511, lon = -98.026325)
        fun get(): LastLocation {
            val realm = Realm.getDefaultInstance()
            val location = realm.where(LastLocation::class.java)
                .findFirst()?.let { return@let realm.copyFromRealm(it) } ?: default
            realm.close()
            return location
        }

        fun put(lat: Double, lon: Double) {
            val realm = Realm.getDefaultInstance()
            realm.executeTransaction {
                realm.delete<LastLocation>()
                val lastLocation = LastLocation(lat = lat, lon = lon)
                realm.copyToRealm(lastLocation)
            }

            realm.close()
        }
    }
}
