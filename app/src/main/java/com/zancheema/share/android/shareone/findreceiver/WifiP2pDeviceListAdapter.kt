package com.zancheema.share.android.shareone.findreceiver

import android.net.wifi.p2p.WifiP2pDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zancheema.share.android.shareone.databinding.ItemReceiverBinding

class WifiP2pDeviceListAdapter(
    private val viewModel: FindReceiverViewModel
) : ListAdapter<WifiP2pDevice, WifiP2pDeviceListAdapter.ViewHolder>(WifiP2pDeviceDiffUtil()) {


    class ViewHolder private constructor(
        private val binding: ItemReceiverBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(device: WifiP2pDevice, viewModel: FindReceiverViewModel) {
            binding.device = device
            binding.viewmodel = viewModel
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = ItemReceiverBinding.inflate(inflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), viewModel)
    }
}

class WifiP2pDeviceDiffUtil : DiffUtil.ItemCallback<WifiP2pDevice>() {
    override fun areItemsTheSame(oldItem: WifiP2pDevice, newItem: WifiP2pDevice): Boolean {
        return oldItem.deviceAddress == newItem.deviceAddress
    }

    override fun areContentsTheSame(oldItem: WifiP2pDevice, newItem: WifiP2pDevice): Boolean {
        return oldItem == newItem
    }
}