package com.img.audition.screens

import VideoHandle.EpEditor
import VideoHandle.OnEditorListener
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.LocationManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Audio.AudioColumns.TRACK
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64.encodeToString
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.loader.content.CursorLoader
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
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.JsonObject
import com.img.audition.R
import com.img.audition.adapters.UserSearch_Adapter
import com.img.audition.dataModel.*
import com.img.audition.databinding.ActivityUploadVideoBinding
import com.img.audition.globalAccess.AppPermission
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.APITags
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.viewModel.MainViewModel
import com.img.audition.viewModel.Status
import com.img.audition.viewModel.ViewModelFactory
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.sql.Timestamp
import java.util.*
import java.util.regex.Pattern

@UnstableApi
class UploadVideoActivity : AppCompatActivity() {

    private val TAG = "UploadVideoActivity"
    private val TARCK = "check 100"
    private var videoSongName = ""
    private var videoSongID = ""
    var videoThumbnail = ""
    private var audiFilePath = ""
    var check:Boolean=false

    private val PLACE_PICKER_REQUEST = 200

    private var postLocation = ""
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
    private var isAllowComment = true
    private var isAllowSharing = true
    private var isAllowDuet = true
    private var userlist: ArrayList<SearchUserData>? = null
    private var walletBalance = 0

    private lateinit var fusedLocation : FusedLocationProviderClient
    private lateinit var userLatLang: UserLatLang
    private lateinit var locationManager: LocationManager
    private lateinit var appPermission : AppPermission

    private var videoOriginalPath: String = ""
    private var audioOriginalPath: String = ""
    private var videoCaption: String = ""
    private var videoHashTag: String = ""
    private var videoTimeStamp: String = ""
    private lateinit var progressDialog: ProgressDialog

    lateinit var videoCapEt : EditText
    lateinit var cycleViewlayout : RelativeLayout
    lateinit var showBtnView : RelativeLayout

    var searchUserName = ""
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        mainViewModel = ViewModelProvider(this, ViewModelFactory(sessionManager.getToken(),apiInterface))[MainViewModel::class.java]

        appPermission =  AppPermission(this@UploadVideoActivity,ConstValFile.PERMISSION_LIST,ConstValFile.REQUEST_PERMISSION_CODE)
        fusedLocation = LocationServices.getFusedLocationProviderClient(this@UploadVideoActivity)
        userLatLang = UserLatLang()
        askForLocation()

        if (!Places.isInitialized()) {
            Places.initialize(this@UploadVideoActivity, "AIzaSyBmniloMXEznkrAL6k0VfoFsJJFAfcRBgg", Locale.ENGLISH);
        }

        videoCapEt = viewBinding.captionForVideoET
        videoCapEt.requestFocus()
        cycleViewlayout = viewBinding.cycleView
        showBtnView = viewBinding.showBtnView
        progressDialog = ProgressDialog(this@UploadVideoActivity)
        progressDialog.setCancelable(false)
        progressDialog.setTitle("Uploading..")
        progressDialog.setMessage("Please wait..")

        viewBinding.backPressIC.setOnClickListener {
            onBackPressed()
        }


        viewBinding.tagFriendBtn.setOnClickListener {
            viewBinding.captionForVideoET.append(" @")
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(videoCapEt,SHOW_IMPLICIT)
        }

        viewBinding.addHashtagBtn.setOnClickListener {
            viewBinding.captionForVideoET.append(" #")
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(videoCapEt,SHOW_IMPLICIT)
        }

        if (sessionManager.getIsFromDuet()){
            isAllowDuet = false
        }


        viewBinding.captionForVideoET.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d(TAG, "onTextChanged: "+check)

                val str =viewBinding.captionForVideoET.text.toString().trim()
                val substrings = str.substring(str.lastIndexOf(" ")+1)

                Log.d(TAG, "onTextChanged: "+substrings)

                if (!(sessionManager.getIsFromDuet())){
                    if (substrings.contains("@") && substrings.length>2){
                        if(!check)
                        {
                            val caption = substrings.toString().trim()
//                val caption = viewBinding.captionForVideoET.text.toString().trim()
                            if (caption.contains("@")){
                                val atIndex =  caption.indexOf("@")
                                if (atIndex!=-1){
                                    searchUserName = caption
                                    if (myApplication.isNetworkConnected()){
                                        searchUser(searchUserName)
                                    }else{
                                       myApplication.showToast(ConstValFile.Check_Connection)
                                    }
                                }
                            }
                        }
                        else{
                            check=false
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}

        })

        viewBinding.postSettingBtn.setOnClickListener {
            showPostSettingDialog()
        }

        viewBinding.addLocation.setOnClickListener {
            if  (myApplication.isNetworkConnected()){
                val fields=Arrays.asList(Place.Field.ADDRESS,Place.Field.ID,Place.Field.NAME,Place.Field.LAT_LNG)
                val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this)
                startActivityForResult(intent, PLACE_PICKER_REQUEST)
            }else{
                myApplication.showToast(ConstValFile.Check_Connection)
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
                videoHashTag = videoHashTag + "#" + Objects.requireNonNull(findMatchHash.group(1)) + " "
                Log.d("checkHash", "postWrite: hashTag InLoop : " + findMatchHash.group(1))
            }
        }

        if (sessionManager.getAudioDuration() == 111){
            videoSongID = sessionManager.getVideoSongID().toString()
            if  (myApplication.isNetworkConnected()){
                writePostOnFirebase(sessionManager.getCreateVideoPath().toString())
            }else{
                myApplication.showToast(ConstValFile.Check_Connection)
            }
        }else{
            if (sessionManager.getVideoSongID()!!.trim().isNotEmpty()){
                videoSongID = sessionManager.getVideoSongID().toString()
                if  (myApplication.isNetworkConnected()){
                    uploadVideoToS3()
                }else{
                    myApplication.showToast(ConstValFile.Check_Connection)
                }
            }else{
                extractAudioFromVideo(videoOriginalPath)
            }
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
                        myApplication.printLogI(location.latitude.toString(),TAG + " latitude :")
                        myApplication.printLogI(location.longitude.toString(),TAG + " longitude :")
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
            if (File(sessionManager.getCreateDuetVideoUrl().toString()).exists()){
                File(sessionManager.getCreateDuetVideoUrl().toString()).delete()
            }

            if (File(sessionManager.getTrimAudioPath().toString()).exists()){
                File(sessionManager.getTrimAudioPath().toString()).delete()
            }

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
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finishAffinity()
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




    private fun getOriginalPathFromUri(context: Context, uri: Uri): String? {
        val projection = arrayOf(MediaStore.Video.Media.DATA)
        val cursorLoader = CursorLoader(context, uri, projection, null, null, null)
        val cursor = cursorLoader.loadInBackground()

        cursor?.use {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            it.moveToFirst()
            Log.d("check900", "getOriginalPathFromUri: $it")
            return it.getString(columnIndex)
        }

        return null
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
        val nameOfS3VideoFile = "biggee/video-" + file.name;

        Log.i("UploadTest","${nameOfS3VideoFile}")
        Log.i("UploadTest","${file.path}")
        Log.i("UploadTest","${filePermission.toString()}")

        observer = transferUtility.upload(
            APITags.bucketName,  //empty bucket name, included in endpoint
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
                    if  (myApplication.isNetworkConnected()){
                        writePostOnFirebase(finalVideoUrl)
                    }else{
                        myApplication.showToast(ConstValFile.Check_Connection)
                    }
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
        if (videoHashTag.isNotEmpty())
            postData.put("hash_tag",videoHashTag.trim())
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
                        if  (myApplication.isNetworkConnected()){
                            uploadContestVideoDataToServer(docRef!!.id,finalVideoUrl)
                        }else{
                            myApplication.showToast(ConstValFile.Check_Connection)
                        }
                    }else{
                        if  (myApplication.isNetworkConnected()){
                            uploadNormalVideoDataToServer(docRef!!.id,finalVideoUrl)
                        }else{
                            myApplication.showToast(ConstValFile.Check_Connection)
                        }
                        myApplication.printLogD("Normal Video",TAG)
                        myApplication.printLogD("Call uploadNormalVideoDataToServer Fun",TARCK)
                    }
                    Log.d("docRef", docRef!!.id.toString())
                    Log.d("finalVideoUrl", finalVideoUrl)
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
        obj.addProperty("isAllowSharing", isAllowSharing)
        obj.addProperty("isAllowDuet", isAllowDuet)
        obj.addProperty("isAllowComment", isAllowComment)
        myApplication.printLogD("isAllowComment = $isAllowComment","isAllow")
        obj.addProperty("caption", videoCaption)
        if (videoHashTag.isNotEmpty())
            obj.addProperty("hashtag",videoHashTag.trim())
        obj.addProperty("typename", "")
        obj.addProperty("filename", finalVideoUrl)
        obj.addProperty("songLink", sessionManager.getVideoSongUrl())
        obj.addProperty("postId", postID)
        if (videoSongID.isNotEmpty())
            obj.addProperty("songid", videoSongID)
        myApplication.printLogD("uploadTime = $videoSongID","SongID")
        obj.addProperty("songName", videoSongName)
        obj.addProperty("language", sessionManager.getSelectedLanguage())

//        if (videoThumbnail.isNotEmpty())
//            obj.addProperty("thumbnail", videoThumbnail)

        obj.addProperty("lat",userLatLang.lat.toString())
        obj.addProperty("long",userLatLang.long.toString())
        obj.addProperty("location", postLocation)

        myApplication.printLogD(obj.toString(),"videoObj")
        val uploadVideoReq = apiInterface.uploadNormalVideoToServer(sessionManager.getToken(),obj)
        myApplication.printLogD("Call uploadNormalVideoDataToServer Fun API",TARCK)
        myApplication.printLogD(obj.toString(),TARCK)
        uploadVideoReq.enqueue(object : Callback<CommanResponse>{
            override fun onResponse(call: Call<CommanResponse>, response: Response<CommanResponse>) {
                progressDialog.dismiss()
                if (response.isSuccessful && response.body()!!.success!!){
                    myApplication.printLogD("Complete  uploadNormalVideoDataToServer Fun",TARCK)
                    val sweetAlertDialog = SweetAlertDialog(this@UploadVideoActivity, SweetAlertDialog.SUCCESS_TYPE)
                    sweetAlertDialog.contentText = ConstValFile.UploadSuccess
                    sweetAlertDialog.setCanceledOnTouchOutside(false)
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
        if (videoHashTag.isNotEmpty())
            obj.addProperty("hashtag",videoHashTag.trim())
        obj.addProperty("typename", "")
        obj.addProperty("filename", finalVideoUrl)
        obj.addProperty("postId", postID)
        obj.addProperty("contestId", sessionManager.getContestID())
        obj.addProperty("status", "contest")
        if (videoSongID.isNotEmpty())
            obj.addProperty("songid", videoSongID)
        obj.addProperty("songLink", sessionManager.getVideoSongUrl())
        obj.addProperty("songName", videoSongName)
        obj.addProperty("language", sessionManager.getSelectedLanguage())
        obj.addProperty("video_or_image",sessionManager.getContestFile())
        obj.addProperty("video_or_image_type",sessionManager.getContestType())
        obj.addProperty("lat",userLatLang.lat.toString())
        obj.addProperty("long",userLatLang.long.toString())
        obj.addProperty("location", postLocation)


        val uploadVideoReq = apiInterface.uploadContestVideoToServer(sessionManager.getToken(),obj)
        myApplication.printLogD("Call uploadContestVideoDataToServer Fun API",TARCK)
        myApplication.printLogD(obj.toString(),TARCK)
        uploadVideoReq.enqueue(object : Callback<CommanResponse>{
            override fun onResponse(call: Call<CommanResponse>, response: Response<CommanResponse>) {
                progressDialog.dismiss()
                if (response.isSuccessful && response.body()!!.success!!){
                    myApplication.printLogD("Complete  uploadContestVideoDataToServer Fun",TARCK)
                    val sweetAlertDialog = SweetAlertDialog(this@UploadVideoActivity, SweetAlertDialog.SUCCESS_TYPE)
                    sweetAlertDialog.titleText = "Congratulations"
                    sweetAlertDialog.setCanceledOnTouchOutside(false)
                    sweetAlertDialog.contentText = "Join Contest Of ₹ ${sessionManager.getContestEntryFee()}"
                    sweetAlertDialog.confirmText = "OK"
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

        if (sessionManager.getIsFromDuet()){
            myApplication.printLogD("${sessionManager.getDuetCaption().toString()} onStart","videoHashTag")
            viewBinding.captionForVideoET.setText(sessionManager.getDuetCaption().toString())
        }else{
            myApplication.printLogD("${sessionManager.getVideoHashTag().toString()} onStart","videoHashTag")
            viewBinding.captionForVideoET.setText(sessionManager.getVideoHashTag().toString())
        }

        myApplication.printLogD("$isFromContest onStart"," isFromContest")
        myApplication.printLogD(videoUri!!, "videoUri")
        videoOriginalPath = if (sessionManager.getIsVideoFromGallery()){
            getOriginalPathFromUri(this@UploadVideoActivity, Uri.parse(videoUri)).toString()
        }else {
            videoUri
        }
        myApplication.printLogD(videoOriginalPath, "videoPath")

        //
       /* val bitmap = getVideoThumbnail(videoOriginalPath)
        if (bitmap!=null){
            viewBinding.videoThumbnail.setImageBitmap(bitmap)
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 20, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            videoThumbnail = encodeToString(byteArray,android.util.Base64.DEFAULT)
            Log.d("videoThumbnail", "videoThumbnail: ${videoThumbnail}")
            Log.d("videoThumbnail", "bitmapSize: ${bitmap.byteCount}")
        }else{

        }*/
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
                if  (myApplication.isNetworkConnected()){
                    progressDialog.show()
                    myApplication.printLogD("Call uploadVideoMainFun",TARCK)
                    uploadVideoMainFun()
                }else{
                    checkInternetDialog()
                }
                /*if (isFromContest){
                    progressDialog.show()
                    val contestFees = sessionManager.getContestEntryFee()
                    myApplication.printLogD("contest.entryfee ${sessionManager.getContestEntryFee()}", "contestCheck")
                    myApplication.printLogD("contest.Id ${sessionManager.getContestID()}", "contestCheck")
                    myApplication.printLogD("contest.fileType ${sessionManager.getContestFile()}", "contestCheck")
                    myApplication.printLogD("contest.file ${sessionManager.getContestFile()}", "contestCheck")
                    myApplication.printLogD("Call getUserWalletBalance",TARCK)
                    myApplication.printLogD("$contestFees contestFees ",TARCK)
                    if  (myApplication.isNetworkConnected()){
                        getUserWalletBalance(contestFees)
                    }else{
                       myApplication.showToast(ConstValFile.Check_Connection)
                    }
                }else{
                    if  (myApplication.isNetworkConnected()){
                        progressDialog.show()
                        myApplication.printLogD("Call uploadVideoMainFun",TARCK)
                        uploadVideoMainFun()
                    }else{
                        checkInternetDialog()
                    }
                }*/
            }
        }
    }


    /*private fun getUserWalletBalance(contestFees: Int){
        mainViewModel.getUserSelfDetails()
            .observe(this){
                it.let {resources->
                    when(resources.status){
                        Status.SUCCESS ->{
                            if (resources.data!!.success!!){
                                val userData = resources.data.data
                                if(userData!=null){
                                    walletBalance =  userData.walletamaount!!.toInt()
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
                                        if (myApplication.isNetworkConnected()){
                                            uploadVideoMainFun()
                                        }else{
                                            checkInternetDialog()
                                        }
                                    }else{
                                        myApplication.printLogD("Inside getUserWalletBalance",TARCK)
                                        myApplication.printLogD("walletBalance $walletBalance",TARCK)
                                        myApplication.printLogD("contestFees $contestFees",TARCK)
                                        myApplication.printLogD("totalWon $totalWon",TARCK)
                                        progressDialog.dismiss()
                                        val sweetAlertDialog = SweetAlertDialog(this@UploadVideoActivity, SweetAlertDialog.WARNING_TYPE)
                                        sweetAlertDialog.titleText = "Wallet Balance"
                                        sweetAlertDialog.contentText = "Please Add Balance"
                                        sweetAlertDialog.confirmText = "₹ $contestFees Add"
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
                                myApplication.showToast("Something went wrong..")
                            }
                        }
                        Status.LOADING ->{
                            myApplication.printLogD(resources.status.toString(),"apiCall 3")
                        }
                        else->{
                            if (resources.message!!.contains("401")){
                                myApplication.printLogD(resources.message.toString(),"apiCall 4")
                                sessionManager.clearLogoutSession()
                                startActivity(Intent(this@UploadVideoActivity, SplashActivity::class.java))
                                finishAffinity()
                            }
                            myApplication.printLogD(resources.status.toString(),"apiCall 5")
                        }
                    }
                }
            }
    }*/



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

   /* override fun onDestroy() {
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
    }*/

    private fun extractAudioFromVideo(inputPath:String){

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
                if  (myApplication.isNetworkConnected()){
                    uploadAudioToServer(outputPath)
                }else{
                    myApplication.showToast(ConstValFile.Check_Connection)
                }
            }

            override fun onFailure() {
                myApplication.printLogD("log : onFailure",TARCK)
                if  (myApplication.isNetworkConnected()){
                    uploadVideoToS3()
                }else{
                    myApplication.showToast(ConstValFile.Check_Connection)
                }
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
                    myApplication.printLogD("log : onSuccess","ffmpeg")
                    myApplication.printLogD("Audio extract Complete",TARCK)
                    audioOriginalPath = outputPath
                    myApplication.printLogD("extractAudioFromVideo Path  : $audioOriginalPath",TARCK)
                    myApplication.printLogD("Call uploadVideoToS3 Fun",TARCK)
                    uploadAudioToServer(outputPath)
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

    private fun uploadAudioToServer(audiFile:String){
         audiFilePath = File(audiFile).absolutePath
        val file = File(audiFile)

        val reqData = MultipartBody.Part.createFormData("typename","musicUpload/mp3")
        val reqFile = MultipartBody.Part.createFormData("audio",videoSongName,file.asRequestBody())

        val req = apiInterface.uploadVideoMusic(sessionManager.getToken(),reqData,reqFile)

        req.enqueue(object :Callback<UploadMusicResponse>{
            override fun onResponse(call: Call<UploadMusicResponse>, response: Response<UploadMusicResponse>) {
                if (response.isSuccessful && response.body()!!.success!!){
                    val songLink = response.body()!!.data?.trackAacFormat.toString()
                    if (sessionManager.getAppSongID()!!.trim().isNotEmpty()){
                        videoSongID = sessionManager.getAppSongID().toString()
                        sessionManager.setVideoSongUrl(songLink)
                        if  (myApplication.isNetworkConnected()){
                            uploadVideoToS3()
                        }else{
                            myApplication.showToast(ConstValFile.Check_Connection)
                        }

                    }else{
                        if  (myApplication.isNetworkConnected()){
                            videoSongID =response.body()!!.data?.Id.toString()
                            sessionManager.setVideoSongUrl(songLink)
                            uploadVideoToS3()
                        }else{
                            myApplication.showToast(ConstValFile.Check_Connection)
                        }
                    }
                }else{
                    myApplication.showToast("Something went wrong..")
                }
            }

            override fun onFailure(call: Call<UploadMusicResponse>, t: Throwable) {
                myApplication.printLogE(t.message.toString(),TAG)
            }

        })
    }


    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun showPostSettingDialog() {
        val dialog = BottomSheetDialog(this@UploadVideoActivity, R.style.CustomBottomSheetDialogTheme)
        dialog.setContentView(R.layout.post_setting_layout)

        val allowComment = dialog.findViewById<Switch>(R.id.allowComment)
        val allowSharing = dialog.findViewById<Switch>(R.id.allowSharing)
        val allowDuet = dialog.findViewById<Switch>(R.id.allowDuet)
        val duetVIew = dialog.findViewById<LinearLayout>(R.id.duetVIew)

        if (sessionManager.getIsFromDuet()) {
            duetVIew!!.visibility = View.GONE
        }

        val applyBtn = dialog.findViewById<TextView>(R.id.applyBtn)

        allowComment!!.isChecked = isAllowComment
        allowSharing!!.isChecked = isAllowSharing
        allowDuet!!.isChecked = isAllowDuet

        applyBtn!!.setOnClickListener {
            isAllowComment = allowComment.isChecked
            isAllowSharing = allowSharing.isChecked
            isAllowDuet = allowDuet.isChecked
            myApplication.printLogD("$isAllowComment isAllowComment","check 300")
            myApplication.printLogD("$isAllowSharing isAllowSharing","check 300")
            myApplication.printLogD("$isAllowDuet isAllowDuet","check 300")
            dialog.dismiss()
        }
        dialog.show()
    }

    fun searchUser(userName : String){
        val obj = JsonObject()
        obj.addProperty("search", userName)
        Log.i("request",obj.toString())

        mainViewModel.getSearchData(obj)
            .observe(this){
                it.let {resources->
                    when(resources.status){
                        Status.SUCCESS ->{
                            if(resources.data!!.success!!){
                                userlist = ArrayList()
                                userlist = resources.data.data!!.users
                                cycleViewlayout.visibility = View.VISIBLE
                                showBtnView.visibility = View.GONE
                                viewBinding.userCycle.adapter = UserSearch_Adapter(userlist!!,this@UploadVideoActivity,userName)
                                Log.i("list_size", "Users : " + userlist!!.size)
                            }else{
                                myApplication.showToast("Something went wrong!!")
                            }
                        }
                        Status.LOADING ->{
                            myApplication.printLogD(resources.status.toString(),"apiCall 3")
                        }
                        else->{
                            if (resources.message!!.contains("401")){
                                myApplication.printLogD(resources.message.toString(),"apiCall 4")
                                sessionManager.clearLogoutSession()
                                startActivity(Intent(this@UploadVideoActivity, SplashActivity::class.java))
                                finishAffinity()
                            }
                            myApplication.printLogD(resources.status.toString(),"apiCall 5")
                        }
                    }
                }
            }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(data!!)
                userLatLang = UserLatLang(place.latLng?.latitude,place.latLng?.longitude)
                myApplication.printLogD(place.toString(),"address")
                myApplication.printLogD(place.name!!,"address loc")
                postLocation = place.name!!.toString()
                viewBinding.addLocation.text = postLocation
            }
            else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                val status = Autocomplete.getStatusFromIntent(data!!)
                Log.i("address 3", status.statusMessage!!)
            }
        }
    }

    override fun onStop() {
        try {
            userlist?.clear()
            viewBinding.userCycle.adapter = null
        }catch (e:Exception){
            e.printStackTrace()
        }
        super.onStop()
    }

    private fun getVideoThumbnail(videoUri: String): Bitmap? {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(videoUri)

        val timeUs = 1000000L // Retrieve thumbnail at 1 second into the video
        val bitmap: Bitmap?

        try {
            bitmap = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            return null
        } finally {
            retriever.release()
        }
        return bitmap
    }

    private fun checkInternetDialog() {
        val sweetAlertDialog = SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
        sweetAlertDialog.titleText = "Internet"
        sweetAlertDialog.contentText = ConstValFile.Check_Connection
        sweetAlertDialog.confirmText = "Retry"
        sweetAlertDialog.setConfirmClickListener {
            sweetAlertDialog.dismiss()
            uploadVideoMainFun()
        }
        sweetAlertDialog.show()
    }
}
