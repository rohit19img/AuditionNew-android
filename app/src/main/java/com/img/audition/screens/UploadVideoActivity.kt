package com.img.audition.screens


import VideoHandle.EpEditor
import VideoHandle.OnEditorListener
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
import androidx.media3.common.util.UnstableApi

import cn.pedant.SweetAlert.SweetAlertDialog
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CannedAccessControlList

import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.JsonObject
import com.img.audition.dataModel.CommanResponse
import com.img.audition.dataModel.UploadMusicResponse
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.sql.Timestamp
import java.util.*
import java.util.regex.Pattern

@UnstableApi class UploadVideoActivity : AppCompatActivity() {

    val TAG = "UploadVideoActivity"
    val TARCK = "check 100"
    private var videoSongName = ""
    private var videoSongID = ""
    private var audiFilePath = ""

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

    private var videoOriginalPath: String = ""
    private var audioOriginalPath: String = ""
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


        if (sessionManager.getVideoSongID()!!.trim().isNotEmpty()){
            videoSongID = sessionManager.getVideoSongID().toString()
            uploadVideoToS3()
        }else{
            extractAudioFromVideo(videoOriginalPath)
        }




       /* val outputPath = createFileAndFolder()
        myApplication.printLogD("Call Compress Video Fun",TARCK)
        compressVideo(videoOriginalPath,outputPath)*/



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
        try {
            if(File(sessionManager.getCreateVideoPath()!!).exists()){
                File(sessionManager.getCreateVideoPath()!!).delete()
            }
            if (File(sessionManager.getTrimAudioPath()!!).exists()){
                File(sessionManager.getTrimAudioPath()!!).delete()
            }
            if(audiFilePath.isNotEmpty() && File(audiFilePath).exists()){
                File(audiFilePath).delete()
            }
        }catch (e:Exception){
            myApplication.printLogE(e.toString(),TAG)
        }
        val intent = Intent(this@UploadVideoActivity, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    private fun sendToAddAmountActivity() {
        val intent = Intent(this@UploadVideoActivity, AddAmountActivity::class.java)
        startActivity(intent)
    }

   /* override fun onBackPressed() {
        val sweetAlertDialog = SweetAlertDialog(this@UploadVideoActivity, SweetAlertDialog.WARNING_TYPE)
        sweetAlertDialog.titleText = "Discard Video"
        sweetAlertDialog.contentText = "Do you want discard the video"
        sweetAlertDialog.confirmText = "Yes"
        sweetAlertDialog.setConfirmClickListener {
            if (File(videoOriginalPath).exists()){
                File(videoOriginalPath).delete()
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
        super.onBackPressed()
    }*/

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


    /*fun compressVideo(inputPath:String,outputPath:String){

        myApplication.printLogD("InSide Compress Video",TARCK)
        val cmd = "-y -i $inputPath -vcodec libx264 -preset veryfast -threads 6 -crf 28 $outputPath"


        EpEditor.execCmd(cmd,0,object : OnEditorListener {
            override fun onSuccess() {
                myApplication.printLogD("log : onSuccess","ffmpeg")
                myApplication.printLogD("Video Compress Complete",TARCK)
                if (!sessionManager.getIsVideoFromGallery()){
                    if (File(videoOriginalPath).exists()){
                        File(videoOriginalPath).delete()
                    }
                }
                videoOriginalPath = outputPath
                extractAudioFromVideo(videoOriginalPath)
            }

            override fun onFailure() {
                myApplication.printLogD("log : onFailure","ffmpeg")
            }

            override fun onProgress(progress: Float) {
                myApplication.printLogD("log onProgress : $progress","ffmpeg")
            }

        })

        FFmpegKit.executeAsync(cmd,
            { session ->
                val state = session.state
                val returnCode = session.returnCode
                if (ReturnCode.isSuccess(returnCode)){
                    myApplication.printLogD("Video Compress Complete",TARCK)
                    if (File(videoOriginalPath).exists()){
                        File(videoOriginalPath).delete()
                    }
                    videoOriginalPath = outputPath

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
    }*/

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
        val file = File(videoOriginalPath)
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
        obj.addProperty("songLink", sessionManager.getVideoSongUrl())
        obj.addProperty("postId", postID)
        obj.addProperty("songid", videoSongID)
        myApplication.printLogD("uploadTime = $videoSongID","SongID")
        obj.addProperty("songName", videoSongName)
        obj.addProperty("language", sessionManager.getSelectedLanguage())
        obj.addProperty("lat",userLatLang.lat.toString())
        obj.addProperty("long",userLatLang.long.toString())

        myApplication.printLogD(obj.toString(),"videoObj")
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
                        sweetAlertDialog.dismiss()
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
        obj.addProperty("songid", videoSongID)
        obj.addProperty("songLink", sessionManager.getVideoSongUrl())
        obj.addProperty("songName", videoSongName)
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

        viewBinding.captionForVideoET.setText(sessionManager.getVideoHashTag().toString())
        myApplication.printLogD("${sessionManager.getVideoHashTag().toString()} onStart","videoHashTag")
        myApplication.printLogD("$isFromContest onStart"," isFromContest")
        myApplication.printLogD(videoUri!!, "videoUri")
//        videoOriginalPath = getOriginalPathFromUri(this@UploadVideoActivity, Uri.parse(videoUri))
        videoOriginalPath = videoUri
        myApplication.printLogD(videoOriginalPath, "videoPath")
        Glide.with(this@UploadVideoActivity).load(videoOriginalPath).into(viewBinding.videoThumbnail)
        myApplication.printLogD("$isFromContest onStart"," isFromContest $TAG")


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
                    myApplication.printLogD("contest.entryfee ${sessionManager.getContestEntryFee()}", "contestCheck")
                    myApplication.printLogD("contest.Id ${sessionManager.getContestID()}", "contestCheck")
                    myApplication.printLogD("contest.fileType ${sessionManager.getContestFile()}", "contestCheck")
                    myApplication.printLogD("contest.file ${sessionManager.getContestFile()}", "contestCheck")
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

    private fun createAudioFilePath():String{
        val timestamp = System.currentTimeMillis()
        videoSongName = "Original-Sound-by-${sessionManager.getUserName()}-$timestamp.aac"
        val appData = filesDir
        myApplication.printLogD(appData.absolutePath,TAG)

        val createFile = File(appData,videoSongName)
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

    override fun onDestroy() {
        try {
            if(File(sessionManager.getCreateVideoPath()!!).exists()){
                File(sessionManager.getCreateVideoPath()!!).delete()
            }
            if (File(sessionManager.getTrimAudioPath()!!).exists()){
                File(sessionManager.getTrimAudioPath()!!).delete()
            }
            if(audiFilePath.isNotEmpty() && File(audiFilePath).exists()){
                File(audiFilePath).delete()
            }
        }catch (e:Exception){
            myApplication.printLogE(e.toString(),TAG)
        }
        super.onDestroy()
    }

    fun extractAudioFromVideo(inputPath:String){

        val outputPath = createAudioFilePath()
        myApplication.printLogD("InSide extractAudioFromVideo",TARCK)
        val cmd = "-y -i $inputPath -vn -acodec copy $outputPath"


        EpEditor.execCmd(cmd,0,object : OnEditorListener {
            override fun onSuccess() {
                myApplication.printLogD("log : onSuccess","ffmpeg")
                myApplication.printLogD("Audio extract Complete",TARCK)
                audioOriginalPath = outputPath
                myApplication.printLogD("extractAudioFromVideo Path  : $audioOriginalPath",TARCK)
                myApplication.printLogD("Call uploadVideoToS3 Fun",TARCK)
                uploadAudioToServer(outputPath)
            }

            override fun onFailure() {
                myApplication.printLogD("log : onFailure",TARCK)
            }

            override fun onProgress(progress: Float) {
                myApplication.printLogD("log onProgress : $progress",TARCK)
            }

        })

        /*FFmpegKit.executeAsync(cmd,
            { session ->
                val state = session.state
                val returnCode = session.returnCode
                if (ReturnCode.isSuccess(returnCode)){
                    myApplication.printLogD("audio extract Complete",TARCK)

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
        }*/
    }

    fun uploadAudioToServer(audiFile:String){
         audiFilePath = File(audiFile).absolutePath
        val file = File(audiFile)


        val reqData = MultipartBody.Part.createFormData("typename","musicUpload/mp3")
        val reqFile = MultipartBody.Part.createFormData("audio",videoSongName,file.asRequestBody())

        val audioReq = apiInterface.uploadVideoMusic(sessionManager.getToken(),reqData,reqFile)
        audioReq.enqueue(object : Callback<UploadMusicResponse>{
            override fun onResponse(call: Call<UploadMusicResponse>, response: Response<UploadMusicResponse>) {
               myApplication.printLogD("uploadAudioToServer $response",TRACK)
                if (response.isSuccessful && response.body()!!.success!!){
                    val songLink = response.body()!!.data?.trackAacFormat.toString()
                    if (sessionManager.getAppSongID()!!.trim().isNotEmpty()){
                        videoSongID = sessionManager.getAppSongID().toString()
                        sessionManager.setVideoSongUrl(songLink)
                        uploadVideoToS3()
                    }else{
                       videoSongID = response.body()!!.data?.Id.toString()
                        sessionManager.setVideoSongUrl(songLink)
                        uploadVideoToS3()
                    }
                }
            }

            override fun onFailure(call: Call<UploadMusicResponse>, t: Throwable) {
                myApplication.printLogD(t.toString(),TRACK)
            }
        })

    }


}
