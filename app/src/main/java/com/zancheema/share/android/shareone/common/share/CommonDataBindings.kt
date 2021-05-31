package com.zancheema.share.android.shareone.common.share

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

object CommonDataBindings {
    @JvmStatic
    @BindingAdapter("image_res", "place_holder_res", requireAll = true)
    fun setImageRes(imageView: ImageView, imageRes: Int, placeHolderRes: Drawable) {
        Glide
            .with(imageView.context)
            .load(imageRes)
            .placeholder(placeHolderRes)
            .into(imageView)
    }
}