package com.zancheema.share.android.shareone.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.zancheema.share.android.shareone.R
import com.zancheema.share.android.shareone.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private lateinit var viewDataBinding: FragmentHomeBinding
    private val viewModel by viewModels<HomeViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewDataBinding = FragmentHomeBinding.inflate(inflater, container, false)
            .apply { viewmodel = viewModel }
        // Inflate the layout for this fragment
        return viewDataBinding.root
    }
}