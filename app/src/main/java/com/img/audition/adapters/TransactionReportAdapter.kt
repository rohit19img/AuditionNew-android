package com.img.audition.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.img.audition.R
import com.img.audition.dataModel.TransactionData
import kotlin.Int

class TransactionReportAdapter(val context: Activity, private val transData: ArrayList<TransactionData>) :
    RecyclerView.Adapter<TransactionReportAdapter.MyViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(R.layout.transaction_report_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.apply {
            val data = transData[position]
            transType.text = data.type
            transAmt.text = data.amount.toString()
            }
        }


    override fun getItemCount(): Int {
        return transData.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var transType: TextView
        var transAmt: TextView

        init {
            transAmt = itemView.findViewById<TextView>(R.id.transAmt)
            transType = itemView.findViewById<TextView>(R.id.transType)
        }
    }
}
