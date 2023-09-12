package com.img.audition.screens.fragment

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.img.audition.R
import com.img.audition.adapters.LanguageSelecteDialog
import com.img.audition.dataModel.UserLatLang
import com.img.audition.dataModel.VideoData
import com.img.audition.databinding.FragmentVideoBinding
import com.img.audition.globalAccess.AppPermission
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.screens.LoginActivity
import com.img.audition.screens.SplashActivity
import com.img.audition.viewModel.MainViewModel
import com.img.audition.viewModel.Status
import com.img.audition.viewModel.ViewModelFactory


@UnstableApi
class VideoFragment(private val contextFromActivity: Context) : Fragment() {

    private val TAG = "VideoFragment"
    private val TRACK = "Check 100"

    private lateinit var _viewBinding: FragmentVideoBinding
    private val view get() = _viewBinding

    private var appPermission: AppPermission? = null
    private var fusedLocation: FusedLocationProviderClient? = null
    private var userLatLang: UserLatLang? = null
    private var locationManager: LocationManager? = null
    private val sessionManager by lazy {
        SessionManager(contextFromActivity)
    }
    private val myApplication by lazy {
        MyApplication(contextFromActivity)
    }
    private lateinit var mainViewModel: MainViewModel
    private var forVideoList: ArrayList<VideoData> = ArrayList()
    private var liveContestList: ArrayList<VideoData> = ArrayList()

    private val bundle by lazy {
        arguments
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appPermission = AppPermission(
            requireActivity(),
            ConstValFile.PERMISSION_LIST,
            ConstValFile.REQUEST_PERMISSION_CODE
        )
        fusedLocation = LocationServices.getFusedLocationProviderClient(requireActivity())
        val apiInterface = RetrofitClient.getInstance().create(ApiInterface::class.java)
        mainViewModel = ViewModelProvider(
            this,
            ViewModelFactory(sessionManager.getToken(), apiInterface)
        )[MainViewModel::class.java]

        userLatLang = UserLatLang()
        myApplication.printLogD(userLatLang?.lat.toString(), "lat $TAG")
        myApplication.printLogD(userLatLang?.long.toString(), "long $TAG")

        val selectedLanguage = sessionManager.getSelectedLanguage()
        if (selectedLanguage.equals("")) {
            myApplication.printLogI("Show Language Dialog", TAG)
            val showLangDialog = LanguageSelecteDialog()
            showLangDialog.show(parentFragmentManager, showLangDialog.tag)
        }
        if (myApplication.isNetworkConnected()) {
            askForLocation()
        } else {
            myApplication.showToast(ConstValFile.Check_Connection)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding = FragmentVideoBinding.inflate(inflater, container, false)



        return _viewBinding.root
    }

    override fun onViewCreated(view1: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view1, savedInstanceState)

        view.notificationButton.setOnClickListener {
            if (myApplication.isNetworkConnected()) {
                if (!(sessionManager.isUserLoggedIn())) {
                    sendToLoginActivity()
                } else {
                    sendToNotificationActivity()
                }
            } else {
                myApplication.showToast(ConstValFile.Check_Connection)
            }
        }
        try {
            forVideoList =
                bundle!!.getSerializable(ConstValFile.ForYouVideoList) as ArrayList<VideoData>
            liveContestList =
                bundle!!.getSerializable(ConstValFile.LiveContestVideoList) as ArrayList<VideoData>
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        if (forVideoList.size > 0) {
           /* view.liveContest.setTextColor(resources.getColor(R.color.textColorWhite))
            view.forYou.setTextColor(resources.getColor(R.color.textColorRed))*/
            val forVideoFrag = ForYouVideoFragment(contextFromActivity)
            val bundle = Bundle()
            bundle.putSerializable(ConstValFile.ForYouVideoList, forVideoList)
            forVideoFrag.arguments = bundle
            loadFragment(forVideoFrag)
        } else {
            Log.d(TAG, "getForYouReelsVideo: No Video")
        }

//        getForYouReelsVideo()

        view.forYou.setOnClickListener {
            view.liveContest.background =
                ContextCompat.getDrawable(it.context, R.drawable.tab_unselected)
            view.forYou.background = ContextCompat.getDrawable(it.context, R.drawable.tab_selected)
            val forVideoFrag = ForYouVideoFragment(contextFromActivity)
            val bundle = Bundle()
            bundle.putSerializable(ConstValFile.ForYouVideoList, forVideoList)
            forVideoFrag.arguments = bundle
            loadFragment(forVideoFrag)

        }

        view.liveContest.setOnClickListener {
            view.liveContest.background =
                ContextCompat.getDrawable(it.context, R.drawable.tab_selected)
            view.forYou.background =
                ContextCompat.getDrawable(it.context, R.drawable.tab_unselected)
            val liveContestVideoFrag = LiveContestVideoFragment(contextFromActivity)
            val bundle = Bundle()
            bundle.putSerializable(ConstValFile.LiveContestVideoList, liveContestList)
            liveContestVideoFrag.arguments = bundle
            loadFragment(liveContestVideoFrag)
        }
    }

    private fun sendToNotificationActivity() {
        val intent = Intent(contextFromActivity, NotificationActivity::class.java)
        contextFromActivity.startActivity(intent)
    }

    private fun sendToLoginActivity() {
        val intent = Intent(contextFromActivity, LoginActivity::class.java)
        contextFromActivity.startActivity(intent)
    }

    private fun askForLocation() {
        if (ContextCompat.checkSelfPermission(
                contextFromActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            myApplication.printLogD("Ask from home Activity", TAG)
        } else {
            locationManager =
                contextFromActivity.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
            if (!locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                myApplication.onGPS()
            } else {
                getLocation()
            }
        }
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                contextFromActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                contextFromActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            myApplication.printLogD("ask permission from home activity", TAG)
        } else {
            fusedLocation?.lastLocation?.addOnSuccessListener { location ->
                if (location != null) {
                    userLatLang = UserLatLang(location.latitude, location.longitude)
                    myApplication.printLogI(userLatLang?.lat.toString(), "latitude")
                    myApplication.printLogI(userLatLang?.long.toString(), "longitude")
                }
            }
        }
    }

    private fun getForYouReelsVideo() {
        mainViewModel.getForYouReelsVideo(
            sessionManager.getSelectedLanguage(),
            userLatLang?.lat,
            userLatLang?.long
        )
            .observe(viewLifecycleOwner) {
                it.let { videoResponse ->
                    when (videoResponse.status) {
                        Status.SUCCESS -> {
                            if (videoResponse.data?.success!!) {
                                val data = videoResponse.data.data
                                if (data.size > 0) {
                                    forVideoList = data
                                    val forVideoFrag = ForYouVideoFragment(contextFromActivity)
                                    val bundle = Bundle()
                                    bundle.putSerializable(
                                        ConstValFile.ForYouVideoList,
                                        forVideoList
                                    )
                                    forVideoFrag.arguments = bundle
                                    loadFragment(forVideoFrag)
                                } else {
                                    Log.d(TAG, "getForYouReelsVideo: No Video")
                                }
                            }
                        }

                        Status.LOADING -> {
                            Log.d(TAG, "getReelsVideo: ${videoResponse.message}")
                        }

                        else -> {
                            if (videoResponse.message!!.contains("401")) {
                                sessionManager.clearLogoutSession()
                                startActivity(
                                    Intent(
                                        contextFromActivity,
                                        SplashActivity::class.java
                                    )
                                )
                                finishAffinity(contextFromActivity as Activity)
                            }
                            Log.e(TAG, "getReelsVideo: ${videoResponse.message}")
                        }
                    }
                }
            }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(_viewBinding.viewContainer.id, fragment)
        transaction.commit()
    }
}