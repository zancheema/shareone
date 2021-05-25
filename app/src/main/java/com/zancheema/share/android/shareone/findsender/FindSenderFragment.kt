package com.zancheema.share.android.shareone.findsender

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zancheema.share.android.shareone.R
import com.zancheema.share.android.shareone.databinding.FragmentFindSenderBinding

class FindSenderFragment : Fragment() {
    private lateinit var viewDataBinding: FragmentFindSenderBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewDataBinding = FragmentFindSenderBinding.inflate(inflater, container, false)
        return viewDataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewDataBinding.rippleBackground.startRippleAnimation()
    }
}