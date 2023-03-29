package com.img.audition.screens

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.img.audition.adapters.SectionFollowFollowingListPager
import com.img.audition.databinding.ActivityFollowFollowingListBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager

class FollowFollowingListActivity : AppCompatActivity() {

    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityFollowFollowingListBinding.inflate(layoutInflater)
    }
    private val sessionManager by lazy {
        SessionManager(this@FollowFollowingListActivity)
    }

    private val myApplication by lazy {
        MyApplication(this@FollowFollowingListActivity)
    }

    private val apiInterface by lazy {
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        val bundle = intent.getBundleExtra(ConstValFile.Bundle)

        viewBinding.userName.text = bundle!!.getString(ConstValFile.UserName).toString()
        viewBinding.tabLayout.setupWithViewPager(viewBinding.viewPager)
        viewBinding.viewPager.adapter = SectionFollowFollowingListPager(supportFragmentManager)
        viewBinding.viewPager.currentItem = bundle.getInt(ConstValFile.PagePosition)

        viewBinding.back.setOnClickListener {
            viewBinding.back.isSelected = false
            onBackPressed()
        }

    }
}