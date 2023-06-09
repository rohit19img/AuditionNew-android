package com.img.audition.adapters

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
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

class FollowingAdapter(val context: Context,val followingList : ArrayList<FollowingList>) : RecyclerView.Adapter<FollowingAdapter.MyViewHolder>() {

    val TAG = "FollowingAdapter"
    private val apiInterface by lazy{
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }
    private val sessionManager by lazy {
        SessionManager(context)
    }
    private val myApplication by lazy {
        MyApplication(context)
    }
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
           followerCount.text = data.followingCount.toString()

           followBtnText.setOnClickListener {
                followUserApi(data.Id,"unfollowed")
               followingList.removeAt(position)
               notifyDataSetChanged()
           }

           itemView.setOnClickListener {
                   val bundle = Bundle()
                   bundle.putString(ConstValFile.USER_IDFORIntent,data.Id)
                   bundle.putBoolean(ConstValFile.UserFollowStatus, true)
                   sendToVideoUserProfile(bundle)
           }
       }

    }

    private fun followUserApi(userId: String?, status: String) {
        myApplication.printLogD("followUserApi: $userId $status",TAG)
        val ffReq = apiInterface.followFollowing(sessionManager.getToken(),userId,status)
        ffReq.enqueue(object : Callback<FollowFollowingResponse> {
            override fun onResponse(call: Call<FollowFollowingResponse>, response: Response<FollowFollowingResponse>) {
                if (response.isSuccessful){
                    myApplication.printLogD("onResponse: FollowFollowing ${response.toString()}",TAG)

                }else{
                    myApplication.printLogE("onResponse: FollowFollowing ${response.toString()}",TAG)

                }
            }
            override fun onFailure(call: Call<FollowFollowingResponse>, t: Throwable) {
                myApplication.printLogE("onFailure: FollowFollowing ${t.toString()}",TAG)

            }
        })
    }

    private fun sendToVideoUserProfile(bundle: Bundle) {
        val intent = Intent(context, OtherUserProfileActivity::class.java)
        intent.putExtra(ConstValFile.Bundle,bundle)
        context.startActivity(intent)
    }
}