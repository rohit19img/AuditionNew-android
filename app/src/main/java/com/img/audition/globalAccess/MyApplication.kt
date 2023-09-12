package com.img.audition.globalAccess

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.lifecycle.LifecycleObserver
import cn.pedant.SweetAlert.SweetAlertDialog


class MyApplication(val context: Context) : Application(), LifecycleObserver {


    fun isNetworkConnected(): Boolean {
        val cm = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
    }

    fun showToast(message: String) {
        val toast = Toast.makeText(context.applicationContext, message, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }

    fun printLogD(logMessage: String, TAG: String) {
        Log.d(TAG, "printLogD: ${logMessage}")
    }

    fun printLogE(logMessage: String, TAG: String) {
        Log.e(TAG, "printLogE: ${logMessage}")
    }

    fun printLogI(logMessage: String, TAG: String) {
        Log.i(TAG, "printLogI: ${logMessage}")
    }

    fun onGPS() {
        val sweetAlertDialog = SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
        sweetAlertDialog.titleText = "Enable GPS"
        sweetAlertDialog.contentText = "Please Enable GPS"
        sweetAlertDialog.confirmText = "Yes"
        sweetAlertDialog.setConfirmClickListener {
            sweetAlertDialog.dismiss()
            try {
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            } catch (e: java.lang.Exception) {
                Log.e("myApplication", "onGPS: ${e.message}")
            }

        }
        sweetAlertDialog.cancelText = "No"
        sweetAlertDialog.setCancelClickListener {
            sweetAlertDialog.dismiss()
        }
        sweetAlertDialog.show()
    }
}