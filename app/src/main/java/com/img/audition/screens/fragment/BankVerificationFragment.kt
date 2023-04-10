package com.img.audition.screens.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE
import com.canhub.cropper.CropImage.getActivityResult
import com.canhub.cropper.CropImageView
import com.img.audition.R
import com.img.audition.adapters.StateListAdapter
import com.img.audition.databinding.FragmentBankVerificationBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.*
import org.json.JSONException
import org.json.JSONObject
import java.io.*


class BankVerificationFragment() : Fragment() {

    val TAG = "BankVerificationFragment"
    lateinit var requestQueue: RequestQueue
    var state = ""
    var image_path = ""

    private lateinit var _viewBinding: FragmentBankVerificationBinding
    private val view get() = _viewBinding
    private var sessionManager: SessionManager? = null

    private var myApplication: MyApplication? = null
    private val apiInterface by lazy {
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestQueue = Volley.newRequestQueue(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding = FragmentBankVerificationBinding.inflate(inflater, container, false)

        sessionManager = SessionManager(requireContext())
        myApplication = MyApplication(requireContext())

        return _viewBinding.root
    }

    override fun onViewCreated(view1: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view1, savedInstanceState)

        val stateAr = requireActivity().resources.getStringArray(R.array.india_states)
        view.stateSpinner.adapter = StateListAdapter(requireContext(), stateAr)

        try {
            view.stateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>,
                    view1: View,
                    i: Int,
                    l: Long
                ) {
                    myApplication!!.printLogD(stateAr[i], "stateAr")
                    state = stateAr[i]
                    if (view.stateSpinner.selectedView != null)
                        (view.stateSpinner.selectedView as TextView).setTextColor(Color.BLACK)
                }

                override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            }
        } catch (e: java.lang.Exception) {
            myApplication!!.printLogE(e.toString(), TAG)
        }

        view.btnUpload.setOnClickListener {
            selectImage()
        }

        view.btnSubmit.setOnClickListener {
            if (validate())
                VerifyBankDetails()
        }
    }

    private fun selectImage() {
        CropImage.activity()
            .setScaleType(CropImageView.ScaleType.FIT_CENTER)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(16, 9)
            .setCropMenuCropButtonTitle("Next")
            .start(requireContext(), this)
    }

    fun validate(): Boolean {
        var valid = true
        if (view.name.getText().toString().length < 3) {
            valid = false
            view.name.setError("Please enter account holder name")
        } else view.name.setError(null)
        if (view.accountNumber.getText().toString().length < 6) {
            valid = false
            view.accountNumber.setError("Please enter account number")
        } else view.accountNumber.setError(null)
        if (view.branchName.getText().toString().length < 2) {
            valid = false
            view.branchName.setError("Please enter branch name")
        } else view.branchName.setError(null)
        if (view.bankName.getText().toString().length < 2) {
            valid = false
            view.bankName.setError("Please enter bank name")
        } else view.bankName.setError(null)
        if (view.accountNumber.getText().toString().length < 6) {
            valid = false
            view.accountNumber.setError("Please enter account number")
        } else if (view.accountNumber.getText().toString() != view.VaccountNumber.getText()
                .toString()
        ) {
            valid = false
            view.accountNumber.setError("Your account number and verify account number not matched")
        } else view.accountNumber.setError(null)
        if (!validIfsc(view.ifscCode.getText().toString())) {
            valid = false
            view.ifscCode.setError("Please enter valid IFSC Code")
        } else view.ifscCode.setError(null)
        if (valid) {
            if (view.stateSpinner.getSelectedItemPosition() == 0) {
                Toast.makeText(context, "Please select your state", Toast.LENGTH_SHORT).show()
                valid = false
            } else if (image_path == "") {
                valid = false
                myApplication!!.showToast("Please click a image of passbook first")
            }
        }
        return valid
    }

    fun validIfsc(text: String): Boolean {
        val pattern = Regex("^[A-Za-z]{4}[0][a-zA-Z0-9]{6}$")
        return if (text.matches(pattern)) true else false
    }

    fun VerifyBankDetails() {
        try {
            val url = APITags.APIBASEURL + "bankRequest"
            Log.i("url", url)
            val strRequest: VolleyMultipartRequest = object : VolleyMultipartRequest(
                Request.Method.POST, url,
                Response.Listener<NetworkResponse> { response ->
                    try {
                        val res = String(response.data, charset("UTF-8"))
                        val jsonObject = JSONObject(res)
                        if (jsonObject.getBoolean("status")) {
                            sessionManager!!.setBankVerified("0")
                            view.invalidBank.setVisibility(View.GONE)
                            view.bankVerified.setVisibility(View.VISIBLE)
                            view.bankText.setText("Your Bank details are sent for verification.")
                            view.bankNotVerified.setVisibility(View.GONE)
                            view.comment.setVisibility(View.GONE)
                            BankDetails()
                            view.bankText.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.Gray
                                )
                            )
                        } else {
                            myApplication!!.showToast(jsonObject.getString("message"))
                        }
                    } catch (e: UnsupportedEncodingException) {
                        myApplication!!.printLogE(e.message!!, TAG)

                    } catch (e: JSONException) {
                        myApplication!!.printLogE(e.message!!, TAG)
                    }
                },
                Response.ErrorListener { error -> Log.i("ErrorResponce", error.toString()) }) {

                override fun getParams(): MutableMap<String, String>? {
                    val map = HashMap<String, String>()
                    map["accountholder"] = view.name.getText().toString()
                    map["accno"] = view.accountNumber.getText().toString()
                    map["ifsc"] = view.ifscCode.getText().toString()
                    map["bankname"] = view.bankName.getText().toString()
                    map["bankbranch"] = view.branchName.getText().toString()
                    map["state"] = state
                    map["typename"] = "bank"
                    myApplication!!.printLogI(map.toString(), TAG)
                    return map
                }

                override fun getHeaders(): MutableMap<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    params["Authorization"] = sessionManager!!.getToken().toString()
                    Log.i("Header", params.toString())
                    return params
                }

                override fun getByteData(): MutableMap<String, DataPart> {
                    val params: MutableMap<String, DataPart> = HashMap<String, DataPart>()
                    params["image"] =
                        DataPart("BANK_Image_.jpg", convertToByte(image_path), "image/jpg")
                    Log.i("data", params.toString())
                    return params
                }
            }
            strRequest.setShouldCache(false)
            strRequest.setRetryPolicy(
                DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
            )
            requestQueue.add<NetworkResponse>(strRequest)
        } catch (e: Exception) {
            e.printStackTrace()
            val d = AlertDialog.Builder(context, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
            d.setTitle("Something went wrong")
            d.setCancelable(false)
            d.setMessage("Something went wrong, Please try again")
            d.setPositiveButton(
                "Retry"
            ) { dialog, which -> VerifyBankDetails() }
            d.setNegativeButton(
                "Cancel"
            ) { dialog, which -> (context as Activity).finishAffinity() }
            try {
                d.show()
            } catch (f: Exception) {
                f.printStackTrace()
            }
        }
    }

    fun convertToByte(path: String?): ByteArray? {
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

    fun BankDetails() {
        try {
            val url = APITags.APIBASEURL + "bankDetails"
            Log.i("url", url)
            val strRequest: StringRequest = object : StringRequest(
                Method.GET, url,
                Response.Listener<String> { response ->
                    try {
                        Log.i("Response is 101", response)
                        //                                JSONObject jsonObject = new JSONArray(response.toString()).getJSONObject(0);
                        val json = JSONObject(response)
                        if (json.getJSONObject("data") != null) {
                            val jsonObject = json.getJSONObject("data")
                            view.accNo.setText(jsonObject.getString("accno"))
                            view.ifscCode.setText(jsonObject.getString("ifsc"))
                            view.bnkName.setText(jsonObject.getString("bankname"))
                            view.branch.setText(jsonObject.getString("bankbranch"))
                            view.stateText.setText(jsonObject.getString("state"))
                            view.holderName.setText(jsonObject.getString("accountholdername"))
                            val bankImage = jsonObject.getString("image")
                            if (bankImage != "") {
                                Glide.with(requireContext()).load(bankImage)
                            }
                            if (jsonObject.has("comment")) view.comment.setText(
                                jsonObject.getString(
                                    "comment"
                                )
                            )
                            view.bankDetails.setVisibility(View.VISIBLE)
                        }
                    } catch (je: JSONException) {
                        je.printStackTrace()
                        val d = AlertDialog.Builder(context)
                        d.setTitle("Something went wrong")
                        d.setCancelable(false)
                        d.setMessage("Something went wrong, Please try again")
                        d.setPositiveButton(
                            "Retry"
                        ) { dialog, which -> BankDetails() }
                        d.setNegativeButton(
                            "Cancel"
                        ) { dialog, which ->
                            dialog.dismiss()
                            //                                        ((Activity) context).finish();
                        }
                        try {
                            if (d != null) {
                                d.show()
                            }
                        } catch (f: java.lang.Exception) {
                            f.printStackTrace()
                        }
                    }
                },
                Response.ErrorListener { error -> //                            ma.dialogdismiss(context);
                    Log.i("ErrorResponce", error.toString())
                }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params: MutableMap<String, String> = java.util.HashMap()
                    //                        params.put("Content-Type", "application/json; charset=UTF-8");
                    params["Authorization"] = sessionManager!!.getToken().toString()
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
            requestQueue.add(strRequest)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            val d = AlertDialog.Builder(context)
            d.setTitle("Something went wrong")
            d.setCancelable(false)
            d.setMessage("Something went wrong, Please try again")
            d.setPositiveButton(
                "Retry"
            ) { dialog, which -> BankDetails() }
            d.setNegativeButton(
                "Cancel"
            ) { dialog, which -> (context as Activity).finishAffinity() }
            try {
                if (d != null) {
                    d.show()
                }
            } catch (f: java.lang.Exception) {
                f.printStackTrace()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result: CropImage.ActivityResult? = getActivityResult(data)
            try {
                image_path = result!!.getUriFilePath(requireContext(), true).toString()
                Log.e("Check file", image_path)
                view.img.setImageURI(Uri.parse(image_path))
            } catch (e: Exception) {
                myApplication!!.printLogE(e.message.toString(), TAG)
            }
        }
    }

}

