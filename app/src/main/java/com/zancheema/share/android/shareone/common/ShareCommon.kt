package com.zancheema.share.android.shareone.common

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zancheema.share.android.shareone.R
import com.zancheema.share.android.shareone.databinding.ItemShareBinding
import kotlinx.parcelize.Parcelize

class LiveShareListAdapter : ListAdapter<LiveShare, LiveShareListAdapter.ViewHolder>(LiveShareDiffUtil()) {

    class ViewHolder private constructor(
        private val binding: ItemShareBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(share: LiveShare) {
            binding.share = share
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = ItemShareBinding.inflate(inflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class LiveShareDiffUtil : DiffUtil.ItemCallback<LiveShare>() {
    override fun areItemsTheSame(oldItem: LiveShare, newItem: LiveShare): Boolean {
        return oldItem.uri == newItem.uri
    }

    override fun areContentsTheSame(oldItem: LiveShare, newItem: LiveShare): Boolean {
        return oldItem == newItem
    }
}

data class LiveShare(
    private val context: Context,
    val name: String,
    val size: Long,
    val uri: Uri,
    val total: Long,
    val progress: MutableLiveData<Int> = MutableLiveData(0)
) {
    val totalBytes = getFormattedFileSize(total, context)

    private val _isComplete = MutableLiveData(false)
    val isComplete: LiveData<Boolean>
        get() = _isComplete

    private val _transferredBytes = MutableLiveData("")
    val transferredBytes: LiveData<String>
        get() = _transferredBytes

    fun update(bytes: Long) {
        val trans = getFormattedFileSize(bytes, context)
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

@Parcelize
data class Shareable(
    val name: String,
    val size: Long,
    val uri: Uri
): Parcelable

fun getFormattedFileSize(size: Long, context: Context): String {
    val gbLimit = Math.pow(1024.0, 3.0)
    val mbLimit = Math.pow(1024.0, 2.0)
    val kbLimit = 1024.0
    return when {
        size >= gbLimit -> context.getString(R.string.format_size_gb, size / gbLimit)
        size >= mbLimit -> context.getString(R.string.format_size_mb, size / mbLimit)
        size >= kbLimit -> context.getString(R.string.format_size_kb, size / kbLimit)
        else -> context.getString(R.string.format_size_bytes, size / 1.0)
    }
}