package com.img.audition.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.img.audition.R
import com.img.audition.dataModel.MusicData
import com.img.audition.databinding.MusiclistrecycledesignBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.videoWork.VideoCacheWork

@UnstableApi
class MusicAdapter(val context: Context, private val musicList: ArrayList<MusicData>) : RecyclerView.Adapter<MusicAdapter.MyMusicHolder>() {

    val playerExo = ExoPlayer.Builder(context.applicationContext).build()

    inner class MyMusicHolder(itemView: MusiclistrecycledesignBinding) : RecyclerView.ViewHolder(itemView.root) {

        val mImage = itemView.img
        val mName = itemView.songname
        val sName = itemView.singername
        val playBtn = itemView.playMusic
        val pauseBtn = itemView.pauseMusic
         val audioExoPlayer = itemView.audioPlayerView

        val mediaSource = ProgressiveMediaSource.Factory(
            CacheDataSource.Factory()
                .setCache(VideoCacheWork.simpleCache)
                .setUpstreamDataSourceFactory(
                    DefaultHttpDataSource.Factory()
                        .setUserAgent("ExoPlayer"))
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR))


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyMusicHolder {
        val itemBinding = MusiclistrecycledesignBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyMusicHolder(itemBinding)
    }

    override fun getItemCount(): Int {
        return musicList.size
    }


    override fun onBindViewHolder(holder: MyMusicHolder, position: Int) {
        holder.apply {
            Glide.with(context).load(ConstValFile.BASEURL+musicList[position].Image).placeholder(R.drawable.ic_music).into(mImage)
            mName.text = musicList[position].title.toString()
            sName.text  = musicList[position].subtitle.toString()

            playBtn.setOnClickListener {
                    for (play in musicList){
                        play.isPlay = false
                    }
                    musicList[position].isPlay = true
                notifyDataSetChanged()

                val mediaItem = MediaItem.fromUri(ConstValFile.BASEURL+musicList[position].trackAacFormat.toString())
                val audioMediaSource = mediaSource.createMediaSource(mediaItem)
                audioExoPlayer.player = playerExo
                playerExo.setMediaSource(audioMediaSource)
                playerExo.prepare()
                playerExo.playWhenReady = true
                Log.d("check 100", "onBindViewHolder: $position")
                playBtn.visibility = View.GONE
                pauseBtn.visibility = View.VISIBLE
            }

            if (musicList[position].isPlay){
                playBtn.visibility = View.GONE
                pauseBtn.visibility = View.VISIBLE
            }else{
                playBtn.visibility = View.VISIBLE
                pauseBtn.visibility = View.GONE
            }

            pauseBtn.setOnClickListener {
                if (playerExo.isPlaying){
                    playerExo.stop()
                    playBtn.visibility = View.VISIBLE
                    pauseBtn.visibility = View.GONE
                }
            }

        }
    }

}


