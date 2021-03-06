package com.zancheema.share.android.shareone.common.share

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zancheema.share.android.shareone.databinding.ItemShareBinding

class LiveShareListAdapter(
    private val lifecycleOwner: LifecycleOwner
) : ListAdapter<LiveShare, LiveShareListAdapter.ViewHolder>(LiveShareDiffUtil()) {

    class ViewHolder private constructor(
        private val binding: ItemShareBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(share: LiveShare) {
            binding.share = share
        }

        companion object {
            fun from(parent: ViewGroup, lifecycleOwner: LifecycleOwner): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = ItemShareBinding.inflate(inflater, parent, false)
                binding.lifecycleOwner = lifecycleOwner
                return ViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent, lifecycleOwner)
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