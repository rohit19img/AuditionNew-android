package com.img.audition.screens.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.img.audition.adapters.FollowingAdapter
import com.img.audition.dataModel.FollowerFollowingListResponse
import com.img.audition.dataModel.FollowingList
import com.img.audition.databinding.FragmentFollowingBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class FollowingFragment(val userID:String) : Fragment() {

    private var adapter: FollowingAdapter? = null
    private lateinit var followList: ArrayList<FollowingList>
    private val TAG = "FollowingFragment"

    private lateinit var _viewBinding : FragmentFollowingBinding
    private val view get() = _viewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _viewBinding = FragmentFollowingBinding.inflate(inflater,container,false)
        return _viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showFollowingList(view.context)
    }

    private fun showFollowingList(context: Context) {
        val apiInterface = RetrofitClient.getInstance().create(ApiInterface::class.java)

        val followReq = apiInterface.getFollowFollowingList(SessionManager(context).getToken(),userID)

        followReq.enqueue(object : Callback<FollowerFollowingListResponse> {

            override fun onResponse(call: Call<FollowerFollowingListResponse>, response: Response<FollowerFollowingListResponse>) {
                if (response.isSuccessful && response.body()!!.success!! && response.body()!=null){
                    followList = response.body()!!.data[0].followingList
                    if (followList.size>0){
                        view.followingCycle.visibility = View.VISIBLE
                        view.noDataView.visibility = View.GONE
                        adapter = FollowingAdapter(context,followList)
                        view.followingCycle.adapter = adapter
                    }else{
                        view.followingCycle.visibility = View.GONE
                        view.noDataView.visibility = View.VISIBLE
                        Log.d(TAG, "No Data")

                    }
                }else{
                    view.followingCycle.visibility = View.GONE
                    view.noDataView.visibility = View.VISIBLE
                    Log.d(TAG, response.toString())
                }
            }

            override fun onFailure(call: Call<FollowerFollowingListResponse>, t: Throwable) {
                view.followingCycle.visibility = View.GONE
                view.noDataView.visibility = View.VISIBLE
               t.printStackTrace()

            }

        })
    }

    override fun onDestroyView() {
        Log.d("check 400", "onDestroyView: $TAG")
        try {
            followList.clear()
            adapter = null
        }catch (e:Exception){
            e.printStackTrace()
        }
        getView()?.destroyDrawingCache()
        super.onDestroyView()
    }
}