package com.img.audition.screens

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.img.audition.R
import com.img.audition.dataModel.UserSelfProfileResponse
import com.img.audition.databinding.ActivityWalletBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.String
import kotlin.LazyThreadSafetyMode
import kotlin.Throwable
import kotlin.getValue
import kotlin.lazy
import kotlin.toString

class WalletActivity : AppCompatActivity() {

    val TAG = "WalletActivity"
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityWalletBinding.inflate(layoutInflater)
    }
    private val sessionManager by lazy {
        SessionManager(this@WalletActivity)
    }

    private val myApplication by lazy {
        MyApplication(this@WalletActivity)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        getUserSelfDetails()

    }

    private val apiInterface by lazy{
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }

    private fun getUserSelfDetails() {
        val userDetilsReq = apiInterface.getUserSelfDetails(sessionManager.getToken())

        userDetilsReq.enqueue(object : Callback<UserSelfProfileResponse> {
            override fun onResponse(call: Call<UserSelfProfileResponse>, response: Response<UserSelfProfileResponse>) {
                if (response.isSuccessful && response.body()!!.success!! && response.body()!=null){
                    myApplication.printLogD(response.toString(),TAG)
                    val userData = response.body()!!.data
                    if(userData!=null){
                        if (userData.image.toString().isNotEmpty()){
                            MyApplication.DownloadImageTask(viewBinding.userImage).execute(userData.image.toString())
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
                            viewBinding.depsoitCash.text = "₹ " + userData.walletamaount.toString()
                        }else{
                            viewBinding.depsoitCash.text = "₹ 0"
                        }

                        if ( userData.totalwon.toString().isNotEmpty()){
                            viewBinding.winning.text = "₹ " + userData.totalwon.toString()
                        }else{
                            viewBinding.winning.text = "₹ 0"
                        }
                    }else{
                        myApplication.printLogE("User Data Null",TAG)
                    }
                }else{
                    myApplication.printLogE("Get Other User Self Data Response Failed ${response.code()}",TAG)
                }
            }

            override fun onFailure(call: Call<UserSelfProfileResponse>, t: Throwable) {
                myApplication.printLogE("Get Other User Self Data onFailure ${t.toString()}",TAG)
            }

        })
    }

}