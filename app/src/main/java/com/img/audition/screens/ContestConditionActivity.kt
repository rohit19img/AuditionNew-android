package com.img.audition.screens

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import com.img.audition.R
import com.img.audition.databinding.ActivityContestConditionBinding

class ContestConditionActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityContestConditionBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        binding.backPressIC.setOnClickListener {
            finish()
        }

    }
}