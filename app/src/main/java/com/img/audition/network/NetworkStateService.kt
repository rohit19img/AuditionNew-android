package com.img.audition.network

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.registerReceiver


class NetworkStateService : Service() {

    private val networkStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            val isInternetAvailable = networkInfo != null && networkInfo.isConnected

           if (isInternetAvailable){
               Log.d("internet", "onReceive: Internet Available")

           }else{
               Log.d("internet", "onReceive: Internet Unavailable")
           }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkStateReceiver, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(networkStateReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}

