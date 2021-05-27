package com.zancheema.share.android.shareone.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log

private const val TAG = "WiFiDirectBroadcastRece"

interface WiFiStatusListener {
    fun setActiveWLAN(active: Boolean)
}

class WiFiStatusBroadcastReceiver(
    var listener: WiFiStatusListener
) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION == action) {
            val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                listener.setActiveWLAN(true)
                Log.d(TAG, "onReceive: Wifi is enabled")
            } else {
                listener.setActiveWLAN(false)
                Log.d(TAG, "onReceive: Wifi is disabled")
            }
        }
    }
}