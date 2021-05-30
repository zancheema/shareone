package com.zancheema.share.android.shareone.common.share

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zancheema.share.android.shareone.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class LiveShare(
    private val context: Context,
    val name: String,
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

    suspend fun update(bytes: Long) {
        val trans = context.getFormattedFileSize(bytes)
        val transferredRatio = context.getString(R.string.bytes_transferred, trans, totalBytes)
        if (transferredRatio != _transferredBytes.value) {
            _transferredBytes.postValue(transferredRatio)
        }

        // Update progress
        val newProgress = ((bytes * 100) / total).toInt()
        if (newProgress > progress.value!!) {
            progress.postValue(newProgress)
        }

        // complete transfer if required
        if (bytes >= total) complete()
    }

    private suspend fun complete() {
        withContext(Dispatchers.Main) {
            _transferredBytes.value = totalBytes
            progress.value = 100
            _isComplete.value = true
        }
    }
}