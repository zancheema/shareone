package com.zancheema.share.android.shareone.receive

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zancheema.share.android.shareone.R

class ReceiveFragment : Fragment() {

    // Parameters
    private var isGroupOwner: Boolean = false
    private var groupOwnerAddress: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = ReceiveFragmentArgs.fromBundle(requireArguments())
        isGroupOwner = args.isGroupOwner
        groupOwnerAddress = args.groupOwnerAddress
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_receive, container, false)
    }
}