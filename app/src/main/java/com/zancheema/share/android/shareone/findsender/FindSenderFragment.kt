package com.zancheema.share.android.shareone.findsender

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.net.Uri
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.*
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
import com.zancheema.share.android.shareone.data.DefaultDataSource
import com.zancheema.share.android.shareone.databinding.FragmentFindSenderBinding

private const val TAG = "FindSenderFragment"

class FindSenderFragment : Fragment(), WifiDirectListener {

    private val viewModel by viewModels<FindSenderViewModel> {
        FindSenderViewModelFactory(DefaultDataSource(requireContext().applicationContext))
    }
    private lateinit var viewDataBinding: FragmentFindSenderBinding

    private lateinit var manager: WifiP2pManager
    private lateinit var channel: Channel
    private lateinit var receiver: BroadcastReceiver
    private lateinit var intentFilter: IntentFilter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewDataBinding = FragmentFindSenderBinding.inflate(inflater, container, false)
            .apply { viewmodel = viewModel }
        return viewDataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewDataBinding.lifecycleOwner = viewLifecycleOwner
        viewDataBinding.rippleBackground.startRippleAnimation()
        manager = requireContext().getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(requireContext(), Looper.getMainLooper(), null)
        receiver = WiFiDirectBroadcastReceiver(this)
        intentFilter = IntentFilter()
        intentFilter.addAction(WIFI_P2P_STATE_CHANGED_ACTION)
        intentFilter.addAction(WIFI_P2P_PEERS_CHANGED_ACTION)
        intentFilter.addAction(WIFI_P2P_CONNECTION_CHANGED_ACTION)
        intentFilter.addAction(WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
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

    override fun getChannel(): Channel = channel

    private val peerListener: PeerListListener by lazy {
        PeerListListener { }
    }

    override fun getPeerListListener(): PeerListListener = peerListener

    private val connectionInfoListener = ConnectionInfoListener { info: WifiP2pInfo ->
        Log.d(TAG, "ConnectionInfoListener: called")
        val address = info.groupOwnerAddress
        if (address != null) {

            val (isGroupOwner, groupOwnerAddress) = if (info.groupFormed && info.isGroupOwner) {
                Pair(true, null)
            } else if (info.groupFormed) {
                Pair(false, address.hostAddress)
            } else {
                return@ConnectionInfoListener
            }
            Log.d(TAG, "isGroupOwner: $isGroupOwner")
            Log.d(TAG, "groupOwnerAddress: $groupOwnerAddress")

            findNavController().navigate(
                FindSenderFragmentDirections.actionFindSenderFragmentToReceiveFragment(
                    isGroupOwner,
                    groupOwnerAddress
                )
            )
        }
    }

    override fun getConnectionListener(): ConnectionInfoListener =
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
        manager.discoverPeers(channel, object : ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "onSuccess: Discover Peers")
            }

            override fun onFailure(reason: Int) {
                Log.d(TAG, "onFailure: Discover Peers")
            }
        })
    }

    private fun stopDiscovery() {
        manager.stopPeerDiscovery(channel, object : ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "onSuccess: Stop Discovery")
            }

            override fun onFailure(reason: Int) {
                Log.d(TAG, "onFailure: Stop Discovery")
            }
        })
    }
}