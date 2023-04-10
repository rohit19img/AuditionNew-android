package com.img.audition.adapters

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.img.audition.dataModel.CommanResponse
import com.img.audition.dataModel.VoteData
import com.img.audition.databinding.VoteItemHolderDesignBinding
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VoteAdapter(val contextFromActivity: Context,val voteList: ArrayList<VoteData>,val id:String,val voteDialog:Dialog) : RecyclerView.Adapter<VoteAdapter.ViewHolder>()
{
    val apiInterface = RetrofitClient.getInstance().create(ApiInterface::class.java)
    val  sessionManager = SessionManager(contextFromActivity.applicationContext)
    inner class ViewHolder(itemView: VoteItemHolderDesignBinding) : RecyclerView.ViewHolder(itemView.root){
        val voteEmoji = itemView.voteImage
        val voteText = itemView.voteText
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
                voteText.text = data.vote + "Vote"


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
        val voteReq = apiInterface.voteToUserVideo(sessionManager.getToken(),obj)

        voteReq.enqueue(object : Callback<CommanResponse>{
            override fun onResponse(call: Call<CommanResponse>, response: Response<CommanResponse>) {
               if (response.isSuccessful && response.body()!!.success!!){
                  showToast(response.body()!!.message.toString())
                   voteDialog.dismiss()
               }else{
                   Log.e("Vote Adapter", "onResponse: $response")
               }
            }

            override fun onFailure(call: Call<CommanResponse>, t: Throwable) {
                Log.e("Vote Adapter", "onFailure: $t")
            }

        })
    }

    override fun getItemCount(): Int {
       return voteList.size
    }

    private fun showToast(msg: String) {
        Toast.makeText(contextFromActivity.applicationContext,msg, Toast.LENGTH_SHORT).show()
    }


}
