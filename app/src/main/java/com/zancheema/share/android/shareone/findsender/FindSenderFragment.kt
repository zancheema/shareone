package com.zancheema.share.android.shareone.findsender

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.zancheema.share.android.shareone.databinding.FragmentFindSenderBinding

class FindSenderFragment : Fragment() {

    private lateinit var uris: Array<Uri>

    private lateinit var viewDataBinding: FragmentFindSenderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        uris = FindSenderFragmentArgs.fromBundle(requireArguments()).uris
    }

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