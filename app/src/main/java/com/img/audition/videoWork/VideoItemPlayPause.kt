package com.img.audition.videoWork

import com.img.audition.adapters.VideoAdapter
import com.img.audition.dataModel.VideoData

interface VideoItemPlayPause{
    fun onPause(holder: VideoAdapter.VideoViewHolder)
    fun onResume(holder: VideoAdapter.VideoViewHolder)
    fun onRestart(holder: VideoAdapter.VideoViewHolder)
    fun onStop(holder: VideoAdapter.VideoViewHolder)


}