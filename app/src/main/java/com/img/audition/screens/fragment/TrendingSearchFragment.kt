package com.img.audition.screens.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import com.img.audition.R
import com.img.audition.adapters.*
import com.img.audition.dataModel.*
import com.img.audition.databinding.FragmentTrendingSearchBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.screens.SplashActivity
import com.img.audition.viewModel.MainViewModel
import com.img.audition.viewModel.Status
import com.img.audition.viewModel.ViewModelFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

@UnstableApi
class TrendingSearchFragment(val contextFromHome: Context) : Fragment() {

    private var adapterTrending: TrendingHashtag? = null
    private val bannerList = arrayListOf(
        R.drawable.banner3,
        R.drawable.banner1,
        R.drawable.banner2,
        R.drawable.banner4
    )
    private val timer = Timer()

    private val TAG = "TrendingSearchFragment"

    private val sessionManager by lazy {
        SessionManager(contextFromHome)
    }
    private var userlist: ArrayList<SearchUserData>? = null
    private var hashtaglist: ArrayList<SearchHashtagsData>? = null
    private var videolist: ArrayList<VideoData>? = null

    private lateinit var _viewBinding : FragmentTrendingSearchBinding
    private val view get() = _viewBinding

    private var trendHashVideoList = ArrayList<TrendingVideoData>()
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _viewBinding = FragmentTrendingSearchBinding.inflate(inflater,container,false)

        _viewBinding.apply {
            userRecycle.layoutManager = GridLayoutManager(contextFromHome,2)
            hashtagRecycle.layoutManager = GridLayoutManager(contextFromHome,1)
            videoRecycle.layoutManager = GridLayoutManager(contextFromHome,3)

            searchBar.addTextChangedListener(
                object : TextWatcher{
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        searchlist(s.toString())
                    }
                    override fun afterTextChanged(s: Editable?) {}
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

        val apiInterface = RetrofitClient.getInstance().create(ApiInterface::class.java)
        mainViewModel = ViewModelProvider(this, ViewModelFactory(sessionManager.getToken(),apiInterface))[MainViewModel::class.java]

        return _viewBinding.root
    }

    override fun onViewCreated(view1: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view1, savedInstanceState)

        view.shimmerVideoView.startShimmer()
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

        mainViewModel.getSearchData(obj)
            .observe(viewLifecycleOwner){
                it.let {resources->
                    when(resources.status){
                        Status.SUCCESS ->{
                            if(resources.data!!.success!!){
                                userlist = ArrayList()
                                hashtaglist = ArrayList()
                                videolist = ArrayList()
                                userlist = resources.data.data!!.users
                                hashtaglist = resources.data.data!!.hashtags
                                videolist = resources.data.data!!.data

                                Log.i("list_size", "Users : " + userlist!!.size)
                                Log.i("list_size", "hashtag : " + hashtaglist!!.size)
                                Log.i("list_size", "Video : " + videolist!!.size)

                                view.userRecycle.adapter = UserSearch_Adapter(userlist!!,contextFromHome,"")
                                view.hashtagRecycle.adapter = HashtagSearch_Adapter(contextFromHome, hashtaglist!!)
                                view.videoRecycle.adapter = VideoSearch_Adapter(contextFromHome, videolist!!)
                            }else{
                                Toast.makeText(contextFromHome,"Something went wrong..", Toast.LENGTH_SHORT).show()
                            }
                        }
                        Status.LOADING ->{
                            Log.d(TAG, resources.status.toString())
                        }
                        else->{
                            if (resources.message!!.contains("401")){
                                sessionManager.clearLogoutSession()
                                startActivity(Intent(contextFromHome, SplashActivity::class.java))
                                requireActivity().finishAffinity()
                            }
                            Log.e(TAG, resources.status.toString())
                        }
                    }
                }
            }
        view.userRecycle.adapter?.notifyDataSetChanged()
        view.hashtagRecycle.adapter?.notifyDataSetChanged()
        view.videoRecycle.adapter?.notifyDataSetChanged()
    }

    private fun getTrendingVideo(){
        mainViewModel.getTrendingVideo()
            .observe(viewLifecycleOwner){
                it.let {videoResponse->
                    when(videoResponse.status){
                        Status.SUCCESS ->{
                            if (videoResponse.data?.success!!){
                                val data = videoResponse.data.data!!
                                trendHashVideoList.add(data)
                                 adapterTrending = TrendingHashtag(contextFromHome,trendHashVideoList)
                                view.shimmerVideoView.stopShimmer()
                                view.shimmerVideoView.hideShimmer()
                                view.shimmerVideoView.visibility = View.GONE
                                view.trendingHashtagCycle.visibility = View.VISIBLE
                                view.trendingHashtagCycle.adapter = adapterTrending
                            }
                        }
                        Status.LOADING ->{
                            Log.d(TAG, videoResponse.status.toString())
                        }
                        else->{
                            if (videoResponse.message!!.contains("401")){
                                sessionManager.clearLogoutSession()
                                startActivity(Intent(contextFromHome, SplashActivity::class.java))
                                requireActivity().finishAffinity()
                            }
                            Log.e(TAG, videoResponse.status.toString())
                        }
                    }
                }
            }
    }

    inner class BannerSlider : TimerTask(){
        override fun run() {
            (contextFromHome as Activity).runOnUiThread {
                if (view.bannerSlider.currentItem < bannerList.size - 1) {
                    view.bannerSlider.currentItem = view.bannerSlider.currentItem + 1
                } else view.bannerSlider.currentItem = 0
            }
        }

    }

    override fun onDestroyView() {

        try {
            videolist?.clear()
            userlist?.clear()
            hashtaglist?.clear()
            trendHashVideoList.clear()
            view.userRecycle.adapter = null
            view.hashtagRecycle.adapter = null
            view.trendingHashtagCycle.adapter = null
            view.videoRecycle.adapter = null
            adapterTrending = null
            bannerList.clear()
            timer.cancel()
        }catch (e:Exception){
            e.printStackTrace()
        }
        Log.d("check 400", "onDestroyView: $TAG")
        getView()?.destroyDrawingCache()
        super.onDestroyView()
    }
}