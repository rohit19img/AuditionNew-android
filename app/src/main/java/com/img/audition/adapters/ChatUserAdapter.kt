package com.img.audition.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.img.audition.R
import com.img.audition.dataModel.ChatUserData
import com.img.audition.databinding.ChatUserListLayoutBinding
import com.img.audition.screens.MessageActivity

@UnstableApi
class ChatUserAdapter(val context: Context,val chatUserList: ArrayList<ChatUserData>) : RecyclerView.Adapter<ChatUserAdapter.MyViewHolder>() {
    class MyViewHolder(itemView: ChatUserListLayoutBinding)
        : RecyclerView.ViewHolder(itemView.root)
    {
        val userName = itemView.username
        val lastMessage = itemView.lastMsg
        val userImage = itemView.userImageView
        val icMsgSeen = itemView.icMsgSeen
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemBinding = ChatUserListLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(itemBinding)
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.apply {

            val list = chatUserList[position]

            if (list.lastMessage!!.seen!!)
                icMsgSeen.setColorFilter(context.resources.getColor(R.color.fbBlueColor))
            else
                icMsgSeen.visibility = View.GONE



            if (list.name!!.isNotEmpty())
                userName.text = list.name.toString()
            else
                userName.text = list.auditionId.toString()

            lastMessage.text = list.lastMessage!!.message.toString()

            Glide.with(context).load(list.image.toString()).placeholder(R.drawable.person_ic).into(userImage)

            itemView.setOnClickListener {
                context.startActivity(
                    Intent(context, MessageActivity::class.java)
                        .putExtra("name",list.name)
                        .putExtra("userid",list.Id)
                        .putExtra("image",list.image)
                        .putExtra("auditionID",list.auditionId)
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return chatUserList.size
    }

}