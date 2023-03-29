package com.img.audition.screens

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.img.audition.R
import com.img.audition.dataModel.UserSelfProfileResponse
import com.img.audition.databinding.ActivityEditProfileBinding
import com.img.audition.globalAccess.AppPermission
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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
    lateinit var appPermission : AppPermission

    lateinit var gender_botSheetBtn:TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        appPermission =  AppPermission(this@EditProfileActivity,
            ConstValFile.PERMISSION_LIST,
            ConstValFile.REQUEST_PERMISSION_CODE_STORAGE)


        progressDialog = ProgressDialog(this@EditProfileActivity)
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Please Wait")

        viewBinding.mobilenumber.isEnabled = false

        gender_botSheetBtn = viewBinding.gender

        gender_botSheetBtn.setOnClickListener {
            genderBottomDialog()
        }
        viewBinding.dob.setOnClickListener {
            pickDate(viewBinding.dob)
        }

        viewBinding.btnSave.setOnClickListener {

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
        mDatePicker.datePicker.maxDate = (System.currentTimeMillis() - 1000)
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
        }else{
            myApplication.showToast("Please Allow Permission..")
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
              if (gender == "male")
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



}