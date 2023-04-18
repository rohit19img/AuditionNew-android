package com.img.audition.adapters

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.img.audition.dataModel.VideoData
import com.img.audition.databinding.TrendHashVideoCycleBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.screens.CommanVideoPlayActivity

class TrendingHashVideoAdapter(val context : Context, val trendVideoList: ArrayList<VideoData>) : RecyclerView.Adapter<TrendingHashVideoAdapter.MyViewHolder>() {
    class MyViewHolder(itemView: TrendHashVideoCycleBinding) : RecyclerView.ViewHolder(itemView.root) {
        val videoThumbnail = itemView.hashtagVideo
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemBinding = TrendHashVideoCycleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(itemBinding)
    }

    override fun getItemCount(): Int {
        return trendVideoList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
       holder.apply {
            Glide.with(context).load(trendVideoList[position].file).into(videoThumbnail)
           itemView.setOnClickListener {
               val bundle = Bundle()
               bundle.putSerializable(ConstValFile.VideoList,trendVideoList)
               bundle.putInt(ConstValFile.VideoPosition,position)
               val intent = Intent(context, CommanVideoPlayActivity::class.java)
               intent.putExtra(ConstValFile.Bundle, bundle)
               context.startActivity(intent)
           }
       }
    }

}