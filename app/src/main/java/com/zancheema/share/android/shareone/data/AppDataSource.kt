package com.zancheema.share.android.shareone.data

interface AppDataSource {
    fun setAvatar(avatar: Avatar)

    fun getAvatar(): Avatar

    fun hasAvatar(): Boolean
}