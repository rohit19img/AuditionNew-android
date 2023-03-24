package com.img.audition.screens.fragment

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE
import com.canhub.cropper.CropImage.activity
import com.canhub.cropper.CropImage.getActivityResult
import com.canhub.cropper.CropImageView
import com.canhub.cropper.CropImageView.Guidelines
import com.img.audition.databinding.FragmentPanValidationBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.APITags
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*



class PanValidationFragment : Fragment() {

    val TAG = "PanValidationFragment"
    var imagepath = ""
    var dob: String = ""
    private lateinit var _viewBinding : FragmentPanValidationBinding
    private val view get() = _viewBinding!!
    private val sessionManager by lazy {
        SessionManager(requireContext())
    }

    private val myApplication by lazy {
        MyApplication(requireContext())
    }
    private val apiInterface by lazy{
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _viewBinding = FragmentPanValidationBinding.inflate(inflater,container,false)
        view.dob.setOnClickListener {
            pickDate(view.dob)
        }

        view.btnSubmit.setOnClickListener {
            validate()
        }

        view.btnUpload.setOnClickListener {
            selectImage()
        }

        return view.root
    }

    fun validate() {
        if (view.name.text.toString() == "") {
            view.name.error = "Please Enter Name"
        } else if (view.name.text.toString().length < 4) {
            view.name.error = "Enter name mor then 4 char"
        } else if (view.dobText.text.toString() == "Date of birth*") {
            view.dobText.error = "Please select dob"
        } else if (imagepath == "") {
                myApplication.showToast("Please select image")
        } else {
            VerifyPanDetails()
        }
    }

    private fun VerifyPanDetails() {
        TODO("Not yet implemented")
    }


    private fun selectImage() {
        activity()
            .setScaleType(CropImageView.ScaleType.FIT_CENTER)
            .setGuidelines(Guidelines.ON)
            .setAspectRatio(16, 9)
            .setCropMenuCropButtonTitle("Next")
            .start(requireContext(), this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result: CropImage.ActivityResult? = getActivityResult(data)
            try {
                imagepath = result!!.getUriFilePath(requireContext(), true).toString()
                Log.e("Check file", imagepath)
                view.img.setImageURI(Uri.parse(imagepath))
            } catch (e: Exception) {
                myApplication.printLogE(e.message.toString(),TAG)
            }
        }
    }

    fun pickDate(dialog: TextView) {
        val mcurrentDate = Calendar.getInstance()
        val mYear = mcurrentDate[Calendar.YEAR]
        val mMonth = mcurrentDate[Calendar.MONTH]
        val mDay = mcurrentDate[Calendar.DAY_OF_MONTH]
        val mDatePicker = DatePickerDialog(
            requireActivity(),
            { datepicker, selectedyear, selectedmonth, selectedday ->
                var selectedmonth = selectedmonth
                selectedmonth = selectedmonth + 1
                val d = "$selectedyear-$selectedmonth-$selectedday"
                val date1: Date
                var date2: String? = null
                try {
                    val d1: DateFormat = SimpleDateFormat("yyyy-MM-dd")
                    date1 = d1.parse(d)
                    val d2: DateFormat = SimpleDateFormat("dd-MMM-yyyy")
                    date2 = d2.format(date1)
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
                dialog.text = "Date : $date2"
                dob = d
            }, mYear, mMonth, mDay
        )
        mDatePicker.datePicker.maxDate = (System.currentTimeMillis() - 5.681e+11).toLong()
        mDatePicker.setTitle("Select Birth Date")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mDatePicker.datePicker.firstDayOfWeek = Calendar.MONDAY
        }
        mDatePicker.show()
    }
}