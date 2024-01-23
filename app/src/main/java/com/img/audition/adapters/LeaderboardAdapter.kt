package com.img.audition.adapters

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.img.audition.screens.VoterListActivity
import java.text.DecimalFormat

@UnstableApi
class LeaderboardAdapter(
    val context: Context,
    val list: ArrayList<LeaderboardData>,
    val contestID: String
) :
    RecyclerView.Adapter<LeaderboardAdapter.MyViewHolder>() {

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

            /*if (list[position].voteCount==0){
                voteCountly.visibility = View.GONE
            }else{
                voteCount.text = list[position].voteCount.toString()
            }*/

            voteCount.text = formatCount(list[position].voteCount!!)
            auditionID.setText(list[position].auditionId)

            if (list[position].name!!.isNotEmpty()) {
                userName.setText(list[position].name)
            } else {
                userName.text = "Biggee User"
            }
            try {
                Glide.with(context).load(list[position].image).placeholder(R.drawable.person_ic)
                    .into(img)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            voteCountBtn.setOnClickListener {
                if (list[position].status == "notstarted" || list[position].status == "closed") {
                    Toast.makeText(it.context, "Contest Not Started Yet.", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    val videoID = list[position].videoID.toString()
                    Log.d("videoId", "onBindViewHolder: ${list[position].videoID.toString()}")
                    val bundle = Bundle()
                    bundle.putString(ConstValFile.VideoID, videoID)
                    val intent = Intent(context, VoterListActivity::class.java)
                    intent.putExtra(ConstValFile.Bundle, bundle)
                    context.startActivity(intent)
                }
            }

            itemView.setOnClickListener {
                Log.i("RohitTest",list[position].status!!)
                if (list[position].status == "notstarted"|| list[position].status == "closed") {
                    Toast.makeText(it.context, "Contest Not Started Yet.", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    val bundle = Bundle()
                    bundle.putString(ConstValFile.USER_ID, list[position].userid)
                    bundle.putString(ConstValFile.ContestID, contestID)
                    Log.d("contestVideo", "userID : " + list[position].userid.toString())
                    Log.d("contestVideo", "contestID : " + list[position].joinleaugeid.toString())
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