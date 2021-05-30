package com.zancheema.share.android.shareone.receive

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zancheema.share.android.shareone.common.share.LiveShare
import com.zancheema.share.android.shareone.data.AppDataSource

class ReceiveViewModel(
    private val dataSource: AppDataSource
) : ViewModel() {
    val nickname: String get() = dataSource.getAvatar().nickname ?: "Unknown"

    private val _shares = MutableLiveData<List<LiveShare>>(emptyList())
    val shares: LiveData<List<LiveShare>>
        get() = _shares

    fun addShare(share: LiveShare) {
        _shares.value = _shares.value!!.toMutableList().apply { add(share) }
    }
}

@Suppress("UNCHECKED_CAST")
class ReceiverViewModelFactory(
    private val dataSource: AppDataSource
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ReceiveViewModel(dataSource) as T
    }
}