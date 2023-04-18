package com.img.audition.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.img.audition.R
import com.img.audition.dataModel.BlockedUserData
import com.img.audition.dataModel.CommanResponse
import com.img.audition.databinding.FollowerfollowingdesignBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BlockedUserAdapter(val context: Context, val blockedUserData : ArrayList<BlockedUserData>) : RecyclerView.Adapter<BlockedUserAdapter.MyViewHolder>() {

    val TAG = "BlockedUserAdapter"
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
        val unblockButton = itemView.followBtnText
        val auditionId = itemView.auditionId
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemBinding = FollowerfollowingdesignBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(itemBinding)
    }

    override fun getItemCount(): Int {
        return blockedUserData.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.apply {
            auditionId.visibility = View.GONE
            followerCount.visibility = View.GONE
            unblockButton.text = "Unblock"
            val data = blockedUserData[position]
            Glide.with(context).load(data.image).placeholder(R.drawable.person_ic).into(userImage)
            if (data.name!!.isNotEmpty()){
                userName.text = data.name.toString()
            }else{
                userName.text = data.auditionId.toString()
            }
            unblockButton.setOnClickListener {
                val sweetAlertDialog = SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                sweetAlertDialog.titleText = "Unblock"
                sweetAlertDialog.contentText = "Do you want unblock this user"
                sweetAlertDialog.confirmText = "Yes"
                sweetAlertDialog.setConfirmClickListener {
                    blockUnblockUser(ConstValFile.Unblock,data.Id.toString())
                    blockedUserData.removeAt(position)
                    notifyDataSetChanged()
                    sweetAlertDialog.dismiss()
                }
                sweetAlertDialog.cancelText = "No"
                sweetAlertDialog.setCancelClickListener {
                    sweetAlertDialog.dismiss()
                }
                sweetAlertDialog.show()
            }
        }
    }

    private fun blockUnblockUser(status: String,userID:String) {
        val blockUnblockReq = apiInterface.blockUnblockUser(sessionManager.getToken(), userID,status)

        blockUnblockReq.enqueue(object :Callback<CommanResponse>{
            override fun onResponse(call: Call<CommanResponse>, response: Response<CommanResponse>) {
                if (response.isSuccessful && response.body()?.success!!){
                    myApplication.showToast("Successfully Unblock..")
                }else{
                    myApplication.printLogE(response.toString(),TAG)
                }
            }
            override fun onFailure(call: Call<CommanResponse>, t: Throwable) {
                myApplication.printLogE(t.toString(),TAG)
            }

        })
    }

}