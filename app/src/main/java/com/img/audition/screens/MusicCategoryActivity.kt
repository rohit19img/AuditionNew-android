package com.img.audition.screens

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.media3.common.util.UnstableApi
import com.img.audition.adapters.MusicAdapter
import com.img.audition.dataModel.MusicData
import com.img.audition.databinding.ActivityChatUserBinding
import com.img.audition.databinding.ActivityMusicCategoryBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.SessionManager
import com.img.audition.viewModel.MainViewModel

@UnstableApi class MusicCategoryActivity : AppCompatActivity() {

    private val TAG = "MusicCategoryActivity"
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMusicCategoryBinding.inflate(layoutInflater)
    }
    private val sessionManager by lazy {
        SessionManager(this@MusicCategoryActivity)
    }
    private val myApplication by lazy {
        MyApplication(this@MusicCategoryActivity)
    }

    private val bundle by lazy {
        intent.getBundleExtra(ConstValFile.Bundle)
    }

   private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewBinding.onBackPressBtn.setOnClickListener {
            onBackPressed()
        }

        viewBinding.searchET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                searchMusic(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
    }


    override fun onStart() {
        super.onStart()
        val musicList = bundle!!.getSerializable(ConstValFile.MusicList) as ArrayList<MusicData>
        viewBinding.musicCycle.adapter = MusicAdapter(this@MusicCategoryActivity,musicList)
    }
}