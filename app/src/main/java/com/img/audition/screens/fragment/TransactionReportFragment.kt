package com.img.audition.screens.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.img.audition.adapters.TransactionReportAdapter
import com.img.audition.dataModel.TransactionReportResponse
import com.img.audition.databinding.FragmentTransactionReportBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.screens.WalletActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class TransactionReportFragment : Fragment() {

    val TAG = "TransactionReportFragment"

    companion object {
        fun newInstance(): TransactionReportFragment {
            return TransactionReportFragment()
        }
    }

    private val myApplication by lazy {
        MyApplication(requireContext())
    }

    private val apiInterface by lazy{
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }

    private val sessionManager by lazy {
        SessionManager(requireContext())
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

        getTransactionReport()
    }

    private fun getTransactionReport() {
        val tranReq = apiInterface.getTransactions(sessionManager.getToken())
        tranReq.enqueue(object : Callback<TransactionReportResponse>{
            override fun onResponse(call: Call<TransactionReportResponse>, response: Response<TransactionReportResponse>) {
                if (response.isSuccessful && response.body()!!.success!!){
                    val data = response.body()!!.data
                    if (data.size>0){
                        view.transReportCycle.visibility = View.VISIBLE
                        view.noTrans.visibility = View.GONE
                        val adapter = TransactionReportAdapter(requireActivity(),data)
                        view.transReportCycle.adapter = adapter
                    }else{
                        view.transReportCycle.visibility = View.GONE
                        view.noTrans.visibility = View.VISIBLE
                    }
                }else{
                    myApplication.printLogE(response.toString(),TAG)
                }
            }

            override fun onFailure(call: Call<TransactionReportResponse>, t: Throwable) {
                myApplication.printLogE(t.toString(),TAG)
            }

        })
    }
}