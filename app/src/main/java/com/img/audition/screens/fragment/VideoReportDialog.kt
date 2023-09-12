package com.img.audition.screens.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.media3.common.util.UnstableApi

import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.img.audition.R
import com.img.audition.adapters.ReportCategoryAdapter
import com.img.audition.adapters.VideoAdapter
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
@UnstableApi
class VideoReportDialog() : BottomSheetDialogFragment() {
     lateinit var contextFromActivity:Context
     lateinit var vID:String
     lateinit var viewType :String
     lateinit var videoAdapter: VideoAdapter
     var videoPosition: Int = 0

    constructor(contextFromActivity: Context,vID: String,viewType: String) : this() {
        this.contextFromActivity = contextFromActivity
        this.vID = vID
        this.viewType = viewType
    }
     constructor(contextFromActivity:Context , vID:String, viewType :String,videoAdapter: VideoAdapter,videoPosition:Int) : this()
     {
         this.viewType = viewType
         this.vID = vID
         this.contextFromActivity = contextFromActivity
         this.videoAdapter = videoAdapter
         this.videoPosition = videoPosition
     }

     private  var adapter: ReportCategoryAdapter? = null
     private val TAG = "VideoReportDialog"
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
            view.title.text =  "Report Video"
        }else if(viewType == ConstValFile.NotInterestedDialogView){
            getNotInterestedCategory()
            view.title.text =  "Not Interested"
        }else{
            userReportCategory()
            view.title.text =  "Report User"
        }
    }


    private fun userReportCategory() {
        val reportCatReq = apiInterface.getReportCategory(sessionManager.getToken())

        reportCatReq.enqueue(object : Callback<ReportCategoryResponse>{
            override fun onResponse(call: Call<ReportCategoryResponse>, response: Response<ReportCategoryResponse>) {
                if (response.isSuccessful && response.body()!!.success!! && response.body()!=null){
                    listReport = response.body()!!.data
                    val adapter = ReportCategoryAdapter(contextFromActivity,listReport,vID,viewType,this@VideoReportDialog)
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

    private fun reportCategory() {
        val reportCatReq = apiInterface.getReportCategory(sessionManager.getToken())

        reportCatReq.enqueue(object : Callback<ReportCategoryResponse>{
            override fun onResponse(call: Call<ReportCategoryResponse>, response: Response<ReportCategoryResponse>) {
                if (response.isSuccessful && response.body()!!.success!! && response.body()!=null){
                    listReport = response.body()!!.data
                    val adapter = ReportCategoryAdapter(requireContext(),listReport,vID,viewType,videoAdapter,videoPosition,this@VideoReportDialog)
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
                    adapter = ReportCategoryAdapter(requireContext(), listReport, vID, viewType, videoAdapter, videoPosition, this@VideoReportDialog)
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

    fun showToast(msg:String){
        Toast.makeText(contextFromActivity,msg,Toast.LENGTH_SHORT).show()
    }

    fun printLogE(msg:String){
        Log.e(TAG, msg)
    }
     override fun onDestroyView() {
         try {
             listReport.clear()
             adapter = null
         }catch (e:Exception){
             e.printStackTrace()
         }
         super.onDestroyView()
     }
}