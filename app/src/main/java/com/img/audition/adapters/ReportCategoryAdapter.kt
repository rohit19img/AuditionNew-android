package com.img.audition.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.img.audition.R
import com.img.audition.dataModel.CommanResponse
import com.img.audition.dataModel.ReportCategoryData
import com.img.audition.databinding.CommanlistCycleDesignBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.screens.HomeActivity
import com.img.audition.screens.OtherUserProfileActivity
import com.img.audition.screens.fragment.VideoReportDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@UnstableApi
class ReportCategoryAdapter() : RecyclerView.Adapter<ReportCategoryAdapter.MyViewHolder>() {

    lateinit var context: Context
    lateinit var  categoryList: ArrayList<ReportCategoryData>
    lateinit var vID: String
    lateinit var viewType: String
    lateinit var  videoAdapter: VideoAdapter
    var videoPosition: Int = 0
    lateinit var  videoReportDialog: VideoReportDialog

    constructor(context: Context, categoryList: ArrayList<ReportCategoryData>, userID: String, viewType: String, videoReportDialog: VideoReportDialog) : this()
    {
        this.context = context
        this.categoryList = categoryList
        this.vID = userID
        this.viewType = viewType
        this.videoReportDialog = videoReportDialog
    }

    constructor(context: Context, categoryList: ArrayList<ReportCategoryData>, vID: String, viewType: String, videoAdapter: VideoAdapter,videoPosition: Int,videoReportDialog: VideoReportDialog) : this()
    {
        this.context = context
        this.categoryList = categoryList
        this.vID = vID
        this.viewType = viewType
        this.videoReportDialog = videoReportDialog
        this.videoAdapter = videoAdapter
        this.videoPosition = videoPosition

    }


    private val TAG = "ReportCategoryAdapter"
    private val apiInterface by lazy{
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }
    private val sessionManager by lazy {
        SessionManager(context.applicationContext)
    }
    inner class MyViewHolder(itemView: CommanlistCycleDesignBinding) : RecyclerView.ViewHolder(itemView.root) {
        val catName = itemView.text
        val reportCard = itemView.reportCard
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemBinding = CommanlistCycleDesignBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(itemBinding)
    }

    override fun getItemCount(): Int {
       return categoryList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.apply {
            catName.text = categoryList[position].name.toString()

            if (!(categoryList[position].isSelected)){
                holder.reportCard.background = context.getDrawable(R.drawable.cardview_look)
                holder.catName.setTextColor(context.getColor(R.color.textColorBlack))
            }else{
                holder.reportCard.background = context.getDrawable(R.drawable.card_rummy_design)
                holder.catName.setTextColor(context.getColor(R.color.white))
            }

            holder.itemView.setOnClickListener {
                for (zz in categoryList) {
                    zz.isSelected = false
                }
                categoryList[position].isSelected = true
                notifyDataSetChanged()

                val sweetAlertDialog = SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                when (viewType) {
                    ConstValFile.ReportDialogView -> {
                        sweetAlertDialog.titleText = "Report"
                        sweetAlertDialog.contentText = "Do you want Report this video"

                    }
                    ConstValFile.NotInterestedDialogView -> {
                        sweetAlertDialog.titleText = "Not Interested"
                        sweetAlertDialog.contentText = "Add Into Not Interested"

                    }
                    else -> {
                        sweetAlertDialog.titleText = "Report User"
                        sweetAlertDialog.contentText = "Do you want Report this user"

                    }
                }
                sweetAlertDialog.confirmText = "Yes"
                sweetAlertDialog.setConfirmClickListener {
                    sweetAlertDialog.dismiss()
                    when (viewType) {
                        ConstValFile.ReportDialogView -> {
                            reportVideo(categoryList[position].Id!!)
                        }
                        ConstValFile.NotInterestedDialogView -> {
                            addIntoNoInterestedVideo(categoryList[position].Id!!)
                        }
                        else -> {
                            reportUser(categoryList[position].Id!!)
                        }
                    }

                }
                sweetAlertDialog.cancelText = "No"
                sweetAlertDialog.setCancelClickListener {
                    sweetAlertDialog.dismiss()
                }
                sweetAlertDialog.show()
            }
        }
    }

    private fun reportUser(categoryID: String) {
        val reportVideoReq = apiInterface.reportUser(sessionManager.getToken(),categoryID,vID)
        Log.d("reportUser", "reportUser: $reportVideoReq")
        reportVideoReq.enqueue(object : Callback<CommanResponse> {
            override fun onResponse(call: Call<CommanResponse>, response: Response<CommanResponse>) {
                if (response.isSuccessful){
                    if ( response.body()!!.success!! && response.body()!=null){
                        showToast("Report Successfully..")
                        videoReportDialog.dismiss()
                        if (context is OtherUserProfileActivity){
                            context.startActivity(Intent(context, HomeActivity::class.java))
                            (context as OtherUserProfileActivity).finish()
                        }
                    }else{
                        showToast(response.body()!!.message!!)
                        videoReportDialog.dismiss()
                    }
                }else{
                    Log.d(TAG, "onResponse: No Data ${response.code()}")
                    videoReportDialog.dismiss()
                }
            }
            override fun onFailure(call: Call<CommanResponse>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message.toString()}")
                videoReportDialog.dismiss()

            }

        })
    }
    private fun reportVideo(categoryID: String) {
        val reportVideoReq = apiInterface.reportTheVideo(sessionManager.getToken(),categoryID,vID)

        Log.d("checkReport", "reportVideo: $reportVideoReq")
        reportVideoReq.enqueue(object : Callback<CommanResponse> {
            override fun onResponse(call: Call<CommanResponse>, response: Response<CommanResponse>) {
                if (response.isSuccessful){
                    if (response.body()!!.success!!){
                        showToast("Report Successfully..")
                        videoAdapter.videoList.removeAt(videoPosition)
                        videoAdapter.notifyItemRemoved(videoPosition)
                        videoReportDialog.dismiss()
                    }else{
                        showToast(response.body()!!.message!!)
                        videoReportDialog.dismiss()
                    }
                }else{
                    videoReportDialog.dismiss()
                    Log.d(TAG, "onResponse: No Data ${response.code()}")
                }
            }
            override fun onFailure(call: Call<CommanResponse>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message.toString()}")
                videoReportDialog.dismiss()
            }

        })
    }

    private fun showToast(msg: String) {
        Toast.makeText(context.applicationContext, msg, Toast.LENGTH_SHORT).show()
    }

    private fun addIntoNoInterestedVideo(categoryID: String) {
        val reportVideoReq = apiInterface.addIntoNotInterestedVideo(sessionManager.getToken(),categoryID,vID)

        reportVideoReq.enqueue(object :Callback<CommanResponse>{
            override fun onResponse(call: Call<CommanResponse>, response: Response<CommanResponse>) {
                if (response.isSuccessful){
                    if (response.body()!!.success!!){
                        showToast("Add Successfully..")
                        videoAdapter.videoList.removeAt(videoPosition)
                        videoAdapter.notifyItemRemoved(videoPosition)
                        videoReportDialog.dismiss()
                    }else{
                        showToast(response.body()!!.message!!)
                        videoReportDialog.dismiss()
                    }
                }else{
                    videoReportDialog.dismiss()
                    Log.d(TAG, "onResponse: No Data ${response.code()}")
                }
            }
            override fun onFailure(call: Call<CommanResponse>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message.toString()}")
                videoReportDialog.dismiss()
            }
        })
    }
}