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
import com.img.audition.dataModel.LiveContestData
import com.img.audition.databinding.FragmentUpCommingContestBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.videoWork.PlayPauseContestVideo


@UnstableApi
class UpComingContestFragment : Fragment() {

    private val TAG = "UpComingContestFragment"
    private lateinit var contestAdapter: ContestLiveAdapter
    private lateinit var contestViewpager2: ViewPager2
    private lateinit var noLiveContest: TextView


    private var list : ArrayList<LiveContestData> = ArrayList()

    private lateinit var videoItemPlayPause: PlayPauseContestVideo

    private val bundle by lazy {
        arguments
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = FragmentUpCommingContestBinding.inflate(inflater,container,false)
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
        list = bundle!!.getSerializable(ConstValFile.UpContestComingList) as  ArrayList<LiveContestData>

        if (list.size>0){
            noLiveContest.visibility = View.GONE
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
        Log.d("check 400", "onDestroyView: $TAG")
        view?.destroyDrawingCache()
        super.onDestroyView()
    }

    override fun onDetach() {
        Log.d("check 400", "onDetach: $TAG")
        super.onDetach()
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