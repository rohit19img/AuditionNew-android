package com.img.audition.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.img.audition.R
import com.img.audition.adapters.ChatsAdapter
import com.img.audition.dataModel.ChatsGetSet
import com.img.audition.databinding.ActivityMessageBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.videoWork.VideoCacheWork
import com.img.audition.viewModel.MainViewModel
import com.img.audition.viewModel.Status
import com.img.audition.viewModel.ViewModelFactory
import io.socket.client.Socket
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@UnstableApi
class MessageActivity : AppCompatActivity() {

    private val TAG = "MessageActivity"
    private lateinit var viewBinding: ActivityMessageBinding
    private val sessionManager by lazy {
        SessionManager(this@MessageActivity)
    }


    private var chat_list: ArrayList<ChatsGetSet> = ArrayList<ChatsGetSet>()
    private var adapter: ChatsAdapter? = null
    private var page_no = 1
    private var canLoadData = true

    private lateinit var mSocket: Socket
    private lateinit var userid: String

    private lateinit var mainViewModel: MainViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        val apiInterface = RetrofitClient.getInstance().create(ApiInterface::class.java)

        mainViewModel = ViewModelProvider(
            this,
            ViewModelFactory(sessionManager.getToken(), apiInterface)
        )[MainViewModel::class.java]

        viewBinding.backPressIC.setOnClickListener {
            finish()
        }

        mSocket = VideoCacheWork.mSocket!!

        viewBinding.sendMessage.setOnClickListener(View.OnClickListener {
            if (MyApplication(this).isNetworkConnected()) {
                Log.i("ChatSizeCheck", "Before : ${chat_list.size}")
                val messageText: String = viewBinding.messageET.text.toString().trim()
                if (messageText.isNotEmpty()) {
                    val jsonObject = JSONObject()
                    try {
                        jsonObject.put("senderId", sessionManager.getUserSelfID())
                        jsonObject.put("receiverId", userid)
                        jsonObject.put("message", messageText)

                        mSocket.emit("message", jsonObject)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    Log.i("data", "Sent data is : $jsonObject")
                    viewBinding.messageET.setText("")

                    val ob = ChatsGetSet()
                    ob.message = messageText
                    ob.senderId = sessionManager.getUserSelfID()!!
                    ob.receiverId = userid
                    try {
                        ob.createdAt = getCurrentTime()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    Log.d("timeCheck", "onCreate: ${getCurrentTime()}")
                    chat_list.add(0, ob)
                    Log.i("ChatSizeCheck", "After : ${chat_list.size}")
                    adapter!!.notifyDataSetChanged()
                    Log.i("ChatSizeCheck", "after notify : ${chat_list.size}")
                    Log.i(
                        "ChatSizeCheck",
                        "after notify : ${viewBinding.chatsRV.layoutManager!!.childCount}"
                    )
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Please Enter Something..",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(this, ConstValFile.Check_Connection, Toast.LENGTH_SHORT).show()
            }
        })

        mSocket.on("message") { args ->
            runOnUiThread {
                try {
                    val response = args[0] as JSONObject
                    val ob = ChatsGetSet()
                    ob.message = response.getString("message")
                    ob.senderId = response.getString("senderId")
                    ob.receiverId = response.getString("receiverId")
                    try {
                        ob.createdAt = getCurrentTime()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    if (response.getString("receiverId") == sessionManager.getUserSelfID()) {
                        chat_list.add(0, ob)
                        adapter!!.notifyDataSetChanged()
                    }
                    Log.i("size", "SIze 1 is " + chat_list.size)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }

        viewBinding.chatsRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!viewBinding.chatsRV.canScrollVertically(0)) {
                    if (canLoadData) {
                        page_no++
                        getChatData()
                    }
                }

//                val layoutManager = viewBinding.chatsRV.layoutManager!!
//
//                val visibleItemCount: Int = layoutManager.childCount
//                val totalItemCount: Int = layoutManager.itemCount
//                val firstVisibleItemPosition: Int = layoutManager.findFirstCompletelyVisibleItemPosition()
//
//                if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
//                    if (canLoadData) {
//                        page_no++
//                        getChatData()
//                    }
//                }
            }
        })


    }

    override fun onResume() {
        super.onResume()

        if (MyApplication(this).isNetworkConnected()) {
            getChatData()
        } else {
            checkInternetDialog()
        }

        val job1 = JSONObject()
        try {
            job1.put("senderId", sessionManager.getUserSelfID())
            job1.put("receiverId", userid)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        mSocket.emit("seen", job1)
    }

    private fun getChatData() {
        Log.i("canLoadData", "Page_no $page_no")
        if (MyApplication(this).isNetworkConnected()) {
            mainViewModel.getChatHistory(userid, page_no).observe(this) {
                it.let { resources ->
                    when (resources.status) {
                        Status.SUCCESS -> {
                            Log.d("check", resources.data!!.message)
                            val chatList = resources.data.data
                            chat_list.addAll(chatList)
                            if (chatList.size > 0) {
                                canLoadData = true
                                if (page_no == 1) {
                                    adapter = ChatsAdapter(this@MessageActivity, chat_list)
                                    viewBinding.chatsRV.adapter = adapter
                                } else
                                    adapter!!.notifyDataSetChanged()
                            } else {
                                canLoadData = false
                                if (page_no == 1) {
                                    adapter = ChatsAdapter(this@MessageActivity, chat_list)
                                    viewBinding.chatsRV.adapter = adapter
                                }
                            }
                        }

                        Status.LOADING -> {
                            Log.d(TAG, resources.status.toString())
                        }

                        else -> {
                            if (resources.message!!.contains("401")) {

                                sessionManager.clearLogoutSession()
                                startActivity(
                                    Intent(
                                        this@MessageActivity,
                                        SplashActivity::class.java
                                    )
                                )
                                finishAffinity()
                            }
                            Log.e(TAG, resources.status.toString())
                        }

                    }
                }
            }
        } else {
            checkInternetDialog()
        }
    }

    override fun onStart() {
        super.onStart()

        userid = intent.getStringExtra("userid").toString()
        val image = intent.getStringExtra("image")
        val username = intent.getStringExtra("name")
        val auditionID = intent.getStringExtra("auditionID")
        val url204 = intent.getStringExtra("url204")

        Log.i("intent_test", "image : $image")
        Log.i("intent_test", "username : $username")
        Log.i("intent_test", "userid : $userid")

        if (username!!.isNotEmpty()) {
            viewBinding.name.text = username
            viewBinding.uname.text = username
        } else {
            viewBinding.name.text = auditionID
            viewBinding.uname.text = auditionID
        }

        try {
            if (url204 != null) {
                if (url204.isNotEmpty()) {
                    viewBinding.messageET.setText(url204.toString())
                }
            }
            Glide.with(this@MessageActivity).load(image).placeholder(R.drawable.person_ic)
                .into(viewBinding.img)
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    override fun onStop() {
        try {
            chat_list.clear()
            adapter = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

        super.onStop()
    }

    private fun checkInternetDialog() {

        val sweetAlertDialog = SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
        sweetAlertDialog.titleText = "Internet"
        sweetAlertDialog.contentText = ConstValFile.Check_Connection
        sweetAlertDialog.confirmText = "Retry"
        sweetAlertDialog.setConfirmClickListener {
            sweetAlertDialog.dismiss()
            getChatData()
        }
        sweetAlertDialog.show()
    }

    fun getCurrentTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("Asia/Kolkata") // Set the timezone to IST
        return dateFormat.format(Date())
    }
}