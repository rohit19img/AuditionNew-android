package com.img.audition.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.img.audition.R
import com.img.audition.dataModel.FollowFollowingResponse
import com.img.audition.dataModel.FollowingList
import com.img.audition.databinding.FollowerfollowingdesignBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.screens.OtherUserProfileActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat

@UnstableApi class FollowingAdapter(val context: Context, private val followingList : ArrayList<FollowingList>) : RecyclerView.Adapter<FollowingAdapter.MyViewHolder>() {
    inner class MyViewHolder(itemView: FollowerfollowingdesignBinding) : RecyclerView.ViewHolder(itemView.root) {

       val userImage = itemView.userImage
       val followerCount = itemView.followerCount
       val userName = itemView.name
       val followBtnText = itemView.followBtnText
       val auditionId = itemView.auditionId
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemBinding = FollowerfollowingdesignBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(itemBinding)
    }

    override fun getItemCount(): Int {
        return followingList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
       holder.apply {
           val data = followingList[position]
            Glide.with(context).load(data.image).placeholder(R.drawable.person_ic).into(userImage)
            if (data.name!=""){
                userName.text = data.name.toString()
            }else{
                userName.text = data.auditionId.toString()
            }
            auditionId.text = data.auditionId.toString()
           followBtnText.text = ConstValFile.Unfollow
           followerCount.text = formatCount(data.followingCount!!) + " Following"

           followBtnText.setOnClickListener {
                followUserApi(data.Id,"unfollowed")
               followingList.removeAt(position)
               notifyDataSetChanged()
           }

           itemView.setOnClickListener {
                   val bundle = Bundle()
                   bundle.putString(ConstValFile.USER_IDFORIntent,data.Id)
                   bundle.putBoolean(ConstValFile.UserFollowStatus, true)
                    bundle.putString("auditionID", data.auditionId)
                   sendToVideoUserProfile(bundle)
           }
       }

    }

    private fun followUserApi(userId: String?, status: String) {
        val apiInterface =  RetrofitClient.getInstance().create(ApiInterface::class.java)
        val ffReq = apiInterface.followFollowing(SessionManager(context).getToken(),userId,status)
        ffReq.enqueue(object : Callback<FollowFollowingResponse> {
            override fun onResponse(call: Call<FollowFollowingResponse>, response: Response<FollowFollowingResponse>) {
                if (response.isSuccessful){
                    Log.d("FollowingAdapter", "onResponse: ${response.message()}")
                }else{
                    Log.e("FollowingAdapter", "onResponse: ${response.message()}")
                }
            }
            override fun onFailure(call: Call<FollowFollowingResponse>, t: Throwable) {
                Log.e("FollowingAdapter", "onFailure: $t")

            }
        })
    }

    private fun sendToVideoUserProfile(bundle: Bundle) {
        val intent = Intent(context, OtherUserProfileActivity::class.java)
        intent.putExtra(ConstValFile.Bundle,bundle)
        context.startActivity(intent)
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