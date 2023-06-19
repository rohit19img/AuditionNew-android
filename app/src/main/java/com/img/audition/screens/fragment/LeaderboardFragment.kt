package com.img.audition.screens.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.media3.common.util.UnstableApi
import cn.pedant.SweetAlert.SweetAlertDialog
import com.img.audition.adapters.LeaderboardAdapter
import com.img.audition.dataModel.LeaderboardData
import com.img.audition.dataModel.LeaderboardDataResponse
import com.img.audition.dataModel.LiveRanksLeaderboardResponse
import com.img.audition.databinding.FragmentLeaderboardBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList


class LeaderboardFragment(val contestID: String, val contestStatus: String) : Fragment() {

    private  var adapter: LeaderboardAdapter? = null
    private lateinit var data: ArrayList<LeaderboardData>
    private val TAG = "LeaderboardFragment"
    private lateinit var _viewBinding : FragmentLeaderboardBinding
    private val view get() = _viewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _viewBinding = FragmentLeaderboardBinding.inflate(inflater,container,false)

        return _viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (MyApplication(view.context).isNetworkConnected()){
            loadView()
        }else{
            checkInternetDialog(view.context)
        }
    }
    private fun loadView() {
        if (MyApplication(requireContext()).isNetworkConnected()){
            if (contestStatus.equals("notstarted")){
                leaderboardapi(requireContext())
            }else{
                liveRankLeaderboardApi(requireContext())
            }
        }else{
            checkInternetDialog(requireContext())
        }
    }

    private fun leaderboardapi(context: Context) {
        val apiInterface = RetrofitClient.getInstance().create(ApiInterface::class.java)
        val leaderboardapiReq = apiInterface.getLeaderboardDetails(SessionManager(context).getToken(),contestID)

        leaderboardapiReq.enqueue(@UnstableApi object : Callback<LeaderboardDataResponse>{
            override fun onResponse(call: Call<LeaderboardDataResponse>, response: Response<LeaderboardDataResponse>) {
                if (response.isSuccessful && response.body()!!.success!!){
                    data = response.body()!!.data
                    if (data.size>0){
                        view.leaderboard.visibility = View.VISIBLE
                        view.noDataView.visibility = View.GONE
                        adapter = LeaderboardAdapter(context,data,contestID)
                        view.leaderboard.adapter = adapter
                    }else{
                        view.leaderboard.visibility = View.GONE
                        view.noDataView.visibility = View.VISIBLE
                    }
                }else{
                    view.leaderboard.visibility = View.GONE
                    view.noDataView.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<LeaderboardDataResponse>, t: Throwable) {
                Log.e(TAG, "onFailure: $t")
                view.leaderboard.visibility = View.GONE
                view.noDataView.visibility = View.VISIBLE
            }
        })
    }


    private fun liveRankLeaderboardApi(context: Context) {
        val apiInterface = RetrofitClient.getInstance().create(ApiInterface::class.java)

        val leaderboardapiReq = apiInterface.getLiveRanksLeaderboardDetails(SessionManager(context).getToken(),contestID)

        leaderboardapiReq.enqueue(@UnstableApi object : Callback<LiveRanksLeaderboardResponse>{
            override fun onResponse(call: Call<LiveRanksLeaderboardResponse>, response: Response<LiveRanksLeaderboardResponse>) {
                if (response.isSuccessful && response.body()!!.success!!){
                    val data = response.body()!!.data!!.jointeams
                    if (data.size>0){
                        view.leaderboard.visibility = View.VISIBLE
                        view.noDataView.visibility = View.GONE
                        val adapter = LiveRankLeaderboardAdapter(context,data,contestID)
                        view.leaderboard.adapter = adapter
                    }else{
                        view.leaderboard.visibility = View.GONE
                        view.noDataView.visibility = View.VISIBLE
                    }

                }else{
                    view.leaderboard.visibility = View.GONE
                    view.noDataView.visibility = View.VISIBLE
                    Log.e(TAG, "onResponse33: $response")
                }
            }

            override fun onFailure(call: Call<LiveRanksLeaderboardResponse>, t: Throwable) {
                Log.e(TAG, "onFailure: $t")
                view.leaderboard.visibility = View.GONE
                view.noDataView.visibility = View.VISIBLE
            }
        })
    }
    override fun onDestroyView() {
        Log.d("check 400", "onDestroyView: $TAG")
        try {
            adapter = null
            data.clear()
        }catch (e:Exception){
            e.printStackTrace()
        }
        getView()?.destroyDrawingCache()
        super.onDestroyView()
    }

    private fun checkInternetDialog(context: Context) {
        val sweetAlertDialog = SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
        sweetAlertDialog.titleText = "Internet"
        sweetAlertDialog.contentText = ConstValFile.Check_Connection
        sweetAlertDialog.confirmText = "Retry"
        sweetAlertDialog.setConfirmClickListener {
            sweetAlertDialog.dismiss()
            loadView()
        }
        sweetAlertDialog.show()
    }
}