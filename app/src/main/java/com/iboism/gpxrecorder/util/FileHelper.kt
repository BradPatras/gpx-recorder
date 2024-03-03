package com.iboism.gpxrecorder.util

import android.content.Context
import android.net.Uri
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

private enum class FileDestinationType {
    CACHE,
}

class FileHelper {
    enum class Format {
        Gpx,
        GeoJson
    }

    private var exporting: Long? = null

    fun isExporting() = exporting

    fun shareRouteFile(context: Context, gpxContentId: Long, format: Format): Completable {
        val sharedFilesDir = File(context.cacheDir, SHARE_PATH).apply { this.mkdirs() }
        val filename = getRouteFilename(gpxContentId, format)

        val sharedFile = File(sharedFilesDir, filename)

        return createFile(context, gpxContentId, sharedFile, format)
                .observeOn(AndroidSchedulers.mainThread())
                .map { file ->
                    ShareHelper(context).shareFile(file)
                }.ignoreElement().onErrorComplete {
                    Alerts(context).genericError(R.string.file_share_failed).show()
                    return@onErrorComplete true
                }
    }

    fun saveRouteFile(
        context: Context,
        gpxContentId: Long,
        destinationFileURI: Uri,
        format: Format
    ): Completable {
        return createFile(context, gpxContentId, destinationFileURI, format)
                .observeOn(AndroidSchedulers.mainThread())
                .ignoreElement().onErrorComplete {
                    Alerts(context).genericError(R.string.file_save_failed).show()
                    return@onErrorComplete true
                }
    }

    fun getRouteFilename(gpxContentId: Long, format: Format): String {
        val realm = Realm.getDefaultInstance()
        val gpx = GpxContent.withId(gpxContentId, realm) ?: throw Exception()

        return when(format) {
            Format.Gpx -> gpx.title.getLegalFilename().withGpxExt()
            Format.GeoJson -> gpx.title.getLegalFilename().withGeoJsonExt()
        }
    }

    private fun createFile(
        context: Context,
        gpxContentId: Long,
        destFile: File,
        format: Format
    ): Single<File> {
        exporting = gpxContentId
        return Single.just(gpxContentId)
                .observeOn(Schedulers.io())
                .map {
                    val realm = Realm.getDefaultInstance()
                    val gpx = GpxContent.withId(it, realm) ?: throw Exception()
                    when(format) {
                        Format.Gpx -> writeGpxToFile(context, gpx, destFile)
                        Format.GeoJson -> writeGeoJSONToFile(gpx, destFile)
                    }
                    realm.close()
                    return@map destFile
                }
                .doFinally { exporting = null }
    }

    private fun createFile(
        context: Context,
        gpxContentId: Long,
        uri: Uri,
        format: Format
    ): Single<Unit> {
        exporting = gpxContentId
        return Single.just(gpxContentId)
                .observeOn(Schedulers.io())
                .map {
                    val realm = Realm.getDefaultInstance()
                    val gpx = GpxContent.withId(it, realm) ?: throw Exception()
                    when(format) {
                        Format.Gpx -> writeGpxToStream(context, gpx, uri)
                        Format.GeoJson -> writeGeoJSONToStream(context, gpx, uri)
                    }

                    realm.close()
                }
                .doFinally { exporting = null }
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
        val fileOutputStream = context.contentResolver.openOutputStream(uri) ?: return
        try {
            fileOutputStream.write(gpxFull.encodeToByteArray())
            fileOutputStream.flush()
            fileOutputStream.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun writeGeoJSONToStream(context: Context, content: GpxContent, uri: Uri) {
        val jsonString = content.getJsonString()
        val fileOutputStream = context.contentResolver.openOutputStream(uri) ?: return
        try {
            fileOutputStream.write(jsonString.encodeToByteArray())
            fileOutputStream.flush()
            fileOutputStream.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
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