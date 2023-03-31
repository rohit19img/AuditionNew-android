package com.img.audition.screens.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.img.audition.R
import com.img.audition.adapters.ReportCategoryAdapter
import com.img.audition.dataModel.CommonResponse
import com.img.audition.dataModel.Languages
import com.img.audition.dataModel.ReportCategoryData
import com.img.audition.dataModel.ReportCategoryResponse
import com.img.audition.databinding.VideoReportDialogBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VideoReportDialog(val vID:String) : BottomSheetDialogFragment() {

    val TAG = "VideoReportDialog"
    private val sessionManager by lazy {
        SessionManager(requireActivity().applicationContext)
    }
    private val apiInterface by lazy{
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }
    private val myApplication by lazy {
        MyApplication(requireActivity().applicationContext)
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
                myApplication.showToast("Please select one of these options..")
            }else{
                reportVideo(categoryID)
                myApplication.showToast("Report Successfully..")
                dismiss()
            }
        }
        reportCategory()
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
                    myApplication.printLogE("No Data ${response.code()}",TAG)
                }
            }

            override fun onFailure(call: Call<ReportCategoryResponse>, t: Throwable) {
                myApplication.printLogE(t.toString(),TAG)
            }

        })
    }

    private fun reportVideo(categoryID: String) {
        val reportVideoReq = apiInterface.reportTheVideo(sessionManager.getToken(),categoryID,vID)

        reportVideoReq.enqueue(object :Callback<CommonResponse>{
            override fun onResponse(call: Call<CommonResponse>, response: Response<CommonResponse>) {
                if (response.isSuccessful && response.body()!!.success!! && response.body()!=null){
//                    myApplication.showToast("Report Successfully..")
                }else{
//                    myApplication.printLogE("No Data ${response.code()}",TAG)
                }
            }
            override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
                myApplication.printLogE(t.message.toString(),TAG)
            }

        })
    }

}