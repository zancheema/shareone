package com.zancheema.share.android.shareone.preparesend

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zancheema.share.android.shareone.broadcast.WiFiStatusListener
import com.zancheema.share.android.shareone.util.Event

class PrepareSendViewModel : ViewModel(), WiFiStatusListener {
    private val _openWlanSettingsEvent = MutableLiveData<Event<Boolean>>()
    val openWlanSettingsEvent: LiveData<Event<Boolean>>
        get() = _openWlanSettingsEvent

    private val _openLocationSettingsEvent = MutableLiveData<Event<Boolean>>()
    val openLocationSettingsEvent: LiveData<Event<Boolean>>
        get() = _openLocationSettingsEvent

    private val _proceedEvent = MutableLiveData<Event<Boolean>>()
    val proceedEvent: LiveData<Event<Boolean>>
        get() = _proceedEvent

    private val _wlanActivationEvent = MutableLiveData<Event<Boolean>>()
    val wlanActivationEvent: LiveData<Event<Boolean>>
        get() = _wlanActivationEvent

    private val _locationActivationEvent = MutableLiveData<Event<Boolean>>()
    val locationActivationEvent: LiveData<Event<Boolean>>
        get() = _locationActivationEvent

    private val _preparationsCompleteEvent = MutableLiveData<Event<Boolean>>()
    val preparationsCompleteEvent: LiveData<Event<Boolean>>
        get() = _preparationsCompleteEvent

    fun openWlanSettings() {
        _openWlanSettingsEvent.value = Event(true)
    }

    fun openLocationSettings() {
        _openLocationSettingsEvent.value = Event(true)
    }

    fun proceed() {
        _proceedEvent.value = Event(true)
    }

    override fun setActiveWLAN(active: Boolean) {
        _wlanActivationEvent.value = Event(active)
        refreshPreparationsStatus()
    }

    fun setActiveLocation(active: Boolean) {
        _locationActivationEvent.value = Event(active)
        refreshPreparationsStatus()
    }

    private fun refreshPreparationsStatus() {
        val wlan = wlanActivationEvent.value?.peekContent()
        val location = locationActivationEvent.value?.peekContent()
        _preparationsCompleteEvent.value = Event(
            wlan != null && location != null && wlan && location
        )
    }
}