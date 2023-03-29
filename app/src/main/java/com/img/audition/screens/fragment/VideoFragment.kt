package com.img.audition.screens.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.fragment.app.Fragment
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.img.audition.adapters.LanguageSelecteDialog
import com.img.audition.adapters.VideoAdapter
import com.img.audition.dataModel.UserLatLang
import com.img.audition.dataModel.VideoData
import com.img.audition.dataModel.VideoResponse
import com.img.audition.databinding.FragmentMusicListBinding
import com.img.audition.databinding.FragmentVideoBinding
import com.img.audition.globalAccess.AppPermission
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.screens.SplashActivity
import com.img.audition.videoWork.VideoItemPlayPause
import com.img.audition.videoWork.playPauseVideo
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class VideoFragment(context: Context) : Fragment() {

    val TAG = "VideoFragment"
    val TRACK = "Check 100"
    var videoList1 = ArrayList<VideoData>()
    val videoList2 = ArrayList<VideoData>()

    private lateinit var _viewBinding : FragmentVideoBinding
    private val view get() = _viewBinding

    lateinit var appPermission : AppPermission
    lateinit var fusedLocation : FusedLocationProviderClient
    lateinit var userLatLang: UserLatLang
    lateinit var locationManager: LocationManager
    var authToken = ""
    private val sessionManager by lazy {
        SessionManager(context)
    }
    private val myApplication by lazy {
        MyApplication(context)
    }

    lateinit var viewPager : ViewPager2
    lateinit var videoAdapter: VideoAdapter
    lateinit var videoItemPlayPause:VideoItemPlayPause
    lateinit var videoShimmerEffect:ShimmerFrameLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TRACK, "onCreate: ")
        appPermission =  AppPermission(requireActivity(),ConstValFile.PERMISSION_LIST,ConstValFile.REQUEST_PERMISSION_CODE)
        fusedLocation = LocationServices.getFusedLocationProviderClient(requireActivity())
        authToken = sessionManager.getToken().toString()


        userLatLang = UserLatLang()
        myApplication.printLogD(userLatLang.lat.toString(),"lat $TAG")
        myApplication.printLogD(userLatLang.long.toString(),"long $TAG")

        val selectedLanguage = sessionManager.getSelectedLanguage()
        if (selectedLanguage.equals("")){
            myApplication.printLogI("Show Language Dialog",TAG)
            val showLangDialog = LanguageSelecteDialog()
            showLangDialog.show(parentFragmentManager,showLangDialog.tag)
        }else{
            if (myApplication.isNetworkConnected()) {

            }else{
                myApplication.showToast(ConstValFile.Check_Connection)
            }

        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _viewBinding = FragmentVideoBinding.inflate(inflater,container,false)


        Log.d(TRACK, "onCreateView: ")



        return _viewBinding.root
    }

    override fun onViewCreated(view1: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view1, savedInstanceState)
        viewPager = view.videoViewpager2

        viewPager.offscreenPageLimit = 2
        videoShimmerEffect =  view.showVideoShimmer as ShimmerFrameLayout
        videoShimmerEffect.visibility = View.VISIBLE
        videoShimmerEffect.startShimmer()

    }



    private fun showReels() {
        val apiInterface = RetrofitClient.getInstance().create(ApiInterface::class.java)
        var apiVideoRequest: Call<VideoResponse>? = null
        myApplication.printLogD(authToken, ConstValFile.TOKEN)

        apiVideoRequest = if (sessionManager.getSelectedLanguage()!="" && userLatLang.lat!=null && userLatLang.long!=null ){
            apiInterface.getVideo(authToken,
                sessionManager.getSelectedLanguage(),userLatLang.lat,userLatLang.long)
        } else if (sessionManager.getSelectedLanguage()!=""){
            apiInterface.getVideo(authToken,sessionManager.getSelectedLanguage(),null,null)
        }else{
            apiInterface.getVideo(authToken,null,userLatLang.lat,userLatLang.long)
        }


        apiVideoRequest.enqueue(@UnstableApi object : Callback<VideoResponse> {
            override fun onResponse(call: Call<VideoResponse>, response: Response<VideoResponse>) {
                if (response.code()==401){
                    sessionManager.clearLogoutSession()
                    startActivity(Intent(context, SplashActivity::class.java))
                    finishAffinity(requireActivity())
                }else{
                    if (response.isSuccessful && response.body()!!.success!! && response.body()!=null){
                        myApplication.printLogD(response.toString(),TAG)
                        videoList1 = response.body()!!.data
                        videoShimmerEffect.stopShimmer()
                        videoShimmerEffect.hideShimmer()
                        videoShimmerEffect.visibility = View.GONE
                        if (videoList2.size == 0){
                            videoList2.addAll(videoList1)
                            videoAdapter = VideoAdapter(context!!,videoList2)
                            viewPager.adapter = videoAdapter
                            videoItemPlayPause = videoAdapter.onActivityStateChanged()

                        }else{
                            videoList2.addAll(videoList1)
                            videoAdapter.notifyItemInserted(videoList2.size - 1)
                            videoItemPlayPause = videoAdapter.onActivityStateChanged()

                        }

                    }else{
                        myApplication.printLogE("GetVideo Response Failed ${response.code()}",TAG)
                    }
                }
            }

            override fun onFailure(call: Call<VideoResponse>, t: Throwable) {
                myApplication.printLogE(t.message.toString(),TAG)
            }

        })
    }

    override fun onAttach(context: Context) {

        Log.d(TRACK, "onAttach: ")

        super.onAttach(context)
    }

    override fun onDetach() {

        Log.d(TRACK, "onDetach: ")
        super.onDetach()
    }

    override fun onDestroy() {
        Log.d(TRACK, "onDestroy: ")
        super.onDestroy()

    }

    override fun onPause() {
        Log.d(TRACK, "onPause: ")
        try {
            val cPos = viewPager.currentItem
            val holder: VideoAdapter.VideoViewHolder = (viewPager.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(cPos) as VideoAdapter.VideoViewHolder
            videoItemPlayPause.onPause(holder,cPos)
        }catch (e:Exception){
            myApplication.printLogE(e.message.toString(),TAG)
        }
        super.onPause()
    }

    override fun onStop() {
        Log.d(TRACK, "onStop: ")
       try {
           val cPos = viewPager.currentItem
           val holder: VideoAdapter.VideoViewHolder = (viewPager.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(cPos) as VideoAdapter.VideoViewHolder
           videoItemPlayPause.onPause(holder,cPos)
       }catch (e:Exception){
           myApplication.printLogE(e.message.toString(),TAG)
       }
        super.onStop()

    }

    override fun onResume() {
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                if (position == videoList2.size-2){
                    showReels()
                    myApplication.printLogD("call again showReels",TAG)
                }
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                myApplication.printLogD(position.toString(),"check 200")
                try {
                    val cPos = position
                    val holder: VideoAdapter.VideoViewHolder = (viewPager.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(cPos) as VideoAdapter.VideoViewHolder

                }catch (e:Exception){
                    myApplication.printLogE(e.message.toString(),TAG)
                }
            }
        })

        try {
            val cPos = viewPager.currentItem
            val holder: VideoAdapter.VideoViewHolder = (viewPager.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(cPos) as VideoAdapter.VideoViewHolder
            videoItemPlayPause.onResume(holder,cPos)
        }catch (e:Exception){
            myApplication.printLogE(e.message.toString(),TAG)
        }
        Log.d(TRACK, "onResume: ")
        super.onResume()
    }


    override fun onStart() {
        showReels()
        Log.d(TRACK, "onStart: ")
        super.onStart()
    }

}