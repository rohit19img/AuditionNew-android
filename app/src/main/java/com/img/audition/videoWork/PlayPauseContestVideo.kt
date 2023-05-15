package com.img.audition.videoWork

import com.img.audition.adapters.ContestLiveAdapter
import com.img.audition.screens.fragment.ContestJoinedAdapter

interface PlayPauseContestVideo {
    fun onPause(holder: ContestLiveAdapter.MyViewHolder, cPos:Int){}
    fun onResume(holder:  ContestLiveAdapter.MyViewHolder,cPos:Int){}

    fun onPause(holder: ContestJoinedAdapter.MyViewHolder, cPos:Int){}
    fun onResume(holder:  ContestJoinedAdapter.MyViewHolder,cPos:Int){}

}