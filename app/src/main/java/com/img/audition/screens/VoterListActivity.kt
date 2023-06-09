package com.img.audition.screens

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.img.audition.adapters.VoterListAdapter
import com.img.audition.dataModel.LeaderboardData
import com.img.audition.dataModel.LeaderboardDataResponse
import com.img.audition.databinding.ActivityVotterListBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class VoterListActivity : AppCompatActivity() {

    private var adapter: VoterListAdapter? = null
    private lateinit var data: ArrayList<LeaderboardData>
    private val TAG = "VoterListActivity"
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityVotterListBinding.inflate(layoutInflater)
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
        val apiInterface = RetrofitClient.getInstance().create(ApiInterface::class.java)

        val voterListReq = apiInterface.getVoterList(SessionManager(this@VoterListActivity).getToken(),videoID)

        voterListReq.enqueue(object : Callback<LeaderboardDataResponse> {
            override fun onResponse(call: Call<LeaderboardDataResponse>, response: Response<LeaderboardDataResponse>) {
                if (response.isSuccessful && response.body()?.success!!){
                    try {
                        data = response.body()!!.data
                        if (data.size>0){
                            viewBinding.voterListCycle.visibility = View.VISIBLE
                            viewBinding.noDataView.visibility = View.GONE
                            adapter = VoterListAdapter(this@VoterListActivity,data)
                            viewBinding.voterListCycle.adapter = adapter
                        }else{
                            viewBinding.voterListCycle.visibility = View.GONE
                            viewBinding.noDataView.visibility = View.VISIBLE
                        }
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