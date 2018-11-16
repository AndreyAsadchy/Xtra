package com.github.exact7.xtra.ui.common

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.exact7.xtra.R

abstract class BaseNetworkFragment : Fragment() {

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            isNetworkAvailable = intent?.let {
                it.getParcelableExtra<NetworkInfo>("networkInfo").state == NetworkInfo.State.CONNECTED
            } == true
            if (isNetworkAvailable) {
                if (!isInitialized) {
                    initialize()
                    isInitialized = true
                } else {
                    onNetworkRestored()
                }
            }
        }
    }
    var isNetworkAvailable = false
        private set
    private var isInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = requireActivity()
        context.registerReceiver(receiver, IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"))
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        isNetworkAvailable = connectivityManager.activeNetworkInfo?.isConnected == true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return if (isNetworkAvailable) {
            createView(inflater, container, savedInstanceState)
        } else {
            inflater.inflate(R.layout.view_offline, container, false)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (isNetworkAvailable) {
            initialize()
            isInitialized = true
        }
    }

    override fun onDestroy() {
        requireActivity().unregisterReceiver(receiver)
        super.onDestroy()
    }

    abstract fun createView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    abstract fun initialize()
    abstract fun onNetworkRestored()
}