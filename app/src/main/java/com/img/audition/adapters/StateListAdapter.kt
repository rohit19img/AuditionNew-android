package com.img.audition.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.img.audition.R

class StateListAdapter(val context: Context,val stateList: Array<String>) : BaseAdapter() {


    override fun getCount(): Int {
        return stateList.size
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
       return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val v: View

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        v = inflater.inflate(R.layout.statelist_textview, null)

        val spinnerText: TextView = v.findViewById<View>(R.id.spinnerText) as TextView
        spinnerText.setText(stateList.get(position))


        return v
    }
}