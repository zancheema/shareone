package com.zancheema.share.android.shareone.common.share

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Shareable(
    val name: String,
    val size: Long,
    val uri: Uri
): Parcelable