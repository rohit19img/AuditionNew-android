package com.img.audition.screens.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.img.audition.adapters.DetailsPrizecardAdapter
import com.img.audition.dataModel.SingleContestDetailsResponse
import com.img.audition.dataModel.SingleContestPriceCard
import com.img.audition.databinding.FragmentDetailsPrizecardBinding
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList


class DetailsPrizecardFragment(val contestID: String) : Fragment() {

    private var adapter: DetailsPrizecardAdapter? = null
    private lateinit var data: ArrayList<SingleContestPriceCard>
    private val TAG = "DetailsPrizecardFragment"
    private lateinit var _viewBinding : FragmentDetailsPrizecardBinding
    private val view get() = _viewBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _viewBinding = FragmentDetailsPrizecardBinding.inflate(inflater,container,false)
        return _viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contestDetails(view.context,contestID)
    }


    private fun contestDetails(context: Context, contestID: String?) {
        val apiInterface = RetrofitClient.getInstance().create(ApiInterface::class.java)

        val contestDetailsReq =  apiInterface.getSingleContestDetails(SessionManager(context).getToken(),contestID)

        contestDetailsReq.enqueue(object : Callback<SingleContestDetailsResponse>{
            override fun onResponse(call: Call<SingleContestDetailsResponse>, response: Response<SingleContestDetailsResponse>) {
                if (response.isSuccessful && response.body()?.success!!){
                    try {
                        data = response.body()!!.data!!.priceCard
                        if (data.size>0){
                            view.dataView.visibility = View.VISIBLE
                            view.noDataView.visibility = View.GONE
                            adapter = DetailsPrizecardAdapter(context,data)
                            view.pricecardrecycle.adapter = adapter
                        }else{
                            view.dataView.visibility = View.GONE
                            view.noDataView.visibility = View.VISIBLE
                        }
                    }catch (e:java.lang.Exception){
                        view.dataView.visibility = View.GONE
                        view.noDataView.visibility = View.VISIBLE
                        Log.e(TAG, "onResponse: ", e)
                    }


                }else{
                    view.dataView.visibility = View.GONE
                    view.noDataView.visibility = View.VISIBLE
                    Log.e(TAG, "onResponse: $response")
                }
            }

            override fun onFailure(call: Call<SingleContestDetailsResponse>, t: Throwable) {
                Log.e(TAG, "onResponse: ", t)
                view.dataView.visibility = View.GONE
                view.noDataView.visibility = View.VISIBLE

            }

        })


    }

    override fun onDestroyView() {
        Log.d("check 400", "onDestroyView: $TAG")
        try {
            data.clear()
            adapter = null
        }catch (e:Exception){
            e.printStackTrace()
        }

        getView()?.destroyDrawingCache()
        super.onDestroyView()
    }

}