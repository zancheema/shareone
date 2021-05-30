package com.zancheema.share.android.shareone.common.share

import android.content.Context
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zancheema.share.android.shareone.R
import com.zancheema.share.android.shareone.databinding.ItemShareBinding
import kotlinx.parcelize.Parcelize

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