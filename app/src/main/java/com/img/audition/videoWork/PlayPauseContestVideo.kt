package com.img.audition.videoWork

import com.img.audition.adapters.ContestLiveAdapter

interface PlayPauseContestVideo {
    fun onPause(holder: ContestLiveAdapter.MyViewHolder, cPos:Int)
    fun onResume(holder:  ContestLiveAdapter.MyViewHolder,cPos:Int)

}