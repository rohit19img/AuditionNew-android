package com.img.audition.screens

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.img.audition.adapters.BlockedUserAdapter
import com.img.audition.dataModel.BlockedUserResponse
import com.img.audition.databinding.ActivityBlockedUsersBinding
import com.img.audition.databinding.ActivityWithdrawBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BlockedUsersActivity : AppCompatActivity() {

    val TAG = "BlockedUsersActivity"
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityBlockedUsersBinding.inflate(layoutInflater)
    }
    private val sessionManager by lazy {
        SessionManager(this@BlockedUsersActivity)
    }

    private val myApplication by lazy {
        MyApplication(this@BlockedUsersActivity)
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

        getBlockedUsers()


    }



    private fun getBlockedUsers() {
        val blockedUsersReq = apiInterface.getBlockedUser(sessionManager.getToken())

        blockedUsersReq.enqueue(object : Callback<BlockedUserResponse> {
            override fun onResponse(call: Call<BlockedUserResponse>, response: Response<BlockedUserResponse>) {
                if (response.isSuccessful && response.body()!!.success!! && response.body()!=null){
                    val data = response.body()!!.data
                    if (data.size>0){
                        viewBinding.noBlockedView.visibility = View.GONE
                        viewBinding.blockedUserCycle.visibility = View.VISIBLE
                        val adapter = BlockedUserAdapter(this@BlockedUsersActivity,data)
                        viewBinding.blockedUserCycle.adapter = adapter
                    }else{
                        viewBinding.noBlockedView.visibility = View.VISIBLE
                        myApplication.printLogD("No Data",TAG)
                    }
                }else{
                    myApplication.printLogE(response.toString(),TAG)
                }
            }
            override fun onFailure(call: Call<BlockedUserResponse>, t: Throwable) {
                myApplication.printLogE(t.toString(),TAG)
            }

        })
    }
}