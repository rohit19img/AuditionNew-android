package com.img.audition.globalAccess

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import cn.pedant.SweetAlert.SweetAlertDialog
import com.cashfree.pg.ui.CFNonWebBaseActivity.onBackPressed
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
        val toast =  Toast.makeText(context.applicationContext,message,Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }

    fun printLogD(logMessage: String,TAG:String){
        Log.d(TAG, "printLogD: ${logMessage}")
    }

    fun printLogE(logMessage: String,TAG:String){
        Log.e(TAG, "printLogE: ${logMessage}")
    }

    fun printLogI(logMessage: String,TAG:String){
        Log.i(TAG, "printLogI: ${logMessage}")
    }

     fun onGPS() {

         val sweetAlertDialog = SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
         sweetAlertDialog.titleText = "Enable GPS"
         sweetAlertDialog.contentText = "Please Enable GPS"
         sweetAlertDialog.confirmText = "₹ Yes"
         sweetAlertDialog.setConfirmClickListener {
             sweetAlertDialog.dismiss()
             try {
                 context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
             }catch (e:java.lang.Exception){
                 Log.e("myApplication", "onGPS: ${e.message}", )
             }

         }
         sweetAlertDialog.cancelText = "No"
         sweetAlertDialog.setCancelClickListener {
             sweetAlertDialog.dismiss()
         }
         sweetAlertDialog.show()
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