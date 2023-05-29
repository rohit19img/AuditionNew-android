package com.img.audition.adapters

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.img.audition.R
import com.img.audition.dataModel.LeaderboardData
import com.img.audition.databinding.LeaderboardrecycledesignBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.screens.CommanVideoPlayActivity
import com.img.audition.screens.VoterListActivity

class VoterListAdapter(val context: Context, val list: ArrayList<LeaderboardData>) :
    RecyclerView.Adapter<VoterListAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: LeaderboardrecycledesignBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val img = itemView.img
        val userName = itemView.userName
        val auditionID = itemView.auditionID
        val voteCountBtn = itemView.voteCountBtn
        val voteCountly = itemView.voteCountly
        val voteCount = itemView.voteCount
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemBinding = LeaderboardrecycledesignBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.apply {
            val data = list[absoluteAdapterPosition]

            voteCountly.visibility = View.GONE
            if (data.name!!.isNotEmpty()){
                userName.text = data.name.toString()
            }else{
                userName.text = "Biggee User"
            }
         auditionID.text = data.auditionId.toString()

            try {
                Glide.with(context).load(data.image).placeholder(R.drawable.person_ic).into(img)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}