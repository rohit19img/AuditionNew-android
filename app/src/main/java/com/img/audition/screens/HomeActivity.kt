package com.img.audition.screens

import android.Manifest
import android.app.ActivityManager
import android.content.Context
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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.img.audition.R
import com.img.audition.dataModel.UserLatLang
import com.img.audition.databinding.ActivityHomeBinding
import com.img.audition.globalAccess.AppPermission
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.NetworkStateService
import com.img.audition.network.SessionManager
import com.img.audition.screens.fragment.*
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


class HomeActivity : AppCompatActivity() {
    val TAG = "HomeActivity"
    lateinit var appPermission : AppPermission
    lateinit var fusedLocation : FusedLocationProviderClient
    lateinit var userLatLang: UserLatLang
    lateinit var locationManager:LocationManager
    var dir = File(File(Environment.getExternalStorageDirectory(), "Audition"), "Audition")

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


        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningTasks = activityManager.getRunningTasks(1)
        val currentActivity = runningTasks[0].topActivity!!.className

        if (currentActivity != HomeActivity::class.java.name) {
            val intent = Intent(applicationContext, NetworkStateService::class.java)
            startService(intent)
            Log.d("internet", "HomeActivity: // App is running in the background")
        } else {
            val intent = Intent(applicationContext, NetworkStateService::class.java)
            stopService(intent)
            Log.d("internet", "HomeActivity: // App is running in the foreground")
        }


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
                    loadFragment(TrendingSearchFragment(this@HomeActivity))
                    true
                }R.id.contest->{
                    loadFragment(ContestFragment(this@HomeActivity))
                    true
                }
                R.id.createVideo->{
                    if (!(sessionManager.isUserLoggedIn())){
                        sendToLoginScreen()
                    }else{
                        fontToDevice(R.font.notosans_medium,ConstValFile.FontName,this@HomeActivity)
                        sendForCreateVideo()
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


    private fun sendForCreateVideo() {
        val bundle = Bundle()
        bundle.putBoolean(ConstValFile.IsFromContest,false)
        val intent = Intent(this@HomeActivity, CameraActivity::class.java)
        intent.putExtra(ConstValFile.Bundle,bundle)
        startActivity(intent)
    }

    fun fontToDevice(resourceId: Int, resourceName: String, context: Context): File {
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


}