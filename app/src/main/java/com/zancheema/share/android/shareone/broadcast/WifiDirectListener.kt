package com.zancheema.share.android.shareone.broadcast

import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener
import android.net.wifi.p2p.WifiP2pManager.PeerListListener

interface WifiDirectListener {
    fun getWifiP2pManager(): WifiP2pManager

    fun getChannel(): WifiP2pManager.Channel

    fun getPeerListListener(): PeerListListener

    fun getConnectionListener(): ConnectionInfoListener

    fun setWifiConnectionStatus(status: WiFiDirectConnectionStatus)

    fun onWiFiEnabled()

    fun onWiFiDisabled()
}

enum class WiFiDirectConnectionStatus {
    DISCONNECTED, PENDING, CONNECTED
}