package com.img.audition.screens.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import com.img.audition.adapters.ContestLiveAdapter
import com.img.audition.dataModel.GetLiveContestDataResponse
import com.img.audition.databinding.FragmentLiveContestBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.screens.LoginActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LiveContestFragment : Fragment() {

    val TAG = "LiveContestFragment"
    private lateinit var _viewBinding : FragmentLiveContestBinding
    private val view get() = _viewBinding
    private val sessionManager by lazy {
        SessionManager(requireContext())
    }

    private val myApplication by lazy {
        MyApplication(requireContext())
    }
    private val apiInterface by lazy{
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _viewBinding = FragmentLiveContestBinding.inflate(inflater,container,false)

        showLiveContest()
        return view.root
    }

    private fun showLiveContest() {
        val liveContestReq = apiInterface.getAllLiveContest(sessionManager.getToken())

        liveContestReq.enqueue( object : Callback<GetLiveContestDataResponse>{
            override fun onResponse(call: Call<GetLiveContestDataResponse>, response: Response<GetLiveContestDataResponse>) {
                if (response.isSuccessful && response.body()!!.success!! && response.body()!=null){
                    val contestData = response.body()!!.data
                    if (contestData.size>0){
                        view.noLiveContest.visibility = View.GONE
                           val contestAdapter = ContestLiveAdapter(requireContext(),contestData)
                            view.contestViewpager2.adapter = contestAdapter
                    }else{
                        myApplication.printLogD(" No Live Contest",TAG)
                        view.noLiveContest.visibility = View.VISIBLE
                    }
                }else{
                    myApplication.printLogE(response.toString(),TAG)
                    view.noLiveContest.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<GetLiveContestDataResponse>, t: Throwable) {
                myApplication.printLogE(t.toString(),TAG)
                view.noLiveContest.visibility = View.VISIBLE
            }

        })
    }



}