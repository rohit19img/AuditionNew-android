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
import java.text.DecimalFormat

class VoterListAdapter(val context: Context, var list: ArrayList<LeaderboardData>) :
    RecyclerView.Adapter<VoterListAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: LeaderboardrecycledesignBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val img = itemView.img
        val userName = itemView.userName
        val auditionIDView = itemView.auditionID
        val voteCountBtn = itemView.voteCountBtn
        val voteCountly = itemView.voteCountly
        val voteCount = itemView.voteCount
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemBinding = LeaderboardrecycledesignBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.apply {
            val data = list[absoluteAdapterPosition]

            voteCount.text = formatCount(data.voteCount!!)

            if (data.name!!.isNotBlank()){
                userName.text = data.name.toString()
            }else{
                userName.text = "Biggee User"
            }
            auditionIDView.text = data.auditionId.toString()

            try {
                Glide.with(context).load(data.image).placeholder(R.drawable.person_ic).into(img)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun filterList(filterList: ArrayList<LeaderboardData>) {
        list = filterList
        notifyDataSetChanged()
    }
    override fun getItemCount(): Int {
        return list.size
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