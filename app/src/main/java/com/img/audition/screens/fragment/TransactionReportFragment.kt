package com.img.audition.screens.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.media3.common.util.UnstableApi
import com.img.audition.adapters.TransactionReportAdapter
import com.img.audition.dataModel.TransactionData
import com.img.audition.dataModel.TransactionReportResponse
import com.img.audition.databinding.FragmentTransactionReportBinding
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.screens.WalletActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList


@UnstableApi class TransactionReportFragment : Fragment() {

    private  var adapter: TransactionReportAdapter? = null
    private lateinit var data: ArrayList<TransactionData>
    private val TAG = "TransactionReportFragment"

    companion object {
        fun newInstance(): TransactionReportFragment {
            return TransactionReportFragment()
        }
    }

    private lateinit var _viewBinding : FragmentTransactionReportBinding
    private val view get() = _viewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _viewBinding = FragmentTransactionReportBinding.inflate(inflater,container,false)

        return _viewBinding.root
    }

    override fun onViewCreated(view1: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view1, savedInstanceState)

        view.closeMusicSheetButton.setOnClickListener {
            if (activity is WalletActivity) {
                (activity as WalletActivity).closeBottomSheet()
            }
        }

        getTransactionReport(view1.context)
    }

    private fun getTransactionReport(context: Context) {
        val apiInterface = RetrofitClient.getInstance().create(ApiInterface::class.java)
        val tranReq = apiInterface.getTransactions(SessionManager(context).getToken())
        tranReq.enqueue(object : Callback<TransactionReportResponse>{
            override fun onResponse(call: Call<TransactionReportResponse>, response: Response<TransactionReportResponse>) {
                if (response.isSuccessful && response.body()!!.success!!){
                     data = response.body()!!.data
                    if (data.size>0){
                        view.transReportCycle.visibility = View.VISIBLE
                        view.noTrans.visibility = View.GONE
                         adapter = TransactionReportAdapter(requireActivity(),data)
                        view.transReportCycle.adapter = adapter
                    }else{
                        view.transReportCycle.visibility = View.GONE
                        view.noTrans.visibility = View.VISIBLE
                    }
                }else{
                    Log.e(TAG, response.toString())
                }
            }

            override fun onFailure(call: Call<TransactionReportResponse>, t: Throwable) {
                t.printStackTrace()
            }

        })
    }

    override fun onDestroyView() {
        Log.d("check 400", "onDestroyView: $TAG")
        try {
            adapter = null
            data.clear()
        }catch (e:Exception){
            e.printStackTrace()
        }
        getView()?.destroyDrawingCache()
        super.onDestroyView()
    }
}