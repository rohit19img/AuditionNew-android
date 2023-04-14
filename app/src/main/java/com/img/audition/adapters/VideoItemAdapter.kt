package com.img.audition.adapters

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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


 class VideoItemAdapter(val context: Context, val videoData : ArrayList<VideoData>) : RecyclerView.Adapter<VideoItemAdapter.VideoItemHolder>() {
    val TAG = "VideoItemAdapter"
    private val sessionManager by lazy {
        SessionManager(context)
    }

    private val apiInterface by lazy{
        RetrofitClient.getInstance().create(ApiInterface::class.java)
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

            videoViewCount.text = list.views.toString()


            itemView.setOnClickListener {
                val bundle = Bundle()
                bundle.putSerializable(ConstValFile.VideoList,videoData)
                bundle.putInt(ConstValFile.VideoPosition,position)
                val intent = Intent(context, CommanVideoPlayActivity::class.java)
                intent.putExtra(ConstValFile.Bundle, bundle)
                context.startActivity(intent)
            }

            if (list.userId.equals(sessionManager.getUserSelfID())){
                videoItemDelete.visibility = View.VISIBLE

            }
            videoItemDelete.setOnClickListener {


                val sweetAlertDialog =
                    SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                sweetAlertDialog.titleText = "Are you Sure to delete this Video ?"
                sweetAlertDialog.show()
                sweetAlertDialog.confirmText = "Yes"
                sweetAlertDialog.cancelText = "No"
                sweetAlertDialog.setConfirmClickListener { sweetAlertDialog ->
                    deleteUserSelfVideo(list.Id.toString())
                    videoData.removeAt(position)
                    notifyDataSetChanged()
                    sweetAlertDialog.dismissWithAnimation()
                }
                sweetAlertDialog.setCancelClickListener { sweetAlertDialog1: SweetAlertDialog? ->
                    sweetAlertDialog.dismissWithAnimation()
                }

            }
        }

    }

    private fun deleteUserSelfVideo(id: String) {
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

}