package com.img.audition.screens

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.media3.common.util.UnstableApi
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.img.audition.databinding.ActivityMusicBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.screens.fragment.AllMusicFragment
import com.img.audition.screens.fragment.FavMusicFragment

@UnstableApi
class MusicActivity : AppCompatActivity() {

    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMusicBinding.inflate(layoutInflater)
    }

    lateinit var searchMusicET:EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewBinding.onBackPressBtn.setOnClickListener {
            onBackPressed()
        }

        searchMusicET =  viewBinding.searchET

       loadFragment(AllMusicFragment(this@MusicActivity))


        viewBinding.tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab!!.position){
                     0 ->{
                         loadFragment(AllMusicFragment(this@MusicActivity))
                     }else ->{
                         loadFragment(FavMusicFragment(this@MusicActivity))
                     }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}

        })
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(viewBinding.viewContainer.id,fragment)
        transaction.commit()
    }
}