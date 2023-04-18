package com.img.audition.screens.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

import com.img.audition.adapters.ContestLiveAdapter
import com.img.audition.dataModel.GetLiveContestDataResponse
import com.img.audition.databinding.FragmentLiveContestBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.videoWork.PlayPauseContestVideo
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@UnstableApi
class LiveContestFragment : Fragment() {

    val TAG = "LiveContestFragment"
    private var contestAdapter:ContestLiveAdapter = ContestLiveAdapter()
    private val sessionManager by lazy {
        SessionManager(requireContext())
    }

    lateinit var contestViewpager2: ViewPager2
    lateinit var noLiveContest: TextView
    private val apiInterface by lazy{
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }

    private var videoItemPlayPause: PlayPauseContestVideo = contestAdapter.onActivityStateChanged()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = FragmentLiveContestBinding.inflate(inflater,container,false)
        val pager by lazy {
            view.contestViewpager2
        }
        contestViewpager2 = pager
        noLiveContest = view.noLiveContest
        return view.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("check 400", "onViewCreated: ")
        showLiveContest(view.context)
    }
    private fun showLiveContest(context: Context) {
        val liveContestReq = apiInterface.getAllLiveContest(sessionManager.getToken())

        liveContestReq.enqueue(  object : Callback<GetLiveContestDataResponse>{
            override fun onResponse(call: Call<GetLiveContestDataResponse>, response: Response<GetLiveContestDataResponse>) {
                if (response.isSuccessful && response.body()!!.success!! && response.body()!=null){
                    val contestData = response.body()!!.data
                    if (contestData.size>0){
                        noLiveContest.visibility = View.GONE
                        contestAdapter = ContestLiveAdapter(context,contestData)
                        contestViewpager2.adapter = contestAdapter
                        videoItemPlayPause = contestAdapter.onActivityStateChanged()

                        Log.d("check 400" ,"onResponse: videoItemPlayPause")
                    }else{
                        Log.d(TAG, " No Live Contest")
                        noLiveContest.visibility = View.VISIBLE
                    }
                }else{
                    Log.e(TAG,response.toString())
                    noLiveContest.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<GetLiveContestDataResponse>, t: Throwable) {
                Log.e(TAG,t.toString())
                noLiveContest.visibility = View.VISIBLE
            }

        })
    }

    override fun onDestroyView() {
        Log.d("check 400", "onDestroyView: $TAG")
        super.onDestroyView()
    }

    override fun onDetach() {
        Log.d("check 400", "onDetach: $TAG")
        super.onDetach()
    }

    override fun onStop() {
        Log.d("check 400", "onStop: $TAG")
        super.onStop()
    }

    override fun onPause() {
        super.onPause()
        Log.d("check 400", "onPause: $TAG")
        try {
            val cPos = contestViewpager2.currentItem
            val holder: ContestLiveAdapter.MyViewHolder = (contestViewpager2.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(cPos) as ContestLiveAdapter.MyViewHolder
            Log.d("check 400", " onPause cPos: $cPos")
            videoItemPlayPause.onPause(holder,cPos)
        }catch (e:java.lang.Exception){
            Log.e("check 400", "1 $e")
        }
    }

    override fun onResume() {
        Log.d("check 400", "onResume: $TAG")
        try {
            val cPos = contestViewpager2.currentItem
            val holder: ContestLiveAdapter.MyViewHolder = (contestViewpager2.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(cPos) as ContestLiveAdapter.MyViewHolder
            videoItemPlayPause.onResume(holder,cPos)
            Log.d("check 400", " onResume cPos: $cPos")
        }catch (e:java.lang.Exception){
            Log.e(TAG,"2 $e")
        }
        super.onResume()
    }
}