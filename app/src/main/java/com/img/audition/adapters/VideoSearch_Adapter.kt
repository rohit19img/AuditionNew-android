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
import androidx.media3.common.util.UnstableApi

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.img.audition.R
import com.img.audition.dataModel.VideoData
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.screens.CommanVideoPlayActivity
import java.text.DecimalFormat

@UnstableApi
class VideoSearch_Adapter(val context : Context, val list : ArrayList<VideoData>) : RecyclerView.Adapter<VideoSearch_Adapter.ViewHolder>(){

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val auditionid =  itemView.findViewById<TextView>(R.id.auditionid)
        val videothumb = itemView.findViewById<ImageView>(R.id.videothumb)
        val plays = itemView.findViewById<TextView>(R.id.plays)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView: View =
            LayoutInflater.from(context).inflate(R.layout.videolistrecycledesign, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            auditionid.text = list[position].auditionId
            plays.text = formatCount(list[position].views!!)
            Glide.with(context).load(list[position].file).placeholder(R.drawable.splash_icon).into(videothumb)
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

     private fun formatCount(count: Int): String {
         val suffix = charArrayOf(' ', 'k', 'M', 'B', 'T', 'P', 'E')
         val numValue: Long = count.toLong()
         val value = Math.floor(Math.log10(numValue.toDouble())).toInt()
         val base = value / 3
         return if (value >= 3 && base < suffix.size) {
             DecimalFormat("#0.0").format(
                 numValue / Math.pow(
                     10.0,
                     (base * 3).toDouble()
                 )
             ) + suffix[base]
         } else {
             DecimalFormat("#,##0").format(numValue)
         }
         /*return when {
             count < 1000 -> count.toString()
             count < 10000 -> String.format("%.1fk", Math.floor(count / 100.0) / 10)
             else -> (count / 1000).toString() + "k"
         }*/
     }
}