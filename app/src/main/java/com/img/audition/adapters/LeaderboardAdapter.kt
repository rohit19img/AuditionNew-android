package com.img.audition.adapters

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
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

@UnstableApi class LeaderboardAdapter(val context: Context, val list: ArrayList<LeaderboardData>) :
    RecyclerView.Adapter<LeaderboardAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: LeaderboardrecycledesignBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val img = itemView.img
        val name = itemView.name
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
            try {
                name.setText(list[position].auditionId)
                Glide.with(context).load(list[position].image).placeholder(R.drawable.person_ic)
                    .into(img)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            itemView.setOnClickListener {
                if (list[position].status == "notstarted"){
                    Toast.makeText(context,"Contest Not Started Yet.",Toast.LENGTH_SHORT).show()
                }else{
                    val bundle = Bundle()
                    bundle.putString(ConstValFile.USER_ID, list[position].userid)
                    bundle.putString(ConstValFile.ContestID, list[position].joinleaugeid)
                    bundle.putBoolean(ConstValFile.IsFromContest, true)
                    val intent = Intent(context, CommanVideoPlayActivity::class.java)
                    intent.putExtra(ConstValFile.Bundle, bundle)
                    context.startActivity(intent)
                }

            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}