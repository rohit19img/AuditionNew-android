package com.img.audition.adapters

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.gson.JsonObject
import com.img.audition.R
import com.img.audition.dataModel.CommanResponse
import com.img.audition.dataModel.VoteData
import com.img.audition.dataModel.Votes
import com.img.audition.databinding.VoteItemHolderDesignBinding
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Timer
import java.util.TimerTask
import java.util.logging.Handler
import kotlin.concurrent.thread

class VoteAdapter(
    private val contextFromActivity: Context,
    private val voteList: ArrayList<VoteData>,
    val id: String,
    val voteDialog: Dialog,
    val videoAdapter: VideoAdapter,
    val position: Int,
    val votes: ArrayList<Votes>

) : RecyclerView.Adapter<VoteAdapter.ViewHolder>()
{
    inner class ViewHolder(itemView: VoteItemHolderDesignBinding) : RecyclerView.ViewHolder(itemView.root){
        val voteEmoji = itemView.voteImage
        val voteText = itemView.voteText
        val voteCount = itemView.voteCount
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = VoteItemHolderDesignBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            try {
                val data = voteList[position]
                voteEmoji.text = data.emoji
                voteText.text = "${data.vote} Votes"
                voteCount.text = votes[position].uservotes


                itemView.setOnClickListener {
                    vote(id, data.likeCategory)
                }
            }catch (e:java.lang.Exception){
                Log.e("VoteAdapter", "onBindViewHolder: ", e.cause)
            }
        }
    }

    private fun vote(id: String, likeCategory: String?) {
        val obj = JsonObject()
        obj.addProperty("videoid", id)
        obj.addProperty("like_category", likeCategory)
        val apiInterface = RetrofitClient.getInstance().create(ApiInterface::class.java)

        val voteReq = apiInterface.voteToUserVideo(SessionManager(contextFromActivity).getToken(),obj)

        val animDialog = Dialog(contextFromActivity)
        animDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        animDialog.setContentView(R.layout.hurray_anim_dialog_layout)

        val hurrayAnim = animDialog.findViewById<LottieAnimationView>(R.id.hurray_anim)
        animDialog.window!!.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        animDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        voteReq.enqueue(object : Callback<CommanResponse>{
            override fun onResponse(call: Call<CommanResponse>, response: Response<CommanResponse>) {
               if (response.isSuccessful){
                   if (response.body()!!.success!!){
                       animDialog.show()
                       videoAdapter.videoList[position].voteStatus = true
                       videoAdapter.notifyItemChanged(position)
                       voteDialog.dismiss()
                   }else{
                       showToast(response.body()!!.message.toString())
                       voteDialog.dismiss()
                   }
               }else{
                   Log.e("Vote Adapter", "onResponse: $response")
                   voteDialog.dismiss()
               }
            }

            override fun onFailure(call: Call<CommanResponse>, t: Throwable) {
                Log.e("Vote Adapter", "onFailure: $t")
                voteDialog.dismiss()
            }

        })

        Timer().schedule(object : TimerTask(){
            override fun run() {
                if (animDialog.isShowing){
                    animDialog.dismiss()
                }
            }
        },2000)
    }

    override fun getItemCount(): Int {
       return voteList.size
    }

    private fun showToast(msg: String) {
        Toast.makeText(contextFromActivity.applicationContext,msg, Toast.LENGTH_SHORT).show()
    }


}
