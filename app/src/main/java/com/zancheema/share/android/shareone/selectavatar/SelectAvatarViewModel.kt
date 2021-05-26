package com.zancheema.share.android.shareone.selectavatar

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zancheema.share.android.shareone.R
import com.zancheema.share.android.shareone.data.AppDataSource
import com.zancheema.share.android.shareone.data.Avatar
import com.zancheema.share.android.shareone.util.Event

private const val TAG = "SelectAvatarViewModel"

class SelectAvatarViewModel(
    private val dataSource: AppDataSource
) : ViewModel() {

    val nickname = MutableLiveData<String>()

    private val _avatarIcon = MutableLiveData(R.drawable.ic_avatar_0)
    val avatarIcon: LiveData<Int>
        get() = _avatarIcon

    private val _avatarSelectedEvent = MutableLiveData<Event<Avatar>>()
    val avatarSelectedEvent: LiveData<Event<Avatar>>
        get() = _avatarSelectedEvent

    fun setAvatarIcon(iconRes: Int) {
        _avatarIcon.value = iconRes
    }

    fun saveAvatar() {
        val icon = avatarIcon.value ?: R.drawable.ic_avatar_0
        val name = nickname.value

        val avatar = Avatar(icon, name)
        Log.d(TAG, "saveAvatar: $avatar")
        dataSource.setAvatar(avatar)
        _avatarSelectedEvent.value = Event(avatar)
    }
}

@Suppress("UNCHECKED_CAST")
class SelectAvatarViewModelFactory(
    private val dataSource: AppDataSource
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SelectAvatarViewModel(dataSource) as T
    }
}