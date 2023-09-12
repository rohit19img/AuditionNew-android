package com.img.audition.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.img.audition.R
import com.img.audition.dataModel.SingleContestPriceCard
import com.img.audition.databinding.SubcontestnamerecycledesignBinding
import java.lang.String
import kotlin.Int
import kotlin.apply

class DetailsPrizecardAdapter(val context:Context,val list:ArrayList<SingleContestPriceCard>) : RecyclerView.Adapter<DetailsPrizecardAdapter.MyViewHolder>() {


    inner class MyViewHolder(itemView: SubcontestnamerecycledesignBinding) : RecyclerView.ViewHolder(itemView.root)
    {
        val winnersleader = itemView.winnersleader
       val  priceleader = itemView.priceleader
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailsPrizecardAdapter.MyViewHolder {
        val itemBinding = SubcontestnamerecycledesignBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: DetailsPrizecardAdapter.MyViewHolder, position: Int) {
       holder.apply {
           winnersleader.text = "" + list[position].startPosition
           priceleader.text = String.valueOf("₹ " + list[position].price)
       }
    }


    override fun getItemCount(): Int {
        return list.size
    }
}