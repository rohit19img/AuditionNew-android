package com.img.audition.screens

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

import com.android.volley.*
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.img.audition.R
import com.img.audition.dataModel.CommanResponse
import com.img.audition.dataModel.UserSelfProfileResponse
import com.img.audition.databinding.ActivityEditProfileBinding
import com.img.audition.globalAccess.AppPermission
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*

 class EditProfileActivity : AppCompatActivity() {
    val TAG = "EditProfileActivity"
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityEditProfileBinding.inflate(layoutInflater)
    }
    private val sessionManager by lazy {
        SessionManager(this@EditProfileActivity)
    }

    private val myApplication by lazy {
        MyApplication(this@EditProfileActivity)
    }
    private val apiInterface by lazy{
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }

    var gender = ""
    var imagePath = ""

    lateinit var progressDialog:ProgressDialog
    var requestQueue: RequestQueue? = null

    lateinit var appPermission : AppPermission

    lateinit var gender_botSheetBtn:TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        progressDialog = ProgressDialog(this@EditProfileActivity)
        progressDialog.setCancelable(false)
        progressDialog.setTitle("Uploading.")
        progressDialog.setMessage("please wait...")

        requestQueue = Volley.newRequestQueue(this@EditProfileActivity)


        viewBinding.backPressIC.setOnClickListener {
            onBackPressed()
        }
        appPermission =  AppPermission(this@EditProfileActivity,
            ConstValFile.PERMISSION_LIST,
            ConstValFile.REQUEST_PERMISSION_CODE_STORAGE)


        viewBinding.mobilenumber.isEnabled = false

        gender_botSheetBtn = viewBinding.gender

        gender_botSheetBtn.setOnClickListener {
            genderBottomDialog()
        }
        viewBinding.dob.setOnClickListener {
            pickDate(viewBinding.dob)
        }

        viewBinding.btnSave.setOnClickListener {
            progressDialog.show()
            val name = viewBinding.name.text.toString().trim()
            val auditionID = viewBinding.auditionid.text.toString().trim()
            val bio = viewBinding.bio.text.toString().trim()
            val gender = viewBinding.gender.text.toString().trim()
            val dob = viewBinding.dob.text.toString().trim()
            editProfile(name,auditionID,bio,gender,dob,imagePath)
            verifyUserDetails(name,auditionID,bio,gender,dob,imagePath)

        }

        viewBinding.changePhoto.setOnClickListener {
            if (appPermission.checkPermissions()){
                selectImage()
            }else{
                appPermission.checkPermissions()
            }
        }

        viewBinding.userImage.setOnClickListener {
            if (appPermission.checkPermissions()){
                selectImage()
            }else{
                appPermission.checkPermissions()
            }
        }
        getUserSelfDetails()
    }

    private fun getUserSelfDetails() {
        val userDetilsReq = apiInterface.getUserSelfDetails(sessionManager.getToken())

        userDetilsReq.enqueue(object : Callback<UserSelfProfileResponse> {
            override fun onResponse(call: Call<UserSelfProfileResponse>, response: Response<UserSelfProfileResponse>) {
                if (response.isSuccessful && response.body()!!.success!! && response.body() != null) {
                    myApplication.printLogD(response.toString(), TAG)
                    val userData = response.body()!!.data
                    if (userData != null) {

                        Glide.with(this@EditProfileActivity).load(userData.image.toString()).placeholder(R.drawable.person_ic).into( viewBinding.userImage)
                        viewBinding.name.setText(userData.name.toString())
                        viewBinding.auditionid.setText(userData.auditionId.toString())
                        viewBinding.mobilenumber.setText(userData.mobile.toString())
                        viewBinding.bio.setText(userData.bio.toString())
                        viewBinding.gender.text = userData.gender.toString()
                        viewBinding.dob.text = userData.dob.toString()
                        gender = userData.gender.toString()
                    } else {
                        myApplication.printLogE(
                            "Get Other User Self Data Response Failed ${response.code()}",
                            TAG
                        )
                    }
                }
            }
            override fun onFailure(call: Call<UserSelfProfileResponse>, t: Throwable) {
                myApplication.printLogE("Get Other User Self Data onFailure ${t.toString()}",TAG)
            }

        })
    }

    fun pickDate(dialog: TextView) {
        val mcurrentDate = Calendar.getInstance()
        val mYear = mcurrentDate[Calendar.YEAR]
        val mMonth = mcurrentDate[Calendar.MONTH]
        val mDay = mcurrentDate[Calendar.DAY_OF_MONTH]
        val mDatePicker = DatePickerDialog(this@EditProfileActivity,
            { datepicker, selectedyear, selectedmonth, selectedday ->
                val d = (selectedmonth + 1).toString() + "/" + selectedday + "/" + selectedyear
                dialog.text = d
            }, mYear, mMonth, mDay
        )
        mDatePicker.datePicker.maxDate = ((System.currentTimeMillis() - (5.681e+11)).toLong())
        mDatePicker.setTitle("Select Birth Date")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mDatePicker.datePicker.firstDayOfWeek = Calendar.MONDAY
        }
        mDatePicker.show()
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
                imagePath = result!!.getUriFilePath(this@EditProfileActivity, true).toString()
                Log.e("Check file", imagePath)
                viewBinding.userImage.setImageURI(Uri.parse(imagePath))
            } catch (e: Exception) {
                myApplication.printLogE(e.message.toString(),TAG)
            }
        }
    }


    private fun genderBottomDialog() {
       try {
           val dialog = BottomSheetDialog(this)
           val view = layoutInflater.inflate(R.layout.gender_bottom_sheet, null)


           val genderRg = view.findViewById<RadioGroup>(R.id.genderRg)
           val maleRb = view.findViewById<RadioButton>(R.id.maleRb)
           val femaleRb = view.findViewById<RadioButton>(R.id.femaleRb)
           val doneBtn = view.findViewById<TextView>(R.id.done_genBtn)
           val cancel = view.findViewById<TextView>(R.id.cancel_genBtn)

          if (gender!=""){
              myApplication.printLogD(gender,"gender")
              if (gender.equals("male" ,true))
                  maleRb.isChecked = true
              else
                  femaleRb.isChecked = true
          }

           doneBtn.setOnClickListener {
               val rb = view.findViewById<RadioButton>(genderRg.checkedRadioButtonId)
               when (genderRg.checkedRadioButtonId) {
                   R.id.maleRb -> {
                       gender_botSheetBtn.text = rb!!.text.toString()
                   }
                   R.id.femaleRb -> gender_botSheetBtn.text = rb!!.text.toString()
               }

               dialog.dismiss()
           }

           cancel.setOnClickListener {
               dialog.dismiss()
           }

           dialog.setContentView(view)
           dialog.show()

       }catch (e:Exception){
           myApplication.printLogE(e.toString(),TAG)
       }
    }

    private fun selectImage() {
        CropImage.activity()
            .setScaleType(CropImageView.ScaleType.FIT_CENTER)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setCropMenuCropButtonTitle("Next")
            .start(this@EditProfileActivity)
    }


    private fun editProfile(
        name: String,
        auditionID: String,
        bio: String,
        gender: String,
        dob: String,
        imagePath: String
    ) {
        try {
            val editProfileApiUrl: String = APITags.APIBASEURL +"editProfile"

            val strRequest: VolleyMultipartRequest = object : VolleyMultipartRequest(
                Method.POST, editProfileApiUrl,
                com.android.volley.Response.Listener<NetworkResponse> { response ->
                    myApplication.printLogD(response.toString(),TAG)
                    progressDialog.dismiss()
                    sendToMainActivity()
                },
                com.android.volley.Response.ErrorListener { error ->
                    myApplication.printLogE(error.toString(),TAG)
                    progressDialog.dismiss()
                })
            {
                override fun getParams(): MutableMap<String, String>? {
                    val map = HashMap<String, String>()
                    map["typename"] = "user-profiles"
                    map["name"] = name
                    map["audition_id"] = auditionID
                    map["image"] = imagePath
                    map["bio"] = bio
                    map["gender"] = gender
                    map["dob"] = dob
                    Log.i("params", map.toString())
                    return map
                }

                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String?> {
                    val params: MutableMap<String, String?> = HashMap()
                    params["Authorization"] = sessionManager.getToken()
                    Log.i("Header", params.toString())
                    return params
                }


            }
            strRequest.setShouldCache(false)
            strRequest.retryPolicy = DefaultRetryPolicy(0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
            requestQueue?.add<NetworkResponse>(strRequest)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            val d = AlertDialog.Builder(this@EditProfileActivity)
            d.setTitle("Something went wrong")
            d.setCancelable(false)
            d.setMessage("Something went wrong, Please try again")
            d.setPositiveButton(
                "Retry"
            ) { dialog, which -> editProfile(name, auditionID, bio, gender, dob, this.imagePath) }
            d.setNegativeButton(
                "Cancel"
            ) { dialog, which -> (this@EditProfileActivity as Activity).finishAffinity() }
            try {
                if (d != null) {
                    d.show()
                }
            } catch (f: java.lang.Exception) {
                f.printStackTrace()
            }
        }
    }

    private fun sendToMainActivity() {
        val intent = Intent(this@EditProfileActivity,HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun verifyUserDetails(
        name: String,
        auditionIdText: String,
        bio: String,
        gender: String,
        dob: String,
        imagePath: String
    ) {
        val typename1 = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), "user-profiles")
        val name1: RequestBody =
            RequestBody.create("multipart/form-data".toMediaTypeOrNull(),name )
        val auditionID: RequestBody =
            RequestBody.create("multipart/form-data".toMediaTypeOrNull(), auditionIdText)
        val bio1: RequestBody =
            RequestBody.create("multipart/form-data".toMediaTypeOrNull(), bio)
        val gender11: RequestBody = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), gender)
        val dob1: RequestBody =
            RequestBody.create("multipart/form-data".toMediaTypeOrNull(), dob)

        var requestBody: RequestBody? = null
        var fileToUpload: MultipartBody.Part? = null
        var mCall: Call<CommanResponse>? = null
        if (!(imagePath.trim().equals(""))) {
            val file: File = File(imagePath)
            requestBody = RequestBody.create("*/*".toMediaTypeOrNull(),file)
            fileToUpload = MultipartBody.Part.createFormData("image",file.name,requestBody)
            mCall = apiInterface.editProfile(sessionManager.getToken(),
                typename1,
                name1,
                auditionID,
                fileToUpload,
                bio1,
                gender11,
                dob1
            )
        } else {
            mCall = apiInterface.editProfile(
                sessionManager.getToken(),
                typename1,
                name1,
                auditionID,
                bio1,
                gender11,
                dob1
            )
        }
        mCall.enqueue(object : Callback<CommanResponse> {
            override fun onResponse(call: Call<CommanResponse>, response: Response<CommanResponse>) {
                progressDialog.dismiss()
                if (response.isSuccessful && response.body()!!.success!!) {

                } else {

                }
            }

            override fun onFailure(call: Call<CommanResponse>, t: Throwable) {
                progressDialog.dismiss()
            }
        })
    }

}