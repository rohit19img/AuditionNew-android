package com.img.audition.adapters

import android.content.Context
import android.content.Intent
import android.media.ThumbnailUtils
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.img.audition.R
import com.img.audition.dataModel.CommanResponse
import com.img.audition.dataModel.VideoData
import com.img.audition.databinding.VideoItemViewBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.screens.CommanVideoPlayActivity
import okhttp3.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat


@UnstableApi
 class VideoItemAdapter(val context: Context,private val videoData : ArrayList<VideoData>) : RecyclerView.Adapter<VideoItemAdapter.VideoItemHolder>() {
    private val TAG = "VideoItemAdapter"
    private val sessionManager by lazy {
        SessionManager(context)
    }
    inner class VideoItemHolder(itemView: VideoItemViewBinding) : RecyclerView.ViewHolder(itemView.root) {
        val videoThumbnail = itemView.videoThumbnail
        val videoViewCount = itemView.videoViewCount
        val videoItemDelete = itemView.videoItemDelete
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


            Glide.with(context).load(list.file).placeholder(R.drawable.splash_icon).into(videoThumbnail)

            videoViewCount.text = formatCount(list.views!!)


            itemView.setOnClickListener {
                val bundle = Bundle()
                bundle.putSerializable(ConstValFile.VideoList,videoData)
                bundle.putInt(ConstValFile.VideoPosition,position)
                val intent = Intent(context, CommanVideoPlayActivity::class.java)
                intent.putExtra(ConstValFile.Bundle, bundle)
                context.startActivity(intent)
            }

            Log.d("video userID", "onBindViewHolder: ${list.userId}")
            Log.d("video userID", "onBindViewHolder: self ${sessionManager.getUserSelfID()}")
            if (list.userId.equals(sessionManager.getUserSelfID())){
                videoItemDelete.visibility = View.VISIBLE
            }
            videoItemDelete.setOnClickListener {
                val sweetAlertDialog =
                    SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                sweetAlertDialog.titleText = "Remove Video"
                sweetAlertDialog.show()
                sweetAlertDialog.confirmText = "Yes"
                sweetAlertDialog.cancelText = "No"
                sweetAlertDialog.setConfirmClickListener { sweetAlertDialog ->
                    deleteUserSelfVideo(list.Id.toString())
                    videoData.removeAt(position)
                    notifyDataSetChanged()
                    sweetAlertDialog.dismissWithAnimation()
                }
                sweetAlertDialog.setCancelClickListener {
                    sweetAlertDialog.dismissWithAnimation()
                }

            }
        }

    }

    private fun deleteUserSelfVideo(id: String) {
        val apiInterface =  RetrofitClient.getInstance().create(ApiInterface::class.java)
        val deleteVideoReg = apiInterface.deleteUserSelfVideo(sessionManager.getToken(),id)
        deleteVideoReg.enqueue(object : Callback<CommanResponse>{
            override fun onResponse(call: retrofit2.Call<CommanResponse>, response: Response<CommanResponse>) {
                if (response.isSuccessful && response.body()!!.success!!){
                    Log.d(TAG, "onResponse: ${response.toString()}")
                    showToast("Video Remove Successfully..")
                }else{
                    Log.e(TAG, "onResponse: ${response.toString()}")
                }
            }

            override fun onFailure(call: retrofit2.Call<CommanResponse>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.toString()}")
            }
        })
    }

    private fun showToast(msg: String) {
        Toast.makeText(context.applicationContext,msg, Toast.LENGTH_SHORT).show()
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