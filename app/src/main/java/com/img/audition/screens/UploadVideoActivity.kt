package com.img.audition.screens


import android.Manifest
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Audio.AudioColumns.TRACK
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import cn.pedant.SweetAlert.SweetAlertDialog
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode

import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.JsonObject
import com.img.audition.dataModel.CommanResponse
import com.img.audition.dataModel.UserLatLang
import com.img.audition.dataModel.UserSelfProfileResponse
import com.img.audition.databinding.ActivityUploadVideoBinding
import com.img.audition.globalAccess.AppPermission
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.APITags
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.sql.Timestamp
import java.util.*
import java.util.regex.Pattern



class UploadVideoActivity : AppCompatActivity() {

    val TAG = "UploadVideoActivity"
    val TARCK = "check 100"
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityUploadVideoBinding.inflate(layoutInflater)
    }
    private val sessionManager by lazy {
        SessionManager(this@UploadVideoActivity)
    }

    private val myApplication by lazy {
        MyApplication(this@UploadVideoActivity)
    }

    private val firebaseDB by lazy {
        FirebaseFirestore.getInstance()
    }
    private val apiInterface by lazy{
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }

    private var isFromContest = false
    private var walletBalance = 0

    lateinit var fusedLocation : FusedLocationProviderClient
    lateinit var userLatLang: UserLatLang
    lateinit var locationManager: LocationManager
    lateinit var appPermission : AppPermission

    private var orignalPath: String = ""
    private var videoCaption: String = ""
    private var videoHashTag: String = ""
    private var videoTimeStamp: String = ""
    lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        appPermission =  AppPermission(this@UploadVideoActivity,ConstValFile.PERMISSION_LIST,ConstValFile.REQUEST_PERMISSION_CODE)
        fusedLocation = LocationServices.getFusedLocationProviderClient(this@UploadVideoActivity)
        userLatLang = UserLatLang()
        askForLocation()


        progressDialog = ProgressDialog(this@UploadVideoActivity)
        progressDialog.setCancelable(false)
        progressDialog.setTitle("Uploading.")
        progressDialog.setMessage("please wait...")


        viewBinding.backPressIC.setOnClickListener {
            onBackPressed()
        }

        viewBinding.uploadVideoBtn.setOnClickListener {
            viewBinding.uploadVideoBtn.isSelected = false
            myApplication.printLogD("on Upload Button Click ",TARCK)
            if (!(sessionManager.isUserLoggedIn())) {
                myApplication.printLogD("Send TO Login Page",TARCK)
                sendToLoginActivity()
            } else {
                myApplication.printLogD("$isFromContest uploadVideoBtn"," isFromContest $TAG")
                if (isFromContest){
                    progressDialog.show()
                    val contestFees = sessionManager.getContestEntryFee()
                    myApplication.printLogD("Call getUserWalletBalance",TARCK)
                    myApplication.printLogD("$contestFees contestFees ",TARCK)
                    getUserWalletBalance(contestFees)
                }else{
                    progressDialog.show()
                    myApplication.printLogD("Call uploadVideoMainFun",TARCK)
                    uploadVideoMainFun()
                }

            }
        }
    }

    private fun uploadVideoMainFun() {
        val cap = viewBinding.captionForVideoET.text.toString().trim()
        videoTimeStamp= Timestamp(System.currentTimeMillis()).toString()
        if (cap.isNotEmpty()){
            val hashPattern = Pattern.compile("#(\\S+)")
            videoCaption = cap
            val findMatchHash = hashPattern.matcher(videoCaption)

            val hashArray: MutableList<String> = ArrayList()
            while (findMatchHash.find()) {
                findMatchHash.group(1)?.let { it1 -> hashArray.add(it1) }
                videoHashTag =
                    videoHashTag + "#" + Objects.requireNonNull(findMatchHash.group(1)) + " "
                Log.d("checkHash", "postWrite: hashTag InLoop : " + findMatchHash.group(1))
            }
        }
        val outputPath = createFileAndFolder()
        myApplication.printLogD("Call Compress Video Fun",TARCK)
        compressVideo(orignalPath,outputPath)
    }

    private fun askForLocation() {
        if (ContextCompat.checkSelfPermission(
                this@UploadVideoActivity,
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
        if (ActivityCompat.checkSelfPermission(this@UploadVideoActivity, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this@UploadVideoActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this@UploadVideoActivity,
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

    private fun sendToLoginActivity() {
        val intent = Intent(this@UploadVideoActivity, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun sendToHomeActivity() {
        val intent = Intent(this@UploadVideoActivity, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    private fun sendToAddAmountActivity() {
        val intent = Intent(this@UploadVideoActivity, AddAmountActivity::class.java)
        startActivity(intent)
    }

    override fun onBackPressed() {
        val sweetAlertDialog = SweetAlertDialog(this@UploadVideoActivity, SweetAlertDialog.WARNING_TYPE)
        sweetAlertDialog.titleText = "Discard Video"
        sweetAlertDialog.contentText = "Do you want discard the video"
        sweetAlertDialog.confirmText = "Yes"
        sweetAlertDialog.setConfirmClickListener {
            if (File(orignalPath).exists()){
                File(orignalPath).delete()
                sessionManager.clearVideoSession()
            }
            sweetAlertDialog.dismiss()
            sendToMain()
        }
        sweetAlertDialog.cancelText = "No"
        sweetAlertDialog.setCancelClickListener {
            sweetAlertDialog.dismiss()
        }
        sweetAlertDialog.show()
    }

    fun sendToMain() {
        val intent = Intent(this@UploadVideoActivity, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }


    private fun getOriginalPathFromUri(context: Context, uri: Uri): String {
        var filePath = ""
        val scheme = uri.scheme
        if (scheme == ContentResolver.SCHEME_CONTENT) {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                filePath = cursor.getString(columnIndex)
                cursor.close()
            }
        } else if (scheme == ContentResolver.SCHEME_FILE) {
            filePath = uri.path!!
        }
        return filePath
    }



    private fun createFileAndFolder():String{
        val timestamp = System.currentTimeMillis()
        val filename = "$timestamp.mp4"
        val appData = filesDir
        myApplication.printLogD(appData.absolutePath,TAG)

        val createFile = File(appData,filename)
        if (!(createFile.exists())){
            try {
                createFile.createNewFile()
                myApplication.printLogD(createFile.absolutePath,TAG)
            }catch (i: IOException){
                myApplication.printLogE(i.toString(),TAG)
            }
        }

        return createFile.absolutePath

    }


    fun compressVideo(inputPath:String,outputPath:String){

        myApplication.printLogD("InSide Compress Video",TARCK)
        val cmd = "-y -i $inputPath -vcodec libx264 -crf 24 $outputPath"

        /*EpEditor.execCmd(cmd,0,object : OnEditorListener {
            override fun onSuccess() {
                myApplication.printLogD("log : onSuccess","ffmpeg")
                myApplication.printLogD("Video Compress Complete",TARCK)
                if (File(orignalPath).exists()){
                    File(orignalPath).delete()
                }
                orignalPath = outputPath

                myApplication.printLogD("Call uploadVideoToS3 Fun",TARCK)
                uploadVideoToS3()
            }

            override fun onFailure() {
                myApplication.printLogD("log : onFailure","ffmpeg")
            }

            override fun onProgress(progress: Float) {
                myApplication.printLogD("log onProgress : $progress","ffmpeg")
            }

        })*/
        FFmpegKit.executeAsync(cmd,
            { session ->
                val state = session.state
                val returnCode = session.returnCode
                if (ReturnCode.isSuccess(returnCode)){
                    myApplication.printLogD("Video Compress Complete",TARCK)
                    if (File(orignalPath).exists()){
                        File(orignalPath).delete()
                    }
                    orignalPath = outputPath

                    myApplication.printLogD("Call uploadVideoToS3 Fun",TARCK)
                    uploadVideoToS3()
                }
                // CALLED WHEN SESSION IS EXECUTED
                Log.d(TAG, String.format("FFmpeg process exited with state %s and rc %s.%s",
                    state, returnCode,  session.failStackTrace))
            },
            {
                myApplication.printLogD("log : $it","ffmpeg")
            })
        {
            myApplication.printLogD("statistics : $it","ffmpeg")
        }
    }

    private fun uploadVideoToS3() {
        myApplication.printLogD("Inside uploadVideoToS3 Fun",TARCK)
        val s3: AmazonS3Client
        val observer: TransferObserver

        val credentials = BasicAWSCredentials(APITags.s3Key, APITags.s3SecretKey)
        s3 = AmazonS3Client(credentials)
        s3.endpoint = APITags.s3EndpointUrl
        val transferUtility: TransferUtility =
            TransferUtility.builder().s3Client(s3).context(this@UploadVideoActivity).build()

        val filePermission = CannedAccessControlList.PublicRead
        val file = File(orignalPath)
        val nameOfS3VideoFile = "audition/video-" + file.name;

        Log.i("UploadTest","${nameOfS3VideoFile}")
        Log.i("UploadTest","${file.path}")
        Log.i("UploadTest","${filePermission.toString()}")

        observer = transferUtility.upload(
            "audition",  //empty bucket name, included in endpoint
            nameOfS3VideoFile,
            file,  //a File object that you want to upload
            filePermission
        )

        Log.i("UploadTest","${observer.id}")
        Log.i("UploadTest","${observer.state}")

        myApplication.printLogD(observer.state.toString(),"uploadVideoToS3")
        observer.setTransferListener(object :TransferListener{
            override fun onStateChanged(id: Int, state: TransferState?) {
                myApplication.printLogD(observer.state.toString(),"uploadVideoToS3")
                if (TransferState.COMPLETED == observer.state) {
                    myApplication.printLogD("Complete Upload Video On S3",TARCK)
                    val finalVideoUrl = APITags.digitalOceanBaseUrl +nameOfS3VideoFile
                    myApplication.printLogD("Call writePostOnFirebase Fun",TARCK)
                    writePostOnFirebase(finalVideoUrl)
                }

                Log.i("UploadTest","State Changed : $state")

            }

            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                val progress = 100.0 * bytesCurrent / bytesTotal
                myApplication.printLogD(progress.toString(),"uploadVideoToS3")
                Log.i("UploadTest","Progress Changed : ${bytesCurrent * 100 / bytesTotal}")
            }

            override fun onError(id: Int, ex: Exception?) {
                Log.i("UploadTest","Error : $id")
                Log.i("UploadTest","Error : ${ex.toString()}")
                Log.i("UploadTest","Error : ${ex!!.message}")

                myApplication.printLogD(ex.toString(),"uploadVideoToS3")
                myApplication.printLogD(id.toString(),"uploadVideoToS3")
            }

        })


    }

    private fun writePostOnFirebase(finalVideoUrl: String) {
        myApplication.printLogD("Inside writePostOnFirebase Fun",TARCK)
        val postData: HashMap<String, Any> = HashMap()
        postData.put("video_url",finalVideoUrl)
        postData.put("video_caption",videoCaption)
        postData.put("hash_tag",videoHashTag)
        postData.put("lat",userLatLang.lat.toString())
        postData.put("long",userLatLang.long.toString())
        postData.put("user_name",sessionManager.getUserName().toString())
        postData.put("date_created",videoTimeStamp)
        postData.put("post_id","")
        postData.put("like_count",0)
        postData.put("date_created",videoTimeStamp)
        postData.put("user_id",sessionManager.getUserSelfID().toString())
        postData.put("language",sessionManager.getSelectedLanguage().toString())

        firebaseDB.collection(ConstValFile.FirebasePostDB)
            .add(postData)
            .addOnSuccessListener(object : OnSuccessListener<DocumentReference>{
                override fun onSuccess(docRef: DocumentReference?) {
                    myApplication.printLogD("Complete writePostOnFirebase Fun",TARCK)
                    if (isFromContest){
                        myApplication.printLogD("Contest Video",TAG)
                        uploadContestVideoDataToServer(docRef!!.id,finalVideoUrl)
                    }else{
                        myApplication.printLogD("Normal Video",TAG)
                        myApplication.printLogD("Call uploadNormalVideoDataToServer Fun",TARCK)
                        uploadNormalVideoDataToServer(docRef!!.id,finalVideoUrl)
                    }
                }

            })

    }

    private fun uploadNormalVideoDataToServer(postID: String, finalVideoUrl: String) {
        myApplication.printLogD("Inside uploadNormalVideoDataToServer Fun",TARCK)
        val obj = JsonObject()
        obj.addProperty("userLike", "")
        obj.addProperty("likeCount", 0)
        obj.addProperty("comment", "")
        obj.addProperty("shares", 0)
        obj.addProperty("caption", videoCaption)
        obj.addProperty("hashtag",videoHashTag)
        obj.addProperty("typename", "")
        obj.addProperty("filename", finalVideoUrl)
        obj.addProperty("postId", postID)
        obj.addProperty("language", sessionManager.getSelectedLanguage())
        obj.addProperty("lat",userLatLang.lat.toString())
        obj.addProperty("long",userLatLang.long.toString())

        val uploadVideoReq = apiInterface.uploadNormalVideoToServer(sessionManager.getToken(),obj)
        myApplication.printLogD("Call uploadNormalVideoDataToServer Fun API",TARCK)
        uploadVideoReq.enqueue(object : Callback<CommanResponse>{
            override fun onResponse(call: Call<CommanResponse>, response: Response<CommanResponse>) {
                progressDialog.dismiss()
                if (response.isSuccessful && response.body()!!.success!!){
                    myApplication.printLogD("Complete  uploadNormalVideoDataToServer Fun",TARCK)
                    val sweetAlertDialog = SweetAlertDialog(this@UploadVideoActivity, SweetAlertDialog.SUCCESS_TYPE)
                    sweetAlertDialog.contentText = ConstValFile.UploadSuccess
                    sweetAlertDialog.show()
                    sweetAlertDialog.setConfirmClickListener {
                        sendToHomeActivity()
                    }
                }
            }
            override fun onFailure(call: Call<CommanResponse>, t: Throwable) {
                progressDialog.dismiss()
                myApplication.showToast(ConstValFile.UploadFailed)
            }
        })


    }


    private fun uploadContestVideoDataToServer(postID: String, finalVideoUrl: String) {
        myApplication.printLogD("Inside uploadNormalVideoDataToServer Fun",TARCK)
        val obj = JsonObject()
        obj.addProperty("userLike", "")
        obj.addProperty("likeCount", 0)
        obj.addProperty("comment", "")
        obj.addProperty("shares", 0)
        obj.addProperty("caption", videoCaption)
        obj.addProperty("hashtag",videoHashTag)
        obj.addProperty("typename", "")
        obj.addProperty("filename", finalVideoUrl)
        obj.addProperty("postId", postID)
        obj.addProperty("contestId", sessionManager.getContestID())
        obj.addProperty("status", "contest")
        obj.addProperty("language", sessionManager.getSelectedLanguage())
        obj.addProperty("video_or_image",sessionManager.getContestFile())
        obj.addProperty("video_or_image_type",sessionManager.getContestType())
        obj.addProperty("lat",userLatLang.lat.toString())
        obj.addProperty("long",userLatLang.long.toString())



        val uploadVideoReq = apiInterface.uploadContestVideoToServer(sessionManager.getToken(),obj)
        myApplication.printLogD("Call uploadContestVideoDataToServer Fun API",TARCK)
        uploadVideoReq.enqueue(object : Callback<CommanResponse>{
            override fun onResponse(call: Call<CommanResponse>, response: Response<CommanResponse>) {
                progressDialog.dismiss()
                if (response.isSuccessful && response.body()!!.success!!){
                    myApplication.printLogD("Complete  uploadContestVideoDataToServer Fun",TARCK)
                    val sweetAlertDialog = SweetAlertDialog(this@UploadVideoActivity, SweetAlertDialog.SUCCESS_TYPE)
                    sweetAlertDialog.titleText = "Congratulations"
                    sweetAlertDialog.contentText = "You are join this Contest!"
                    sweetAlertDialog.show()
                    sweetAlertDialog.setConfirmClickListener {
                        sendToHomeActivity()
                    }
                }
            }
            override fun onFailure(call: Call<CommanResponse>, t: Throwable) {
                progressDialog.dismiss()
                myApplication.showToast(ConstValFile.UploadFailed)
            }
        })
    }

    override fun onStart() {
        super.onStart()
        isFromContest = sessionManager.getIsFromContest()
        val videoUri = sessionManager.getCreateVideoPath()
        myApplication.printLogD("$isFromContest onStart"," isFromContest")
        myApplication.printLogD(videoUri!!, "videoUri")
//        orignalPath = getOriginalPathFromUri(this@UploadVideoActivity, Uri.parse(videoUri))
        orignalPath = videoUri
        myApplication.printLogD(orignalPath, "videoPath")
        Glide.with(this@UploadVideoActivity).load(orignalPath).into(viewBinding.videoThumbnail)
        myApplication.printLogD("$isFromContest onStart"," isFromContest $TAG")
    }

    private fun getUserWalletBalance(contestFees: Int) {
        val userDetilsReq = apiInterface.getUserSelfDetails(sessionManager.getToken())
        userDetilsReq.enqueue(object : Callback<UserSelfProfileResponse> {
            override fun onResponse(call: Call<UserSelfProfileResponse>, response: Response<UserSelfProfileResponse>) {
                if (response.isSuccessful && response.body()!!.success!! && response.body()!=null){
                    myApplication.printLogD(response.toString(),TAG)
                    val userData = response.body()!!.data
                    if(userData!=null){
                        walletBalance =  userData.walletamaount!!
                        myApplication.printLogD("walletBalance $walletBalance",TARCK)
                        myApplication.printLogD("contestFees $contestFees",TARCK)

                        val isValid  =  contestFees <=  walletBalance

                        Log.i(TRACK,"contestFees : $contestFees")
                        Log.i(TRACK,"walletBalance : $walletBalance")

                        myApplication.printLogD("condition $isValid",TARCK)
                        val totalWon =  userData.totalwon
                        if (contestFees <=  walletBalance){
                            myApplication.printLogD("Inside getUserWalletBalance",TARCK)
                            myApplication.printLogD("walletBalance $walletBalance",TARCK)
                            myApplication.printLogD("contestFees $contestFees",TARCK)
                            myApplication.printLogD("totalWon $totalWon",TARCK)
                            myApplication.printLogD("Call uploadVideoMainFun",TARCK)
                            uploadVideoMainFun()
                        }else{
                            myApplication.printLogD("Inside getUserWalletBalance",TARCK)
                            myApplication.printLogD("walletBalance $walletBalance",TARCK)
                            myApplication.printLogD("contestFees $contestFees",TARCK)
                            myApplication.printLogD("totalWon $totalWon",TARCK)
                            progressDialog.dismiss()
                            val sweetAlertDialog = SweetAlertDialog(this@UploadVideoActivity, SweetAlertDialog.WARNING_TYPE)
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


}
