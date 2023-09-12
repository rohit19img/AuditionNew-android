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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

    }

    override fun onResume() {
        super.onResume()
        val bundle = intent.getBundleExtra(ConstValFile.Bundle)

        val userID = bundle!!.getString(ConstValFile.USER_ID)
        viewBinding.userName.text = bundle!!.getString(ConstValFile.UserName).toString()
        viewBinding.tabLayout.setupWithViewPager(viewBinding.viewPager)
        viewBinding.viewPager.adapter = SectionFollowFollowingListPager(supportFragmentManager,userID!!)
        viewBinding.viewPager.currentItem = bundle.getInt(ConstValFile.PagePosition)

        viewBinding.back.setOnClickListener {
            viewBinding.back.isSelected = false
            onBackPressed()
        }
    }
}