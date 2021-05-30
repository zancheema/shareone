package com.zancheema.share.android.shareone.common.share

import android.content.Context
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zancheema.share.android.shareone.R
import com.zancheema.share.android.shareone.databinding.ItemShareBinding
import kotlinx.parcelize.Parcelize
import java.nio.ByteBuffer

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

fun ByteArray.getLong(): Long {
    val wrapped = ByteBuffer.wrap(this)
    return wrapped.long
}

fun ByteArray.getInt(): Int {
    val wrapped = ByteBuffer.wrap(this)
    return wrapped.int
}