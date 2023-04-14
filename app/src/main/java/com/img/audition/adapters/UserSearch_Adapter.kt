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
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.img.audition.R
import com.img.audition.dataModel.Searchgetset
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.screens.OtherUserProfileActivity
import java.lang.String
import kotlin.Int

class UserSearch_Adapter(val list: ArrayList<Searchgetset.User>, val context: Context) : RecyclerView.Adapter<UserSearch_Adapter.ViewHolder>(){

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
            username.setText(list[position].name)
            audiid.setText(list[position].audition_id)
            follcount.setText(String.valueOf(list[position].followers_count) + " Followers")

            itemView.setOnClickListener {
                if (list[position].is_self === false) {
                    val bundle = Bundle()
                    bundle.putString(ConstValFile.USER_IDFORIntent, list[position].get_id())
                    bundle.putBoolean(ConstValFile.UserFollowStatus, list[position].followStatus!!)
                    context.startActivity(
                        Intent(context, OtherUserProfileActivity::class.java)
                            .putExtra(ConstValFile.Bundle,bundle)
                    )
                } else {
//                    context.startActivity(
//                        Intent(context, Profile_activity::class.java)
//                            .putExtra("userid", list[position].get_id())
//                    )
                }
            }

        }
    }

}