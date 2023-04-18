package com.img.audition.screens

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.location.LocationManager
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.motion.widget.Debug.getLocation
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.JsonObject
import com.img.audition.dataModel.BoostPostGetSet
import com.img.audition.R
import com.img.audition.dataModel.UserLatLang
import com.img.audition.dataModel.UserSelfProfileResponse
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BoostPostActivity : AppCompatActivity() {

    val TAG = "BoostPostActivity"
    var boostPostTool: Toolbar? = null
    var budgetSeekbar: SeekBar? = null
    var durationSeekBar: SeekBar? = null
    var kmRadiusSeekBar: SeekBar? = null
    var budgetValue: TextView? = null
    var durationValue: TextView? = null
    var totalBudgetSpend: TextView? = null
    var totalDaysSpend: TextView? = null
    var btnBoostPost: TextView? = null
    var kmRadiusValue: TextView? = null
    var selectLocationBtn: TextView? = null
    var totalKmRadius: TextView? = null
    var budget = 0
    var duration: Int = 0
    var kmRadius: Int = 0
    var boostRate: Int = 0
    var videoID = ""

    lateinit var fusedLocation : FusedLocationProviderClient
    lateinit var userLatLang: UserLatLang
    lateinit var locationManager:LocationManager

    var sessionManager: SessionManager? = null
    var dialog: ProgressDialog? = null

    val apiInterface = RetrofitClient.getInstance().create(ApiInterface::class.java)

    private val myApplication by lazy {
        MyApplication(this@BoostPostActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_boost_post)

        dialog = ProgressDialog(this)
        dialog!!.setTitle("Progressing")
        dialog!!.setMessage("Please wait..")

        sessionManager = SessionManager(this@BoostPostActivity)
        fusedLocation = LocationServices.getFusedLocationProviderClient(this@BoostPostActivity)

        boostPostTool = findViewById(R.id.boostPostTool)
        budgetSeekbar = findViewById(R.id.budgetSeekbar)
        durationSeekBar = findViewById(R.id.durationSeekBar)
        kmRadiusSeekBar = findViewById(R.id.kmRadiusSeekBar)
        budgetValue = findViewById(R.id.budgetValue)
        selectLocationBtn = findViewById(R.id.selectLocationBtn)
        durationValue = findViewById(R.id.durationValue)
        kmRadiusValue = findViewById(R.id.kmRadiusValue)
        totalBudgetSpend = findViewById(R.id.totalBudgetSpend)
        totalKmRadius = findViewById(R.id.totalKmRadius)
        totalDaysSpend = findViewById(R.id.totalDaysSpend)
        btnBoostPost = findViewById(R.id.btnBoostPost)


        userLatLang = UserLatLang()
        askForLocation()


        boostPostTool!!.title = ""
        setSupportActionBar(boostPostTool)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        boostPostTool!!.navigationIcon!!.setColorFilter(
            resources.getColor(R.color.white),
            PorterDuff.Mode.SRC_ATOP
        )
        boostPostTool!!.setNavigationOnClickListener(View.OnClickListener { onBackPressed() })


        videoID = intent.getStringExtra("videoID")!!
        Log.d("videoID", "onCreate: BOAC : $videoID")

        val boostRateReq: Call<JsonObject> = apiInterface.getBoostRate(sessionManager!!.getToken())
        boostRateReq.enqueue(object : Callback<JsonObject?> {
            override fun onResponse(call: Call<JsonObject?>, response: Response<JsonObject?>) {
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        Log.d("check", "onResponse: " + response.body()!!["data"])
                        try {
                            boostRate = response.body()!!["data"].toString().toInt()
                        } catch (e: Exception) {
                            Log.d("check", "Exception" + e.message)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                Log.d("check", "onResponse: $t")
            }
        })

        selectLocationBtn!!.setOnClickListener(View.OnClickListener { })
        budgetSeekbar!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                budget = seekBar.progress
                budgetValue!!.setText("$budget ₹")
                totalBudgetSpend!!.setText("$budget ₹ Over")
                if (budget != 0) {
                    val cur = seekBar.width / seekBar.max
                    budgetValue!!.setX(cur.toFloat() * budget + 10)
                }
                //                budgetValue.setY(seekBar.getPivotY()+10);
                Log.d(
                    "Pos", budgetValue!!.getX().toString() +
                            ": " + seekBar.width.toString() + ":" + seekBar.x.toString()
                )
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        durationSeekBar!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                duration = seekBar.progress
                durationValue!!.setText("$duration Days")
                totalDaysSpend!!.setText("$duration Days and ")
                if (duration != 0) {
                    val cur = seekBar.width / seekBar.max
                    durationValue!!.setX(cur.toFloat() * duration + 10)
                }
                //                durationValue.setY(seekBar.getPivotY());
                Log.d(
                    "Pos", durationValue!!.getX().toString() +
                            ": " + seekBar.width.toString() + ":" + seekBar.x.toString()
                )
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        kmRadiusSeekBar!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                kmRadius = seekBar.progress
                kmRadiusValue!!.setText("$kmRadius km")
                if (kmRadius != 0) {
                    budget = kmRadius * boostRate
                    totalBudgetSpend!!.setText("$budget ₹ Over ")
                    totalKmRadius!!.setText("$kmRadius Km")
                    val cur = seekBar.width / seekBar.max
                    kmRadiusValue!!.setX(cur.toFloat() * kmRadius + 10)
                }
                Log.d(
                    "Pos", kmRadiusValue!!.getX().toString() +
                            ": " + seekBar.width.toString() + ":" + seekBar.x.toString()
                )
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        btnBoostPost!!.setOnClickListener(View.OnClickListener {
            dialog!!.show()
            getUserWalletBalance(budget)
        })

    }

    private fun boostPost() {
        if (budget > 0 && duration > 0 && kmRadius > 0 && userLatLang.lat != null && userLatLang.long != null) {
            Log.d("Boost Post Details", """budget$budget   duration$duration budget$budget 
                        kmRadius$kmRadius latitude${userLatLang.lat} longitude${userLatLang.long} videoID$videoID""".trimIndent())

            val boostObject = JsonObject()
            boostObject.addProperty("radius", kmRadius)
            boostObject.addProperty("days", duration)
            boostObject.addProperty("lat",  userLatLang.lat)
            boostObject.addProperty("long", userLatLang.long)
            boostObject.addProperty("long", kmRadius)
            boostObject.addProperty("videoId", videoID)
            val boostAPi: ApiInterface = RetrofitClient.getInstance().create(ApiInterface::class.java)
            val boostReq: Call<BoostPostGetSet> = boostAPi.boostPost(sessionManager!!.getToken(), boostObject)
            boostReq.enqueue(object : Callback<BoostPostGetSet> {
                override fun onResponse(
                    call: Call<BoostPostGetSet>,
                    response: Response<BoostPostGetSet>
                ) {
                    dialog!!.dismiss()
                    Log.d("BoostPostDetails", "onResponse: $response")
                    Log.d(
                        "BoostPostDetails",
                        "onResponse: Data " + response.body()!!.data
                    )
                    MyApplication(this@BoostPostActivity).showToast("Post Boosted")
//                            startActivity(Intent(this@BoostPostActivity, HomeActivity::class.java))
                    finish()
                }

                override fun onFailure(call: Call<BoostPostGetSet>, t: Throwable) {
                    Log.d("Boost Post Details", "onFailure: $t")
                }
            })
        } else {
            dialog!!.dismiss()
            Toast.makeText(this@BoostPostActivity, "Please Select All ", Toast.LENGTH_SHORT)
                .show()
        }
    }


    private fun askForLocation() {
        if (ContextCompat.checkSelfPermission(
                this@BoostPostActivity,
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

    private fun getUserWalletBalance(contestFees: Int) {
        val userDetilsReq = apiInterface.getUserSelfDetails(sessionManager!!.getToken())
        userDetilsReq.enqueue(object : Callback<UserSelfProfileResponse> {
            override fun onResponse(call: Call<UserSelfProfileResponse>, response: Response<UserSelfProfileResponse>) {
                if (response.isSuccessful && response.body()!!.success!! && response.body()!=null){
                    myApplication.printLogD(response.toString(),TAG)
                    val userData = response.body()!!.data
                    if(userData!=null){
                        val walletBalance =  userData.walletamaount!!

                        Log.i(TAG,"Boost Fees : $contestFees")
                        Log.i(TAG,"walletBalance : $walletBalance")

                        val totalWon =  userData.totalwon
                        if (contestFees <=  walletBalance){
                           boostPost()
                        }else{
                            val sweetAlertDialog = SweetAlertDialog(this@BoostPostActivity, SweetAlertDialog.WARNING_TYPE)
                            sweetAlertDialog.titleText = "Wallet Balance"
                            sweetAlertDialog.contentText = "Please Add Balance"
                            sweetAlertDialog.confirmText = "₹ Add"
                            sweetAlertDialog.setConfirmClickListener {
                                sweetAlertDialog.dismiss()
                                sendToAddAmountActivity()
                            }
                            sweetAlertDialog.cancelText = "No"
                            sweetAlertDialog.setCancelClickListener {
                                sweetAlertDialog.dismiss()
                                onBackPressed()

                            }
                            sweetAlertDialog.show()
                        }
                    }else{
                        myApplication.printLogE("Wallet Data Null",TAG)
                    }
                }else{
                    myApplication.printLogE("Get getUserWalletBalance Response Failed ${response.code()}",TAG)
                }
            }

            override fun onFailure(call: Call<UserSelfProfileResponse>, t: Throwable) {
                myApplication.printLogE("Get getUserWalletBalance onFailure ${t.toString()}",TAG)
            }
        })
    }

    private fun sendToAddAmountActivity() {
        val intent = Intent(this@BoostPostActivity, AddAmountActivity::class.java)
        startActivity(intent)
    }
}