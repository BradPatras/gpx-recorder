package com.iboism.gpxrecorder.util

import android.content.Context
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.model.XmlSerializable
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.File
import java.nio.charset.StandardCharsets
import android.content.Intent
import android.support.v4.content.FileProvider
import com.iboism.gpxrecorder.model.GpxContent


/**
 * Created by Brad on 11/18/2017.
 */
class FileHelper(
        var context: Context
) {
    companion object {
        private val REPLACE_CONTENT_TAG = "replacemewithcontent"
        private val REPLACE_DATE_TAG = "replacemewithdate"
        val SHARE_PATH = "shared"
        val SHARE_FILENAME = "recorded.gpx"
    }

    fun shareFile(gpxSharedFile: File) {
        val contentUri = FileProvider.getUriForFile(context, "com.iboism.gpxrecorder.fileprovider", gpxSharedFile)

        if (contentUri != null) {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // temp permission for receiving app to read this file
            shareIntent.setDataAndType(contentUri, context.contentResolver.getType(contentUri))
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
            context.startActivity(Intent.createChooser(shareIntent, "Choose an app"))
        }
    }

    fun gpxFileWith(content: GpxContent): File {// todo add exception handling
        val sharedFilesPath = File(context.cacheDir, SHARE_PATH)
        sharedFilesPath.mkdirs()

        val inputStream = context.resources.openRawResource(R.raw.gpx_stub)
        val gpxStub = IOUtils.toString(inputStream, StandardCharsets.UTF_8)

        var gpxFull = gpxStub.replaceFirst(REPLACE_CONTENT_TAG, content.getXmlString())

        val gpxSharedFile = File(sharedFilesPath, SHARE_FILENAME)
        FileUtils.writeStringToFile(gpxSharedFile, gpxFull, StandardCharsets.UTF_8)

        return gpxSharedFile
    }

}