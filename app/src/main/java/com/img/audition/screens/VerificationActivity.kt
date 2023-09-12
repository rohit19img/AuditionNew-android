package com.img.audition.screens

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.img.audition.databinding.ActivityVerificationBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.SessionManager
import com.img.audition.screens.fragment.BankVerificationFragment
import com.img.audition.screens.fragment.MobileVerificationFragment
import com.img.audition.screens.fragment.PanValidationFragment

class VerificationActivity : AppCompatActivity() {

    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityVerificationBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewBinding.tabLayout.setupWithViewPager(viewBinding.viewPager)
        viewBinding.viewPager.adapter = SectionPagerAdapter(supportFragmentManager)

        viewBinding.backPressIC.setOnClickListener {
            onBackPressed()
        }
    }

    class SectionPagerAdapter(fm: FragmentManager?) :
        FragmentStatePagerAdapter(fm!!) {
        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> MobileVerificationFragment()
                1 -> PanValidationFragment()
                else -> BankVerificationFragment()
            }
        }

        override fun getCount(): Int {
            return 3
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return when (position) {
                0 -> "Mobile & Email"
                1 -> "Pan Card"
                else -> "Bank"
            }
        }
    }
}