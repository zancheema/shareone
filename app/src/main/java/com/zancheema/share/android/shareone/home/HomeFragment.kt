package com.zancheema.share.android.shareone.home

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.zancheema.share.android.shareone.R
import com.zancheema.share.android.shareone.data.DefaultDataSource
import com.zancheema.share.android.shareone.databinding.FragmentHomeBinding
import com.zancheema.share.android.shareone.util.EventObserver

private const val TAG = "HomeFragment"

class HomeFragment : Fragment() {

    private val prepareSendPermissions =
        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)

    private val selectFilesIntent: Intent = Intent(Intent.ACTION_GET_CONTENT).apply {
        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "*/*"
    }

    private val prepareSendPermissionsRequest =
        registerForActivityResult(RequestMultiplePermissions()) { permissions ->
            if (permissions.values.all { it }) {
                launchFilePicker()
            } else {
                Snackbar.make(
                    requireView(),
                    R.string.permissions_not_granted,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

    private val selectFiles =
        registerForActivityResult(StartActivityForResult()) { result ->
            result?.data?.clipData?.let { clipData ->
                val uris = Array<Uri>(clipData.itemCount) { index ->
                    Log.d(TAG, "uris[$index]: ${clipData.getItemAt(index).uri}")
                    clipData.getItemAt(index).uri
                }
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToPrepareSendFragment(
                        uris
                    )
                )
            }
        }

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
        viewDataBinding.lifecycleOwner = viewLifecycleOwner
        setUpNavigation()
    }

    private fun setUpNavigation() {
        val navController = findNavController()
        if (!viewModel.avatarIsSelected()) {
            navController.navigate(HomeFragmentDirections.actionHomeFragmentToSelectAvatarFragment())
        }
        viewModel.prepareSendEvent.observe(viewLifecycleOwner, EventObserver {
            if (it) {
                if (allPermissionsGranted(prepareSendPermissions.toList())) {
                    launchFilePicker()
                } else {
                    prepareSendPermissionsRequest.launch(prepareSendPermissions)
                }
            }
        })
        viewModel.prepareReceiveEvent.observe(viewLifecycleOwner, EventObserver {
            navController.navigate(HomeFragmentDirections.actionHomeFragmentToPrepareReceiveFragment())
        })
    }

    private fun launchFilePicker() {
        Toast.makeText(requireContext(), R.string.press_and_hold_to_select, Toast.LENGTH_LONG)
            .show()
        selectFiles.launch(selectFilesIntent)
    }

    private fun allPermissionsGranted(permissions: Collection<String>): Boolean {
        for (p in permissions) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    p
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }
}