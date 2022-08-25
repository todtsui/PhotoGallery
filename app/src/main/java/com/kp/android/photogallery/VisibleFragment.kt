package com.kp.android.photogallery

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment

private const val TAG = "VisibleFragment"

abstract class VisibleFragment : Fragment() {

    private val onShowNotification = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent) {
//            Toast.makeText(requireContext(),
//                    "Got a broadcast: ${intent.action}",
//                    Toast.LENGTH_LONG)
//                    .show()
            Log.i(TAG, "canceling notification")
            resultCode = Activity.RESULT_CANCELED

        }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(PollWorker.ACTION_SHOW_NOTIFICATION)
       //在onStart()函数里登记
        requireActivity().registerReceiver(
            onShowNotification,
            filter,
            PollWorker.PERM_PRIVATE,
            null
        )
    }

    override fun onStop() {
        super.onStop()
        //在onStop()函数里撤销登记
        requireActivity().unregisterReceiver(onShowNotification)
    }
}