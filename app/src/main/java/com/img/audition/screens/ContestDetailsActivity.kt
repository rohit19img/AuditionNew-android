package com.img.audition.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import java.text.SimpleDateFormat

@UnstableApi class ContestDetailsActivity : AppCompatActivity() {

    private val TAG = "ContestDetailsActivity"
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityContestDetailsBinding.inflate(layoutInflater)
    }
    private val sessionManager by lazy {
        SessionManager(this@ContestDetailsActivity)
    }

    private val bundle by lazy {
        intent.getBundleExtra(ConstValFile.Bundle)
    }
    private var contestID = ""
    private var contestStatus = ""
    private var isContestJoin = false
    private var prizecard: ArrayList<SingleContestPriceCard>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewBinding.backPressIC.setOnClickListener {
            onBackPressed()
        }
        viewBinding.showShimmer.startShimmer()
        viewBinding.tabLayout.setupWithViewPager(viewBinding.viewpager)
        viewBinding.viewpager.adapter = SectionPagerAdapter(supportFragmentManager)
    }

    private fun contestDetails(contestID: String?) {
        val apiInterface =  RetrofitClient.getInstance().create(ApiInterface::class.java)
        val contestDetailsReq =  apiInterface.getSingleContestDetails(sessionManager.getToken(),contestID)

        contestDetailsReq.enqueue(object : Callback<SingleContestDetailsResponse>{
            override fun onResponse(call: Call<SingleContestDetailsResponse>, response: Response<SingleContestDetailsResponse>) {
               if (response.isSuccessful && response.body()?.success!!){
                   try {
                       val data = response.body()!!.data
                       viewBinding.showShimmer.stopShimmer()
                       viewBinding.showShimmer.hideShimmer()
                       viewBinding.showShimmer.visibility = View.GONE
                       viewBinding.winner.text = data!!.totalwinners.toString()
                       viewBinding.maxuser.text =  "Max ${data.maximumUser.toString()} Users"
                       viewBinding.joinuser.text ="${data.joinedusers.toString()} User Joined"
                       viewBinding.winamount.text = "₹ " + data.winAmount
                       viewBinding.btnJoin.text = "₹ " + data.entryfee
                       viewBinding.progress.max = data.maximumUser!!
                       viewBinding.progress.progress = data.joinedusers!!
                       prizecard = data.priceCard


                       if(data.isBonus == 1) {
                           viewBinding.bonusLL.visibility = View.VISIBLE
                           viewBinding.contestBonus.text = "${data.bonusPercentage}%"
                       } else
                           viewBinding.bonusLL.visibility = View.GONE

                       var dateFormat =  SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                       var dateFormat1 =  SimpleDateFormat("MMM dd  hh:mm a")

                       viewBinding.contestStartDate.text = "Start Date : "+data.startDate.toString()
                       viewBinding.contestEndDate.text = "End Date : "+data.endDate.toString()

                       try{
                           viewBinding.contestStartDate.text = "Start Date : ${dateFormat1.format(dateFormat.parse(data.startDate))}"
                       } catch (e : Exception){
                           Log.i("Exception"," ${ e.message}")
                       }
                       try{
                           viewBinding.contestEndDate.text = "End Date : ${dateFormat1.format(dateFormat.parse(data.endDate))}"
                       } catch (e : java.lang.Exception){
                           Log.i("Exception"," ${ e.message}")
                       }


                   }catch (e: Exception){
                       e.printStackTrace()
                   }


               }else{
                   Log.e(TAG, "onResponse: ${response.toString()}")

               }
            }

            override fun onFailure(call: Call<SingleContestDetailsResponse>, t: Throwable) {
                t.printStackTrace()

            }

        })


    }


    inner class SectionPagerAdapter(fm: FragmentManager?) :
        FragmentStatePagerAdapter(fm!!) {
        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> DetailsPrizecardFragment(contestID)
                1 -> LeaderboardFragment(contestID,contestStatus)
                else -> LeaderboardFragment(contestID,contestStatus)
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
        contestStatus = bundle!!.getString(ConstValFile.ContestStatus).toString()
        isContestJoin = bundle!!.getBoolean(ConstValFile.IsContestJoin,false)


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
        bundle.putBoolean(ConstValFile.isFromDuet, false)
        sessionManager.setIsFromContest(true)
        val intent = Intent(this@ContestDetailsActivity, SnapCameraActivity::class.java)
        intent.putExtra(ConstValFile.Bundle, bundle)
        startActivity(intent)
    }
}