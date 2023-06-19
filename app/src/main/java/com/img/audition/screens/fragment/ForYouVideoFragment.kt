package com.img.audition.screens.fragment

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import cn.pedant.SweetAlert.SweetAlertDialog
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.img.audition.R
import com.img.audition.adapters.LanguageSelecteDialog
import com.img.audition.adapters.VideoAdapter
import com.img.audition.dataModel.UserLatLang
import com.img.audition.dataModel.VideoData
import com.img.audition.databinding.FragmentForYouVideoBinding
import com.img.audition.databinding.FragmentVideoBinding
import com.img.audition.globalAccess.AppPermission
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.screens.SplashActivity
import com.img.audition.videoWork.VideoItemPlayPause
import com.img.audition.viewModel.MainViewModel
import com.img.audition.viewModel.Status
import com.img.audition.viewModel.ViewModelFactory
import java.io.File

@UnstableApi
class ForYouVideoFragment(private val contextFromActivity:Context) : Fragment() {
    private val TAG = "ForYouVideoFragment"
    private val TRACK = "Check 100"
    private lateinit var _viewBinding : FragmentForYouVideoBinding
    private val view get() = _viewBinding

    private  var appPermission : AppPermission? = null
    private  var fusedLocation : FusedLocationProviderClient? = null
    private  var userLatLang: UserLatLang? = null
    private  var locationManager: LocationManager? = null
    private val sessionManager by lazy {
        SessionManager(contextFromActivity)
    }
    private val myApplication by lazy {
        MyApplication(contextFromActivity)
    }
    private val bundle by lazy {
        arguments
    }

    private lateinit var mainViewModel: MainViewModel
    var videoAdapter: VideoAdapter? = null
    private var videoList2 : ArrayList<VideoData> = ArrayList()

    private var videoList1 : ArrayList<VideoData> = ArrayList()

    private lateinit var viewPager : ViewPager2

    private lateinit var videoItemPlayPause: VideoItemPlayPause
    private lateinit var videoShimmerEffect: ShimmerFrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TRACK, "onCreate: ")
        appPermission =  AppPermission(requireActivity(),
            ConstValFile.PERMISSION_LIST,
            ConstValFile.REQUEST_PERMISSION_CODE)
        fusedLocation = LocationServices.getFusedLocationProviderClient(requireActivity())

        val apiInterface = RetrofitClient.getInstance().create(ApiInterface::class.java)

        mainViewModel = ViewModelProvider(this,
            ViewModelFactory(sessionManager.getToken(),apiInterface)
        )[MainViewModel::class.java]

        userLatLang = UserLatLang()
        myApplication.printLogD(userLatLang?.lat.toString(),"lat $TAG")
        myApplication.printLogD(userLatLang?.long.toString(),"long $TAG")

        val selectedLanguage = sessionManager.getSelectedLanguage()
        if (selectedLanguage.equals("")){
            myApplication.printLogI("Show Language Dialog",TAG)
            val showLangDialog = LanguageSelecteDialog()
            showLangDialog.show(parentFragmentManager,showLangDialog.tag)
        }else{
            if (myApplication.isNetworkConnected()) {
                askForLocation()
            }else{
                myApplication.showToast(ConstValFile.Check_Connection)
            }
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _viewBinding = FragmentForYouVideoBinding.inflate(inflater,container,false)

        Log.d(TRACK, "onCreateView: ")

        try {
            videoList2 = bundle!!.getSerializable(ConstValFile.ForYouVideoList) as  ArrayList<VideoData>
        }catch (e:java.lang.Exception){
            e.printStackTrace()
        }

        viewPager = _viewBinding.videoViewpager2
        viewPager.offscreenPageLimit = 2
        videoShimmerEffect =  _viewBinding.showVideoShimmer
        videoShimmerEffect.visibility = View.VISIBLE
        videoShimmerEffect.startShimmer()


        if (videoList2.size>0){
            videoShimmerEffect.stopShimmer()
            videoShimmerEffect.hideShimmer()
            videoShimmerEffect.visibility = View.GONE
            videoAdapter = VideoAdapter(contextFromActivity,videoList2)
            viewPager.adapter = videoAdapter
            videoItemPlayPause = videoAdapter!!.onActivityStateChanged()
        }else{
            if (myApplication.isNetworkConnected()) {
                getReelsVideo(1)
            }else{
                myApplication.showToast(ConstValFile.Check_Connection)
            }
        }

        return _viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    override fun onPause() {
        Log.d(TRACK, "onPause: ")
        try {
            val cPos = viewPager.currentItem
            val holder: VideoAdapter.VideoViewHolder = (viewPager.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(cPos) as VideoAdapter.VideoViewHolder
            videoItemPlayPause.onPause(holder,cPos)
        }catch (e:Exception){
            myApplication.printLogE(e.message.toString(),TAG+"2")
        }
        super.onPause()
    }

    override fun onStop() {
        Log.d(TRACK, "onStop: ")
        try {
            val cPos = viewPager.currentItem
            val holder: VideoAdapter.VideoViewHolder = (viewPager.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(cPos) as VideoAdapter.VideoViewHolder
            videoItemPlayPause.onStop(holder,cPos)
        }catch (e:Exception){
            myApplication.printLogE(e.message.toString(),TAG+"3")
        }
        super.onStop()

    }

    override fun onResume() {

        try {
            if (File(sessionManager.getCreateVideoPath().toString()).exists()){
                File(sessionManager.getCreateVideoPath().toString()).delete()
            }

            if (File(sessionManager.getCreateDuetVideoUrl().toString()).exists()){
                File(sessionManager.getCreateDuetVideoUrl().toString()).delete()
            }

            if (File(sessionManager.getTrimAudioPath().toString()).exists()){
                File(sessionManager.getTrimAudioPath().toString()).delete()
            }

        }catch (e :java.lang.Exception){
            e.printStackTrace()
        }
        sessionManager.clearVideoSession()
        sessionManager.clearContestSession()
        sessionManager.clearDuetSession()

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == videoList2.size-2){
                    if (myApplication.isNetworkConnected()) {
                        getReelsVideo(2)
                    }else{
                        checkInternetDialog()
                    }

                }
            }
        })

        try {
            val cPos = viewPager.currentItem
            val holder: VideoAdapter.VideoViewHolder = (viewPager.getChildAt(0) as RecyclerView).findViewHolderForAdapterPosition(cPos) as VideoAdapter.VideoViewHolder
            videoItemPlayPause.onResume(holder,cPos)
        }catch (e:Exception){
            myApplication.printLogE(e.message.toString(),TAG+"5")
        }
        Log.d(TRACK, "onResume: ")
        super.onResume()
    }

    private fun askForLocation() {
        if (ContextCompat.checkSelfPermission(contextFromActivity, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            myApplication.printLogD("Ask from home Activity",TAG)
        } else {
            locationManager = contextFromActivity.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
            if (!locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                myApplication.onGPS()
            } else {
                getLocation()
            }
        }
    }
    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(contextFromActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(contextFromActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            myApplication.printLogD("ask permission from home activity",TAG)
        } else {
            fusedLocation?.lastLocation?.addOnSuccessListener {location ->
                if (location != null) {
                    userLatLang = UserLatLang(location.latitude,location.longitude)
                    myApplication.printLogI(userLatLang?.lat.toString(),  "latitude")
                    myApplication.printLogI(userLatLang?.long.toString(),"longitude")
                }
            }
        }
    }


    override fun onStart() {
        /*if (myApplication.isNetworkConnected()) {
            getReelsVideo(1)
        }else{
            myApplication.showToast(ConstValFile.Check_Connection)
        }*/
        Log.d(TRACK, "onStart: ")
        super.onStart()
    }

    fun getReelsVideo(callTime:Int){
        mainViewModel.getForYouReelsVideo(sessionManager.getSelectedLanguage(),userLatLang?.lat,userLatLang?.long)
            .observe(viewLifecycleOwner){
                it.let {videoResponse->
                    when(videoResponse.status){
                        Status.SUCCESS ->{
                            if (videoResponse.data?.success!!){
                                val videoData = videoResponse.data.data
                                if (callTime==1){
                                    videoList2.clear()
                                }
                                videoList1.clear()
                                videoList1 = videoData
                                videoShimmerEffect.stopShimmer()
                                videoShimmerEffect.hideShimmer()
                                videoShimmerEffect.visibility = View.GONE

                                if (videoList2.size == 0){
                                    try {
                                        videoList2.addAll(videoList1)
                                        videoAdapter = VideoAdapter(contextFromActivity,videoList2)
                                        viewPager.adapter = videoAdapter

                                        videoItemPlayPause = videoAdapter!!.onActivityStateChanged()
                                    }catch (e:Exception){
                                        myApplication.printLogE(e.toString(),TAG)
                                    }
                                }else{
                                    videoList2.addAll(videoList1)

                                    videoAdapter?.notifyItemInserted(videoList2.size - 1)
                                    videoItemPlayPause = videoAdapter!!.onActivityStateChanged()

                                }
                            }
                        }
                        Status.LOADING ->{
                            Log.d(TRACK, "getReelsVideo: ${videoResponse.status}")
                        }
                        else->{
                            if (videoResponse.message!!.contains("401")){
                                myApplication.printLogD(videoResponse.message.toString(),"getReelsVideo 4")
                                sessionManager.clearLogoutSession()
                                startActivity(Intent(context, SplashActivity::class.java))
                                ActivityCompat.finishAffinity(requireActivity())
                            }
                            Log.d(TRACK, "getReelsVideo: ${videoResponse.status}")
                        }
                    }
                }
            }

    }

    private fun checkInternetDialog() {
        val sweetAlertDialog = SweetAlertDialog(contextFromActivity, SweetAlertDialog.WARNING_TYPE)
        sweetAlertDialog.titleText = "Internet"
        sweetAlertDialog.contentText = ConstValFile.Check_Connection
        sweetAlertDialog.confirmText = "Retry"
        sweetAlertDialog.setConfirmClickListener {
            sweetAlertDialog.dismiss()
            getReelsVideo(2)
        }
        sweetAlertDialog.show()
    }
}