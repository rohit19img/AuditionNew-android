package com.img.audition.screens

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.img.audition.adapters.LeaderboardAdapter
import com.img.audition.adapters.VoterListAdapter
import com.img.audition.dataModel.LeaderboardDataResponse
import com.img.audition.databinding.ActivityVotterListBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VoterListActivity : AppCompatActivity() {

    val TAG = "VoterListActivity"
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityVotterListBinding.inflate(layoutInflater)
    }
    private val sessionManager by lazy {
        SessionManager(this@VoterListActivity)
    }

    private val myApplication by lazy {
        MyApplication(this@VoterListActivity)
    }
    private val apiInterface by lazy{
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }

    private val bundle by lazy {
        intent.getBundleExtra(ConstValFile.Bundle)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
    }

    override fun onStart() {
        super.onStart()
        val videoID = bundle!!.getString(ConstValFile.VideoID)
        getVoterList(videoID!!)
    }

    private fun getVoterList(videoID:String) {
        val voterListReq = apiInterface.getVoterList(sessionManager.getToken(),videoID)

        voterListReq.enqueue(object : Callback<LeaderboardDataResponse> {
            override fun onResponse(call: Call<LeaderboardDataResponse>, response: Response<LeaderboardDataResponse>) {
                if (response.isSuccessful && response.body()?.success!!){
                    try {
                        val data = response.body()!!.data
                        val adapter = VoterListAdapter(this@VoterListActivity,data)
                        viewBinding.voterListCycle.adapter = adapter
                    }catch (e:java.lang.Exception){
                        Log.e(TAG, "onResponse: $e")
                    }

                }else{
                    Log.e(TAG, "onResponse: $response")
                }
            }

            override fun onFailure(call: Call<LeaderboardDataResponse>, t: Throwable) {
                Log.e(TAG, "onFailure: $t")
            }

        })
    }
}