package com.img.audition.screens.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.img.audition.adapters.DetailsPrizecardAdapter
import com.img.audition.dataModel.SingleContestDetailsResponse
import com.img.audition.databinding.FragmentDetailsPrizecardBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class DetailsPrizecardFragment(val contestID: String) : Fragment() {

    val TAG = "JoinedContestFragment"
    private lateinit var _viewBinding : FragmentDetailsPrizecardBinding
    private val view get() = _viewBinding
    private val sessionManager by lazy {
        SessionManager(requireContext())
    }

    private val myApplication by lazy {
        MyApplication(requireContext())
    }
    private val apiInterface by lazy{
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _viewBinding = FragmentDetailsPrizecardBinding.inflate(inflater,container,false)

        return _viewBinding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contestDetails(contestID)
    }


    private fun contestDetails(contestID: String?) {
        val contestDetailsReq =  apiInterface.getSingleContestDetails(sessionManager.getToken(),contestID)

        contestDetailsReq.enqueue(object : Callback<SingleContestDetailsResponse>{
            override fun onResponse(call: Call<SingleContestDetailsResponse>, response: Response<SingleContestDetailsResponse>) {
                if (response.isSuccessful && response.body()?.success!!){
                    try {
                        val data = response.body()!!.data!!.priceCard
                        val adapter = DetailsPrizecardAdapter(requireContext(),data)
                        view.pricecardrecycle.adapter = adapter


                    }catch (e:java.lang.Exception){
                        myApplication.printLogE(e.toString(),TAG)
                    }


                }else{
                    myApplication.printLogE(response.toString(),TAG)
                }
            }

            override fun onFailure(call: Call<SingleContestDetailsResponse>, t: Throwable) {
                myApplication.printLogE(t.toString(),TAG)

            }

        })


    }

}