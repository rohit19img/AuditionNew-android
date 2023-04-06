package com.img.audition.screens.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.JsonObject
import com.img.audition.dataModel.Searchgetset
import com.img.audition.dataModel.VideoData
import com.img.audition.databinding.FragmentTrendingSearchBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class TrendingSearchFragment : Fragment() {
    private val sessionManager by lazy {
        SessionManager(requireContext())
    }
    private val myApplication by lazy {
        MyApplication(requireContext())
    }

    private val apiInterface by lazy{
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }

    var userlist: ArrayList<Searchgetset.User>? = null
    var hashtaglist: ArrayList<Searchgetset.Hashtag>? = null
    var videolist: java.util.ArrayList<VideoData>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = FragmentTrendingSearchBinding.inflate(inflater,container,false)

        view.apply {
            userRecycle.layoutManager = GridLayoutManager(requireContext(),2)
            hashtagRecycle.layoutManager = GridLayoutManager(requireContext(),3)
            videoRecycle.layoutManager = GridLayoutManager(requireContext(),2)

            searchBar.addTextChangedListener(
                object : TextWatcher{
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {

                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        if(searchBar.text.toString().length > 2){
                            searchlist(searchBar.text.toString())
                        }
                    }

                    override fun afterTextChanged(s: Editable?) {

                    }
                }
            )

            searchBar.setOnEditorActionListener { v, actionId, event ->
                if(actionId == EditorInfo.IME_ACTION_SEARCH){
                    searchlist(searchBar.text.toString())
                    true
                } else {
                    false
                }
            }
        }

        searchlist("")

        return view.root
    }

    fun searchlist(searchData : String){
        val obj = JsonObject()
        obj.addProperty("search", searchData)

        val responseCall: Call<Searchgetset> =
            apiInterface.search(sessionManager.getToken(), obj)
        responseCall.enqueue(object : Callback<Searchgetset> {
            override fun onResponse(call: Call<Searchgetset>, response: Response<Searchgetset>) {
                if (response.isSuccessful) {
                    response.body()!!.message

                    if(response.body()!!.success){
                        userlist = response.body()!!.data!!.users
                        hashtaglist = response.body()!!.data!!.hashtags
                        videolist = response.body()!!.data!!.data
                    }


                } else {
                    myApplication.showToast("Something went wrong!!")
                }
            }

            override fun onFailure(call: Call<Searchgetset>, t: Throwable) {
                myApplication.printLogD("text", t.message!!)
            }
        })

    }

}