package com.img.audition.screens.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import com.img.audition.adapters.MusicAdapter
import com.img.audition.dataModel.MusicData
import com.img.audition.dataModel.MusicDataResponse
import com.img.audition.databinding.FragmentAllMusicBinding
import com.img.audition.databinding.FragmentFavMusicBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.screens.MusicActivity
import com.img.audition.screens.SplashActivity
import com.img.audition.videoWork.PlayPauseAudio
import com.img.audition.viewModel.MainViewModel
import com.img.audition.viewModel.Status
import com.img.audition.viewModel.ViewModelFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList


@UnstableApi
class FavMusicFragment(private val contextFromMusicActivity: MusicActivity) : Fragment() {

    private val TAG = "FavMusicFragment"
    private var musicList = ArrayList<MusicData>()
    private lateinit var musicCycle : RecyclerView
    private lateinit var playPauseAudio: PlayPauseAudio

    private lateinit var _viewBinding : FragmentFavMusicBinding
    private val view get() = _viewBinding
    private lateinit var mainViewModel: MainViewModel
    private lateinit var musicAdapter: MusicAdapter
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
        _viewBinding = FragmentFavMusicBinding.inflate(inflater,container,false)

        mainViewModel = ViewModelProvider(this, ViewModelFactory(sessionManager.getToken(),apiInterface))[MainViewModel::class.java]

        musicCycle = _viewBinding.musicCycle
        return _viewBinding.root
    }

    override fun onViewCreated(view1: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view1, savedInstanceState)

        view.shimmerVideoView.startShimmer()
        showMusicList(view1.context)
    }

    private fun showMusicList(context: Context) {
        mainViewModel.getFavMusicList()
            .observe(viewLifecycleOwner){
                it.let {resources->
                    when(resources.status){
                        Status.SUCCESS ->{
                            if (resources.data!!.success!!){
                                myApplication.printLogD(resources.data.toString(),TAG)
                                musicList = resources.data.data
                                musicAdapter = MusicAdapter(context,musicList)
                                playPauseAudio = musicAdapter.onActivityStateChanged()
                                musicCycle.adapter = musicAdapter
                                view.shimmerVideoView.stopShimmer()
                                view.shimmerVideoView.hideShimmer()
                                view.shimmerVideoView.visibility = View.GONE
                                view.musicCycle.visibility = View.VISIBLE
                            }else{
                                myApplication.showToast("Something went wrong..")
                            }
                        }
                        Status.LOADING ->{
                            myApplication.printLogD(resources.status.toString(),"apiCall 3")
                        }
                        else->{
                            if (resources.message!!.contains("401")){
                                myApplication.printLogD(resources.message.toString(),"apiCall 4")
                                sessionManager.clearLogoutSession()
                                startActivity(Intent(contextFromMusicActivity, SplashActivity::class.java))
                                requireActivity().finishAffinity()
                            }
                            myApplication.printLogD(resources.status.toString(),"apiCall 5")
                        }
                    }
                }
            }

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

    override fun onDestroyView() {
        Log.d("check 400", "onDestroyView: $TAG")
        musicList.clear()
        getView()?.destroyDrawingCache()
        super.onDestroyView()
    }
}