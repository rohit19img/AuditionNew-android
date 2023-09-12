package com.img.audition.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import java.text.DecimalFormat

class BlockedUserAdapter(val context: Context, private val blockedUserData : ArrayList<BlockedUserData>) : RecyclerView.Adapter<BlockedUserAdapter.MyViewHolder>() {
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
                    if  (MyApplication(context).isNetworkConnected()){
                        blockUnblockUser(ConstValFile.Unblock,data.Id.toString())
                        blockedUserData.removeAt(position)
                        notifyDataSetChanged()
                    }else{
                        Toast.makeText(context,ConstValFile.Check_Connection,Toast.LENGTH_SHORT).show()
                    }
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
        val apiInterface =  RetrofitClient.getInstance().create(ApiInterface::class.java)
        val blockUnblockReq = apiInterface.blockUnblockUser(SessionManager(context).getToken(), userID,status)
        blockUnblockReq.enqueue(object :Callback<CommanResponse>{
            override fun onResponse(call: Call<CommanResponse>, response: Response<CommanResponse>) {
                if (response.isSuccessful && response.body()?.success!!){
                    Toast.makeText(context,"Successfully Unblock..",Toast.LENGTH_SHORT).show()
                }else{
                    Log.e("BlockedUserAdapter", "onResponse: ${response.message()}")
                }
            }
            override fun onFailure(call: Call<CommanResponse>, t: Throwable) {
                Log.e("BlockedUserAdapter", "onFailure: $t")
            }

        })
    }


}