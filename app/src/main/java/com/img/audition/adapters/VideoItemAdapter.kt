package com.img.audition.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.img.audition.R
import com.img.audition.dataModel.VideoData
import com.img.audition.databinding.VideoItemViewBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.screens.CommanVideoPlayActivity


@UnstableApi class VideoItemAdapter(val context: Context, val videoData : ArrayList<VideoData>) : RecyclerView.Adapter<VideoItemAdapter.VideoItemHolder>() {
    val TAG = "VideoItemAdapter"
    inner class VideoItemHolder(itemView: VideoItemViewBinding) : RecyclerView.ViewHolder(itemView.root) {
        val videoThumbnail = itemView.videoThumbnail
        val videoViewCount = itemView.videoViewCount
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoItemHolder {
        val itemBinding = VideoItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VideoItemHolder(itemBinding)
    }

    override fun getItemCount(): Int {
        return videoData.size
    }

    override fun onBindViewHolder(holder: VideoItemHolder, position: Int) {
        holder.apply {
            val list = videoData[position]

            Glide.with(context).load(list.file)
                .diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.splash_icon).into(videoThumbnail)

            videoViewCount.text = list.views.toString()


            itemView.setOnClickListener {
                val bundle = Bundle()
                bundle.putSerializable(ConstValFile.VideoList,videoData)
                bundle.putInt(ConstValFile.VideoPosition,position)
                val intent = Intent(context, CommanVideoPlayActivity::class.java)
                intent.putExtra(ConstValFile.Bundle, bundle)
                context.startActivity(intent)
            }
        }

    }

}