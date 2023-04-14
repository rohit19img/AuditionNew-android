package com.img.audition.screens.fragment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.img.audition.R
import com.img.audition.dataModel.NotificationDataResponse
import com.img.audition.databinding.ActivityEditProfileBinding
import com.img.audition.databinding.ActivityNotificationBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationActivity : AppCompatActivity() {

    val TAG = "NotificationActivity"
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityNotificationBinding.inflate(layoutInflater)
    }
    private val sessionManager by lazy {
        SessionManager(this@NotificationActivity)
    }

    private val myApplication by lazy {
        MyApplication(this@NotificationActivity)
    }
    private val apiInterface by lazy{
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewBinding.backPressIC.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onStart() {
        super.onStart()
        getNotification()
    }

    private fun getNotification() {
        val notificationReq = apiInterface.getNotification(sessionManager.getToken())

        notificationReq.enqueue(object : Callback<NotificationDataResponse> {
            override fun onResponse(all: Call<NotificationDataResponse>, response: Response<NotificationDataResponse>) {
                if (response.isSuccessful && response.body()!!.success!!){
                    val data = response.body()!!.data
                    if (data.size>0){
                        viewBinding.noNotificationView.visibility = View.GONE
                        viewBinding.notiCycle.visibility = View.VISIBLE
                        val adapter = NotificationAdapter(this@NotificationActivity,data)
                        viewBinding.notiCycle.adapter = adapter
                    }else{
                        viewBinding.noNotificationView.visibility = View.VISIBLE
                        myApplication.printLogD("No Notification",TAG);
                    }
                }else{
                    myApplication.printLogE(response.toString(),TAG);
                }
            }
            override fun onFailure(call: Call<NotificationDataResponse>, t: Throwable) {
                myApplication.printLogE(t.toString(),TAG);
            }

        })
    }

}
