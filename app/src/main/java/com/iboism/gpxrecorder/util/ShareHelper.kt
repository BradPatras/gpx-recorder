package com.iboism.gpxrecorder.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

/**
 * Created by bradpatras on 12/19/17.
 */
class ShareHelper(val context: Context) {
    fun shareFile(gpxSharedFile: File) {
        val contentUri = FileProvider.getUriForFile(context, "com.iboism.gpxrecorder.fileprovider", gpxSharedFile)

        if (contentUri != null) {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            shareIntent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/octet-stream", "text/plain"))
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
            shareIntent.type = "application/octet-stream"
            context.startActivity(Intent.createChooser(shareIntent, "Choose an app"))
        }
    }
}