package com.zancheema.share.android.shareone.send

import android.content.Context
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.zancheema.share.android.shareone.R
import com.zancheema.share.android.shareone.common.share.Shareable
import com.zancheema.share.android.shareone.home.HomeFragmentDirections
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.ByteBuffer

private const val TAG = "SendFragment"

class SendFragment : Fragment() {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

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
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_send, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initialize()
        connector = ConnectorFactory().create()
        coroutineScope.launch {
            connector.connect()
        }
    }

    private fun initialize() {
        requireActivity().onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                closeConnection()
                findNavController().navigate(HomeFragmentDirections.actionGlobalHomeFragment())
            }
        })
    }

    private inner class ConnectorFactory {
        fun create(): Connector {
            return if (isGroupOwner) Server() else Client(groupOwnerAddress!!)
        }
    }

    private inner class Client(private val hostAdd: String) : Connector {

        private val socket = Socket()

        override suspend fun connect() {
            Log.d(TAG, "connect: client")
            socket.connect(InetSocketAddress(hostAdd, 8888), 1000)
            sender = Sender(socket, shareables)
        }

        override fun disconnect() {
            TODO("Not yet implemented")
        }
    }

    private inner class Server : Connector {

        override suspend fun connect() {
            serverSocket = ServerSocket(8888)
                .apply { soTimeout = 1000 } // throwing poll timeout error here
            Log.d(TAG, "connect: server accepting...")
            val socket = serverSocket.accept()
            Log.d(TAG, "connect: accepted")
            sender = Sender(socket, shareables)
        }

        override fun disconnect() {
            TODO("Not yet implemented")
        }
    }

    private interface Connector {
        suspend fun connect()

        fun disconnect()
    }

    private inner class Sender(
        socket: Socket,
        private val shareables: Array<Shareable>
    ) {

        private val outputStream = socket.getOutputStream()

        fun send() {
            try {
                // Write the sender name
//                val senderName =
                // 1. Write the sender name length


            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "send: ", e)
            }
        }
    }

    private fun getBytes(num: Int): ByteArray {
        val buffer = ByteBuffer.allocate(4) // int is 4 bytes
        buffer.putInt(num)
        return buffer.array()
    }

    private fun getBytes(num: Long): ByteArray {
        val buffer = ByteBuffer.allocate(8) // long is 8 bytes
        buffer.putLong(num)
        return buffer.array()
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