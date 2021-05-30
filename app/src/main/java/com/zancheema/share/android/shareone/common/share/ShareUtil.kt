package com.zancheema.share.android.shareone.common.share

import android.app.Activity
import android.content.Context
import android.util.Log
import com.zancheema.share.android.shareone.R
import java.nio.ByteBuffer

private const val TAG = "ShareUtil"

fun Context.getFormattedFileSize(size: Long): String {
    val gbLimit = Math.pow(1024.0, 3.0)
    val mbLimit = Math.pow(1024.0, 2.0)
    val kbLimit = 1024.0
    return when {
        size >= gbLimit -> getString(R.string.format_size_gb, size / gbLimit)
        size >= mbLimit -> getString(R.string.format_size_mb, size / mbLimit)
        size >= kbLimit -> getString(R.string.format_size_kb, size / kbLimit)
        else -> getString(R.string.format_size_bytes, size / 1.0)
    }
}

fun Int.getBytes(): ByteArray {
    val buffer = ByteBuffer.allocate(4) // int is 4 bytes
    buffer.putInt(this)
    return buffer.array()
}

fun Long.getBytes(): ByteArray {
    val buffer = ByteBuffer.allocate(8) // long is 8 bytes
    buffer.putLong(this)
    return buffer.array()
}

fun ByteArray.toLong(): Long {
    val wrapped = ByteBuffer.wrap(this)
    return wrapped.long
}

fun ByteArray.toInt(): Int {
    val wrapped = ByteBuffer.wrap(this)
    return wrapped.int
}

fun Activity.getMediaPath(mediaName: String): String {
    val baseDir = externalMediaDirs[0].toString() + "/"
    Log.d(TAG, "getMediaPath: baseDir: $baseDir")

    val subDir = when {
        mediaName.isImageName() -> "Images/"
        mediaName.isAudioName() -> "Audios/"
        mediaName.isVideoName() -> "Videos/"
        mediaName.isAPKName() -> "APKs/"
        else -> "Files/"
    }

    return baseDir + subDir + mediaName
}

fun String.isImageName(): Boolean = lowercase().run {
    endsWith(".jpg") || endsWith(".jpeg") || endsWith(".png")
}

fun String.isAudioName(): Boolean = lowercase().endsWith(".mp3")

fun String.isVideoName(): Boolean = lowercase().run {
    endsWith(".mp4") || endsWith(".mkv")
}

fun String.isAPKName(): Boolean = lowercase().endsWith(".apk")
