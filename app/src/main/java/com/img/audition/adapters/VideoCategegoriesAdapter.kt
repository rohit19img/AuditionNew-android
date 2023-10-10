package com.img.audition.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.img.audition.dataModel.LiveContestData
import com.img.audition.dataModel.VideoCategorisData
import com.img.audition.databinding.CategoryListRecyclerBinding
import com.img.audition.network.APITags
import com.img.audition.screens.ContestVideoActivity

class VideoCategegoriesAdapter(val context: Context,val list: ArrayList<VideoCategorisData>) :
    RecyclerView.Adapter<VideoCategegoriesAdapter.CategoriesViewHolder>() {

    inner class CategoriesViewHolder(v : CategoryListRecyclerBinding): RecyclerView.ViewHolder(v.root) {
        val img = v.img
        val categoryName = v.categoryName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriesViewHolder {
        val itemBinding = CategoryListRecyclerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoriesViewHolder(itemBinding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: CategoriesViewHolder, position: Int) {
        holder.apply {
            categoryName.text = list[position].contest_name
            Glide.with(context).load( list[position].file).into(img)

            itemView.setOnClickListener {
                context.startActivity(
                    Intent(context, ContestVideoActivity::class.java)
                        .putExtra("_id",list[position]._id)
                )
            }
        }
    }

}