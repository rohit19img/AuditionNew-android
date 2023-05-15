package com.img.audition.screens.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import com.img.audition.adapters.ContestLiveAdapter
import com.img.audition.dataModel.GetJoinedContestDataResponse
import com.img.audition.databinding.FragmentJoinedContestBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.videoWork.PlayPauseContestVideo
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


@UnstableApi
class JoinedContestFragment : Fragment() {

    val TAG = "JoinedContestFragment"
    private lateinit var _viewBinding : FragmentJoinedContestBinding
    private var contestAdapter:ContestJoinedAdapter = ContestJoinedAdapter()

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

    private var videoItemPlayPause: PlayPauseContestVideo = contestAdapter.onActivityStateChanged()



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
                        contestAdapter = ContestJoinedAdapter(requireContext(),contestData)
                        view.contestViewpager2.adapter = contestAdapter
                        videoItemPlayPause = contestAdapter.onActivityStateChanged()
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

    override fun onPause() {
        super.onPause()
        Log.d("check 400", "onPause: $TAG")
        try {
            val cPos = view.contestViewpager2.currentItem
            val holder: ContestJoinedAdapter.MyViewHolder = (view.contestViewpager2.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(cPos) as ContestJoinedAdapter.MyViewHolder
            Log.d("check 400", " onPause cPos: $cPos")
            videoItemPlayPause.onPause(holder,cPos)
        }catch (e:java.lang.Exception){
            Log.e("check 400", "1 $e")
        }
    }

    override fun onResume() {
        Log.d("check 400", "onResume: $TAG")
        try {
            val cPos = view.contestViewpager2.currentItem
            val holder: ContestJoinedAdapter.MyViewHolder = (view.contestViewpager2.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(cPos) as ContestJoinedAdapter.MyViewHolder
            videoItemPlayPause.onResume(holder,cPos)
            Log.d("check 400", " onResume cPos: $cPos")
        }catch (e:java.lang.Exception){
            Log.e(TAG,"2 $e")
        }
        super.onResume()
    }

}