package com.img.audition.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.img.audition.R
import com.img.audition.dataModel.VideoData
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.screens.CommanVideoPlayActivity

class VideoSearch_Adapter(val context : Context, val list : ArrayList<VideoData>) : RecyclerView.Adapter<VideoSearch_Adapter.ViewHolder>(){

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var auditionid: TextView
        var hashtagplays: TextView? = null
        var videothumb: ImageView

        init {
            auditionid = itemView.findViewById<TextView>(R.id.auditionid)
            videothumb = itemView.findViewById<ImageView>(R.id.videothumb)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView: View =
            LayoutInflater.from(context).inflate(R.layout.videolistrecycledesign, parent, false)
        return ViewHolder(itemView)
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            auditionid.text = list[position].auditionId
            hashtagplays!!.text = "${list[position].views} Plays"
            Glide.with(context).load(list[position].file).diskCacheStrategy(DiskCacheStrategy.ALL).into(videothumb)
            itemView.setOnClickListener {
                val bundle = Bundle()
                bundle.putSerializable(ConstValFile.VideoList,list)
                bundle.putInt(ConstValFile.VideoPosition,position)
                val intent = Intent(context, CommanVideoPlayActivity::class.java)
                intent.putExtra(ConstValFile.Bundle, bundle)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

}