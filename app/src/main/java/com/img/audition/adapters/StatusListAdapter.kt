package com.img.audition.adapters

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request.Method
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.img.audition.StoryView.MyStory
import com.img.audition.StoryView.StoryView
import com.img.audition.dataModel.StatusData
import com.img.audition.databinding.StatusSingleListBinding
import com.img.audition.network.APITags
import com.img.audition.network.SessionManager
import omari.hamza.storyview.callback.StoryClickListeners
import omari.hamza.storyview.utils.StoryViewHeaderInfo
import java.text.SimpleDateFormat
import java.util.*

class StatusListAdapter(val context: Context, val list : ArrayList<StatusData>, val fm : FragmentManager) : RecyclerView.Adapter<StatusListAdapter.StatusViewHolder>() {

    val TAG : String = "StatusListAdapter"
    private val session by lazy {
        SessionManager(context)
    }

    val requestQueue by lazy {
        Volley.newRequestQueue(context)
    }

    inner class StatusViewHolder(v : StatusSingleListBinding) : RecyclerView.ViewHolder(v.root){
        val userImage  = v.userImage
        val userName  = v.userName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusViewHolder {
        return StatusViewHolder(StatusSingleListBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return  list.size
    }

    override fun onBindViewHolder(holder: StatusViewHolder, position: Int) {
        holder.userName.text = list[position].name
        Glide.with(context).load(list[position].image).into(holder.userImage)

        holder.itemView.setOnClickListener {
            val myStories: ArrayList<MyStory> = ArrayList()

            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSS")
            simpleDateFormat.timeZone = TimeZone.getTimeZone("GMT+5:30")

            var infolist: ArrayList<StoryViewHeaderInfo> = ArrayList()

            for(data in list[position].status!!){

                Log.i("id","Data id : ${data._id}")

                val ago: String = DateUtils.getRelativeTimeSpanString(
                    simpleDateFormat.parse(data.createdAt).time,
                    Calendar.getInstance().timeInMillis,
                    DateUtils.MINUTE_IN_MILLIS
                ).toString()

                infolist.add(StoryViewHeaderInfo(list[position].name,ago,list[position].image))
                myStories.add(MyStory(data.media,data.seenBy!!.size))
            }

            Log.d(TAG, "Size : ${myStories.size}")

            StoryView.Builder(fm)
                .setStoriesList(myStories)
                .setStoryDuration(15000)
                .setStoryClickListeners(object : StoryClickListeners {
                    override fun onDescriptionClickListener(position: Int) {

                    }

                    override fun onTitleIconClickListener(position: Int) {

                    }
                })
                .setOnStoryChangedCallback { it
                    ViewStatus(list[position].status?.get(it)?._id!!)
                }
                .setStartingIndex(0)
                .setHeadingInfoList(infolist)
                .build()
                .show()
        }

    }

    fun ViewStatus(id : String){
        val url = APITags.APIBASEURL+"view-status"
        val strRequest: StringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                Log.i("response",response)
            },
            Response.ErrorListener { error ->
                Log.i("ErrorResponce", error.toString())
                val networkResponse = error.networkResponse
                if (networkResponse != null) {
                    // HTTP Status Code: 401 Unauthorized
                } else {
                    ViewStatus(id)
                }
            }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["Authorization"] = session.getToken()!!
                Log.i("Header", params.toString())
                return params
            }

            override fun getParams(): MutableMap<String, String>? {
                val params: MutableMap<String, String> = HashMap()
                params["id"] = id
                Log.i("params", params.toString())
                return params
            }
        }
        strRequest.setShouldCache(false)
        strRequest.retryPolicy = DefaultRetryPolicy(
            0,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        requestQueue!!.add(strRequest)
    }
}