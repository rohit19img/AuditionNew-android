package com.img.audition.adapters

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import com.img.audition.R
import com.img.audition.dataModel.SearchHashtagsData
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.screens.HashtagVideoActivity
import java.text.DecimalFormat

@UnstableApi class HashtagSearch_Adapter(val context: Context, val list: ArrayList<SearchHashtagsData>) :
    RecyclerView.Adapter<HashtagSearch_Adapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var hashtagname: TextView
        var hashtagplays: TextView

        init {
            hashtagname = itemView.findViewById<TextView>(R.id.hashtagname)
            hashtagplays = itemView.findViewById<TextView>(R.id.hashtagplays)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView: View =
            LayoutInflater.from(context).inflate(R.layout.hahtaglistrecycledesign, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.hashtagname.text =  list[position].Name
        holder.hashtagplays.text = formatCount(list[position].Videos!!) + "Videos"
        holder.itemView.setOnClickListener(View.OnClickListener {
            sendHashTagVideo(list[position].Name.toString())
        })
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private fun sendHashTagVideo(hashTag:String){
        val bundle = Bundle()
        bundle.putString(ConstValFile.VideoHashTag,hashTag)
        val intent = Intent(context, HashtagVideoActivity::class.java)
        intent.putExtra(ConstValFile.Bundle,bundle)
        context.startActivity(intent)
    }

    private fun formatCount(count: Int): String {
        val suffix = charArrayOf(' ', 'k', 'M', 'B', 'T', 'P', 'E')
        val numValue: Long = count.toLong()
        val value = Math.floor(Math.log10(numValue.toDouble())).toInt()
        val base = value / 3
        return if (value >= 3 && base < suffix.size) {
            DecimalFormat("#0.0").format(
                numValue / Math.pow(
                    10.0,
                    (base * 3).toDouble()
                )
            ) + suffix[base]
        } else {
            DecimalFormat("#,##0").format(numValue)
        }
        /*return when {
            count < 1000 -> count.toString()
            count < 10000 -> String.format("%.1fk", Math.floor(count / 100.0) / 10)
            else -> (count / 1000).toString() + "k"
        }*/
    }
}