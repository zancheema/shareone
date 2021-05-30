package com.zancheema.share.android.shareone.common.share

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zancheema.share.android.shareone.R

data class LiveShare(
    private val context: Context,
    val name: String,
    val size: Long,
    val uri: Uri,
    val total: Long,
    val progress: MutableLiveData<Int> = MutableLiveData(0)
) {
    private val totalBytes = context.getFormattedFileSize(total)

    private val _isComplete = MutableLiveData(false)
    val isComplete: LiveData<Boolean>
        get() = _isComplete

    private val _transferredBytes = MutableLiveData("")
    val transferredBytes: LiveData<String>
        get() = _transferredBytes

    fun update(bytes: Long) {
        val trans = context.getFormattedFileSize(bytes)
        _transferredBytes.value = context.getString(R.string.bytes_transferred, trans, totalBytes)

        // Update progress
        val newProgress = ((bytes / total) * 100).toInt()
        if (newProgress > progress.value!!) {
            progress.value = newProgress
        }

        // complete transfer if required
        if (bytes >= total) complete()
    }

    fun complete() {
        transferredBytes
        progress.value = 100
        _isComplete.value = true
    }
}