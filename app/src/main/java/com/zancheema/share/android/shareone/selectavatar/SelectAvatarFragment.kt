package com.zancheema.share.android.shareone.selectavatar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.zancheema.share.android.shareone.R
import com.zancheema.share.android.shareone.data.DefaultDataSource
import com.zancheema.share.android.shareone.databinding.FragmentSelectAvatarBinding
import com.zancheema.share.android.shareone.util.EventObserver

class SelectAvatarFragment : Fragment() {

    private lateinit var viewDataBinding: FragmentSelectAvatarBinding
    private val viewModel by viewModels<SelectAvatarViewModel> {
        SelectAvatarViewModelFactory(
            DefaultDataSource(requireContext().applicationContext)
        )
    }
    private val iconDrawables = arrayOf(
        R.drawable.ic_avatar_0,
        R.drawable.ic_avatar_1,
        R.drawable.ic_avatar_2,
        R.drawable.ic_avatar_3,
        R.drawable.ic_avatar_4,
        R.drawable.ic_avatar_5,
        R.drawable.ic_avatar_6,
        R.drawable.ic_avatar_7,
        R.drawable.ic_avatar_8,
        R.drawable.ic_avatar_9
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewDataBinding = FragmentSelectAvatarBinding.inflate(inflater, container, false)
            .apply {
                iconDrawables = this@SelectAvatarFragment.iconDrawables
                viewmodel = viewModel
            }
        return viewDataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewDataBinding.lifecycleOwner = viewLifecycleOwner
        setUpViews()
        setUpNavigation()
    }

    private fun setUpViews() {
        viewModel.avatarIcon.observe(viewLifecycleOwner) { icon ->
            Glide
                .with(requireContext())
                .load(icon)
                .into(viewDataBinding.ivSelectedAvatar)
        }
    }

    private fun setUpNavigation() {
        viewModel.avatarSelectedEvent.observe(viewLifecycleOwner, EventObserver {
            findNavController().navigate(SelectAvatarFragmentDirections.actionSelectAvatarFragmentToHomeFragment())
        })
    }
}