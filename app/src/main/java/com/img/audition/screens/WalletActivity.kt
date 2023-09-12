package com.img.audition.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.img.audition.R
import com.img.audition.dataModel.UserSelfProfileResponse
import com.img.audition.databinding.ActivityWalletBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.screens.fragment.TransactionReportFragment
import com.img.audition.viewModel.MainViewModel
import com.img.audition.viewModel.Status
import com.img.audition.viewModel.ViewModelFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.LazyThreadSafetyMode
import kotlin.Throwable
import kotlin.getValue
import kotlin.lazy
import kotlin.toString

@UnstableApi class WalletActivity : AppCompatActivity() {


    private val TAG = "WalletActivity"
    private val TRANS_TAG = "tarns_tag"
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityWalletBinding.inflate(layoutInflater)
    }

    private lateinit var mainViewModel: MainViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        mainViewModel = ViewModelProvider(this, ViewModelFactory(SessionManager(this).getToken(),apiInterface))[MainViewModel::class.java]

        viewBinding.btnAddCash.setOnClickListener {
            sendToAddCashActivity()
        }

        viewBinding.btnWithdraw.setOnClickListener {
            sendToWithdrawCashActivity()
        }

        viewBinding.transactionReportBtn.setOnClickListener {
            showTransactionReportSheet()
        }

        viewBinding.backPressIC.setOnClickListener {
            onBackPressed()
        }

    }

    private fun sendToAddCashActivity() {
        val intent = Intent(this@WalletActivity,AddAmountActivity::class.java)
        startActivity(intent)
    }

    private fun sendToWithdrawCashActivity() {
        val intent = Intent(this@WalletActivity,WithdrawActivity::class.java)
        startActivity(intent)
    }

    private val apiInterface by lazy{
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }

    private fun getUserWalletBalance(){
        mainViewModel.getUserSelfDetails()
            .observe(this){
                it.let {resources->
                    when(resources.status){
                        Status.SUCCESS ->{
                            if (resources.data!!.success!!){
                                val userData = resources.data.data
                                if(userData!=null){
                                    if (userData.image.toString().isNotEmpty()){
                                        Glide.with(this@WalletActivity).load(userData.image.toString()).placeholder(R.drawable.person_ic).into(viewBinding.userImage)
                                    }else{
                                        viewBinding.userImage.setImageResource(R.drawable.person_ic)
                                    }
                                    if (userData.team.toString().isNotEmpty()){
                                        viewBinding.teamName.text = userData.team.toString()
                                    }else{
                                        viewBinding.teamName.text = userData.auditionId.toString()
                                    }
                                    if (userData.totalbonus.toString().isNotEmpty()){
                                        viewBinding.bonus.text = "₹ " + userData.totalbonus.toString()
                                    }else{
                                        viewBinding.bonus.text = "₹ 0"
                                    }

                                    if (userData.walletamaount.toString().isNotEmpty()){
                                        viewBinding.depsoitCash.text = "₹ " + userData.totalbalance.toString()
                                    }else{
                                        viewBinding.depsoitCash.text = "₹ 0"
                                    }

                                    if ( userData.totalwon.toString().isNotEmpty()){
                                        viewBinding.winning.text = "₹ " + userData.totalwon.toString()
                                    }else{
                                        viewBinding.winning.text = "₹ 0"
                                    }
                                }else{
                                    Log.e(TAG,"User Data Null")
                                }
                            }else{
                                Toast.makeText(this,"Something went wrong..", Toast.LENGTH_SHORT).show()

                            }
                        }
                        Status.LOADING ->{
                            Log.e(TAG,resources.status.toString())
                        }
                        else->{
                            Log.e(TAG,resources.status.toString())
                        }
                    }
                }
            }


    }

    private fun showTransactionReportSheet() {
        var transFragment = supportFragmentManager.findFragmentByTag(TRANS_TAG)
        if (transFragment == null) {
            transFragment = TransactionReportFragment.newInstance()

            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(viewBinding.frameContainer.id,transFragment)
            transaction.commit()
        }
        val behavior = BottomSheetBehavior.from(viewBinding.frameContainer)
        if (behavior.state != BottomSheetBehavior.STATE_EXPANDED) {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        } else {
            behavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }


    fun closeBottomSheet() {
        val behavior = BottomSheetBehavior.from(viewBinding.frameContainer)
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    override fun onResume() {
        super.onResume()
        getUserWalletBalance()
    }
}