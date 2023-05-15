package com.img.audition.screens.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import com.img.audition.R
import com.img.audition.adapters.MusicAdapter
import com.img.audition.dataModel.MusicData
import com.img.audition.dataModel.MusicDataResponse
import com.img.audition.databinding.FragmentFavMusicBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.screens.MusicActivity
import com.img.audition.videoWork.PlayPauseAudio
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList


class FavMusicFragment(val contextFromMusicActivity: MusicActivity) : Fragment() {

    val TAG = "FavMusicFragment"
    var musicList = ArrayList<MusicData>()
    lateinit var musicCycle : RecyclerView
    lateinit var playPauseAudio: PlayPauseAudio


    lateinit var musicAdapter: MusicAdapter
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
        val view = FragmentFavMusicBinding.inflate(inflater,container,false)

        musicCycle = view.musicCycle
        return view.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showMusicList()
    }

    fun showMusicList(){
        val musicListReq = apiInterface.getFavMusicList(sessionManager.getToken()!!)
        musicListReq.enqueue(@UnstableApi object : Callback<MusicDataResponse> {
            override fun onResponse(call: Call<MusicDataResponse>, response: Response<MusicDataResponse>) {
                myApplication.printLogD(response.toString(),TAG)
                if (response.isSuccessful && response.body()!!.success!!){
                    myApplication.printLogD(response.toString(),TAG)
                    musicList = response.body()!!.data
                    musicAdapter = MusicAdapter(requireContext(),musicList)
                    playPauseAudio = musicAdapter.onActivityStateChanged()
                    musicCycle.adapter = musicAdapter
                }else{
                    myApplication.printLogE(response.toString(),TAG)
                }
            }

            override fun onFailure(call: Call<MusicDataResponse>, t: Throwable) {
                myApplication.printLogE(t.toString(),TAG)
            }

        })
    }

    override fun onPause() {
        try {
            playPauseAudio.stop()
            Log.d(TAG, "onPause: stop")

        }catch (e:Exception){
            myApplication.printLogE(e.message.toString(),TAG+"2")
        }
        super.onPause()
    }

    override fun onResume() {
        super.onResume()

        contextFromMusicActivity.searchMusicET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchMusic(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
    }

    private fun searchMusic(toString: String) {
        val searchList: ArrayList<MusicData> = ArrayList()
        myApplication.printLogD("size music list  ${musicList.size}",TAG)
        for (ds in musicList) {
            if (ds.title!!.toLowerCase().contains(toString.lowercase(Locale.getDefault()))) {
                searchList.add(ds)
            }
        }
        musicAdapter.filterList(searchList)

    }
}