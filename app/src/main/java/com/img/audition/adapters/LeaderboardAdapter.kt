package com.img.audition.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.img.audition.R
import com.img.audition.dataModel.LeaderboardData
import com.img.audition.databinding.LeaderboardrecycledesignBinding

class LeaderboardAdapter(val context: Context, val list : ArrayList<LeaderboardData>) : RecyclerView.Adapter<LeaderboardAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: LeaderboardrecycledesignBinding) : RecyclerView.ViewHolder(itemView.root) {
        val img = itemView.img
        val name = itemView.name
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemBinding = LeaderboardrecycledesignBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: LeaderboardAdapter.MyViewHolder, position: Int) {
       holder.apply {
           try {
                name.setText(list[position].auditionId)
               Glide.with(context).load(list[position].image).placeholder(R.drawable.person_ic).into(img)
           } catch (e: Exception) {
               e.printStackTrace()
           }
       }
    }

    override fun getItemCount(): Int {
       return list.size
    }
}