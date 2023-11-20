package com.img.audition.screens.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.media3.common.util.UnstableApi
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.material.tabs.TabLayout
import com.img.audition.R
import com.img.audition.dataModel.GetLiveContestDataResponse
import com.img.audition.dataModel.LiveContestData
import com.img.audition.databinding.FragmentContestBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.screens.ContestConditionActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


@UnstableApi
class ContestFragment(val contextFromHome : Context) : Fragment() {
    private val TAG = "ContestFragment"

    var list_upcoming = ArrayList<LiveContestData>()
    var list_live  = ArrayList<LiveContestData>()
    var list_completed = ArrayList<LiveContestData>()

    lateinit var tabLayout: TabLayout
    lateinit var viewContainer: FrameLayout
    lateinit var contestConditionBtn:ImageButton

    private val myApplication by lazy {
        MyApplication(contextFromHome)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view= FragmentContestBinding.inflate(inflater,container,false)
        tabLayout = view.tabLayout
        viewContainer = view.viewContainer
        contestConditionBtn = view.contestConditionBtn
        return view.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (myApplication.isNetworkConnected()){
            getAllContest()
        }else{
            checkInternetDialog()
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab!!.position){
                    0 ->{

                        val customView = tab.customView
                        customView?.background = ContextCompat.getDrawable(customView!!.context, R.drawable.tab_selected)
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


        contestConditionBtn.setOnClickListener {
            val intent = Intent(requireContext(),ContestConditionActivity::class.java)
            startActivity(intent)
        }

    }

    private fun loadFragment(fragment: Fragment) {
        try {
            val transaction = childFragmentManager.beginTransaction()
            transaction.replace(viewContainer.id,fragment)
            transaction.commit()
        }catch (e:Exception){
            Log.e(TAG, "loadFragment: ", e)
        }
    }

    private fun getAllContest() {
        val apiInterface = RetrofitClient.getInstance().create(ApiInterface::class.java)
        val liveContestReq = apiInterface.getAllLiveContest(SessionManager(contextFromHome).getToken())
        Log.d("auth", "getAllContest: auth " + SessionManager(contextFromHome).getToken())

        if (myApplication.isNetworkConnected()){
            liveContestReq.enqueue(  object : Callback<GetLiveContestDataResponse> {
                override fun onResponse(call: Call<GetLiveContestDataResponse>, response: Response<GetLiveContestDataResponse>) {
                    if (response.isSuccessful && response.body()!!.success!! && response.body()!=null){
                        val contestData = response.body()!!.data
                        if (contestData.size>0){

                            list_upcoming = ArrayList()
                            list_live = ArrayList()
                            list_completed = ArrayList()

                            for(zz in contestData) {
                                if(zz.status == "notstarted")
                                    list_upcoming.add(zz)
                                else if(zz.finalStatus.equals("IsReviewed",true) || zz.finalStatus.equals("pending",true))
                                    list_live.add(zz)
                                else if(zz.finalStatus == "winnerdeclared")
                                    list_completed.add(zz)
                            }

//                            for(zz in contestData){
//                                when (zz.status) {
//                                    "started" -> list_live.add(zz)
//                                    "completed" -> list_completed.add(zz)
//                                    "notstarted" -> list_upcoming.add(zz)
//                                }
//                            }

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
        }else{
            checkInternetDialog()
        }
    }

    override fun onDestroyView() {
        Log.d("check 400", "onDestroyView: $TAG")
        try {
            list_upcoming.clear()
            list_completed.clear()
            list_live.clear()
        }catch (e:Exception){
            e.printStackTrace()
        }
        view?.destroyDrawingCache()
        super.onDestroyView()
    }

    private fun checkInternetDialog() {
        val sweetAlertDialog = SweetAlertDialog(contextFromHome, SweetAlertDialog.WARNING_TYPE)
        sweetAlertDialog.titleText = "Internet"
        sweetAlertDialog.contentText = ConstValFile.Check_Connection
        sweetAlertDialog.confirmText = "Retry"
        sweetAlertDialog.setConfirmClickListener {
            sweetAlertDialog.dismiss()
            getAllContest()
        }
        sweetAlertDialog.show()
    }
}