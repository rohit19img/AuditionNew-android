package com.img.audition.screens

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.img.audition.adapters.MusicAdapter
import com.img.audition.dataModel.MusicData
import com.img.audition.dataModel.MusicDataResponse
import com.img.audition.databinding.ActivityMusicBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.videoWork.PlayPauseAudio
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class MusicActivity : AppCompatActivity() {

    val TAG = "MusicActivity"
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMusicBinding.inflate(layoutInflater)
    }
    private val sessionManager by lazy {
        SessionManager(this@MusicActivity)
    }

    private val myApplication by lazy {
        MyApplication(this@MusicActivity)
    }

    private val apiInterface by lazy{
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }
    var musicList = ArrayList<MusicData>()
    lateinit var musicAdapter: MusicAdapter
    lateinit var playPauseAudio: PlayPauseAudio
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewBinding.onBackPressBtn.setOnClickListener {
            onBackPressed()
        }

        showMusicList()

        viewBinding.searchET.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchMusic(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
    }


    fun showMusicList(){
        val musicListReq = apiInterface.getMusicList(sessionManager.getToken()!!)
        musicListReq.enqueue(object : Callback<MusicDataResponse> {
            override fun onResponse(call: Call<MusicDataResponse>, response: Response<MusicDataResponse>) {
                if (response.isSuccessful && response.body()!!.success!!){
                    myApplication.printLogD(response.toString(),TAG)
                     musicList = response.body()!!.data
                     musicAdapter = MusicAdapter(this@MusicActivity,musicList)
                    playPauseAudio = musicAdapter.onActivityStateChanged()
                    viewBinding.musicCycle.adapter = musicAdapter
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
        Log.d(TAG, "onPause: ")
        try {
            playPauseAudio.stop()
        }catch (e:Exception){
            myApplication.printLogE(e.message.toString(),TAG+"2")
        }
        super.onPause()
    }

    private fun searchMusic(toString: String) {
        val searchList: ArrayList<MusicData> = ArrayList()
        for (ds in musicList) {
            if (ds.title!!.toLowerCase().contains(toString.lowercase(Locale.getDefault()))) {
                searchList.add(ds)
            }
        }
        if (searchList.isEmpty()) {
            Toast.makeText(this, "No Data Found..", Toast.LENGTH_SHORT).show()
        } else {
            musicAdapter.filterList(searchList)
        }
    }
}