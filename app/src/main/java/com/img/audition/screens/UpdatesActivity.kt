package com.img.audition.screens

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.bumptech.glide.Glide
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageView
import com.google.gson.JsonObject
import com.img.audition.R
import com.img.audition.StoryView.StoryView
import com.img.audition.adapters.StatusListAdapter
import com.img.audition.dataModel.CommanResponse
import com.img.audition.dataModel.StatusData
import com.img.audition.dataModel.StatusGetSet
import com.img.audition.databinding.ActivityUpdatesBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.APITags
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import omari.hamza.storyview.callback.StoryClickListeners
import omari.hamza.storyview.model.MyStory
import omari.hamza.storyview.utils.StoryViewHeaderInfo
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat


class UpdatesActivity : AppCompatActivity() {

    private val TAG = "UpdatesActivity"
    private val _viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityUpdatesBinding.inflate(layoutInflater)
    }
    lateinit var progressDialog: ProgressDialog

    private val view get() = _viewBinding
    private val sessionManager by lazy {
        SessionManager(this)
    }
    private var apiInterface : ApiInterface?= null
    private val myApplication by lazy {
        MyApplication(this@UpdatesActivity)
    }


    private var imagePath = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_viewBinding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading Data...")
        progressDialog.setCancelable(false)

        _viewBinding.backPressIC.setOnClickListener {
            onBackPressed()
        }

        _viewBinding.updatesRV.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        getStatus()

        if (sessionManager.getUserProfileImage() != "") {
            Glide.with(this).load(sessionManager.getUserProfileImage()).into(view.profileImage)
        }

        view.profileImage.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf<String>(Manifest.permission.READ_MEDIA_IMAGES),
                        ConstValFile.REQUEST_PERMISSION_CODE_STORAGE
                    )
                }
            } else if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE),
                    ConstValFile.REQUEST_PERMISSION_CODE_STORAGE
                )
            } else {
                selectImage()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ConstValFile.REQUEST_PERMISSION_CODE_STORAGE) {
            selectImage()
        }

       /* if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result: CropImage.ActivityResult? = CropImage.getActivityResult(data)
            try {
                imagePath = result!!.getUriFilePath(this, true).toString()
                Log.e("Check file", imagePath)
                _viewBinding.profileImage.setImageURI(Uri.parse(imagePath))

                uploadVideoToS3(imagePath)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }*/
    }

    private fun selectImage() {
       /* CropImage.activity()
            .setScaleType(CropImageView.ScaleType.FIT_CENTER)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setCropMenuCropButtonTitle("Next")
            .setAspectRatio(1, 1)
            .start(this)*/
    }

    private fun uploadVideoToS3(imagePath : String) {
        val s3: AmazonS3Client
        val observer: TransferObserver

        val credentials = BasicAWSCredentials(APITags.s3Key, APITags.s3SecretKey)
        s3 = AmazonS3Client(credentials)
        s3.endpoint = APITags.s3EndpointUrl
        val transferUtility: TransferUtility =
            TransferUtility.builder().s3Client(s3).context(this@UpdatesActivity).build()

        val filePermission = CannedAccessControlList.PublicRead
        val file = File(imagePath)
        val nameOfS3VideoFile = "biggee/image-" + file.name;

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
        observer.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState?) {
                myApplication.printLogD(observer.state.toString(),"uploadVideoToS3")
                if (TransferState.COMPLETED == observer.state) {
                    myApplication.printLogD("Complete Upload Video On S3",TAG)
                    val finalVideoUrl = APITags.digitalOceanBaseUrl +nameOfS3VideoFile
                    myApplication.printLogD("finalVideoUrl : $finalVideoUrl",TAG)
                    if  (myApplication.isNetworkConnected()){
                        UploadStatus(finalVideoUrl)
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


    private fun UploadStatus(imagePath: String) {
        val jsonObject : JsonObject = JsonObject()
        jsonObject.addProperty("media",imagePath)

        Log.i(TAG,"Image path : $imagePath")
        apiInterface = RetrofitClient.getInstance().create(ApiInterface::class.java)

        var mCall: Call<CommanResponse> = apiInterface!!.UploadStatus(sessionManager.getToken(), jsonObject)
        mCall.enqueue(object : Callback<CommanResponse> {
            override fun onResponse(call: Call<CommanResponse>, response: Response<CommanResponse>) {
                progressDialog.dismiss()
                myApplication.printLogD(response.toString(),TAG)

                if(response.isSuccessful){
                    val res = response.body()!!
                    myApplication.printLogD(res.toString(),TAG)

                    if (res.success!!) {
                        _viewBinding.profileImage.borderWidth = com.intuit.sdp.R.dimen._2sdp
                        _viewBinding.profileImage.borderColor = getColor(R.color.bgColorRed)
                        getStatus()
                    } else {
                        Toast.makeText(this@UpdatesActivity, res.message, Toast.LENGTH_LONG).show()
                    }
                } else {
                    try {
                        val jObjError = JSONObject(response.errorBody().toString())
                        Log.i(TAG, jObjError.toString())
                        Toast.makeText(this@UpdatesActivity, jObjError.getJSONObject("error").getString("message"), Toast.LENGTH_LONG).show()
                    } catch (e: JSONException) {
                        Log.i(TAG, e.message!!)
                    }

                }
            }

            override fun onFailure(call: Call<CommanResponse>, t: Throwable) {
                progressDialog.dismiss()
            }
        })
    }

    fun getStatus(){
        apiInterface = RetrofitClient.getInstance().create(ApiInterface::class.java)

        var mCall: Call<StatusGetSet> = apiInterface!!.GetStatus(sessionManager.getToken())
        mCall.enqueue(object : Callback<StatusGetSet> {
            override fun onResponse(call: Call<StatusGetSet>, response: Response<StatusGetSet>) {
                progressDialog.dismiss()
                myApplication.printLogD(response.toString(),TAG)

                if(response.isSuccessful){
                    val list : ArrayList<StatusData> = response.body()!!.data!!
                    _viewBinding.updatesRV.adapter = StatusListAdapter(this@UpdatesActivity,list,supportFragmentManager)
                } else {
                    try {
                        val jObjError = JSONObject(response.errorBody().toString())
                        Log.i(TAG, jObjError.toString())
                        Toast.makeText(this@UpdatesActivity, jObjError.getJSONObject("error").getString("message"), Toast.LENGTH_LONG).show()
                    } catch (e: JSONException) {
                        Log.i(TAG, e.message!!)
                    }

                }
            }

            override fun onFailure(call: Call<StatusGetSet>, t: Throwable) {
                progressDialog.dismiss()
            }
        })
    }

}