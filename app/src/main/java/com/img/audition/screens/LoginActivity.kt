package com.img.audition.screens

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.img.audition.R
import com.img.audition.adapters.ImageSlider
import com.img.audition.databinding.ActivityLoginBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.SessionManager
import java.util.*

class LoginActivity : AppCompatActivity() {
    val imageList = arrayListOf(
        R.drawable.item2,
        R.drawable.item1,
        R.drawable.item3,
        R.drawable.item4
    )

    val timer = Timer()
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        val imagSlider = ImageSlider(this@LoginActivity,imageList)
        viewBinding.imageSlidePager.adapter = imagSlider

        timer.scheduleAtFixedRate(SlideTimer(),2000,3000)

        viewBinding.phoneLoginButton.setOnClickListener {
            sendToPhoneLoginActivity()
        }
    }

    private fun sendToPhoneLoginActivity() {
        val homeIntent = Intent(this@LoginActivity,PhoneLoginActivity::class.java)
        startActivity(homeIntent)
    }


    inner class SlideTimer : TimerTask(){
        override fun run() {
            this@LoginActivity.runOnUiThread(Runnable {
                if (viewBinding.imageSlidePager.currentItem < imageList.size - 1) {
                    viewBinding.imageSlidePager.currentItem = viewBinding.imageSlidePager.currentItem + 1
                } else viewBinding.imageSlidePager.currentItem = 0
            })
        }

    }
}