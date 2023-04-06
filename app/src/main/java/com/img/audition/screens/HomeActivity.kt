package com.img.audition.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.media3.common.util.UnstableApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.img.audition.R
import com.img.audition.dataModel.UserLatLang
import com.img.audition.databinding.ActivityHomeBinding
import com.img.audition.globalAccess.AppPermission
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.SessionManager
import com.img.audition.screens.fragment.*
import java.io.File
import java.io.IOException

@UnstableApi class HomeActivity : AppCompatActivity() {
    val TAG = "HomeActivity"
    lateinit var appPermission : AppPermission
    lateinit var fusedLocation : FusedLocationProviderClient
    lateinit var userLatLang: UserLatLang
    lateinit var locationManager:LocationManager


    var authToken = ""
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityHomeBinding.inflate(layoutInflater)
    }
    private val sessionManager by lazy {
        SessionManager(this@HomeActivity)
    }

    private val myApplication by lazy {
        MyApplication(this@HomeActivity)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)


        appPermission =  AppPermission(this@HomeActivity,ConstValFile.PERMISSION_LIST,ConstValFile.REQUEST_PERMISSION_CODE)
        fusedLocation = LocationServices.getFusedLocationProviderClient(this@HomeActivity)
        authToken = sessionManager.getToken().toString()
        myApplication.printLogD(authToken,ConstValFile.TOKEN)

        userLatLang = UserLatLang()
        askForLocation()

        appPermission.checkPermissions()

        loadFragment(VideoFragment(this@HomeActivity))

        viewBinding.bottomNav.setOnItemSelectedListener {
            when(it.itemId){
                R.id.home -> {
                    loadFragment(VideoFragment(this@HomeActivity))
                    true
                }
                R.id.search->{
                    loadFragment(TrendingSearchFragment())
                    true
                }R.id.contest->{
                    loadFragment(ContestFragment())
                    true
                }
                R.id.createVideo->{
                    if (!(sessionManager.isUserLoggedIn())){
                        sendToLoginScreen()
                    }else{
                        startActivity(Intent(this@HomeActivity,CameraActivity::class.java))
                    }
                    false
                }
                R.id.profile->{
                    if (!(sessionManager.isUserLoggedIn())){
                        sendToLoginScreen()
                    }else{
                        loadFragment(ProfileFragment(this@HomeActivity))
                    }
                    true
                }else -> {
                    loadFragment(VideoFragment(this@HomeActivity))
                    true

                }
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(viewBinding.viewContainer.id,fragment)
        transaction.commit()
    }


    private fun askForLocation() {
        if (ContextCompat.checkSelfPermission(
                this@HomeActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                ConstValFile.REQUEST_PERMISSION_CODE_LOCATION
            )
        } else {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                myApplication.onGPS()
            } else {
                getLocation()
            }
        }
    }


    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(this@HomeActivity, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this@HomeActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this@HomeActivity,
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                ConstValFile.REQUEST_PERMISSION_CODE_LOCATION
            )
        } else {
            fusedLocation.lastLocation
                .addOnSuccessListener(this
                ) { location ->
                    if (location != null) {
                        userLatLang = UserLatLang(location.latitude,location.longitude)
                        myApplication.printLogI(userLatLang.lat.toString(),TAG + " latitude :")
                        myApplication.printLogI(userLatLang.long.toString(),TAG + " longitude :")
                    }
                }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ConstValFile.REQUEST_PERMISSION_CODE_LOCATION) {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                myApplication.onGPS()
            } else {
                getLocation()
            }
        }
    }

    fun sendToLoginScreen(){
        val intent = Intent(this@HomeActivity, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }


}