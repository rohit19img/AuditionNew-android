package com.img.audition.screens.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.media3.common.util.UnstableApi
import com.img.audition.R
import com.img.audition.adapters.MusicAdapter
import com.img.audition.dataModel.MusicDataResponse
import com.img.audition.databinding.FragmentMusicListBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.screens.CameraActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


@UnstableApi class MusicListFragment : Fragment() {

    val TAG = "MusicListFragment"

    companion object {
        fun newInstance(): MusicListFragment {
            return MusicListFragment()
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

    private lateinit var _viewBinding : FragmentMusicListBinding
    private val view get() = _viewBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _viewBinding = FragmentMusicListBinding.inflate(inflater,container,false)

        return _viewBinding.root
    }

    override fun onViewCreated(view1: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view1, savedInstanceState)
        showMusicList()

        view.closeMusicSheetButton.setOnClickListener {
            if (activity is CameraActivity) {
                (activity as CameraActivity).closeBottomSheet()
            }
        }
    }

    fun showMusicList(){
        val musicListReq = apiInterface.getMusicList(sessionManager.getToken()!!)
        musicListReq.enqueue(object : Callback<MusicDataResponse> {
            override fun onResponse(call: Call<MusicDataResponse>, response: Response<MusicDataResponse>) {
                if (response.isSuccessful && response.body()!!.success!!){
                    myApplication.printLogD(response.toString(),TAG)
                    val data = response.body()!!.data
                    val adapter = MusicAdapter(requireContext(),data)
                    view.musicCycle.adapter = adapter
                }else{
                    myApplication.printLogE(response.toString(),TAG)
                }
            }

            override fun onFailure(call: Call<MusicDataResponse>, t: Throwable) {
                myApplication.printLogE(t.toString(),TAG)
            }

        })
    }



}