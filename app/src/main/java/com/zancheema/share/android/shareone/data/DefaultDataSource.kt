package com.zancheema.share.android.shareone.data

import android.content.Context
import com.zancheema.share.android.shareone.R

private const val PREFERENCES_NAME = "app-shared-preferences"
private const val ICON = "icon"
private const val NICKNAME = "nickname";

class DefaultDataSource(context: Context) : AppDataSource {

    private val preferences =
        context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun setAvatar(avatar: Avatar) {
        preferences.edit()
            .putInt(ICON, avatar.iconRes)
            .putString(NICKNAME, avatar.nickname)
            .apply()
    }

    override fun getAvatar() = Avatar(
        preferences.getInt(ICON, R.drawable.ic_avatar_0),
        preferences.getString(NICKNAME, null)
    )

    override fun hasAvatar(): Boolean {
        return preferences.getInt(ICON, -1) != -1 ||
                preferences.getString(NICKNAME, null) != null
    }
}