package com.zancheema.share.android.shareone.home

import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.zancheema.share.android.shareone.R
import com.zancheema.share.android.shareone.common.share.Shareable
import com.zancheema.share.android.shareone.data.DefaultDataSource
import com.zancheema.share.android.shareone.databinding.FragmentHomeBinding
import com.zancheema.share.android.shareone.util.EventObserver

private const val TAG = "HomeFragment"

class HomeFragment : Fragment() {

    private val selectFilesIntent: Intent = Intent(Intent.ACTION_GET_CONTENT).apply {
        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "*/*"
    }

    private val selectFiles =
        registerForActivityResult(StartActivityForResult()) { result ->
            if (result.data == null) return@registerForActivityResult

            if (result.data!!.data != null) {
                val uri = result.data!!.data!!
                val (name, size) = getMetadata(uri)
                val shareable = Shareable(name, size, uri)
                Log.d(TAG, "shareable: $shareable")

                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToPrepareSendFragment(
                        arrayOf(shareable)
                    )
                )
            } else if (result.data!!.clipData != null) {
                val clipData = result.data!!.clipData!!
                val shareables = Array(clipData.itemCount) { index ->
                    val uri = clipData.getItemAt(index).uri
                    val (name, size) = getMetadata(uri)
                    Shareable(name, size, uri)
                }
                for (index in shareables.indices) Log.d(
                    TAG,
                    "shareables[$index]: ${shareables[index]}"
                )
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToPrepareSendFragment(
                        shareables
                    )
                )
            }
        }

    private fun getMetadata(uri: Uri): Pair<String, Long> {

        // The query, because it only applies to a single document, returns only
        // one row. There's no need to filter, sort, or select fields,
        // because we want all fields for one document.
        val cursor: Cursor? = requireContext().contentResolver.query(
            uri, null, null, null, null, null
        )

        cursor?.use {
            // moveToFirst() returns false if the cursor has 0 rows. Very handy for
            // "if there's anything to look at, look at it" conditionals.
            if (it.moveToFirst()) {

                // Note it's called "Display Name". This is
                // provider-specific, and might not necessarily be the file name.
                val displayName: String =
                    it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))

                val sizeIndex: Int = it.getColumnIndex(OpenableColumns.SIZE)
                // If the size is unknown, the value stored is null. But because an
                // int can't be null, the behavior is implementation-specific,
                // and unpredictable. So as
                // a rule, check if it's null before assigning to an int. This will
                // happen often: The storage API allows for remote files, whose
                // size might not be locally known.
                val size: String = if (!it.isNull(sizeIndex)) {
                    // Technically the column stores an int, but cursor.getString()
                    // will do the conversion automatically.
                    it.getString(sizeIndex)
                } else {
                    "Unknown"
                }
                return Pair(displayName, size.toLong())
            }
        }
        return Pair("Unknown", 0L)
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
            if (it) launchFilePicker()
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