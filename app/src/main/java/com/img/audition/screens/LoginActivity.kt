package com.img.audition.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.JsonObject
import com.img.audition.R
import com.img.audition.adapters.ImageSlider
import com.img.audition.dataModel.LoginResponse
import com.img.audition.databinding.ActivityLoginBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class LoginActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {
    val imageList = arrayListOf(
        R.drawable.item2,
        R.drawable.item1,
        R.drawable.item3,
        R.drawable.item4
    )

    private val myApplication by lazy {
        MyApplication(this@LoginActivity)
    }
    val TAG = "LoginActivity"
    private lateinit var mGoogleApiClient : GoogleApiClient
    val timer = Timer()
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    private val sessionManager by lazy {
        SessionManager(this@LoginActivity)
    }

    private val apiInterface by lazy{
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(Scopes.PLUS_LOGIN))
            .build()


         mGoogleApiClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this@LoginActivity, this /* OnConnectionFailedListener */)
            .addApi<GoogleSignInOptions>(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()

        val imagSlider = ImageSlider(this@LoginActivity,imageList)
        viewBinding.imageSlidePager.adapter = imagSlider

        timer.scheduleAtFixedRate(SlideTimer(),2000,3000)

        viewBinding.phoneLoginButton.setOnClickListener {
            sendToPhoneLoginActivity()
        }
        viewBinding.googleLogin.setOnClickListener {
            try {
                signIn()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun sendToPhoneLoginActivity() {
        val homeIntent = Intent(this@LoginActivity,PhoneLoginActivity::class.java)
        startActivity(homeIntent)
    }

    private fun signIn() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient)
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        startActivityForResult(signInIntent, 1)
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

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        myApplication.printLogE( "onConnectionFailed:$connectionResult",TAG)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            handleSignInResult(result)
        }
    }


    private fun handleSignInResult(result: GoogleSignInResult) {
        myApplication.printLogE( "handleSignInResult:$result",TAG)
        if (result.isSuccess) {
            // Signed in successfully, show authenticated UI.
            val acct = result.signInAccount
            if (acct!!.email == "") {
                Toast.makeText(this@LoginActivity, "Email id not found,please try manually.",Toast.LENGTH_SHORT).show()
            } else {
                val FGemail = acct.email.toString()
                val FGname = acct.displayName
                val FGimage = acct.photoUrl.toString()
                Log.i("Email", FGemail + "nothing")
                Log.i("name", FGname!!)
                Log.i("image", FGimage)
                onTokenRefresh(FGemail,FGname)

            }
        } else {
            Log.i("where", "else")
        }
    }

    private fun socialLoginApi(email: String?, FGname: String) {
        val obj = JsonObject()
        obj.addProperty("email", email)
        obj.addProperty("appid", sessionManager.getNotificationToken())
        val socialReq = apiInterface.socialLogin(obj)

        socialReq.enqueue(object : Callback<LoginResponse>{
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful && response.body()!!.success!!){
                    val token = response.body()!!.data!!.token.toString()
                    val userId = response.body()!!.data!!.id.toString()
                    sessionManager.setGuestLogin(false)
                    sessionManager.createUserLoginSession(true,token,email)
                    sessionManager.setUserSelfID(userId)
                    sessionManager.setToken(token)
                    sessionManager.setUserName(FGname)
                    sessionManager.setMobileVerified(true)
                    Thread.sleep(500)
                    myApplication.showToast("Login Successfully..")
                    sendToHomeActivity()
                }else{
                    Log.e(TAG, "onResponse: $response")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e(TAG, "onResponse: $t")
            }

        })
    }

    fun sendToHomeActivity(){
        val homeIntent = Intent(this@LoginActivity,HomeActivity::class.java)
        homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(homeIntent)
        finish()
    }

    fun onTokenRefresh(email: String, FGname: String) {
        val refreshedToken = arrayOf("")
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(object :
            OnCompleteListener<String?> {
            override fun onComplete(task: Task<String?>) {
                if (!task.isSuccessful()) {
                    myApplication.printLogE(task.exception.toString(),TAG);
                    return
                }
                val token: String? = task.getResult()
                myApplication.printLogD(token.toString(),"Firebase Token")
                refreshedToken[0] = token.toString()
                sessionManager.setNotificationToken(refreshedToken[0])
                socialLoginApi(email,FGname)
            }
        })
        FirebaseMessaging.getInstance().subscribeToTopic("All-user")
    }
}