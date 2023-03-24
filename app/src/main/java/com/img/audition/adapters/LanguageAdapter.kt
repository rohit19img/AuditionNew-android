package com.img.audition.adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.img.audition.R
import com.img.audition.dataModel.Languages
import com.img.audition.databinding.LanguageItemBinding


class LanguageAdapter(val context: Context, val langList: ArrayList<Languages>) : RecyclerView.Adapter<LanguageAdapter.MyViewHolder>() {
    inner class MyViewHolder(itemView: LanguageItemBinding) : RecyclerView.ViewHolder(itemView.root) {
        val langTextView = itemView.langE
        val langHoldCard = itemView.langHoldCard
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : MyViewHolder {
        val itemBinding = LanguageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(itemBinding) }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
       holder.apply {
           langTextView.text = langList[position].language!!.capitalize()
           if (!(langList[position].isSelected)){
               holder.langHoldCard.setBackground(context.getDrawable(R.drawable.cardview_look))
               holder.langTextView.setTextColor(context.getColor(R.color.textColorBlack))
           }else{
               holder.langHoldCard.setBackground(context.getDrawable(R.drawable.card_rummy_design))
               holder.langTextView.setTextColor(context.getColor(R.color.white))
           }

           holder.itemView.setOnClickListener {
               for (zz in langList) zz.isSelected = false
               langList[position].isSelected = true
               notifyDataSetChanged()
           }
       }
    }

    override fun getItemCount(): Int {
       return langList.size
    }
}