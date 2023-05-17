package com.img.audition.screens.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.media3.common.util.UnstableApi
import com.google.android.material.tabs.TabLayout
import com.img.audition.adapters.ContestLiveAdapter
import com.img.audition.dataModel.GetLiveContestDataResponse
import com.img.audition.dataModel.LiveContestData
import com.img.audition.databinding.FragmentContestBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


@UnstableApi
class ContestFragment(val contextFromHome : Context) : Fragment() {
    val TAG = "ContestFragment"
    private val sessionManager by lazy {
        SessionManager(requireContext())
    }
    private val myApplication by lazy {
        MyApplication(contextFromHome)
    }

    var list_upcoming = ArrayList<LiveContestData>()
    var list_live  = ArrayList<LiveContestData>()
    var list_completed = ArrayList<LiveContestData>()

    private val apiInterface by lazy{
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }

    lateinit var tabLayout: TabLayout
    lateinit var viewContainer: FrameLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view= FragmentContestBinding.inflate(inflater,container,false)
        tabLayout = view.tabLayout
        viewContainer = view.viewContainer


        return view.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getAllContest()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab!!.position){
                    0 ->{
                        val upcoming = UpComingContestFragment()
                        val bundle = Bundle()
                        bundle.putSerializable(ConstValFile.UpContestComingList,list_upcoming)
                        upcoming.arguments = bundle
                        loadFragment(upcoming)
                    }
                    1 ->{
                        val upcoming = LiveContestFragment()
                        val bundle = Bundle()
                        bundle.putSerializable(ConstValFile.LiveContestList,list_live)
                        upcoming.arguments = bundle
                        loadFragment(upcoming)

                    }else ->{
                    val upcoming = CompletedContestFragment()
                    val bundle = Bundle()
                    bundle.putSerializable(ConstValFile.CompletedContestList,list_completed)
                    upcoming.arguments = bundle
                    loadFragment(upcoming)
                }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}

        })

    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(viewContainer.id,fragment)
        transaction.commit()
    }

    private fun getAllContest() {
        val liveContestReq = apiInterface.getAllLiveContest(sessionManager.getToken())

        liveContestReq.enqueue(  object : Callback<GetLiveContestDataResponse> {
            override fun onResponse(call: Call<GetLiveContestDataResponse>, response: Response<GetLiveContestDataResponse>) {
                if (response.isSuccessful && response.body()!!.success!! && response.body()!=null){
                    val contestData = response.body()!!.data
                    if (contestData.size>0){

                        list_upcoming = ArrayList()
                        list_live = ArrayList()
                        list_completed = ArrayList()

                        for(zz in contestData){
                            when (zz.status) {
                                "started" -> list_live.add(zz)
                                "completed" -> list_completed.add(zz)
                                "notstarted" -> list_upcoming.add(zz)
                            }
                        }

                        val upcoming = UpComingContestFragment()
                        val bundle = Bundle()
                        bundle.putSerializable(ConstValFile.UpContestComingList,list_upcoming)
                        upcoming.arguments = bundle
                        loadFragment(upcoming)


                        Log.d("check 400" ,"onResponse: videoItemPlayPause")
                    }else{
                        Log.d(TAG, " No Live Contest")
                    }
                }else{
                    Log.e(TAG,response.toString())
                }
            }

            override fun onFailure(call: Call<GetLiveContestDataResponse>, t: Throwable) {
                Log.e(TAG,t.toString())
            }

        })
    }
}