package com.img.audition.screens.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.img.audition.R
import com.img.audition.adapters.ReportCategoryAdapter
import com.img.audition.dataModel.CommanResponse
import com.img.audition.dataModel.ReportCategoryData
import com.img.audition.dataModel.ReportCategoryResponse
import com.img.audition.databinding.VideoReportDialogBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

 class VideoReportDialog(val contextFromActivity:Context ,val vID:String, val viewType :String) : BottomSheetDialogFragment() {

    val TAG = "VideoReportDialog"
    private val sessionManager by lazy {
        SessionManager(requireActivity().applicationContext)
    }
    private val apiInterface by lazy{
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    lateinit var view: VideoReportDialogBinding
    var listReport: ArrayList<ReportCategoryData> = ArrayList()
    var categoryID : String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        view = VideoReportDialogBinding.inflate(inflater,container,false)

        return view.root
    }

    override fun onViewCreated(view1: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view1, savedInstanceState)

        if (viewType == ConstValFile.ReportDialogView){
            reportCategory()
            view.reportSubmitBtn.text =  "Report Video"
            view.title.text =  "Report Video"
        }else if(viewType == ConstValFile.NotInterestedDialogView){
            getNotInterestedCategory()
            view.reportSubmitBtn.text =  "Not Interested"
            view.title.text =  "Not Interested"
        }else{
            reportCategory()
            view.reportSubmitBtn.text =  "Report User"
            view.title.text =  "Report User"
        }


        view.reportSubmitBtn.setOnClickListener {
            var reportCat = ""
            for (zz in listReport) {
                Log.i("report_test","${zz.name} : ${zz.isSelected}")
                if (zz.isSelected){
                    reportCat = zz.name!!
                    categoryID = zz.Id!!
                }
            }
            if (reportCat == ""){
                showToast("Please select one of these options..")
            }
            else{
                if (viewType == ConstValFile.ReportDialogView){
                    reportVideo(categoryID)
                }else if(viewType == ConstValFile.NotInterestedDialogView){
                    addIntoNoInterestedVideo(categoryID)
                }else{
                    reportVideo(categoryID)
                }
                dismiss()
            }
        }
    }


    private fun reportCategory() {
        val reportCatReq = apiInterface.getReportCategory(sessionManager.getToken())

        reportCatReq.enqueue(object : Callback<ReportCategoryResponse>{
            override fun onResponse(call: Call<ReportCategoryResponse>, response: Response<ReportCategoryResponse>) {
                if (response.isSuccessful && response.body()!!.success!! && response.body()!=null){
                    listReport = response.body()!!.data
                    val adapter = ReportCategoryAdapter(requireContext(),listReport,vID)
                    view.reportCycle.adapter = adapter
                }else{
                    printLogE("No Data ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ReportCategoryResponse>, t: Throwable) {
                printLogE(t.toString())
            }

        })
    }

    private fun getNotInterestedCategory() {
        val reportCatReq = apiInterface.getNotInterestedCategory(sessionManager.getToken())

        reportCatReq.enqueue(object : Callback<ReportCategoryResponse>{
            override fun onResponse(call: Call<ReportCategoryResponse>, response: Response<ReportCategoryResponse>) {
                if (response.isSuccessful && response.body()!!.success!! && response.body()!=null){
                    listReport = response.body()!!.data
                    val adapter = ReportCategoryAdapter(requireContext(),listReport,vID)
                    view.reportCycle.adapter = adapter
                }else{
                    printLogE("No Data ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ReportCategoryResponse>, t: Throwable) {
                printLogE(t.toString())
            }

        })
    }

    private fun reportVideo(categoryID: String) {
        val reportVideoReq = apiInterface.reportTheVideo(sessionManager.getToken(),categoryID,vID)

        reportVideoReq.enqueue(object :Callback<CommanResponse>{
            override fun onResponse(call: Call<CommanResponse>, response: Response<CommanResponse>) {
                if (response.isSuccessful && response.body()!!.success!! && response.body()!=null){
                    showToast("Report Successfully..")
                }else{
                  printLogE("No Data ${response.code()}")
                }
            }
            override fun onFailure(call: Call<CommanResponse>, t: Throwable) {
                printLogE(t.message.toString())
            }

        })
    }

    private fun addIntoNoInterestedVideo(categoryID: String) {
        val reportVideoReq = apiInterface.addIntoNotInterestedVideo(sessionManager.getToken(),categoryID,vID)

        reportVideoReq.enqueue(object :Callback<CommanResponse>{
            override fun onResponse(call: Call<CommanResponse>, response: Response<CommanResponse>) {
                if (response.isSuccessful && response.body()!!.success!! && response.body()!=null){
                    showToast("Add Successfully..")
                }else{
                    printLogE("No Data ${response.code()}")
                }
            }
            override fun onFailure(call: Call<CommanResponse>, t: Throwable) {
                printLogE(t.message.toString())
            }

        })
    }

    fun showToast(msg:String){
        Toast.makeText(contextFromActivity,msg,Toast.LENGTH_SHORT).show()
    }

    fun printLogE(msg:String){
        Log.e(TAG, msg)
    }
}