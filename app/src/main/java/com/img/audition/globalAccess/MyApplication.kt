package com.img.audition.globalAccess

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.provider.Settings
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import java.net.URL

class MyApplication(val context: Context) : Application(), LifecycleObserver {


    fun isInForeground(): Boolean {
        return ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
    }


     fun isNetworkConnected(): Boolean {
        val cm = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
    }

    fun showToast(message:String){
        Toast.makeText(context.applicationContext,message,Toast.LENGTH_SHORT).show()
    }

    fun printLogD(logMessage: String,TAG:String){
        Log.d(TAG, "printLogD: ${logMessage}")
    }

    fun printLogE(logMessage: String,TAG:String){
        Log.d(TAG, "printLogD: ${logMessage}")
    }

    fun printLogI(logMessage: String,TAG:String){
        Log.d(TAG, "printLogD: ${logMessage}")
    }

     fun onGPS() {
        val builder = AlertDialog.Builder(context.applicationContext)
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton(
            "Yes"
        ) { dialog, which -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
            .setNegativeButton(
                "No"
            ) { dialog, which -> dialog.cancel() }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    fun getAndroidFolder(): String? {
        val m = packageManager
        var packageLocation = packageName
        try {
            val p: PackageInfo = m.getPackageInfo(packageLocation, 0)
            packageLocation = p.applicationInfo.dataDir
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w("check", "Error Package name not found ", e)
        }
        return packageLocation
    }

    class DownloadImageTask(bmImage: ImageView) :
        AsyncTask<String?, Void?, Bitmap?>() {
        @SuppressLint("StaticFieldLeak")
        var bmImage: ImageView

        init {
            this.bmImage = bmImage
        }
        @Deprecated("Deprecated in Java")
        override fun onPostExecute(result: Bitmap?) {
            bmImage.setImageBitmap(result)
        }

        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg params: String?): Bitmap? {
            val urldisplay: String = params[0].toString()
            var mIcon11: Bitmap? = null
            try {
                val `in` = URL(urldisplay).openStream()
                mIcon11 = BitmapFactory.decodeStream(`in`)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return mIcon11
        }
    }

}