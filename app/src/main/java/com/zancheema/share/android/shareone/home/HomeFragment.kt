package com.zancheema.share.android.shareone.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.zancheema.share.android.shareone.data.DefaultDataSource
import com.zancheema.share.android.shareone.databinding.FragmentHomeBinding
import com.zancheema.share.android.shareone.util.EventObserver

class HomeFragment : Fragment() {
    private lateinit var viewDataBinding: FragmentHomeBinding
    private val viewModel by viewModels<HomeViewModel> {
        HomeViewModelFactory(
            DefaultDataSource(
                requireContext()
            )
        )
    }

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

        setUpNavigation()
    }

    private fun setUpNavigation() {
        val navController = findNavController()
        if (!viewModel.avatarIsSelected()) {
            navController.navigate(HomeFragmentDirections.actionHomeFragmentToSelectAvatarFragment())
        }
        viewModel.prepareSendEvent.observe(viewLifecycleOwner, EventObserver {
            if (it) navController.navigate(HomeFragmentDirections.actionHomeFragmentToPrepareSendFragment())
        })
        viewModel.prepareReceiveEvent.observe(viewLifecycleOwner, EventObserver {
            if (it) navController.navigate(HomeFragmentDirections.actionHomeFragmentToPrepareReceiveFragment())
        })
    }
}