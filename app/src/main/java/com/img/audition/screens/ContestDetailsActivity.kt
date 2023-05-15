package com.img.audition.screens

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.media3.common.util.UnstableApi
import com.img.audition.dataModel.SingleContestDetailsResponse
import com.img.audition.dataModel.SingleContestPriceCard
import com.img.audition.databinding.ActivityContestDetailsBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.screens.fragment.DetailsPrizecardFragment
import com.img.audition.screens.fragment.LeaderboardFragment
import com.img.audition.snapCameraKit.SnapCameraActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

@UnstableApi class ContestDetailsActivity : AppCompatActivity() {

    val TAG = "ContestDetailsActivity"
    var ifOfferApplied = false
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityContestDetailsBinding.inflate(layoutInflater)
    }
    private val sessionManager by lazy {
        SessionManager(this@ContestDetailsActivity)
    }

    private val myApplication by lazy {
        MyApplication(this@ContestDetailsActivity)
    }
    private val apiInterface by lazy{
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }

    private val bundle by lazy {
        intent.getBundleExtra(ConstValFile.Bundle)
    }
    var contestID = ""
    var isContestJoin = false
    var prizecard: ArrayList<SingleContestPriceCard>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewBinding.backPressIC.setOnClickListener {
            onBackPressed()
        }
        viewBinding.tabLayout.setupWithViewPager(viewBinding.viewpager)
        viewBinding.viewpager.adapter = SectionPagerAdapter(supportFragmentManager)
    }

    private fun contestDetails(contestID: String?) {
        val contestDetailsReq =  apiInterface.getSingleContestDetails(sessionManager.getToken(),contestID)

        contestDetailsReq.enqueue(object : Callback<SingleContestDetailsResponse>{
            override fun onResponse(call: Call<SingleContestDetailsResponse>, response: Response<SingleContestDetailsResponse>) {
               if (response.isSuccessful && response.body()?.success!!){
                   try {
                       val data = response.body()!!.data

                       viewBinding.winner.text = data!!.totalwinners.toString()
                       viewBinding.maxuser.text = "Max.Join " + data.maximumUser
                       viewBinding.joinuser.text = "Joined " + data.joinedusers
                       viewBinding.winamount.text = "₹ " + data.winAmount
                       viewBinding.btnJoin.text = "₹ " + data.entryfee

                       viewBinding.progress.max = data.maximumUser!!
                       viewBinding.progress.progress = data.joinedusers!!

                       prizecard = data.priceCard

                   }catch (e: Exception){
                       myApplication.printLogE(e.toString(),TAG)
                   }


               }else{
                   myApplication.printLogE(response.toString(),TAG)
               }
            }

            override fun onFailure(call: Call<SingleContestDetailsResponse>, t: Throwable) {
                myApplication.printLogE(t.toString(),TAG)

            }

        })


    }


    inner class SectionPagerAdapter(fm: FragmentManager?) :
        FragmentStatePagerAdapter(fm!!) {
        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> DetailsPrizecardFragment(contestID)
                1 -> LeaderboardFragment(contestID)
                else -> LeaderboardFragment(contestID)
            }
        }

        override fun getCount(): Int {
            return 2
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return when (position) {
                0 -> "Prize card"
                1 -> "Leaderboard"
                else -> "Leaderboard"
            }
        }
    }

    override fun onResume() {
        super.onResume()

        contestID = bundle!!.getString(ConstValFile.ContestID).toString()
        isContestJoin = bundle!!.getBoolean(ConstValFile.IsContestJoin,false)
        myApplication.printLogD(contestID,"contestID")
        myApplication.printLogD(isContestJoin.toString(), "IsContestJoin")
        contestDetails(contestID)

        if (!isContestJoin){
            viewBinding.btnJoin.visibility = View.VISIBLE
        }else{
            viewBinding.btnJoin.visibility = View.GONE
        }



        viewBinding.btnJoin.setOnClickListener {
            if (!(sessionManager.isUserLoggedIn())) {
                sendToLoginScreen()
            } else {
                sendForCreateVideo()
            }
        }

    }

    fun sendToLoginScreen() {
        val intent = Intent(this@ContestDetailsActivity, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun sendForCreateVideo() {
        val bundle = Bundle()
        bundle.putBoolean(ConstValFile.IsFromContest, true)
        sessionManager.setIsFromContest(true)
        val intent = Intent(this@ContestDetailsActivity, SnapCameraActivity::class.java)
        intent.putExtra(ConstValFile.Bundle, bundle)
        startActivity(intent)
    }
}