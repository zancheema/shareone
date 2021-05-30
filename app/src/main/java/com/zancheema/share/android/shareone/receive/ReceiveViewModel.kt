package com.zancheema.share.android.shareone.receive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zancheema.share.android.shareone.data.AppDataSource

class ReceiveViewModel(
    private val dataSource: AppDataSource
) : ViewModel() {
    val nickname: String get() = dataSource.getAvatar().nickname ?: "Unknown"
}

@Suppress("UNCHECKED_CAST")
class ReceiverViewModelFactory(
    private val dataSource: AppDataSource
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ReceiveViewModel(dataSource) as T
    }
}