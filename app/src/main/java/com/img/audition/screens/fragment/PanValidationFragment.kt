package com.img.audition.screens.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.android.volley.*
import com.android.volley.Response.ErrorListener
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE
import com.canhub.cropper.CropImage.activity
import com.canhub.cropper.CropImage.getActivityResult
import com.canhub.cropper.CropImageView
import com.canhub.cropper.CropImageView.Guidelines
import com.img.audition.R
import com.img.audition.dataModel.UserVerificationResponse
import com.img.audition.databinding.FragmentPanValidationBinding
import com.img.audition.network.*
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class PanValidationFragment : Fragment() {

    val TAG = "PanValidationFragment"
    var imagepath = ""
    var dob: String = ""
    var requestQueue: RequestQueue? = null

    private lateinit var _viewBinding : FragmentPanValidationBinding
    private val view get() = _viewBinding
    private val sessionManager by lazy {
        SessionManager(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestQueue = Volley.newRequestQueue(activity)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _viewBinding = FragmentPanValidationBinding.inflate(inflater,container,false)

        return view.root
    }

    override fun onViewCreated(view1: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view1, savedInstanceState)

        AllVerify()

        view.dob.setOnClickListener {
            pickDate(view.dob)
        }

        view.btnSubmit.setOnClickListener {
            validate(view1.context)
        }

        view.btnUpload.setOnClickListener {
            selectImage()
        }
    }

    fun validate(context: Context) {
        if (view.name.text.toString() == "") {
            view.name.error = "Please Enter Name"
        } else if (view.name.text.toString().length < 4) {
            view.name.error = "Enter name mor then 4 char"
        } else if (view.dobText.text.toString() == "Date of birth*") {
            view.dobText.error = "Please select dob"
        } else if (imagepath == "") {
            Toast.makeText(context,"Please select image", Toast.LENGTH_SHORT).show()
        } else {
            VerifyPanDetails()
        }
    }

    fun VerifyPanDetails() {
        try {
            val url = APITags.APIBASEURL + "panrequest"
            val strRequest: VolleyMultipartRequest = @SuppressLint("SetTextI18n")
            object : VolleyMultipartRequest(
                Method.POST, url,
                Response.Listener<NetworkResponse?> {
                    sessionManager.setPANVerified("0")
                    view.panVerified.text = "Your PAN Card details are sent for verification."
                    view.invalidRequest.visibility = View.GONE
                    view.cardVerified.visibility = View.VISIBLE
                    view.cardNotVerified.visibility = View.GONE
                    view.comment.visibility = View.GONE
                    PANDetails()
                },
                ErrorListener { error -> Log.i("ErrorResponce", error.toString()) }) {
                override fun getParams(): Map<String, String> {
                    val map = HashMap<String, String>()
                    map["panname"] = view.name.text.toString()
                    map["pannumber"] = view.panNumber.text.toString()
                    map["dob"] = dob
                    map["typename"] = "pancard"
                    Log.d("map", map.toString())
                    return map
                }

                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String?> {
                    val params: MutableMap<String, String?> = HashMap()
                    params["Authorization"] = sessionManager.getToken()
                    Log.i("Header", params.toString())
                    return params
                }

                override fun getByteData(): Map<String, DataPart> {
                    val params: MutableMap<String, DataPart> = HashMap()
                    params["image"] =
                        DataPart("BANK_Image.jpg", convertToByte(imagepath), "image/jpg")
                    Log.i("data", params.toString())
                    return params
                }
            }
            strRequest.setShouldCache(false)
            strRequest.retryPolicy = DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
            requestQueue!!.add<NetworkResponse>(strRequest)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    private fun selectImage() {
        activity()
            .setScaleType(CropImageView.ScaleType.FIT_CENTER)
            .setGuidelines(Guidelines.ON)
            .setAspectRatio(16, 9)
            .setCropMenuCropButtonTitle("Next")
            .start(requireContext(), this)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result: CropImage.ActivityResult? = getActivityResult(data)
            try {
                imagepath = result!!.getUriFilePath(requireContext(), true).toString()
                Log.e("Check file", imagepath)
                view.img.setImageURI(Uri.parse(imagepath))
            } catch (e: Exception) {
               e.printStackTrace()
            }
        }
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    fun pickDate(dialog: TextView) {
        val mcurrentDate = Calendar.getInstance()
        val mYear = mcurrentDate[Calendar.YEAR]
        val mMonth = mcurrentDate[Calendar.MONTH]
        val mDay = mcurrentDate[Calendar.DAY_OF_MONTH]
        val mDatePicker = DatePickerDialog(
            requireActivity(),
            { datepicker, selectedyear, selectedmonth, selectedday ->
                var selectedmonth = selectedmonth
                selectedmonth += 1
                val d = "$selectedyear-$selectedmonth-$selectedday"
                val date1: Date
                var date2: String? = null
                try {
                    val d1: DateFormat = SimpleDateFormat("yyyy-MM-dd")
                    date1 = d1.parse(d) as Date
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
        mDatePicker.datePicker.firstDayOfWeek = Calendar.MONDAY
        mDatePicker.show()
    }


    fun convertToByte(path: String?): ByteArray {
        Log.i("path", path!!)
        val file = File(path)
        Log.i("length", file.length().toString())
        if (file.exists()) {
            Log.i("file", "exists")
        } else Log.i("file", " not exists")
        val b = ByteArray(file.length().toInt())
        try {
            val fileInputStream = FileInputStream(file)
            fileInputStream.read(b)
            //            for (int i = 0; i < b.length; i++) {
//                System.out.print((char)b[i]);
//            }
        } catch (e: FileNotFoundException) {
            println("File Not Found.")
            e.printStackTrace()
        } catch (e1: IOException) {
            println("Error Reading The File.")
            e1.printStackTrace()
        }
        return b
    }

    fun PANDetails() {
        try {
            val url = APITags.APIBASEURL + "panDetails"
            Log.i("url", url)
            val strRequest: StringRequest = object : StringRequest(
                Method.GET, url,
                Response.Listener<String> { response ->
                    try {
                        Log.i("Response is", response)
                        val json = JSONObject(response)
                        val jsonObject = json.getJSONObject("data")
                        val panName = jsonObject.getString("panname")
                        if (panName != "") {
                            view.panname.text = panName
                        }
                        val pannumber = jsonObject.getString("pannumber")
                        if (pannumber != "") {
                            view.number.text = pannumber
                        }
                        val pandob = jsonObject.getString("pandob")
                        if (pandob != "") {
                            view.pandob.text = pandob
                        }
                        val panImage = jsonObject.getString("image")
                        if (panImage != "") {
                            Glide.with(requireContext()).load(panImage).into(view.pancard)
                        }

                        if (jsonObject.has("comment")) view.comment.text = jsonObject.getString("comment")
                        view.cardDetails.visibility = View.VISIBLE
                    } catch (je: JSONException) {
                        je.printStackTrace()
                        val d = AlertDialog.Builder(
                            activity, AlertDialog.THEME_DEVICE_DEFAULT_DARK
                        )
                        d.setTitle("Something went wrong")
                        d.setCancelable(false)
                        d.setMessage("Something went wrong, Please try again")
                        d.setPositiveButton(
                            "Retry"
                        ) { _, _ -> PANDetails() }
                        d.setNegativeButton(
                            "Cancel"
                        ) { _, _ -> (requireActivity() as Activity).finish() }
                        try {
                            d.show()
                        } catch (f: java.lang.Exception) {
                            f.printStackTrace()
                        }
                    }
                },
                ErrorListener { error ->
                    Log.i("ErrorResponce", error.toString())
                    val networkResponse = error.networkResponse
                    if (networkResponse != null) {
                        // HTTP Status Code: 401 Unauthorized
                    } else {
                        val d = AlertDialog.Builder(
                            activity, AlertDialog.THEME_DEVICE_DEFAULT_DARK
                        )
                        d.setTitle("Something went wrong")
                        d.setCancelable(false)
                        d.setMessage("Something went wrong, Please try again")
                        d.setPositiveButton(
                            "Retry"
                        ) { dialog, _ ->
                            dialog.dismiss()
                            PANDetails() }
                        d.setNegativeButton(
                            "Cancel"
                        ) { dialog, _ ->
                            dialog.dismiss()
                            (requireActivity() as Activity).finish() }
                        try {
                            d.show()
                        } catch (f: java.lang.Exception) {
                            f.printStackTrace()
                        }
                    }
                }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    //                        params.put("Content-Type", "application/json; charset=UTF-8");
                    params["Authorization"] = sessionManager.getToken()!!
                    Log.i("Header", params.toString())
                    return params
                }
            }
            strRequest.setShouldCache(false)
            strRequest.retryPolicy = DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
            requestQueue!!.add(strRequest)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun AllVerify() {
        val apiInterface = RetrofitClient.getInstance().create(ApiInterface::class.java)

        val allVerifyReq = apiInterface.getAllVerificationsData(sessionManager.getToken())
        allVerifyReq.enqueue(object : Callback<UserVerificationResponse> {
            override fun onResponse(
                call: Call<UserVerificationResponse>,
                response: retrofit2.Response<UserVerificationResponse>
            ) {
                Log.i("pan_verify","pan verify : ${response.body()!!.data!!.pan_verify}")

                if (response.isSuccessful && response.body()!!.success!! && response.body()!=null){
                    val data = response.body()!!.data

                    if (data!=null){
                        val mobile_verify = data.mobileVerify
                        val bank_verify = data.bankVerify
                        val pan_verify = data.pan_verify


                        if(mobile_verify == 1 && data.emailVerify == 1){
                            sessionManager.setBankVerified(bank_verify.toString())
                            sessionManager.setPANVerified(pan_verify.toString())

                            if (pan_verify == 0) {
                                view.panVerified.text = "Your PAN Card details are sent for verification."
                                view.cardVerified.visibility = View.VISIBLE
                                view.invalidRequest.visibility = View.GONE
                                view.panicon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_credit_card_green))
                                view.cardNotVerified.visibility = View.GONE
                                PANDetails()
                            } else if (pan_verify == 1) {
                                view.invalidRequest.visibility = View.GONE
                                view.cardNotVerified.visibility = View.GONE
                                view.cardVerified.visibility = View.VISIBLE
                                view.panicon.setImageResource(R.drawable.ic_credit_card_green)
                                PANDetails()
                            } else if (pan_verify == -1) {
                                view.panicon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_credit_card_green))
                                view.invalidRequest.visibility = View.GONE
                                view.cardVerified.visibility = View.GONE
                                view.cardNotVerified.setVisibility(View.VISIBLE)
                            } else {
                                view.panicon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_credit_card_green))
                                view.invalidRequest.visibility = View.GONE
                                view.cardNotVerified.visibility = View.VISIBLE
                                view.comment.visibility = View.VISIBLE
                                view.panVerified.text = "Your PAN Card details are Rejected."
                                view.panVerified.setTextColor(ContextCompat.getColor(requireContext(), R.color.bgColorRed))
                                view.cardVerified.visibility = View.VISIBLE
                                PANDetails()
                            }
                        } else {
                            view.invalidRequest.visibility = View.VISIBLE
                            view.cardNotVerified.visibility = View.GONE
                            view.comment.visibility = View.GONE
                            view.cardVerified.visibility = View.GONE
                        }
                    }

                }else{
                    Log.e(TAG, response.toString())
                }
            }

            override fun onFailure(call: Call<UserVerificationResponse>, t: Throwable) {
                Log.i("Exception",t.toString())
                Log.i("Exception",t.message.toString())
            }

        })
    }

    override fun onDestroyView() {
        Log.d("check 400", "onDestroyView: $TAG")
        getView()?.destroyDrawingCache()
        super.onDestroyView()
    }
}