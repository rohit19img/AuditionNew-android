package com.img.audition.screens

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.img.audition.R
import com.img.audition.dataModel.UserLatLang
import com.img.audition.dataModel.VideoData
import com.img.audition.dataModel.WebSliderResponse
import com.img.audition.databinding.ActivityHomeBinding
import com.img.audition.globalAccess.AppPermission
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.screens.fragment.ContestFragment
import com.img.audition.screens.fragment.ProfileFragment
import com.img.audition.screens.fragment.TrendingSearchFragment
import com.img.audition.screens.fragment.VideoFragment
import com.img.audition.snapCameraKit.SnapCameraActivity
import com.img.audition.viewModel.MainViewModel
import com.img.audition.viewModel.Status
import com.img.audition.viewModel.ViewModelFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File


@UnstableApi
class HomeActivity : AppCompatActivity() {
    private val TAG = "HomeActivity"
    lateinit var appPermission: AppPermission
    lateinit var popupDialog: Dialog
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityHomeBinding.inflate(layoutInflater)
    }
    private val sessionManager by lazy { SessionManager(this@HomeActivity) }
    private val myApplication by lazy { MyApplication(this) }
    val apiInterface = RetrofitClient.getInstance().create(ApiInterface::class.java)

    private lateinit var mainViewModel: MainViewModel
    private var fusedLocation: FusedLocationProviderClient? = null
    private var userLatLang: UserLatLang? = null
    private var locationManager: LocationManager? = null

    private var forYouVideoList: ArrayList<VideoData> = ArrayList()
    private var liveContestVideoList: ArrayList<VideoData> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewBinding.showVideoShimmer.startShimmer()
        viewBinding.showVideoShimmer.visibility = View.VISIBLE

        appPermission = AppPermission(
            this@HomeActivity,
            ConstValFile.PERMISSION_LIST,
            ConstValFile.REQUEST_PERMISSION_CODE
        )
        appPermission.checkPermissions()

        if (myApplication.isNetworkConnected()) {
            askForLocation()
        } else {
            myApplication.showToast(ConstValFile.Check_Connection)
        }


        mainViewModel = ViewModelProvider(
            this,
            ViewModelFactory(sessionManager.getToken(), apiInterface)
        )[MainViewModel::class.java]
        popupDialog = Dialog(this)
        userLatLang = UserLatLang()
        fusedLocation = LocationServices.getFusedLocationProviderClient(this)

        if (!(SplashActivity.isPopupBannerShow)) {
            SplashActivity.isPopupBannerShow = true
            showPopupDialog()
        }

        clearTempSession()

        getReelsVideo()

        viewBinding.bottomNav.backgroundTintList =
            ContextCompat.getColorStateList(this, android.R.color.transparent)
        viewBinding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    clearTempSession()
                    if (myApplication.isNetworkConnected()) {
                        val videoFragment = VideoFragment(this)
                        val bundle = Bundle()
                        bundle.putSerializable(ConstValFile.ForYouVideoList, forYouVideoList)
                        bundle.putSerializable(
                            ConstValFile.LiveContestVideoList,
                            liveContestVideoList
                        )
                        videoFragment.arguments = bundle
                        loadFragment(videoFragment)
                        true
                    } else {
                        checkInternetDialog(R.id.home)
                        false
                    }
                }

                R.id.search -> {
                    clearTempSession()
                    if (myApplication.isNetworkConnected()) {
                        loadFragment(TrendingSearchFragment(this@HomeActivity))
                        true
                    } else {
                        checkInternetDialog(R.id.search)
                        false
                    }
                }

                R.id.contest -> {
                    clearTempSession()
                    if (myApplication.isNetworkConnected()) {
                        loadFragment(ContestFragment(this@HomeActivity))
                        true
                    } else {
                        checkInternetDialog(R.id.contest)
                        false
                    }
                }

                R.id.createVideo -> {
                    clearTempSession()
                    if (!(sessionManager.isUserLoggedIn())) {
                        sendToLoginScreen()
                    } else {
                        /* fontToDevice(R.font.notosans_medium,ConstValFile.FontName,this@HomeActivity)*/
                        sendForCreateVideo()
                    }
                    false
                }

                R.id.profile -> {
                    clearTempSession()
                    if (myApplication.isNetworkConnected()) {
                        if (!(sessionManager.isUserLoggedIn())) {
                            sendToLoginScreen()
                        } else {
                            loadFragment(ProfileFragment(this@HomeActivity))
                        }
                        true
                    } else {
                        checkInternetDialog(R.id.profile)
                        false
                    }
                }

                else -> {
                    clearTempSession()
                    if (myApplication.isNetworkConnected()) {
                        val videoFragment = VideoFragment(this)
                        val bundle = Bundle()
                        bundle.putSerializable(ConstValFile.ForYouVideoList, forYouVideoList)
                        bundle.putSerializable(
                            ConstValFile.LiveContestVideoList,
                            liveContestVideoList
                        )
                        videoFragment.arguments = bundle
                        loadFragment(videoFragment)
                        true
                    } else {
                        checkInternetDialog(R.id.home)
                        false
                    }
                }
            }
        }
    }

    private fun askForLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            appPermission.checkPermissions()
        } else {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            if (!locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                myApplication.onGPS()
            } else {
                getLocation()
            }
        }
    }

    private fun showPopupDialog() {
        popupDialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        popupDialog.setContentView(layoutInflater.inflate(R.layout.popup_dialog_layout, null))
        popupDialog.window!!.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val popupImage = popupDialog.findViewById<ImageView>(R.id.popupImage)
        val dialogClose = popupDialog.findViewById<ImageButton>(R.id.dialogClose)
        val popupImageReq = apiInterface.getWebSliderBanner(sessionManager.getToken())

        popupImageReq.enqueue(object : Callback<WebSliderResponse> {
            override fun onResponse(
                call: Call<WebSliderResponse>, response: Response<WebSliderResponse>
            ) {
                if (response.isSuccessful && response.body()!!.success!!) {
                    val banner = response.body()!!.data?.image.toString()
                    if (banner.isEmpty() || banner.isBlank()) {
                        popupDialog.dismiss()
                    } else {
                        Glide.with(this@HomeActivity).load(banner).into(popupImage)
                    }
                } else {
                    popupDialog.dismiss()
                    Log.e(TAG, "onResponse: ${response.toString()}")
                }
            }

            override fun onFailure(call: Call<WebSliderResponse>, t: Throwable) {
                popupDialog.dismiss()
                t.printStackTrace()
            }
        })

        dialogClose.setOnClickListener {
            popupDialog.dismiss()
        }

        popupDialog.show()

    }


    private fun loadFragment(fragment: Fragment) {
        try {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(viewBinding.viewContainer.id, fragment)
            transaction.commit()
        } catch (e: Exception) {
            Log.e(TAG, "loadFragment: ", e)
        }
    }

    fun sendToLoginScreen() {
        val intent = Intent(this@HomeActivity, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }


    private fun sendForCreateVideo() {
        val bundle = Bundle()
        bundle.putBoolean(ConstValFile.IsFromContest, false)
        bundle.putBoolean(ConstValFile.isFromDuet, false)
        val intent = Intent(this@HomeActivity, SnapCameraActivity::class.java)
        intent.putExtra(ConstValFile.Bundle, bundle)
        startActivity(intent)
    }

    /*  fun fontToDevice(resourceId: Int, resourceName: String, context: Context): File {
          val path: String =
              (filesDir.absolutePath + File.separator + ConstValFile.FONT) + File.separator
          val folder = File(path)
          if (!folder.exists()) folder.mkdirs()
          val dataPath = "$path$resourceName.ttf"
          val f1 = File(dataPath)
          Log.d("check", "path: FontPath: $dataPath")
          val In = context.resources.openRawResource(resourceId)
          try {
              FileOutputStream(f1).use { outputStream -> IOUtils.copy(In, outputStream) }
          } catch (e: FileNotFoundException) {
              Log.d("check", "path: fontToDevice: $e")
              e.printStackTrace()
          } catch (e: IOException) {
              e.printStackTrace()
          }
          return File(dataPath)
      }
  */

    private fun clearTempSession() {
        try {
            if (File(sessionManager.getCreateVideoPath().toString()).exists()) {
                File(sessionManager.getCreateVideoPath().toString()).delete()
            }
            if (File(sessionManager.getCreateDuetVideoUrl().toString()).exists()) {
                File(sessionManager.getCreateDuetVideoUrl().toString()).delete()
            }
            if (File(sessionManager.getTrimAudioPath().toString()).exists()) {
                File(sessionManager.getTrimAudioPath().toString()).delete()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        sessionManager.clearVideoSession()
        sessionManager.clearContestSession()
        sessionManager.clearDuetSession()
    }

    private fun getReelsVideo() {
        mainViewModel.getForYouReelsVideo(
            sessionManager.getSelectedLanguage(),
            userLatLang?.lat,
            userLatLang?.long
        )
            .observe(this) {
                it.let { videoResponse ->
                    when (videoResponse.status) {
                        Status.SUCCESS -> {
                            val data = videoResponse.data!!.data
                            if (data.size > 0) {
                                val videoFragment = VideoFragment(this)
                                val bundle = Bundle()
                                viewBinding.showVideoShimmer.stopShimmer()
                                viewBinding.showVideoShimmer.hideShimmer()
                                viewBinding.showVideoShimmer.visibility = View.GONE
                                forYouVideoList = data
                                bundle.putSerializable(
                                    ConstValFile.ForYouVideoList,
                                    forYouVideoList
                                )
                                videoFragment.arguments = bundle
                                loadFragment(videoFragment)
                            } else {
                                Log.d(TAG, "getForYouReelsVideo: No Video")
                            }

                        }

                        Status.LOADING -> {
                            Log.d(TAG, "getForYouReelsVideo: ${videoResponse.message}")
                        }

                        else -> {
                            viewBinding.showVideoShimmer.stopShimmer()
                            viewBinding.showVideoShimmer.hideShimmer()
                            viewBinding.showVideoShimmer.visibility = View.GONE
                            if (videoResponse.message!!.contains("401")) {
                                sessionManager.clearLogoutSession()
                                startActivity(Intent(this, SplashActivity::class.java))
                                finishAffinity()
                            }
                            Log.e(TAG, "getForYouReelsVideo: ${videoResponse.message}")
                        }
                    }
                }
            }
        /*  mainViewModel.getLiveContestReelsVideo(sessionManager.getSelectedLanguage(), userLatLang?.lat, userLatLang?.long)
              .observe(this) {
                  it.let { videoResponse ->
                      when (videoResponse.status) {
                          Status.SUCCESS -> {
                              val data = videoResponse.data!!.data
                              if (data.size>0){
                                  liveContestVideoList = data
                                  bundle.putSerializable(ConstValFile.LiveContestVideoList,liveContestVideoList)
                              }else{
                                  Log.d(TAG, "getLiveContestReelsVideo: No Video")
                              }
                          }
                          Status.LOADING -> {
                              Log.d(TAG, "getLiveContestReelsVideo: ${videoResponse.message}")
                          }
                          else -> {
                              if (videoResponse.message!!.contains("401")) {
                                  sessionManager.clearLogoutSession()
                                  startActivity(Intent(this, SplashActivity::class.java))
                                  finishAffinity()
                              }
                              Log.e(TAG, "getLiveContestReelsVideo: ${videoResponse.message}")
                          }
                      }
                  }
              }*/


    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            appPermission.checkPermissions()
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

    override fun onBackPressed() {
        if (viewBinding.bottomNav.selectedItemId == R.id.home)
            super.onBackPressed()
        else
            viewBinding.bottomNav.selectedItemId = R.id.home
    }

    private fun checkInternetDialog(id: Int) {

        val sweetAlertDialog = SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
        sweetAlertDialog.titleText = "Internet"
        sweetAlertDialog.contentText = ConstValFile.Check_Connection
        sweetAlertDialog.confirmText = "Ok"
        sweetAlertDialog.setConfirmClickListener {
            sweetAlertDialog.dismiss()
            when (id) {
                R.id.home -> {
                    clearTempSession()
                    if (myApplication.isNetworkConnected()) {
                        val videoFragment = VideoFragment(this)
                        val bundle = Bundle()
                        bundle.putSerializable(ConstValFile.ForYouVideoList, forYouVideoList)
                        bundle.putSerializable(
                            ConstValFile.LiveContestVideoList,
                            liveContestVideoList
                        )
                        videoFragment.arguments = bundle
                        loadFragment(videoFragment)
                        viewBinding.bottomNav.selectedItemId = R.id.home
                    } else {
                        checkInternetDialog(R.id.home)
                    }
                }

                R.id.search -> {
                    clearTempSession()
                    if (myApplication.isNetworkConnected()) {
                        loadFragment(TrendingSearchFragment(this@HomeActivity))
                        viewBinding.bottomNav.selectedItemId = R.id.search
                    } else {
                        checkInternetDialog(R.id.search)
                    }
                }

                R.id.contest -> {
                    clearTempSession()
                    if (myApplication.isNetworkConnected()) {
                        loadFragment(ContestFragment(this@HomeActivity))
                        viewBinding.bottomNav.selectedItemId = R.id.contest
                    } else {
                        checkInternetDialog(R.id.contest)
                    }
                }

                R.id.createVideo -> {
                    clearTempSession()
                    if (!(sessionManager.isUserLoggedIn())) {
                        sendToLoginScreen()
                    } else {
                        /* fontToDevice(R.font.notosans_medium,ConstValFile.FontName,this@HomeActivity)*/
                        sendForCreateVideo()
                    }
                }

                R.id.profile -> {
                    clearTempSession()
                    if (myApplication.isNetworkConnected()) {
                        if (!(sessionManager.isUserLoggedIn())) {
                            sendToLoginScreen()
                        } else {
                            loadFragment(ProfileFragment(this@HomeActivity))
                            viewBinding.bottomNav.selectedItemId = R.id.profile
                        }
                    } else {
                        checkInternetDialog(R.id.profile)
                    }
                }

                else -> {
                    clearTempSession()
                    if (myApplication.isNetworkConnected()) {
                        val videoFragment = VideoFragment(this)
                        val bundle = Bundle()
                        bundle.putSerializable(ConstValFile.ForYouVideoList, forYouVideoList)
                        bundle.putSerializable(
                            ConstValFile.LiveContestVideoList,
                            liveContestVideoList
                        )
                        videoFragment.arguments = bundle
                        loadFragment(videoFragment)
                        viewBinding.bottomNav.selectedItemId = R.id.home
                    } else {
                        checkInternetDialog(R.id.home)
                    }
                }
            }
        }
        sweetAlertDialog.show()
    }
}