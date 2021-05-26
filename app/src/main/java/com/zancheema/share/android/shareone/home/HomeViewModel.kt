package com.zancheema.share.android.shareone.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zancheema.share.android.shareone.data.AppDataSource
import com.zancheema.share.android.shareone.util.Event

class HomeViewModel(
    private val dataSource: AppDataSource
) : ViewModel() {

    private val _prepareSendEvent = MutableLiveData<Event<Boolean>>()
    val prepareSendEvent: LiveData<Event<Boolean>>
        get() = _prepareSendEvent

    private val _prepareReceiveEvent = MutableLiveData<Event<Boolean>>()
    val prepareReceiveEvent: LiveData<Event<Boolean>>
        get() = _prepareReceiveEvent

    fun avatarIsSelected(): Boolean {
        return dataSource.hasAvatar()
    }

    fun prepareSend() {
        _prepareSendEvent.value = Event(true)
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