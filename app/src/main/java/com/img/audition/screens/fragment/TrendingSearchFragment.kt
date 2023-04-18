package com.img.audition.screens.fragment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.JsonObject
import com.img.audition.R
import com.img.audition.adapters.*
import com.img.audition.dataModel.Searchgetset
import com.img.audition.dataModel.TrendingVideoData
import com.img.audition.dataModel.TrendingVideoResponse
import com.img.audition.dataModel.VideoData
import com.img.audition.databinding.FragmentTrendingSearchBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class TrendingSearchFragment(val contextFromHome: Context) : Fragment() {

    val bannerList = arrayListOf(
        R.drawable.banner3,
        R.drawable.banner1,
        R.drawable.banner2,
        R.drawable.banner4
    )
    val timer = Timer()

    val TAG = "TrendingSearchFragment"
    private val sessionManager by lazy {
        SessionManager(contextFromHome)
    }
    private val myApplication by lazy {
        MyApplication(contextFromHome)
    }

    private val apiInterface by lazy{
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }

    var userlist: ArrayList<Searchgetset.User>? = null
    var hashtaglist: ArrayList<Searchgetset.Hashtag>? = null
    var videolist: ArrayList<VideoData>? = null

    private lateinit var _viewBinding : FragmentTrendingSearchBinding
    private val view get() = _viewBinding!!

    private var trendHashVideoList = ArrayList<TrendingVideoData>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _viewBinding = FragmentTrendingSearchBinding.inflate(inflater,container,false)

        _viewBinding.apply {
            userRecycle.layoutManager = GridLayoutManager(contextFromHome,2)
            hashtagRecycle.layoutManager = GridLayoutManager(contextFromHome,1)
            videoRecycle.layoutManager = GridLayoutManager(contextFromHome,3)

            searchBar.addTextChangedListener(
                object : TextWatcher{
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {

                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        if(searchBar.text.toString().length > 2){
                            searchlist(searchBar.text.toString())
                        }
                    }

                    override fun afterTextChanged(s: Editable?) {

                    }
                }
            )

            searchBar.setOnEditorActionListener { v, actionId, event ->
                if(actionId == EditorInfo.IME_ACTION_SEARCH){
                    searchlist(searchBar.text.toString())
                    true
                } else {
                    false
                }
            }
        }


        return _viewBinding.root
    }

    override fun onViewCreated(view1: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view1, savedInstanceState)
        getTrendingVideo()
        searchlist("")

        val imagSlider = ImageSlider(view1.context,bannerList)
        view.bannerSlider.adapter = imagSlider

        timer.scheduleAtFixedRate(BannerSlider(),2000,3000)

    }
    fun searchlist(searchData : String){
        val obj = JsonObject()
        obj.addProperty("search", searchData)
        Log.i("request",obj.toString())

        val responseCall: Call<Searchgetset> =
            apiInterface.search(sessionManager.getToken(), obj)
        responseCall.enqueue( object : Callback<Searchgetset> {
            override fun onResponse(call: Call<Searchgetset>, response: Response<Searchgetset>) {
                if (response.isSuccessful) {
                    response.body()!!.message

                    userlist = ArrayList()
                    hashtaglist = ArrayList()
                    videolist = ArrayList()

                    if(response.body()!!.success){
                        userlist = response.body()!!.data!!.users
                        hashtaglist = response.body()!!.data!!.hashtags
                        videolist = response.body()!!.data!!.data

                        Log.i("list_size", "Users : " + userlist!!.size)
                        Log.i("list_size", "hashtag : " + hashtaglist!!.size)
                        Log.i("list_size", "Video : " + videolist!!.size)

                    }
                } else {
                    myApplication.showToast("Something went wrong!!")
                }

                view.userRecycle.adapter = UserSearch_Adapter(userlist!!,contextFromHome)
                view.hashtagRecycle.adapter = HashtagSearch_Adapter(contextFromHome, hashtaglist!!)
                view.videoRecycle.adapter = VideoSearch_Adapter(contextFromHome, videolist!!)

            }

            override fun onFailure(call: Call<Searchgetset>, t: Throwable) {
                myApplication.printLogD("text", t.message!!)
            }
        })

    }

    private fun getTrendingVideo() {
        val trendingVideoReq  = apiInterface.getTrendingVideo(sessionManager.getToken())
        trendingVideoReq.enqueue(object : Callback<TrendingVideoResponse> {
            override fun onResponse(call: Call<TrendingVideoResponse>, response: Response<TrendingVideoResponse>) {

                if (response.isSuccessful && response.body()!!.success!!) {

                    val data = response.body()!!.data
                    trendHashVideoList.add(data!!)
                    myApplication.printLogD(trendHashVideoList.size.toString(),"trending 1")
                    val adapter = TrendingHashtag(contextFromHome,trendHashVideoList)
                    view.trendingHashtagCycle.adapter = adapter
                }else{
                    myApplication.printLogE(response.toString(),TAG)
                }

            }

            override fun onFailure(call: Call<TrendingVideoResponse>, t: Throwable) {
               myApplication.printLogE(t.message.toString(),TAG)
            }
        })
    }

    inner class BannerSlider : TimerTask(){
        override fun run() {
            (contextFromHome as Activity).runOnUiThread(Runnable {
                if (view.bannerSlider.currentItem < bannerList.size - 1) {
                    view.bannerSlider.currentItem =view.bannerSlider.currentItem + 1
                } else view.bannerSlider.currentItem = 0
            })
        }

    }
}