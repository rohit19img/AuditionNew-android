package com.img.audition.screens.fragment

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.img.audition.dataModel.NotificationData
import com.img.audition.databinding.NotirecycledesignBinding

class NotificationAdapter(val context: Context,val datalist: ArrayList<NotificationData>) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: NotirecycledesignBinding) : RecyclerView.ViewHolder(itemView.root) {
        val notiTitle = itemView.notification
        val notiTime = itemView.time
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = NotirecycledesignBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemBinding)
    }

    override fun getItemCount(): Int {
       return datalist.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       holder.apply {
            notiTitle.text = datalist[position].title.toString()
           notiTime.text = datalist[position].createdAt.toString()
       }
    }

}
