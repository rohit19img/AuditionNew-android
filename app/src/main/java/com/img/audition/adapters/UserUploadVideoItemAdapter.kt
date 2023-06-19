package com.img.audition.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.img.audition.R
import com.img.audition.dataModel.VideoData
import com.img.audition.databinding.UserUploadVideoItemLayoutBinding
import com.img.audition.network.SessionManager
import java.text.DecimalFormat

class UserUploadVideoItemAdapter(val context: Context, private val videoData : ArrayList<VideoData>) : RecyclerView.Adapter<UserUploadVideoItemAdapter.VideoItemHolder>() {
    private val TAG = "UserUploadVideoItemAdapter"
    private val sessionManager by lazy {
        SessionManager(context)
    }

    inner class VideoItemHolder(itemView: UserUploadVideoItemLayoutBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val videoThumbnail = itemView.videoThumbnail
        val videoViewCount = itemView.videoViewCount
        val radioGroupV = itemView.radioGroupV
        val selectRadio = itemView.selectRadio
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoItemHolder {
        val itemBinding = UserUploadVideoItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VideoItemHolder(itemBinding)
    }

    override fun getItemCount(): Int {
        return videoData.size
    }

    override fun onBindViewHolder(holder: VideoItemHolder, position: Int) {
        holder.apply {
            val list = videoData[position]

            Glide.with(context).load(list.file).placeholder(R.drawable.splash_icon)
                .into(videoThumbnail)

            videoViewCount.text = formatCount(list.views!!)
            selectRadio.isChecked = videoData[position].isSelected
            itemView.setOnClickListener {
                Log.d("video url", "onBindViewHolder: ${list.file}")
                for (zz in videoData) zz.isSelected = false
                videoData[position].isSelected = true
                notifyDataSetChanged()
            }

            selectRadio.setOnClickListener {
                Log.d("video url", "onBindViewHolder: ${list.file}")
                for (zz in videoData) zz.isSelected = false
                videoData[position].isSelected = true
                notifyDataSetChanged()
            }
        }
    }


    private fun showToast(msg: String) {
        Toast.makeText(context.applicationContext, msg, Toast.LENGTH_SHORT).show()
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