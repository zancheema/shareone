package com.zancheema.share.android.shareone.broadcast

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log

class WiFiDirectBroadcastReceiver(private val listener: WifiDirectListener) :
    BroadcastReceiver() {
    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION == action) {
            val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                listener.onWiFiEnabled()
            } else {
                listener.onWiFiDisabled()
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION == action) {
            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            Log.d(TAG, "onReceive: WIFI_P2P_PEERS_CHANGED_ACTION")
            listener.getWifiP2pManager()
                .requestPeers(listener.getChannel(), listener.getPeerListListener())
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION == action) {
            // Respond to new connection or disconnections
            listener.getWifiP2pManager().requestConnectionInfo(
                listener.getChannel(),
                listener.getConnectionListener()
            )
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION == action) {
            // Respond to this device's wifi state changing
            // This won't work for Android 10+
        }
    }

    companion object {
        private const val TAG = "WiFiDirectBroadcastRece"
    }
}