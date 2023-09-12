package com.img.audition.videoWork

import com.img.audition.adapters.VideoAdapter
import com.img.audition.dataModel.VideoData

interface VideoItemPlayPause{
    fun onPause(holder: VideoAdapter.VideoViewHolder, cPos:Int)
    fun onResume(holder: VideoAdapter.VideoViewHolder,cPos:Int)
    fun onRestart(holder: VideoAdapter.VideoViewHolder,cPos:Int)
    fun onStop(holder: VideoAdapter.VideoViewHolder,cPos:Int)


}