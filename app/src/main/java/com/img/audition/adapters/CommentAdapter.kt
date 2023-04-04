package com.img.audition.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.img.audition.R
import com.img.audition.dataModel.CommentData
import com.img.audition.databinding.CommentItemDesignBinding
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CommentAdapter(val context: Context,val commentList:ArrayList<CommentData>) : RecyclerView.Adapter<CommentAdapter.CommentHolder>() {
    inner class CommentHolder(itemView: CommentItemDesignBinding) : RecyclerView.ViewHolder(itemView.root) {
        val commentText = itemView.commentText
        val commentTime = itemView.commentTime
        val commentUsername = itemView.commentUsername
        val commentUImage = itemView.commentUImage
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentHolder {
        val itemBinding = CommentItemDesignBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentHolder(itemBinding)
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    override fun onBindViewHolder(holder: CommentHolder, position: Int) {
      holder.apply {
          val list = commentList[position]
          commentText.text = list.comment.toString()
          Glide.with(context).load(list.userImage).placeholder(R.drawable.person_ic).into(commentUImage)
          commentUsername.text = list.commentBy.toString()

          val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.forLanguageTag("Hi"))
          val dateFormat1: SimpleDateFormat
          try {
              val calendar = Calendar.getInstance().time
              val dateToday = calendar.date
              Log.i("date", dateFormat.parse(list.createdAt.toString()).date.toString())
              Log.i("date", dateToday.toString())
              dateFormat1 =
                  if (dateFormat.parse(list.createdAt.toString()).date == dateToday) SimpleDateFormat(
                      "hh:mm aa",
                      Locale.forLanguageTag("Hi")
                  ) else SimpleDateFormat("MMM dd, yyyy hh:mm aa", Locale.forLanguageTag("Hi"))
              commentTime.text = dateFormat1.format(dateFormat.parse(list.createdAt.toString()))
          } catch (e: ParseException) {
              e.printStackTrace()
          }
      }
    }
}