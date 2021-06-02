package com.zancheema.share.android.shareone.common

import android.net.Uri

interface ContentConverter<out T: Uri> {
    fun onConverted(contentUri: (T) -> Unit)
}

class DefaultContentConverter : ContentConverter<Uri> {
    override fun onConverted(contentUri: (Uri) -> Unit) {
        TODO("Not yet implemented")
    }
}