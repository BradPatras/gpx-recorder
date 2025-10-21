package com.iboism.gpxrecorder.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.GpxContent
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.File
import java.nio.charset.StandardCharsets

/**
 * Created by Brad on 11/18/2017.
 */

private const val SHARE_PATH = "shared"
private const val REPLACE_CONTENT_TAG = "replacemewithcontent"
private const val GPX_FILE_EXTENSION = ".gpx"
private const val GEOJSON_FILE_EXTENSION = ".geojson"

class FileHelper {
    enum class Format {
        Gpx,
        GeoJson
    }

    fun saveRouteFilesToDownloads(context: Context, gpxContentIds: List<Long>, format: Format, shouldUseIsoDateFilename: Boolean): Completable {
        val saveFileSingles = gpxContentIds.map {
            saveFileToDownloads(context, it, format, shouldUseIsoDateFilename)
        }
        
        return Single.zip(saveFileSingles) { results ->
            results.all { it as Boolean }
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .ignoreElement()
            .onErrorComplete {
                Alerts(context).genericError(R.string.file_save_failed).show()
                true
            }
    }

    private fun saveFileToDownloads(context: Context, gpxContentId: Long, format: Format, shouldUseIsoDateFilename: Boolean): Single<Boolean> {
        return Single.fromCallable {
            val realm = Realm.getDefaultInstance()
            val gpx = GpxContent.withId(gpxContentId, realm) ?: throw Exception("Failed to fetch gpx route")
            val filename = getRouteFilename(gpxContentId, format, shouldUseIsoDateFilename)
            val content = when(format) {
                Format.Gpx -> getGpxStub(context).replaceFirst(REPLACE_CONTENT_TAG, gpx.getXmlString())
                Format.GeoJson -> gpx.getJsonString()
            }
            realm.close()
            
            saveToMediaStore(context, filename, content, format)
        }.subscribeOn(Schedulers.io())
    }

    private fun saveToMediaStore(context: Context, fileName: String, content: String, format: Format): Boolean {
        val mimeType = when(format) {
            Format.Gpx -> "application/gpx+xml"
            Format.GeoJson -> "application/json"
        }
        
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        
        return try {
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    outputStream.write(content.toByteArray())
                }
                true
            } ?: false
        } catch (_: Exception) {
            false
        }
    }

    fun shareRouteFiles(context: Context, gpxContentIds: List<Long>, format: Format, shouldUseIsoDateFilename: Boolean): Completable {
        val filesSingles = gpxContentIds.map {
            createShareRouteFile(context, it, format, shouldUseIsoDateFilename)
        }
        return Single.zip(filesSingles) { files ->
            files.filterIsInstance<File>()
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess {
                ShareHelper(context).shareFiles(it)
            }
            .ignoreElement()
            .onErrorComplete {
                Alerts(context).genericError(R.string.file_save_failed).show()
                true
            }
    }

    fun createShareRouteFile(context: Context, gpxContentId: Long, format: Format, shouldUseIsoDateFilename: Boolean): Single<File> {
        val sharedFilesDir = File(context.cacheDir, SHARE_PATH).apply { this.mkdirs() }
        val filename = getRouteFilename(gpxContentId, format, shouldUseIsoDateFilename)
        val sharedFile = File(sharedFilesDir, filename)

        return createFile(context, gpxContentId, sharedFile, format)
            .observeOn(AndroidSchedulers.mainThread())
    }


    fun saveRouteFile(
        context: Context,
        gpxContentId: Long,
        destinationFileURI: Uri,
        format: Format
    ): Completable {
        return createFile(context, gpxContentId, destinationFileURI, format)
            .observeOn(AndroidSchedulers.mainThread())
            .ignoreElement()
            .onErrorComplete {
                Alerts(context).genericError(R.string.file_save_failed).show()
                return@onErrorComplete true
            }
    }

    fun getRouteFilename(gpxContentId: Long, format: Format, shouldUseIsoDate: Boolean): String {
        val realm = Realm.getDefaultInstance()
        val gpx = GpxContent.withId(gpxContentId, realm) ?: throw Exception("Failed to fetch gpx route")
        val filename = if (shouldUseIsoDate) {
            gpx.date
        } else {
            gpx.title
        }
        realm.close()
        return when(format) {
            Format.Gpx -> filename.getLegalFilename().withGpxExt()
            Format.GeoJson -> filename.getLegalFilename().withGeoJsonExt()
        }
    }

    private fun createFile(
        context: Context,
        gpxContentId: Long,
        destFile: File,
        format: Format
    ): Single<File> {
        return Single.just(gpxContentId)
            .observeOn(Schedulers.io())
            .map {
                val realm = Realm.getDefaultInstance()
                val gpx = GpxContent.withId(it, realm) ?: throw Exception("Failed to fetch gpx route")
                when(format) {
                    Format.Gpx -> writeGpxToFile(context, gpx, destFile)
                    Format.GeoJson -> writeGeoJSONToFile(gpx, destFile)
                }
                realm.close()
                return@map destFile
            }
    }

    private fun createFile(
        context: Context,
        gpxContentId: Long,
        uri: Uri,
        format: Format
    ): Single<Unit> {
        return Single.just(gpxContentId)
            .observeOn(Schedulers.io())
            .map {
                val realm = Realm.getDefaultInstance()
                val gpx = GpxContent.withId(it, realm) ?: throw Exception("Failed to fetch gpx route")
                when(format) {
                    Format.Gpx -> writeGpxToStream(context, gpx, uri)
                    Format.GeoJson -> writeGeoJSONToStream(context, gpx, uri)
                }

                realm.close()
            }
    }

    private fun getGpxStub(context: Context): String {
        val inputStream = context.resources.openRawResource(R.raw.gpx_stub)
        val gpxStub = IOUtils.toString(inputStream, StandardCharsets.UTF_8)
        inputStream.close()
        return gpxStub
    }

    private fun writeGpxToFile(context: Context, gpx: GpxContent, file: File): File {
        val gpxFull = getGpxStub(context).replaceFirst(REPLACE_CONTENT_TAG, gpx.getXmlString())
        FileUtils.writeStringToFile(file, gpxFull, StandardCharsets.UTF_8)

        return file
    }

    private fun writeGeoJSONToFile(content: GpxContent, file: File): File {
        FileUtils.writeStringToFile(file, content.getJsonString(), StandardCharsets.UTF_8)

        return file
    }

    private fun writeGpxToStream(context: Context, gpx: GpxContent, uri: Uri) {
        val gpxFull = getGpxStub(context).replaceFirst(REPLACE_CONTENT_TAG, gpx.getXmlString())
        val fileOutputStream = context.contentResolver.openOutputStream(uri) ?: throw Exception("Failed to open output stream")
        fileOutputStream.write(gpxFull.encodeToByteArray())
        fileOutputStream.flush()
        fileOutputStream.close()
    }

    private fun writeGeoJSONToStream(context: Context, content: GpxContent, uri: Uri) {
        val jsonString = content.getJsonString()
        val fileOutputStream = context.contentResolver.openOutputStream(uri) ?: throw Exception("Failed to open output stream")
        fileOutputStream.write(jsonString.encodeToByteArray())
        fileOutputStream.flush()
        fileOutputStream.close()
    }

    private fun String.getLegalFilename(): String {
        return this
            .trim()
            .replace("[#{}&\\\\<>*?/ \$!'\"`:|=,]".toRegex(), "_")
    }

    private fun String.withGpxExt(): String {
        return "$this$GPX_FILE_EXTENSION"
    }

    private fun String.withGeoJsonExt(): String {
        return "$this$GEOJSON_FILE_EXTENSION"
    }

    companion object : SingletonHolder<FileHelper>(::FileHelper)
}