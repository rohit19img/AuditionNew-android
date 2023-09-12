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
import java.text.DecimalFormat
import kotlin.random.Random

class TrendingHashtag(val context: Context,val hashtagData:ArrayList<TrendingVideoData>) : RecyclerView.Adapter<TrendingHashtag.MyViewHolder>() {


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
        return hashtagData[0].data.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
      holder.apply {
          val hashtagVideoData = hashtagData[0].data
          val hashtagImageList = hashtagData[0].images
          val imageIndex = Random.nextInt(0,hashtagImageList.size)
          val imageUrl = hashtagImageList[imageIndex]
          Glide.with(context).load(imageUrl).placeholder(R.drawable.auditon_logo).into(hashtagImage)

          hashtagViews.text = formatCount(hashtagVideoData[position].views!!)
          hashtagName.text = hashtagVideoData[position].name.toString()
          val adapter = TrendingHashVideoAdapter(context,hashtagVideoData[position].videos)
          hashtagCycle.adapter = adapter
      }
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