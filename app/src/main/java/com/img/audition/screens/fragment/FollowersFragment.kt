package com.img.audition.screens.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.img.audition.R
import com.img.audition.adapters.FollowerAdapter
import com.img.audition.dataModel.FollowerFollowingListResponse
import com.img.audition.databinding.FragmentFollowersBinding
import com.img.audition.databinding.FragmentProfileBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FollowersFragment : Fragment() {
    val TAG = "FollowersFragment"

    private lateinit var _viewBinding :  FragmentFollowersBinding
    private val view get() = _viewBinding!!
    private val sessionManager by lazy {
        SessionManager(requireContext())
    }
    private val myApplication by lazy {
        MyApplication(requireContext())
    }
    private val apiInterface by lazy{
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _viewBinding = FragmentFollowersBinding.inflate(inflater,container,false)

        showFollowersList()
        return _viewBinding.root
    }

    private fun showFollowersList() {
        val followReq = apiInterface.getFollowFollowingList(sessionManager.getToken())

        followReq.enqueue(object : Callback<FollowerFollowingListResponse>{
            override fun onResponse(call: Call<FollowerFollowingListResponse>, response: Response<FollowerFollowingListResponse>) {
                if (response.isSuccessful && response.body()!!.success!! && response.body()!=null){
                    val data = response.body()!!.data[0].followerList
                    if (data.size>0){
                        val adapter = FollowerAdapter(requireContext(),data)
                       view.followerCycle.adapter = adapter
                    }else{
                        myApplication.printLogD("No Data",TAG)
                    }
                }else{
                    myApplication.printLogE(response.toString(),TAG)
                }
            }

            override fun onFailure(call: Call<FollowerFollowingListResponse>, t: Throwable) {
                myApplication.printLogE(t.toString(),TAG)
            }

        })
    }


}