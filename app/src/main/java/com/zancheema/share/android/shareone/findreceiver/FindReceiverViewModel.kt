package com.zancheema.share.android.shareone.findreceiver

import android.net.wifi.p2p.WifiP2pDevice
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zancheema.share.android.shareone.util.Event

class FindReceiverViewModel : ViewModel() {
    val devices = MutableLiveData<List<WifiP2pDevice>>()

    private val _deviceSelectedEvent = MutableLiveData<Event<WifiP2pDevice>>()
    val deviceSelectedEvent: LiveData<Event<WifiP2pDevice>>
        get() = _deviceSelectedEvent

    fun selectDevice(device: WifiP2pDevice) {
        _deviceSelectedEvent.value = Event(device)
    }
}