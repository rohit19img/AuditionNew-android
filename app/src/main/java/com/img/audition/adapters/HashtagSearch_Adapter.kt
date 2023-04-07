package com.img.audition.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.img.audition.R
import com.img.audition.dataModel.Searchgetset

class HashtagSearch_Adapter(val context: Context, val list: ArrayList<Searchgetset.Hashtag>) :
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
        holder.hashtagname.text = "#" + list[position].name
        holder.hashtagplays.text = "2.5M Plays"
        holder.itemView.setOnClickListener(View.OnClickListener {
//            context.startActivity(
//                Intent(context, Hashtag_Activity::class.java)
//                    .putExtra("hashtag", list[position].name)
//                    .putExtra("id", list[position].get_id())
//                    .putExtra("plays", "2.5M Plays")
//            )
        })
    }

    override fun getItemCount(): Int {
        return list.size
    }

}