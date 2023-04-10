package com.img.audition.adapters

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.img.audition.R
import com.img.audition.dataModel.OfferData
import com.img.audition.screens.AddAmountActivity
import kotlin.Int

class AddCashOfferAdapter(val context: Activity, val offerData: ArrayList<OfferData>?) :
    RecyclerView.Adapter<AddCashOfferAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context.applicationContext)
                .inflate(R.layout.offer_details_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.apply {
            if (offerData != null) {
               val data = offerData!![position]
                offerCode.text = data.offerCode.toString()
                offerMaxAmt.text = "Max Amount :"+data.maxAmount.toString()
                offerMinAmt.text = "Min Amount :"+data.minAmount.toString()
                offerUseTimes.text = "Use Only :"+data.userTime.toString() +" Times"
                offerTitle.text = data.title.toString()
                if (data.bonusType.equals("per",true)) {
                    offerMsg.text =  "You will get "+data.bonus.toString() +" %"
                }else {
                    offerMsg.text =  "You will get "+data.bonus.toString() +" ₹"
                }


            }
        }

        holder.itemView.setOnClickListener {
            if (holder.addMoney.text.toString() != "") {
                val addCashAmt = holder.addMoney.text.toString().trim { it <= ' ' }.toInt()
                if (addCashAmt >= offerData!![position].minAmount!!) {
                    if (context is AddAmountActivity) {
                        (context as AddAmountActivity).offerId = ""
                        (context as AddAmountActivity).offerMinAmt = 0
                        (context as AddAmountActivity).offerMaxAmt = 0
                        (context as AddAmountActivity).ifOfferApplied = false
                    }
                    holder.applyCode.visibility = View.GONE
                    Toast.makeText(
                        context.applicationContext,
                        "Add Cash More Then : " +offerData!![position].minAmount.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (addCashAmt <= offerData!![position].maxAmount!!) {
                    if (context is AddAmountActivity) {
                        (context as AddAmountActivity).offerId = ""
                        (context as AddAmountActivity).offerMinAmt = 0
                        (context as AddAmountActivity).offerMaxAmt = 0
                        (context as AddAmountActivity).ifOfferApplied = false
                    }
                    holder.applyCode.visibility = View.GONE
                    Toast.makeText(
                        context.applicationContext,
                        "Add Cash Less Then : " + offerData!![position].maxAmount.toString(),
                        Toast.LENGTH_SHORT).show()
                } else {
                    holder.applyCode.visibility = View.GONE
                    holder.applyCode.visibility = View.VISIBLE
                    if (context is AddAmountActivity) {
                        (context as AddAmountActivity).offerId = offerData!![position].Id.toString()
                        (context as AddAmountActivity).offerMinAmt =
                            offerData!![position].minAmount!!
                        (context as AddAmountActivity).offerMaxAmt =
                            offerData!![position].maxAmount!!
                        (context as AddAmountActivity).ifOfferApplied = true
                    }
                }
            } else {
                if (context is AddAmountActivity) {
                    (context as AddAmountActivity).offerId = ""
                    (context as AddAmountActivity).offerMinAmt = 0
                    (context as AddAmountActivity).offerMaxAmt = 0
                    (context as AddAmountActivity).ifOfferApplied = false
                }
                holder.applyCode.visibility = View.GONE
                Toast.makeText(context.applicationContext, "Enter Amount", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun getItemCount(): Int {
        Log.d("offerListSize", "getItemCount: ${offerData!!.size}")
        return offerData!!.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var offerTitle: TextView
        var offerMsg: TextView
        var offerCode: TextView
        var offerUseTimes: TextView
        var offerMinAmt: TextView
        var offerMaxAmt: TextView
        var applyCode: TextView
        var addMoney: EditText

        init {
            offerCode = itemView.findViewById<TextView>(R.id.offerCode)
            offerTitle = itemView.findViewById<TextView>(R.id.offerTitle)
            offerMsg = itemView.findViewById<TextView>(R.id.offerMsg)
            offerUseTimes = itemView.findViewById<TextView>(R.id.offerUseTimes)
            offerMinAmt = itemView.findViewById<TextView>(R.id.offerMinAmt)
            offerMaxAmt = itemView.findViewById<TextView>(R.id.offerMaxAmt)
            applyCode = context.findViewById<TextView>(R.id.applyCode)
            addMoney = context.findViewById<EditText>(R.id.addMoney)
        }
    }
}
