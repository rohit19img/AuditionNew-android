package com.img.audition.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.img.audition.R
import com.img.audition.dataModel.ChatsGetSet
import com.img.audition.network.SessionManager
import java.lang.Exception
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class ChatsAdapter(var context: Context, var list: ArrayList<ChatsGetSet>) :
    RecyclerView.Adapter<ChatsAdapter.MyViewHolder>() {
    private var ME_USER = 0
    private var OTHER_USER = 1


    override fun getItemViewType(position: Int): Int {
        return if (list[position].senderId == SessionManager(context).getUserSelfID()) ME_USER else OTHER_USER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return if (viewType == ME_USER) MyViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.chat_me_user, parent, false)
        ) else MyViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.chat_other_user, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        Log.d("msgDateTime", "onBindViewHolder: ${list[position].createdAt}")

        holder.message.text = list[position].message

       try {
           holder.time.text = formatDateTime(list[position].createdAt)
       }catch (e:Exception){
           e.printStackTrace()
       }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class MyViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var message: TextView
        var time: TextView

        init {
            message = v.findViewById(R.id.message)
            time = v.findViewById(R.id.time)
        }
    }


    private fun formatDateTime(dateTimeString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy MMMM dd hh:mm a", Locale.getDefault())

        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        // Set the timezone to IST
        outputFormat.timeZone = TimeZone.getTimeZone("Asia/Kolkata")

        val date = inputFormat.parse(dateTimeString)
        return outputFormat.format(date)
    }
}