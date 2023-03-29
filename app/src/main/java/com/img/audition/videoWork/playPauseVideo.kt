package com.img.audition.videoWork

import com.img.audition.adapters.VideoAdapter

interface playPauseVideo {
    fun stop(holder: VideoAdapter.VideoViewHolder, cPos:Int)
    fun play(holder: VideoAdapter.VideoViewHolder,cPos:Int)
}