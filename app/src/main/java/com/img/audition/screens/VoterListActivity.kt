package com.img.audition.screens

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.snap.camerakit.internal.se
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

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

        viewBinding.backPressIC.setOnClickListener {
            onBackPressed()
        }

        viewBinding.searchET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchInList(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
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

    private fun searchInList(value: String) {
        val searchList: ArrayList<LeaderboardData> = ArrayList()
        for (ds in data) {
            if (ds.name!!.toLowerCase().contains(value.lowercase(Locale.getDefault())) || ds.auditionId!!.contains(value)) {
                searchList.add(ds)
            }
        }
        adapter!!.filterList(searchList)
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