package com.zancheema.share.android.shareone.send

import android.content.Context
import android.content.Intent
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.zancheema.share.android.shareone.R
import com.zancheema.share.android.shareone.common.share.*
import com.zancheema.share.android.shareone.data.DefaultDataSource
import com.zancheema.share.android.shareone.databinding.FragmentSendBinding
import com.zancheema.share.android.shareone.home.HomeFragmentDirections
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException

private const val TAG = "SendFragment"

class SendFragment : Fragment() {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val viewModel by viewModels<SendViewModel> {
        SendViewModelFactory(DefaultDataSource(requireContext().applicationContext))
    }
    private lateinit var viewDataBinding: FragmentSendBinding
    private lateinit var toolbar: Toolbar
    private lateinit var fabOpenReceived: FloatingActionButton

    // Parameters
    private var isGroupOwner: Boolean = false
    private var groupOwnerAddress: String? = null
    private lateinit var shareables: Array<Shareable>

    private lateinit var serverSocket: ServerSocket
    private lateinit var connector: Connector
    private lateinit var sender: Sender

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = SendFragmentArgs.fromBundle(requireArguments())
        isGroupOwner = args.isGroupOwner
        groupOwnerAddress = args.groupOwnerAddress
        shareables = args.shareables
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewDataBinding = FragmentSendBinding.inflate(inflater, container, false)
        return viewDataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewDataBinding.lifecycleOwner = viewLifecycleOwner
        initialize()
        connector = SenderConnectorFactory().create()
        coroutineScope.launch {
            connector.connect()
        }
    }

    override fun onDestroy() {
        connector.closeConnection()
        super.onDestroy()
    }

    private fun initialize() {
        toolbar = requireActivity().findViewById(R.id.toolbar)
        fabOpenReceived = requireActivity().findViewById(R.id.fabOpenReceived)
//        fabOpenReceived.slideDown()

        val listAdapter = LiveShareListAdapter(viewLifecycleOwner)
        viewDataBinding.rcShareList.adapter = listAdapter
        viewModel.shares.observe(viewLifecycleOwner) { shares ->
            listAdapter.submitList(shares)
        }

        requireActivity().onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (listOf(Intent.ACTION_SEND, Intent.ACTION_SEND_MULTIPLE).contains(requireActivity().intent.action)) {
                    requireActivity().finish()
                } else {
                    findNavController().navigate(HomeFragmentDirections.actionGlobalHomeFragment())
                    remove()
                }
            }
        })
    }

    private inner class SenderConnectorFactory : ConnectorFactory<Connector> {
        override fun create(): Connector {
            return if (isGroupOwner) Server() else Client(groupOwnerAddress!!)
        }
    }

    private inner class Client(private val hostAdd: String) : Connector {

        private val socket = Socket()

        override suspend fun connect() {
            Log.d(TAG, "connect: client")
            socket.connect(InetSocketAddress(hostAdd, 5000), 1000)
            sender = Sender(socket, shareables)
            sender.send()
        }

        override fun closeConnection() {
            this@SendFragment.closeConnection()
        }
    }

    private inner class Server : Connector {

        override suspend fun connect() {
            serverSocket = ServerSocket(5000)
//                .apply { soTimeout = 1000 } // throwing poll timeout error here
            Log.d(TAG, "connect: server accepting...")
            val socket = serverSocket.accept()
            Log.d(TAG, "connect: accepted")
            sender = Sender(socket, shareables)
            sender.send()
        }

        override fun closeConnection() {
            this@SendFragment.closeConnection()
        }
    }

    private inner class Sender(
        socket: Socket,
        private val shareables: Array<Shareable>
    ) {

        private val outputStream = socket.getOutputStream()
        private val inputStream = socket.getInputStream()

        suspend fun send() {
            try {
                // Read Receiver name
                // Read name length
                var buffer = ByteArray(Int.SIZE_BYTES)
                inputStream.read(buffer)
                val length = buffer.toInt()
                // Read name string
                buffer = ByteArray(length)
                inputStream.read(buffer)
                var name = String(buffer)
                Log.d(TAG, "receive: name: $name")
                withContext(Dispatchers.Main) {
                    val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
                    toolbar.title = resources.getString(R.string.sending_to, name)
                }

                // Write the sender name
                // 1. Write character length
                name = viewModel.nickname
                buffer = name.length.getBytes()
                outputStream.write(buffer, 0, Int.SIZE_BYTES)
                // 1. Write name
                buffer = name.toByteArray()
                outputStream.write(buffer)

                // Write no. of files
                val noOfFiles = shareables.size.getBytes()
                outputStream.write(noOfFiles, 0, Int.SIZE_BYTES)

                // Write all files sequentially
                for (shareable in shareables) {
                    // Write name of shareable
                    // 1. Write size of name
                    name = shareable.name
                    buffer = name.length.getBytes()
                    outputStream.write(buffer, 0, Int.SIZE_BYTES)
                    // 2. Write name
                    buffer = name.toByteArray()
                    outputStream.write(buffer)

                    // Write size of file
                    buffer = shareable.size.getBytes()
                    outputStream.write(buffer)

                    // Write the shareable
                    val liveShare =
                        LiveShare(requireContext(), shareable.name, shareable.uri, shareable.size)
                    withContext(Dispatchers.Main) { viewModel.addShare(liveShare) }
                    buffer = ByteArray(1024)
                    val contentResolver = requireContext().contentResolver
                    val contentInputStream = contentResolver.openInputStream(liveShare.uri)
                    var bytes = 0
                    var transferredBytes = 0L
                    while ((contentInputStream?.read(buffer)!!.also { bytes = it }) != -1) {
                        outputStream.write(buffer, 0, bytes)

                        // Update Live Share
                        transferredBytes += bytes
                        withContext(Dispatchers.IO) { liveShare.update(transferredBytes) }
                    }
                    Log.d(
                        TAG,
                        "receive: [total, transferred]: [${shareable.size}, $transferredBytes]"
                    )
                }
                // Notify sender that transfer is complete
                withContext(Dispatchers.Main) {
                    toolbar.setTitle(R.string.complete)
//                    fabOpenReceived.slideUp()
                }

                Log.d(TAG, "send: successful")
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "send: ", e)
                withContext(Dispatchers.Main) {
                    toolbar.setTitle(R.string.cancelled)
                }
            } catch (e: SocketException) {
                e.printStackTrace()
                Log.e(TAG, "send: ", e)
                withContext(Dispatchers.Main) {
                    toolbar.setTitle(R.string.error)
                }
            }
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