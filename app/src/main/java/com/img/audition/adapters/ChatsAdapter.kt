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
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class ChatsAdapter(var context: Context, var list: ArrayList<ChatsGetSet>) :
    RecyclerView.Adapter<ChatsAdapter.MyViewHolder>() {
    var ME_USER = 0
    var OTHER_USER = 1
    val session: SessionManager by lazy{
        SessionManager(context)
    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position].senderId == session.getUserSelfID()) ME_USER else OTHER_USER
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
        holder.message.text = list[position].message
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.forLanguageTag("Hi"))
        val dateFormat1: SimpleDateFormat
        try {
            val calendar = Calendar.getInstance().time
            val dateToday = calendar.date
            Log.i("date", dateFormat.parse(list[position].createdAt).date.toString())
            Log.i("date", dateToday.toString())
            dateFormat1 =
                if (dateFormat.parse(list[position].createdAt).date == dateToday) SimpleDateFormat(
                    "hh:mm aa",
                    Locale.forLanguageTag("Hi")
                ) else SimpleDateFormat("MMM dd, yyyy hh:mm aa", Locale.forLanguageTag("Hi"))
            holder.time.text = dateFormat1.format(dateFormat.parse(list[position].createdAt))
        } catch (e: ParseException) {
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
}