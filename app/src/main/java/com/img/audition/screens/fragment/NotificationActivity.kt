package com.img.audition.screens.fragment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.img.audition.adapters.NotificationAdapter
import com.img.audition.dataModel.NotificationData
import com.img.audition.dataModel.NotificationDataResponse
import com.img.audition.databinding.ActivityNotificationBinding
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class NotificationActivity : AppCompatActivity() {

    private  var adapter: NotificationAdapter? = null
    private lateinit var data: ArrayList<NotificationData>
    private val TAG = "NotificationActivity"
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityNotificationBinding.inflate(layoutInflater)
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
        val apiInterface = RetrofitClient.getInstance().create(ApiInterface::class.java)

        val notificationReq = apiInterface.getNotification(SessionManager(this@NotificationActivity).getToken())

        notificationReq.enqueue(object : Callback<NotificationDataResponse> {
            override fun onResponse(all: Call<NotificationDataResponse>, response: Response<NotificationDataResponse>) {
                if (response.isSuccessful && response.body()!!.success!!){
                    data = response.body()!!.data
                    if (data.size>0){
                        viewBinding.noNotificationView.visibility = View.GONE
                        viewBinding.notiCycle.visibility = View.VISIBLE
                        adapter = NotificationAdapter(this@NotificationActivity,data)
                        viewBinding.notiCycle.adapter = adapter
                    }else{
                        viewBinding.noNotificationView.visibility = View.VISIBLE
                    }
                }else{
                    Log.e(TAG, response.toString())
                }
            }
            override fun onFailure(call: Call<NotificationDataResponse>, t: Throwable) {
                t.printStackTrace()
            }

        })
    }

    override fun onStop() {
        try {
            adapter = null
            data.clear()
        }catch (e:Exception){
            e.printStackTrace()
        }

        super.onStop()
    }
}
