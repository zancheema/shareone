package com.zancheema.share.android.shareone.preparereceive

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.location.LocationManager
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.zancheema.share.android.shareone.R
import com.zancheema.share.android.shareone.broadcast.WiFiStatusBroadcastReceiver
import com.zancheema.share.android.shareone.databinding.FragmentPrepareReceiveBinding
import com.zancheema.share.android.shareone.util.EventObserver

class PrepareReceiveFragment : Fragment() {
    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private val requestMultiplePermissions =
        registerForActivityResult(RequestMultiplePermissions()) { permissions ->
            if (permissions.values.all { it }) {
                startProcessing()
            } else {
                Snackbar.make(requireView(), "Permissions not granted", Snackbar.LENGTH_SHORT)
                    .show()
                findNavController().popBackStack()
            }
        }

    private val viewModel by viewModels<PrepareReceiveViewModel>()
    private lateinit var viewDataBinding: FragmentPrepareReceiveBinding
    private lateinit var broadcastReceiver: BroadcastReceiver
    private lateinit var intentFilter: IntentFilter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewDataBinding = FragmentPrepareReceiveBinding.inflate(inflater, container, false)
            .apply { viewmodel = viewModel }
        return viewDataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewDataBinding.lifecycleOwner = viewLifecycleOwner
        if (allPermissionsGranted(permissions.toList())) startProcessing()
        else requestMultiplePermissions.launch(permissions)
        setUpView()
        setUpNavigation()
    }

    private fun setUpView() {
        viewModel.wlanActivationEvent.observe(viewLifecycleOwner, EventObserver {
            // change wifi icon tint according to wifi status
            val tint = if (it) R.color.blue else R.color.color_on_primary
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val theme = requireActivity().theme
                viewDataBinding.ivWlan.imageTintList =
                    ColorStateList.valueOf(resources.getColor(tint, theme))
            } else {
                viewDataBinding.ivWlan.imageTintList =
                    ColorStateList.valueOf(resources.getColor(tint))
            }
        })
        // change location icon tint according to location status
        viewModel.locationActivationEvent.observe(viewLifecycleOwner, EventObserver {
            val tint = if (it) R.color.blue else R.color.color_on_primary
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val theme = requireActivity().theme
                viewDataBinding.ivLocation.imageTintList =
                    ColorStateList.valueOf(resources.getColor(tint, theme))
            } else {
                viewDataBinding.ivLocation.imageTintList =
                    ColorStateList.valueOf(resources.getColor(tint))
            }
        })
        // enabled or disable next button
        viewModel.preparationsCompleteEvent.observe(viewLifecycleOwner, EventObserver {
            viewDataBinding.btnNext.isEnabled = it
        })
    }

    private fun setUpNavigation() {
        viewModel.openWlanSettingsEvent.observe(viewLifecycleOwner, EventObserver {
            if (it) startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        })
        viewModel.openLocationSettingsEvent.observe(viewLifecycleOwner, EventObserver {
            if (it) startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        })
        viewModel.proceedEvent.observe(viewLifecycleOwner, EventObserver {
            findNavController().navigate(PrepareReceiveFragmentDirections.actionPrepareReceiveFragmentToFindSenderFragment())
        })
    }

    @SuppressLint("MissingPermission")
    private fun startProcessing() {
        broadcastReceiver = WiFiStatusBroadcastReceiver(viewModel)
        intentFilter = IntentFilter()
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        intentFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
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

    override fun onResume() {
        super.onResume()
        if (::broadcastReceiver.isInitialized) {
            requireActivity().registerReceiver(broadcastReceiver, intentFilter)
        }
        viewModel.setActiveLocation(isLocationEnabled(requireContext()))
    }

    override fun onPause() {
        super.onPause()
        if (::broadcastReceiver.isInitialized) {
            requireActivity().unregisterReceiver(broadcastReceiver)
        }
    }

    private fun isLocationEnabled(context: Context): Boolean {
        var locationMode = 0
        try {
            locationMode =
                Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE)
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
        }
        return locationMode != Settings.Secure.LOCATION_MODE_OFF
    }
}