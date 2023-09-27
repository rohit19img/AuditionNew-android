package com.img.audition.screens

import android.Manifest
import android.app.Activity
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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageView
import com.img.audition.R
import com.img.audition.dataModel.CommanResponse
import com.img.audition.databinding.ActivityAboutUsBinding
import com.img.audition.databinding.ActivityUpdatesBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.viewModel.MainViewModel
import com.img.audition.viewModel.ViewModelFactory
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

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
    private val apiInterface by lazy {
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }

    private var imagePath = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_viewBinding.root)

        progressDialog =  ProgressDialog(this)
        progressDialog.setMessage("Loading Data...")
        progressDialog.setCancelable(false)

        _viewBinding.backPressIC.setOnClickListener {
            onBackPressed()
        }

        if(sessionManager.getUserProfileImage() != ""){
            Glide.with(this).load(sessionManager.getUserProfileImage()).into(view.profileImage)
        }

        view.profileImage.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf<String>(Manifest.permission.READ_MEDIA_IMAGES),
                        ConstValFile.REQUEST_PERMISSION_CODE_STORAGE
                    )
                }
            } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE),
                    ConstValFile.REQUEST_PERMISSION_CODE_STORAGE
                )
            }else{
                selectImage()
            }

            _viewBinding.updatesRV.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)

            getStatus()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ConstValFile.REQUEST_PERMISSION_CODE_STORAGE) {
            selectImage()
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result: CropImage.ActivityResult? = CropImage.getActivityResult(data)
            try {
                imagePath = result!!.getUriFilePath(this, true).toString()
                Log.e("Check file", imagePath)
                _viewBinding.profileImage.setImageURI(Uri.parse(imagePath))

                UploadStatus(imagePath)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun selectImage() {
        CropImage.activity()
            .setScaleType(CropImageView.ScaleType.FIT_CENTER)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setCropMenuCropButtonTitle("Next")
            .setAspectRatio(1,1)
            .start(this)
    }

    private fun UploadStatus(
        imagePath: String
    ) {
        val file: File = File(imagePath)
        var requestBody: RequestBody? = RequestBody.create("*/*".toMediaTypeOrNull(),file)
        var fileToUpload: MultipartBody.Part? = MultipartBody.Part.createFormData("image",file.name,requestBody!!)
        var mCall: Call<CommanResponse>? = apiInterface.UploadStatus(sessionManager.getToken(), fileToUpload)

        mCall!!.enqueue(object : Callback<CommanResponse> {
            override fun onResponse(call: Call<CommanResponse>, response: Response<CommanResponse>) {
                progressDialog.dismiss()
                val res : CommanResponse = response.body()!!
                if (response.isSuccessful && res.success!!) {
                    _viewBinding.profileImage.borderWidth = com.intuit.sdp.R.dimen._2sdp
                    _viewBinding.profileImage.borderColor = getColor(R.color.bgColorRed)
                } else {
                    Toast.makeText(this@UpdatesActivity,res.message, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<CommanResponse>, t: Throwable) {
                progressDialog.dismiss()
            }
        })
    }

    fun getStatus(){

    }
}