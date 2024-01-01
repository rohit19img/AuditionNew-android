package com.img.audition.paymentGateway

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.img.audition.databinding.PaymentAppItemBinding

class PaymentAppListAdapter(
    private val paymentAppList: List<PaymentAppModel>,
    private val onPaymentAppClick :(clickedPaymentApp:PaymentAppModel)-> Unit
) : RecyclerView.Adapter<PaymentAppListAdapter.ViewHolder>() {
    class ViewHolder(itemView: PaymentAppItemBinding) : RecyclerView.ViewHolder(itemView.root) {
        val paymentAppImage = itemView.paymentAppImage
        val paymentAppName = itemView.paymentAppName
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val binding = PaymentAppItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            paymentAppList[position].let { items->
                paymentAppImage.setImageResource(items.appIcon)
                paymentAppName.text = items.applicationName

                itemView.setOnClickListener {
                    onPaymentAppClick(items)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return paymentAppList.size
    }
}