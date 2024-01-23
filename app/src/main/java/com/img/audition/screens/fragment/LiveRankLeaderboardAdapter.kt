package com.img.audition.screens.fragment

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
import com.img.audition.dataModel.Jointeams
import com.img.audition.databinding.LayoutForCompleteContestLeaderboradBinding
import com.img.audition.databinding.LiveRankLeaderboardLayoutBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.screens.CommanVideoPlayActivity
import com.img.audition.screens.VoterListActivity

@UnstableApi
class LiveRankLeaderboardAdapter(val context: Context, val list: ArrayList<Jointeams>, private val contestID:String) :
    RecyclerView.Adapter<LiveRankLeaderboardAdapter.MyViewHolder>() {

    private val myApplication by lazy {
        MyApplication(context)
    }

    class MyViewHolder(itemView: LiveRankLeaderboardLayoutBinding) :
//    LayoutForCompleteContestLeaderboradBinding
        RecyclerView.ViewHolder(itemView.root) {
        val img = itemView.img
        val userName = itemView.userName
        val auditionID = itemView.auditionID
        val voteCountBtn = itemView.voteCountBtn
        val voteCountly = itemView.voteCountly
        val voteCount = itemView.voteCount
        val winAmt = itemView.winAmt
        val winamt11  = itemView.winamt11
        val rank = itemView.rank
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemBinding = LiveRankLeaderboardLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.apply {

            val data = list[position]

            if (data.winingamount.toString().isNotEmpty()){
                winAmt.text = "₹${data.winingamount.toString()}"
                winamt11.visibility = View.VISIBLE
            }else{
                winAmt.visibility = View.GONE
                winamt11.visibility = View.GONE
            }
            rank.text = "#${data.getcurrentrank.toString()}"

            voteCount.text = data.vote.toString()
           /* if (data.vote==0){
                voteCountly.visibility = View.GONE
            }else{
                voteCount.text = data.vote.toString()
            }*/
            auditionID.setText(data.auditionId)

            if (list[position].teamname!!.isNotEmpty()){
                userName.text = list[position].teamname
            }else{
                userName.text = "Biggee User"
            }
            try {
                Glide.with(context).load(list[position].image).placeholder(R.drawable.person_ic).into(img)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            voteCountBtn.setOnClickListener {
                if (myApplication.isNetworkConnected()){
                    if (list[position].status == "notstarted" || list[position].status == "closed"){
                        Toast.makeText(it.context,"Contest Not Started Yet.", Toast.LENGTH_SHORT).show()
                    }else {
                        val videoID = list[position].videoId.toString()
                        Log.d("videoId", "onBindViewHolder: ${list[position].videoId.toString()}")
                        val bundle = Bundle()
                        bundle.putString(ConstValFile.VideoID, videoID)
                        bundle.putString("Rank", "No")
                        val intent = Intent(context, VoterListActivity::class.java)
                        intent.putExtra(ConstValFile.Bundle, bundle)
                        context.startActivity(intent)
                    }
                }else{
                    Toast.makeText(context,ConstValFile.Check_Connection,Toast.LENGTH_SHORT).show()
                }
            }

            itemView.setOnClickListener {
                if (myApplication.isNetworkConnected()){
                    if (list[position].status == "notstarted" || list[position].status == "closed"){
                        Toast.makeText(it.context,"Contest Not Started Yet.", Toast.LENGTH_SHORT).show()
                    }else{
                        val bundle = Bundle()
                        bundle.putString(ConstValFile.USER_ID, list[position].userid)
                        bundle.putString(ConstValFile.ContestID, contestID)
                        Log.d("contestVideo", "userID : "+list[position].userid.toString())
                        Log.d("contestVideo", "contestID : "+list[position].joinleaugeId.toString())
                        bundle.putBoolean(ConstValFile.IsFromContest, true)
                        val intent = Intent(context, CommanVideoPlayActivity::class.java)
                        intent.putExtra(ConstValFile.Bundle, bundle)
                        context.startActivity(intent)
                    }

                }else{
                    Toast.makeText(context,ConstValFile.Check_Connection,Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}
