package com.img.audition.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.media3.common.C
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.img.audition.R
import com.img.audition.dataModel.CommonResponse
import com.img.audition.dataModel.ReportCategoryData
import com.img.audition.databinding.CommanlistCycleDesignBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.screens.CameraActivity
import com.img.audition.screens.fragment.VideoReportDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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