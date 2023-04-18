package com.img.audition.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.img.audition.R
import com.img.audition.dataModel.TrendingVideoData
import com.img.audition.databinding.TrendingHashtagCycleBinding
import com.img.audition.globalAccess.MyApplication
import kotlin.random.Random

class TrendingHashtag(val context: Context,val hashtagData:ArrayList<TrendingVideoData>) : RecyclerView.Adapter<TrendingHashtag.MyViewHolder>() {

    private val myApplication by lazy {
        MyApplication(context)
    }
    class MyViewHolder(itemView: TrendingHashtagCycleBinding) : RecyclerView.ViewHolder(itemView.root) {
        val  hashtagCycle = itemView.hashtagVideoCycle
        val hashtagImage = itemView.hashtagImage
        val hashtagName = itemView.hashtagName
        val hashtagViews = itemView.hashtagViews

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemBinding = TrendingHashtagCycleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(itemBinding)
    }

    override fun getItemCount(): Int {
        myApplication.printLogD(hashtagData[0].data.size.toString(),"trending 2")
        return hashtagData[0].data.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
      holder.apply {
          myApplication.printLogD(hashtagData.size.toString(),"trending 3")
          val hashtagVideoData = hashtagData[0].data
          val hashtagImageList = hashtagData[0].images
          val imageIndex = Random.nextInt(0,hashtagImageList.size)
          val imageUrl = hashtagImageList[imageIndex]
          myApplication.printLogD(imageUrl,"trending 4")
          Glide.with(context).load(imageUrl).placeholder(R.drawable.auditon_logo).into(hashtagImage)

          hashtagViews.text = hashtagVideoData[position].views.toString()
          hashtagName.text = hashtagVideoData[position].name.toString()
          val adapter = TrendingHashVideoAdapter(context,hashtagVideoData[position].videos)
          hashtagCycle.adapter = adapter
      }
    }



}