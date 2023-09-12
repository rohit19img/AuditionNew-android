package com.img.audition.screens

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
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
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.lang.Thread.sleep
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


@UnstableApi class LoginActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {
    val imageList = arrayListOf(
        R.drawable.item2,
        R.drawable.item1,
        R.drawable.item3,
        R.drawable.item4
    )

    lateinit var google_id : String
    lateinit var access_token : String

    private var isReferChecked = false

    private val myApplication by lazy {
        MyApplication(this@LoginActivity)
    }
    val TAG = "LoginActivity"
    private lateinit var mGoogleApiClient : GoogleApiClient
    val timer = Timer()
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    private lateinit var fbCallbackManager: CallbackManager
    private lateinit var fbLoginManager: LoginManager

    private val sessionManager by lazy {
        SessionManager(this@LoginActivity)
    }

    private val apiInterface by lazy{
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }

    val serverClientId = "839125335573-j2o78oa5mgjllcj9ppfacmis3nnid52b.apps.googleusercontent.com"
    val client_secret = "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"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestServerAuthCode(serverClientId)
            .requestScopes(Scope(Scopes.PLUS_LOGIN))
            .build()


        mGoogleApiClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this,  this )
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build();

        val imagSlider = ImageSlider(this@LoginActivity,imageList)
        viewBinding.imageSlidePager.adapter = imagSlider

        timer.scheduleAtFixedRate(SlideTimer(),2000,3000)

        viewBinding.phoneLoginButton.setOnClickListener {
            var referCode:String? = null
            if (isReferChecked){
                referCode = viewBinding.referCodeET.text.toString()
            }

            sendToPhoneLoginActivity(referCode)
        }
        viewBinding.googleLogin.setOnClickListener {
            try {
                signIn()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        FacebookSdk.sdkInitialize(this@LoginActivity)
        fbCallbackManager = CallbackManager.Factory.create()
//        PrintHashKey()
        facebookLogin()


        viewBinding.facebookLogin.setOnClickListener {
                fbLoginManager.logInWithReadPermissions(this@LoginActivity,listOf("email","public_profile"))
        }

        viewBinding.haveReferCodeCB.setOnClickListener {
            if (viewBinding.haveReferCodeCB.isChecked){
                viewBinding.referCodeET.visibility  = View.VISIBLE
                isReferChecked = true
            }else{
                isReferChecked = false
                viewBinding.referCodeET.visibility  = View.GONE
            }
        }

       /* LoginManager.getInstance().registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                val profile = Profile.getCurrentProfile()

                val request : GraphRequest = GraphRequest.newMeRequest(loginResult.accessToken)
                { obj, response ->
                    var name = ""
                    var email = ""
                    val jsonObject = response!!.getJSONObject()
                    try{
                        email = jsonObject!!.getString("email")
                        name = jsonObject!!.getString("name")
                    } catch (e : Exception){
                            myApplication.printLogE(e.toString(),TAG)
                    }

//                    socialLoginApi(email,name)
                }

                val parameters = Bundle()
                parameters.putString("fields", "id,name,email")
                request.parameters = parameters
                request.executeAsync()
            }

            override fun onCancel() {
               myApplication.showToast("Login Canceled.")
            }

            override fun onError(error: FacebookException) {
                myApplication.showToast("Cannot connect facebook error.")
            }
        })*/
    }

    private fun sendToPhoneLoginActivity(referCode: String?) {
        val homeIntent = Intent(this@LoginActivity,PhoneLoginActivity::class.java)
        homeIntent.putExtra("ReferCode",referCode)
        startActivity(homeIntent)
    }

    private fun signIn() {
        if (myApplication.isNetworkConnected()){
            Auth.GoogleSignInApi.signOut(mGoogleApiClient)
            val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
            startActivityForResult(signInIntent, 1)
        }else{
            myApplication.showToast(ConstValFile.Check_Connection)
        }
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

        fbCallbackManager.onActivityResult(requestCode,resultCode,data)
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == -1){
            if (requestCode == 1) {
                val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data!!)
                handleSignInResult(result!!)
            }
        }

    }

    private fun handleSignInResult(result: GoogleSignInResult) {
        myApplication.printLogE( "handleSignInResult:$result",TAG)
        if (result.isSuccess) {
            // Signed in successfully, show authenticated UI.
            val acct = result.signInAccount

            google_id = acct!!.id!!
            val toekn = acct!!.serverAuthCode
            access_token = requestToken(toekn!!)
            Log.i("AccessToken","AccessToken123 : $access_token")


            runGoogle()


//            if (acct!!.email == "") {
//                Toast.makeText(this@LoginActivity, "Email id not found,please try manually.",Toast.LENGTH_SHORT).show()
//            } else {
//
//                val FGemail = acct.email.toString()
//                val FGname = acct.displayName
//                val FGimage = acct.photoUrl.toString()
//                Log.i("Email", FGemail + "nothing")
//                Log.i("name", FGname!!)
//                Log.i("image", FGimage)
//                onTokenRefresh(FGemail,FGname)
//
//            }
        } else {
            Log.i("where", "else")
        }
    }

    fun runGoogle() {
        try {
            sleep(1000)
            if (access_token != "") {
                onTokenRefresh(access_token)
            } else runGoogle()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    fun requestToken(token: String): String {
        val client = OkHttpClient()

        val requestBody: RequestBody = FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("client_id", serverClientId)
            .add("client_secret", client_secret)
            .add("redirect_uri", "")
            .add("code", token)
            .build()
        val request: Request = Request.Builder()
            .url("https://www.googleapis.com/oauth2/v4/token")
            .post(requestBody)
            .build()
        val token1 = arrayOf("")
        client.newCall(request).enqueue(object : okhttp3.Callback {

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.i("AccessToken","Failure : ${e.toString()}")
                Log.i("AccessToken","Failure : ${e.message}")
            }

            @Throws(IOException::class)
            override fun onResponse(
                @NonNull call: okhttp3.Call,
                @NonNull response: okhttp3.Response
            ) {
                try {
                    val jsonObject = JSONObject(response.body!!.string())
                    token1[0] = jsonObject.getString("id_token")
//                    token1[0] = jsonObject.getString("access_token")
                    access_token = token1[0]

//                    Log.i("AccessToken","AccessToken : $access_token")
//                    Log.i("AccessToken","jsonObject : $jsonObject")

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        })
        return token1[0]
    }

    private fun socialLoginApi(googleToken:String) {
        val obj = JsonObject()
        obj.addProperty("token", googleToken)
        if (isReferChecked){
            obj.addProperty("refer_code", viewBinding.referCodeET.text.toString())
        }
        obj.addProperty("appid", sessionManager.getNotificationToken())
        val socialReq = apiInterface.socialLogin(obj)

        Log.d("check200", "socialLoginApi: $obj")
        socialReq.enqueue(object : Callback<LoginResponse>{
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful && response.body()!!.success!!){
                    myApplication.printLogI("${response.body()!!.data!!.id}","login userID")
                    sessionManager.clearLogoutSession()
                    val token = response.body()!!.data!!.token.toString()
                    val userId = response.body()!!.data!!.id.toString()
                    val auditionID = response.body()!!.data!!.auditionID.toString()
                    sessionManager.setGuestLogin(false)
                    sessionManager.createUserLoginSession(true,token,"")
                    sessionManager.setUserSelfID(userId)
                    sessionManager.setToken(token)
                    sessionManager.setUserAuditionID(auditionID)
                    sessionManager.setEmailVerified(true)
                    sleep(200)
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

    fun onTokenRefresh(googleToken: String) {
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
                socialLoginApi(googleToken)
            }
        })
        FirebaseMessaging.getInstance().subscribeToTopic("All-user")
    }

    /*fun LoginFacebook() {
        if (myApplication.isNetworkConnected())
            LoginManager.getInstance().logInWithReadPermissions(this@LoginActivity, listOf("public_profile","email"))
        else
           myApplication.showToast(ConstValFile.Check_Connection)
    }*/

    private fun PrintHashKey() {
        try {
            val info =
                packageManager.getPackageInfo("com.img.audition", PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
                System.out.print("Key hash is " + Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

    }


    private fun facebookLogin(){

        fbLoginManager = LoginManager.getInstance();
        fbCallbackManager = CallbackManager.Factory.create()
        fbLoginManager.registerCallback(fbCallbackManager,object :FacebookCallback<LoginResult>{
            override fun onCancel() {
                Log.d(TAG, "onCancel: ")
            }

            override fun onError(error: FacebookException) {
                Log.e(TAG, "onError: ", error)
            }

            override fun onSuccess(result: LoginResult) {
               val graphRequest = GraphRequest.newMeRequest(result.accessToken,
               object : GraphRequest.GraphJSONObjectCallback{
                   override fun onCompleted(obj: JSONObject?, response: GraphResponse?) {
                       if (obj!=null){
                          try {
                              val name: String = obj.getString("name")
                              val email: String = obj.getString("email")
                              val fbUserID: String = obj.getString("id")

                              disconnectFromFacebook();
                          }
                          catch (e:Exception) {
                               e.printStackTrace();
                           }
                       }
                   }

               })
                val parameters = Bundle()
                parameters.putString("fields", "id, name, email, gender")
                graphRequest.parameters = parameters
                graphRequest.executeAsync()
            }


        })
    }

    private fun disconnectFromFacebook() {
        if (AccessToken.getCurrentAccessToken() == null){
            return
        }
          GraphRequest(AccessToken.getCurrentAccessToken(),
            "/me/permissions/",
            null,
            HttpMethod.DELETE,
              { LoginManager.getInstance().logOut() }).executeAsync()

    }


    override fun onStop() {
        try {
            timer.cancel()
            imageList.clear()
        }catch (e:Exception){
            e.printStackTrace()
        }

        super.onStop()

    }

}