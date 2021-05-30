package com.zancheema.share.android.shareone.findreceiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pConfig
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
import androidx.navigation.fragment.findNavController
import com.zancheema.share.android.shareone.broadcast.WiFiDirectBroadcastReceiver
import com.zancheema.share.android.shareone.broadcast.WiFiDirectConnectionStatus
import com.zancheema.share.android.shareone.broadcast.WifiDirectListener
import com.zancheema.share.android.shareone.common.share.Shareable
import com.zancheema.share.android.shareone.databinding.FragmentFindReceiverBinding
import com.zancheema.share.android.shareone.util.EventObserver

private const val TAG = "FindReceiverFragment"

class FindReceiverFragment : Fragment(), WifiDirectListener {

    private lateinit var shareables: Array<Shareable>

    private val viewModel by viewModels<FindReceiverViewModel>()
    private lateinit var viewDataBinding: FragmentFindReceiverBinding
    private lateinit var manager: WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel
    private lateinit var receiver: BroadcastReceiver
    private lateinit var intentFilter: IntentFilter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        shareables = FindReceiverFragmentArgs.fromBundle(requireArguments()).shareables
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
        setUpProcessing()
    }

    private fun setUpProcessing() {
        viewModel.deviceSelectedEvent.observe(viewLifecycleOwner, EventObserver { device ->
            connect(device.deviceAddress)
        })
    }

    @SuppressLint("MissingPermission")
    private fun connect(address: String) {
        val config = WifiP2pConfig()
        config.deviceAddress = address
        Log.d(TAG, "connect: $address")
        manager.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(
                    TAG,
                    "onSuccess: Successfully connected to $address"
                )
            }

            override fun onFailure(reason: Int) {
                Log.d(TAG, "onFailure: Failed to connect to $address")
            }
        })
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
                    Pair(true, null)
                } else if (info.groupFormed) {
                    Pair(false, address.hostAddress)
                } else {
                    Log.d(TAG, "ConnectionInfoListener: not condition matched")
                    return@ConnectionInfoListener
                }
                // Navigate using passing isGroupOwner and groupOwnerAddress as arguments
                findNavController().navigate(
                    FindReceiverFragmentDirections.actionFindReceiverFragmentToSendFragment(
                        isGroupOwner,
                        groupOwnerAddress,
                        shareables
                    )
                )
            } else {
                Log.d(TAG, "ConnectionInfoListener: address is null")
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