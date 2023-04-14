package com.img.audition.screens.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.img.audition.R
import com.img.audition.adapters.ContestLiveAdapter
import com.img.audition.dataModel.GetJoinedContestDataResponse
import com.img.audition.dataModel.GetLiveContestDataResponse
import com.img.audition.databinding.FragmentJoinedContestBinding
import com.img.audition.databinding.FragmentLiveContestBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class JoinedContestFragment : Fragment() {

    val TAG = "JoinedContestFragment"
    private lateinit var _viewBinding : FragmentJoinedContestBinding
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _viewBinding = FragmentJoinedContestBinding.inflate(inflater,container,false)

        view.contestViewpager2.offscreenPageLimit = 1
        showJoinedContest()
        return view.root
    }


    private fun showJoinedContest() {
        val liveContestReq = apiInterface.getJoinedContest(sessionManager.getToken())

        liveContestReq.enqueue( object : Callback<GetJoinedContestDataResponse> {
            override fun onResponse(call: Call<GetJoinedContestDataResponse>, response: Response<GetJoinedContestDataResponse>) {
                if (response.isSuccessful && response.body()!!.success!! && response.body()!=null){
                    val contestData = response.body()!!.data
                    if (contestData.size>0){
                        view.noJoinedContest.visibility = View.GONE
                        val contestAdapter = ContestJoinedAdapter(requireContext(),contestData)
                        view.contestViewpager2.adapter = contestAdapter
                    }else{
                        myApplication.printLogD(" No Live Contest",TAG)
                        view.noJoinedContest.visibility = View.VISIBLE
                    }
                }else{
                    myApplication.printLogE(response.toString(),TAG)
                    view.noJoinedContest.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<GetJoinedContestDataResponse>, t: Throwable) {
                myApplication.printLogE(t.toString(),TAG)
                view.noJoinedContest.visibility = View.VISIBLE
            }

        })
    }

}