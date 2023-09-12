package com.img.audition.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.img.audition.R
import com.img.audition.dataModel.CommentData
import com.img.audition.databinding.CommentItemDesignBinding
import java.util.*
import kotlin.collections.ArrayList

@UnstableApi class CommentAdapter(val context: Context, private val commentList: ArrayList<CommentData>,private val videoAdapter: VideoAdapter) : RecyclerView.Adapter<CommentAdapter.CommentHolder>() {



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

          Log.d("check date", "onBindViewHolder: ${list.createdAt.toString()}")
          commentTime.text = list.createdAt.toString()

          itemView.setOnLongClickListener {
              videoAdapter.deleteComment(list.comment_id!!,position)
              false
          }
      }
    }
}