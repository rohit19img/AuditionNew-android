package com.img.audition.screens

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ContestDetailsActivity : AppCompatActivity() {

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
    var prizecard: ArrayList<SingleContestPriceCard>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewBinding.backPressIC.setOnClickListener {
            onBackPressed()
        }
        if (bundle!=null){
            contestID = bundle!!.getString(ConstValFile.ContestID).toString()
            myApplication.printLogD(contestID!!,"contestID")
            contestDetails(contestID)
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

                   }catch (e:java.lang.Exception){
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
}