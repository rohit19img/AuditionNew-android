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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import cn.pedant.SweetAlert.SweetAlertDialog
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.img.audition.R
import com.img.audition.adapters.LanguageSelecteDialog
import com.img.audition.adapters.VideoAdapter
import com.img.audition.adapters.VideoCategegoriesAdapter
import com.img.audition.dataModel.UserLatLang
import com.img.audition.dataModel.VideoData
import com.img.audition.databinding.FragmentLiveContestVideoBinding
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
class LiveContestVideoFragment(private val contextFromActivity:Context) : Fragment() {

    private val TAG = "LiveContestVideoFragment"
    private val TRACK = "Check 100"
    private lateinit var _viewBinding : FragmentLiveContestVideoBinding
    private val view get() = _viewBinding

    private val sessionManager by lazy {
        SessionManager(contextFromActivity)
    }
    private val myApplication by lazy {
        MyApplication(contextFromActivity)
    }

    private lateinit var mainViewModel: MainViewModel
    var categories : RecyclerView?= null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TRACK, "onCreate: ")

        val apiInterface = RetrofitClient.getInstance().create(ApiInterface::class.java)

        mainViewModel = ViewModelProvider(this,
            ViewModelFactory(sessionManager.getToken(),apiInterface)
        )[MainViewModel::class.java]

//        val selectedLanguage = sessionManager.getSelectedLanguage()
//        if (selectedLanguage.equals("")){
//            myApplication.printLogI("Show Language Dialog",TAG)
//            val showLangDialog = LanguageSelecteDialog()
//            showLangDialog.show(parentFragmentManager,showLangDialog.tag)
//        }else{
//
//        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _viewBinding = FragmentLiveContestVideoBinding.inflate(inflater,container,false)

        Log.d(TRACK, "onCreateView: ")


        categories = _viewBinding.categories
        categories!!.layoutManager = LinearLayoutManager(contextFromActivity,LinearLayoutManager.VERTICAL,false)

        if (myApplication.isNetworkConnected()) {
            getReelsCategories(1)
        }else{
            checkInternetDialog(1)
        }

        return _viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    fun getReelsCategories(callTime:Int){
        mainViewModel.getLiveContestReelsCategories(sessionManager.getSelectedLanguage())
            .observe(viewLifecycleOwner){
                it.let {VideoContestsResponse->
                    when(VideoContestsResponse.status){
                        Status.SUCCESS ->{
                            if (VideoContestsResponse.data?.success!!){
                                _viewBinding.showVideoShimmer.visibility = View.GONE
                                _viewBinding.noLiveVideo.visibility = View.GONE
                                _viewBinding.categories.visibility = View.VISIBLE

                                Log.i("TAG",
                                    "Size : ${VideoContestsResponse.data.data.size}"
                                )
                                if(VideoContestsResponse.data.data.size > 0) {
                                    Log.i(
                                        "TAG",
                                        "name : ${VideoContestsResponse.data.data[0].contest_name}"
                                    )
                                    Log.i(
                                        "TAG",
                                        "id : ${VideoContestsResponse.data.data[0]._id}"
                                    )
                                    Log.i(
                                        "TAG",
                                        "file : ${VideoContestsResponse.data.data[0].file}"
                                    )
                                }

                                _viewBinding.categories.adapter = VideoCategegoriesAdapter(contextFromActivity,VideoContestsResponse.data.data)
                            }
                            else{
                                // success false
                                _viewBinding.showVideoShimmer.visibility = View.GONE
                               _viewBinding.noLiveVideo.visibility = View.VISIBLE
                               _viewBinding.categories.visibility = View.GONE
                            }
                        }
                        Status.LOADING ->{
                            Log.d(TRACK, "getReelsVideo: ${VideoContestsResponse.status}")
                        }
                        else->{
                            if (VideoContestsResponse.message!!.contains("401")){
                                myApplication.printLogD(VideoContestsResponse.message.toString(),"getReelsVideo 4")
                                sessionManager.clearLogoutSession()
                                startActivity(Intent(context, SplashActivity::class.java))
                                ActivityCompat.finishAffinity(requireActivity())
                            }
                            Log.d(TRACK, "getReelsVideo: ${VideoContestsResponse.status}")
                        }
                    }
                }
            }
    }

    private fun checkInternetDialog(callTime: Int) {
        val sweetAlertDialog = SweetAlertDialog(contextFromActivity, SweetAlertDialog.WARNING_TYPE)
        sweetAlertDialog.titleText = "Internet"
        sweetAlertDialog.contentText = ConstValFile.Check_Connection
        sweetAlertDialog.confirmText = "Retry"
        sweetAlertDialog.setConfirmClickListener {
            sweetAlertDialog.dismiss()
            getReelsCategories(callTime)
        }
        sweetAlertDialog.show()
    }
}