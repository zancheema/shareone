package com.zancheema.share.android.shareone.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zancheema.share.android.shareone.common.share.Shareable
import com.zancheema.share.android.shareone.data.AppDataSource
import com.zancheema.share.android.shareone.util.Event

private const val TAG = "HomeViewModel"

class HomeViewModel(
    private val dataSource: AppDataSource
) : ViewModel() {

    private val _launchFilePickerEvent = MutableLiveData<Event<Boolean>>()
    val launchFilePickerEvent: LiveData<Event<Boolean>>
        get() = _launchFilePickerEvent

    private val _prepareReceiveEvent = MutableLiveData<Event<Boolean>>()
    val prepareReceiveEvent: LiveData<Event<Boolean>>
        get() = _prepareReceiveEvent

    private val _prepareSendEvent = MutableLiveData<Event<Array<Shareable>>>()
    val prepareSendEvent: LiveData<Event<Array<Shareable>>>
        get() = _prepareSendEvent

    fun prepareSend(shareable: Shareable) {
        Log.d(TAG, "prepareSend: called")
        _prepareSendEvent.value = Event(arrayOf(shareable))
    }

    fun prepareSend(shareables: Array<Shareable>) {
        Log.d(TAG, "prepareSend: called")
        _prepareSendEvent.value = Event(shareables)
    }

    fun prepareSend(shareables: Collection<Shareable>) {
        Log.d(TAG, "prepareSend: called")
        _prepareSendEvent.value = Event(shareables.toTypedArray())
    }

    fun avatarIsSelected(): Boolean {
        return dataSource.hasAvatar()
    }

    fun launchFilePicker() {
        _launchFilePickerEvent.value = Event(true)
    }

    fun prepareReceive() {
        _prepareReceiveEvent.value = Event(true)
    }
}

@Suppress("UNCHECKED_CAST")
class HomeViewModelFactory(
    private val dataSource: AppDataSource
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return HomeViewModel(dataSource) as T
    }
}