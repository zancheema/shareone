package com.zancheema.share.android.shareone.findreceiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.net.Uri
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.zancheema.share.android.shareone.broadcast.WiFiDirectBroadcastReceiver
import com.zancheema.share.android.shareone.broadcast.WiFiDirectConnectionStatus
import com.zancheema.share.android.shareone.broadcast.WifiDirectListener
import com.zancheema.share.android.shareone.databinding.FragmentFindReceiverBinding

private const val TAG = "FindReceiverFragment"

class FindReceiverFragment : Fragment(), WifiDirectListener {

    private lateinit var uris: Array<Uri>

    private val viewModel by viewModels<FindReceiverViewModel>()
    private lateinit var viewDataBinding: FragmentFindReceiverBinding
    private lateinit var manager: WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel
    private lateinit var receiver: BroadcastReceiver
    private lateinit var intentFilter: IntentFilter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        uris = FindReceiverFragmentArgs.fromBundle(requireArguments()).uris
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewDataBinding = FragmentFindReceiverBinding.inflate(inflater, container, false)
        return viewDataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        manager = requireContext().getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(requireContext(), Looper.getMainLooper(), null)
        receiver = WiFiDirectBroadcastReceiver(this)
        intentFilter = IntentFilter()
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)

        viewDataBinding.lifecycleOwner = viewLifecycleOwner
        setUpViews()
    }

    private fun setUpViews() {
        val listAdapter = WifiP2pDeviceListAdapter(viewModel)
        viewDataBinding.rcReceiverList.adapter = listAdapter
        viewModel.devices.observe(viewLifecycleOwner) { devices ->
            listAdapter.submitList(devices)
        }
    }

    override fun onResume() {
        super.onResume()
        requireContext().registerReceiver(receiver, intentFilter)
        startDiscovery()
    }

    override fun onPause() {
        requireContext().unregisterReceiver(receiver)
        stopDiscovery()
        super.onPause()
    }

    override fun getWifiP2pManager(): WifiP2pManager = manager

    override fun getChannel(): WifiP2pManager.Channel = channel

    private val peerListener: WifiP2pManager.PeerListListener by lazy {
        WifiP2pManager.PeerListListener { devices: WifiP2pDeviceList ->
            Log.d(TAG, "No. of devices: ${devices.deviceList.size}")
            viewModel.devices.value = devices.deviceList.sortedBy { it.deviceName }
        }
    }

    override fun getPeerListListener(): WifiP2pManager.PeerListListener = peerListener

    private val connectionInfoListener =
        WifiP2pManager.ConnectionInfoListener { info: WifiP2pInfo ->
            Log.d(TAG, "ConnectionInfoListener: called")
            val address = info.groupOwnerAddress
            if (address != null) {

                val (isGroupOwner, groupOwnerAddress) = if (info.groupFormed && info.isGroupOwner) {
                    Pair(first = true, second = false)
                } else if (info.groupFormed) {
                    Pair(false, address.hostAddress)
                } else {
                    return@ConnectionInfoListener
                }
                // TODO: navigate using passing isGroupOwner and groupOwnerAddress as arguments
            }
        }

    override fun getConnectionListener(): WifiP2pManager.ConnectionInfoListener =
        connectionInfoListener

    override fun setWifiConnectionStatus(status: WiFiDirectConnectionStatus) {
        // nothing
    }

    override fun onWiFiEnabled() {
        // nothing
    }

    override fun onWiFiDisabled() {
        // nothing
    }

    @SuppressLint("MissingPermission")
    private fun startDiscovery() {
        manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "onSuccess: Discover Peers")
            }

            override fun onFailure(reason: Int) {
                Log.d(TAG, "onFailure: Discover Peers")
            }
        })
    }

    private fun stopDiscovery() {
        manager.stopPeerDiscovery(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "onSuccess: Stop Discovery")
            }

            override fun onFailure(reason: Int) {
                Log.d(TAG, "onFailure: Stop Discovery")
            }
        })
    }
}