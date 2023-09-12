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
import androidx.fragment.app.Fragment
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.img.audition.R
import com.img.audition.dataModel.SearchUserData
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.screens.CommanVideoPlayActivity
import com.img.audition.screens.HomeActivity
import com.img.audition.screens.OtherUserProfileActivity
import com.img.audition.screens.UploadVideoActivity
import com.img.audition.screens.fragment.ProfileFragment
import java.lang.String
import java.text.DecimalFormat
import kotlin.Int

@UnstableApi class UserSearch_Adapter(val list: ArrayList<SearchUserData>, val context: Context, val replacedText : kotlin.String) : RecyclerView.Adapter<UserSearch_Adapter.ViewHolder>(){

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var username: TextView
        var follcount: TextView
        var audiid: TextView
        var foll: TextView
        var imageView1: ImageView

        init {
            imageView1 = itemView.findViewById<ImageView>(R.id.imageView1)
            username = itemView.findViewById<TextView>(R.id.username)
            follcount = itemView.findViewById<TextView>(R.id.follcount)
            audiid = itemView.findViewById<TextView>(R.id.audiid)
            foll = itemView.findViewById<TextView>(R.id.foll)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.userlistrecycledesign, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            Glide.with(context).load(list[position].image).into(imageView1)
            username.text = list[position].name
            audiid.text = list[position].auditionId
            follcount.text =  formatCount(list[position].followersCount!!)+ " Followers"


                itemView.setOnClickListener {
                    if (context is UploadVideoActivity) {

                        context.searchUserName = ""
                        var userNameID = ""
                        val userId = list[position].auditionId.toString()
                        if (userId.contains("@")){
                            userNameID = userId.replace("@","")
                        }else{
                            userNameID = userId
                        }
                        context.check=true;
                        context.videoCapEt.setText(context.videoCapEt.text.toString().replace("$replacedText","@"+userNameID).toString()!!)
                        val fullCap = context.videoCapEt.text.toString()
                        context.videoCapEt.setSelection(fullCap.length)

                        list.clear()
                        notifyDataSetChanged()
                        context.cycleViewlayout.visibility = View.GONE
                        context.showBtnView.visibility = View.VISIBLE

                    //context.videoCapEt.append(list[position].auditionId.toString())
                    } else {
                        if (!(list[position].isSelf!!)) {
                            val bundle = Bundle()
                            bundle.putString(ConstValFile.USER_IDFORIntent, list[position].Id)
                            bundle.putString("auditionID", list[position].auditionId)
                            bundle.putBoolean(
                                ConstValFile.UserFollowStatus,
                                list[position].followStatus!!
                            )
                            context.startActivity(
                                Intent(context, OtherUserProfileActivity::class.java)
                                    .putExtra(ConstValFile.Bundle, bundle)
                            )
                        } else {
                            sendToUserSelfProfile()
                        }
                    }
                }
            }
        }

    private fun sendToUserSelfProfile() {
        val activity = context as HomeActivity
        val myFragment: Fragment = ProfileFragment(context)
        activity.supportFragmentManager.beginTransaction()
                .replace(R.id.viewContainer, myFragment).addToBackStack(null).commit()
    }

    private fun formatCount(count: Int): kotlin.String {
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

