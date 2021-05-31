package com.zancheema.share.android.shareone.findsender

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zancheema.share.android.shareone.data.AppDataSource
import com.zancheema.share.android.shareone.data.Avatar

class FindSenderViewModel(
    dataSource: AppDataSource
) : ViewModel() {

    private val _avatar = MutableLiveData<Avatar>()
    val avatar: LiveData<Avatar>
        get() = _avatar

    init {
        _avatar.value = dataSource.getAvatar()
    }
}

@Suppress("UNCHECKED_CAST")
class FindSenderViewModelFactory(
    private val dataSource: AppDataSource
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return FindSenderViewModel(dataSource) as T
    }
}