package com.zancheema.share.android.shareone.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.zancheema.share.android.shareone.data.AppDataSource
import com.zancheema.share.android.shareone.data.DefaultDataSource
import com.zancheema.share.android.shareone.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private lateinit var dataSource: AppDataSource
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        dataSource = DefaultDataSource(requireContext())
        setUpNavigation()
    }

    private fun setUpNavigation() {
        if (!dataSource.hasAvatar()) {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToSelectAvatarFragment())
        }
    }
}