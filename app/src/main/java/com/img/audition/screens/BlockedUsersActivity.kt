package com.img.audition.screens

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.img.audition.adapters.BlockedUserAdapter
import com.img.audition.dataModel.BlockedUserData
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
import java.util.ArrayList

class BlockedUsersActivity : AppCompatActivity() {

    private var adapter: BlockedUserAdapter? = null
    private lateinit var data: ArrayList<BlockedUserData>
    val TAG = "BlockedUsersActivity"
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityBlockedUsersBinding.inflate(layoutInflater)
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
        val apiInterface =  RetrofitClient.getInstance().create(ApiInterface::class.java)
        val blockedUsersReq = apiInterface.getBlockedUser(SessionManager(this).getToken())

        blockedUsersReq.enqueue(object : Callback<BlockedUserResponse> {
            override fun onResponse(call: Call<BlockedUserResponse>, response: Response<BlockedUserResponse>) {
                if (response.isSuccessful && response.body()!!.success!! && response.body()!=null){
                     data = response.body()!!.data
                    if (data.size>0){
                        viewBinding.noBlockedView.visibility = View.GONE
                        viewBinding.blockedUserCycle.visibility = View.VISIBLE
                         adapter = BlockedUserAdapter(this@BlockedUsersActivity,data)
                        viewBinding.blockedUserCycle.adapter = adapter
                    }else{
                        viewBinding.noBlockedView.visibility = View.VISIBLE
                    }
                }else{
                    Log.e(TAG, "onResponse: ${response.toString()}")
                }
            }
            override fun onFailure(call: Call<BlockedUserResponse>, t: Throwable) {
                Log.e(TAG, "onResponse: ${t.toString()}")

            }

        })
    }

    override fun onStop() {
        try {
            data.clear()
            adapter = null
        }catch (e:Exception){
            e.printStackTrace()
        }

        super.onStop()
    }
}