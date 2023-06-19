package com.img.audition.screens.fragment

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
import com.img.audition.dataModel.GetJoinedContestDataResponse
import com.img.audition.dataModel.LiveContestData
import com.img.audition.databinding.FragmentJoinedContestBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.videoWork.PlayPauseContestVideo
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


@UnstableApi
class CompletedContestFragment : Fragment() {

    lateinit var contestViewpager2: ViewPager2
    lateinit var noLiveContest: TextView

    private val TAG = "CompletedContestFragment"
    private lateinit var _viewBinding : FragmentJoinedContestBinding
    lateinit var contestAdapter:ContestLiveAdapter

    private val view get() = _viewBinding

    lateinit var videoItemPlayPause: PlayPauseContestVideo

    private val bundle by lazy {
        arguments
    }
    var list : ArrayList<LiveContestData> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _viewBinding = FragmentJoinedContestBinding.inflate(inflater,container,false)
        val pager by lazy {
            view.contestViewpager2
        }
        contestViewpager2 = pager
        noLiveContest = view.noJoinedContest
        return view.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        list = bundle!!.getSerializable(ConstValFile.CompletedContestList) as  ArrayList<LiveContestData>

        if (list.size>0){
            noLiveContest.visibility = View.GONE
            list.reverse()
            contestAdapter = ContestLiveAdapter(view.context,list)
            contestViewpager2.adapter = contestAdapter
            videoItemPlayPause = contestAdapter.onActivityStateChanged()

            Log.d("check 400" ,"onResponse: videoItemPlayPause")
        }else{
            Log.d(TAG, " No Live Contest")
            noLiveContest.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        getView()?.destroyDrawingCache()
        super.onDestroyView()
    }

    override fun onPause() {
        super.onPause()
        Log.d("check 400", "onPause: $TAG")
        try {
            val cPos = view.contestViewpager2.currentItem
            val holder: ContestLiveAdapter.MyViewHolder = (view.contestViewpager2.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(cPos) as ContestLiveAdapter.MyViewHolder
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
            val holder: ContestLiveAdapter.MyViewHolder = (view.contestViewpager2.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(cPos) as ContestLiveAdapter.MyViewHolder
            videoItemPlayPause.onResume(holder,cPos)
            Log.d("check 400", " onResume cPos: $cPos")
        }catch (e:java.lang.Exception){
            Log.e(TAG,"2 $e")
        }
        super.onResume()
    }

    override fun onStop() {
        Log.d("check 400", "onStop: $TAG")
        try {
            val cPos = contestViewpager2.currentItem
            val holder: ContestLiveAdapter.MyViewHolder = (contestViewpager2.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(cPos) as ContestLiveAdapter.MyViewHolder
            Log.d("check 400", " onPause cPos: $cPos")
            videoItemPlayPause.onStop(holder,cPos)
        }catch (e:java.lang.Exception){
            Log.e("check 400", "1 $e")
        }
        super.onStop()
    }
}