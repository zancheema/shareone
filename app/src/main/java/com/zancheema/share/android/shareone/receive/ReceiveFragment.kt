package com.zancheema.share.android.shareone.receive

import android.content.Context
import android.net.Uri
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.zancheema.share.android.shareone.R
import com.zancheema.share.android.shareone.common.share.*
import com.zancheema.share.android.shareone.data.DefaultDataSource
import com.zancheema.share.android.shareone.databinding.FragmentReceiveBinding
import com.zancheema.share.android.shareone.home.HomeFragmentDirections
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

private const val TAG = "ReceiveFragment"

class ReceiveFragment : Fragment() {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val viewModel by viewModels<ReceiveViewModel> {
        ReceiverViewModelFactory(DefaultDataSource(requireContext().applicationContext))
    }
    private lateinit var viewDataBinding: FragmentReceiveBinding

    // Parameters
    private var isGroupOwner: Boolean = false
    private var groupOwnerAddress: String? = null

    private lateinit var serverSocket: ServerSocket
    private lateinit var connector: Connector
    private lateinit var receiver: Receiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = ReceiveFragmentArgs.fromBundle(requireArguments())
        isGroupOwner = args.isGroupOwner
        groupOwnerAddress = args.groupOwnerAddress
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewDataBinding = FragmentReceiveBinding.inflate(inflater, container, false)
        return viewDataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewDataBinding.lifecycleOwner = viewLifecycleOwner
        initialize()
        connector = ReceiverConnectorFactory().create()
        coroutineScope.launch {
            connector.connect()
        }
    }

    private fun initialize() {
        val listAdapter = LiveShareListAdapter(viewLifecycleOwner)
        viewDataBinding.rcShareList.adapter = listAdapter
        viewModel.shares.observe(viewLifecycleOwner) { shares ->
            listAdapter.submitList(shares)
        }

        requireActivity().onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                closeConnection()
                findNavController().navigate(HomeFragmentDirections.actionGlobalHomeFragment())
            }
        })
    }

    private inner class ReceiverConnectorFactory : ConnectorFactory<Connector> {
        override fun create(): Connector {
            return if (isGroupOwner) Server() else Client(groupOwnerAddress!!)
        }
    }

    private inner class Client(private val hostAdd: String) : Connector {

        private val socket = Socket()

        override suspend fun connect() {
            Log.d(TAG, "connect: client")
            socket.connect(InetSocketAddress(hostAdd, 8888), 1000)
            receiver = Receiver(socket)
            receiver.receive()
        }

        override fun closeConnection() {
            TODO("Not yet implemented")
        }
    }

    private inner class Server : Connector {

        override suspend fun connect() {
            serverSocket = ServerSocket(8888)
//                .apply { soTimeout = 1000 } // throwing poll timeout error here
            Log.d(TAG, "connect: server accepting...")
            val socket = serverSocket.accept()
            Log.d(TAG, "connect: accepted")
            receiver = Receiver(socket)
            receiver.receive()
        }

        override fun closeConnection() {
            TODO("Not yet implemented")
        }
    }

    private inner class Receiver(socket: Socket) {

        private val inputStream = socket.getInputStream()
        private val outputStream = socket.getOutputStream()

        suspend fun receive() {
            try {
                // Write Receiver name
                var name = viewModel.nickname
                var buffer = name.length.getBytes()
                outputStream.write(buffer, 0, Int.SIZE_BYTES)
                // 1. Write the sender name length
                buffer = name.toByteArray()
                outputStream.write(buffer)

                // Read sender name
                // Read name length
                buffer = ByteArray(Int.SIZE_BYTES)
                inputStream.read(buffer)
                var length = buffer.toInt()
                // Read name string
                buffer = ByteArray(length)
                inputStream.read(buffer)
                name = String(buffer)
                Log.d(TAG, "receive: name: $name")
                withContext(Dispatchers.Main) {
                    val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
                    toolbar.title = resources.getString(R.string.receiving_from, name)
                }

                // Read no. of files
                buffer = ByteArray(Int.SIZE_BYTES)
                inputStream.read(buffer)
                val noOfFiles = buffer.toInt()
                Log.d(TAG, "receive: noOfFiles: $noOfFiles")

                // Read files sequentially
                for (index in 0 until noOfFiles) {
                    // Read name of file
                    // Read name length
                    buffer = ByteArray(Int.SIZE_BYTES)
                    inputStream.read(buffer)
                    length = buffer.toInt()
                    // Read name string
                    buffer = ByteArray(length)
                    inputStream.read(buffer)
                    name = String(buffer)

                    // Read size of file
                    buffer = ByteArray(Long.SIZE_BYTES)
                    inputStream.read(buffer)
                    val size = buffer.toLong()
                    Log.d(TAG, "receive: size[$index]: $size")

                    val path = requireActivity().getMediaPath(name)
                    val f = File(path)

                    Log.d(TAG, "receive: creating dirs")
                    val dirs = File(f.parent)
                    if (!dirs.exists()) dirs.mkdirs()
                    f.createNewFile()
                    Log.d(TAG, "receive: dir created")

                    val share = LiveShare(requireContext(), name, Uri.parse(path), size)
                    withContext(Dispatchers.Main) { viewModel.addShare(share) }

                    Log.d(TAG, "receive: name[$index]: $name")
                    Log.d(TAG, "receive: size[$index]: $size")
                    val transferredBytes = copyFile(inputStream, FileOutputStream(f), size, share)
//                    if (transferredBytes == size) share.complete()
//                    share.complete()
                    Log.d(TAG, "receive: copied[$index]")
                }

                Log.d(TAG, "receive: successful")
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "receive: ", e)
            }
        }

        private suspend fun copyFile(
            inputStream: InputStream,
            outputStream: FileOutputStream,
            totalBytes: Long,
            share: LiveShare
        ): Long {
            var transferredBytes = 0L
            val buffer = ByteArray(1024)

            while (transferredBytes < totalBytes) {
                try {
                    val bytes = inputStream.read(
                        buffer,
                        0,
                        buffer.size.toLong().coerceAtMost(totalBytes - transferredBytes).toInt()
                    )
                    if (bytes == -1) break
                    outputStream.write(buffer, 0, bytes)

                    transferredBytes += bytes
                    withContext(Dispatchers.IO) { share.update(transferredBytes) }
                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.e(TAG, "copyFile: ", e)
                    return transferredBytes
                }
            }
            return transferredBytes
        }
    }

    private fun closeConnection() {
        if (!isGroupOwner && groupOwnerAddress != null) {
            val config = WifiP2pConfig()
            config.deviceAddress = groupOwnerAddress
            val manager =
                requireContext().getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
            val channel = manager.initialize(requireContext(), Looper.getMainLooper(), null)
            manager.removeGroup(channel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.d(TAG, "onSuccess: remove group")
                }

                override fun onFailure(reason: Int) {
                    Log.d(TAG, "onFailure: remove group")
                }
            })
        }
        if (::serverSocket.isInitialized) {
            try {
                serverSocket.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}