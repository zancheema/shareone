package com.zancheema.share.android.shareone.util

import android.view.View
import android.view.animation.AnimationUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.zancheema.share.android.shareone.R

fun FloatingActionButton.slideUp() {
    val animation =
        AnimationUtils.loadAnimation(context, R.anim.fab_slide_up)
    startAnimation(animation)
    visibility = View.VISIBLE
}

fun FloatingActionButton.slideDown() {
    val animation =
        AnimationUtils.loadAnimation(context, R.anim.fab_slide_down)
    startAnimation(animation)
    visibility = View.GONE
}