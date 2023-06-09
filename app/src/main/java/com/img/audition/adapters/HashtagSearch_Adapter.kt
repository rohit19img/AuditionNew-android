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
        holder.hashtagplays.text = "${list[position].Videos} Videos"
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

}