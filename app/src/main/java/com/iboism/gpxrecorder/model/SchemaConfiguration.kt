package com.iboism.gpxrecorder.model

import io.realm.RealmConfiguration
private const val SCHEMA_VERSION: Long = 10

const val REALM_SHARED_PREFERENCES_NAME = "kRealmSharedPrefs"
const val REALM_INIT_FAILED_KEY = "kRealmInitialized"

class Schema {
    companion object {
        fun configuration(): RealmConfiguration{
            return RealmConfiguration.Builder()
                    .schemaVersion(SCHEMA_VERSION)
                    .migration { realm, oldVersion, newVersion ->
                        var version = oldVersion

                        // Initial version -> 10
                        if (version < 10L && version != 0L) {
                            realm.schema.get("Waypoint")
                                    ?.addField("dist", Double::class.java)
                            realm.schema.get("Waypoint")
                                    ?.transform { obj ->
                                        obj.setDouble("dist", 0.0)
                                    }
                            version = 10L
                        }

                        // 10 -> 11
                        if (version == 10L) {
                            version++
                        }
                    }.build()
        }
    }
}