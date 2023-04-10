package com.img.audition.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.img.audition.R
import com.img.audition.dataModel.ReportCategoryData
import com.img.audition.databinding.CommanlistCycleDesignBinding

class ReportCategoryAdapter(val context:Context,val categoryList: ArrayList<ReportCategoryData>,val vID:String) : RecyclerView.Adapter<ReportCategoryAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: CommanlistCycleDesignBinding) : RecyclerView.ViewHolder(itemView.root) {
        val catName = itemView.text
        val reportCard = itemView.reportCard
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemBinding = CommanlistCycleDesignBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(itemBinding)
    }

    override fun getItemCount(): Int {
       return categoryList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.apply {
            catName.text = categoryList[position].name.toString()

            if (!(categoryList[position].isSelected)){
                holder.reportCard.background = context.getDrawable(R.drawable.cardview_look)
                holder.catName.setTextColor(context.getColor(R.color.textColorBlack))
            }else{
                holder.reportCard.background = context.getDrawable(R.drawable.card_rummy_design)
                holder.catName.setTextColor(context.getColor(R.color.white))
            }

            holder.itemView.setOnClickListener {
                for (zz in categoryList) {
                    zz.isSelected = false
                }
                categoryList[position].isSelected = true
                notifyDataSetChanged()
            }
        }
    }

}