package com.img.audition.screens

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.img.audition.R
import com.img.audition.adapters.ChatsAdapter
import com.img.audition.dataModel.ChatsGetSet
import com.img.audition.databinding.ActivityMessageBinding
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.videoWork.VideoCacheWork
import io.socket.client.Socket
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class MessageActivity : AppCompatActivity() {

    val TAG = "OtherUserProfileActivity"
    private lateinit var viewBinding : ActivityMessageBinding
    private val sessionManager by lazy {
        SessionManager(this@MessageActivity)
    }

    private val apiInterface by lazy{
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }

    private val myApplication by lazy {
        MyApplication(this@MessageActivity)
    }

    var chat_list: ArrayList<ChatsGetSet> = ArrayList<ChatsGetSet>()
    var adapter: ChatsAdapter? = null
    var page_no = 1
    var canLoadData = true

    lateinit var mSocket : Socket
    lateinit var userid : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.backPressIC.setOnClickListener {
            finish()
        }

        mSocket = VideoCacheWork.mSocket!!
//        viewBinding.chatsRV.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)

        viewBinding.sendMessage.setOnClickListener(View.OnClickListener {
            Log.i("ChatSizeCheck","Before : ${chat_list.size}")
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
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                val calendar = Calendar.getInstance().time
                val year = calendar.year
                val month = calendar.month + 1
                val day = calendar.day
                val hour = calendar.hours
                val minutes = calendar.minutes
                val seconds = calendar.seconds
                var now: Date? = null
                try {
                    now = dateFormat.parse(year.toString() + "-" + month + "-" + day + "T" + hour + ":" + minutes + ":" + seconds)
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
                val ob = ChatsGetSet()
                ob.message = messageText
                ob.senderId = sessionManager.getUserSelfID()!!
                ob.receiverId = userid
                ob.createdAt = now.toString()
                chat_list.add(0, ob)
                Log.i("ChatSizeCheck","After : ${chat_list.size}")
                adapter!!.notifyDataSetChanged()
                Log.i("ChatSizeCheck","after notify : ${chat_list.size}")
                Log.i("ChatSizeCheck","after notify : ${viewBinding.chatsRV.layoutManager!!.childCount}")
            } else {
                Toast.makeText(applicationContext, "Please Enter Something..", Toast.LENGTH_SHORT).show()
            }
        })

        mSocket.on("message") { args ->
            runOnUiThread {
                try {
                    val response = args[0] as JSONObject
                    Log.i("ReceivedSocket", "message :  Data is $response")
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                    val calendar = Calendar.getInstance().time
                    val year = calendar.year
                    val month = calendar.month + 1
                    val day = calendar.date
                    val hour = calendar.hours
                    val minutes = calendar.minutes
                    val seconds = calendar.seconds
                    var now: Date? = null
                    try {
                        now =
                            dateFormat.parse(year.toString() + "-" + month + "-" + day + "T" + hour + ":" + minutes + ":" + seconds)
                    } catch (e: ParseException) {
                        e.printStackTrace()
                    }
                    val ob = ChatsGetSet()
                    ob.message = response.getString("message")
                    ob.senderId = response.getString("senderId")
                    ob.receiverId = response.getString("receiverId")
                    ob.createdAt = now.toString()
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
        getChatData()

        val job1 = JSONObject()
        try {
            job1.put("senderId", sessionManager.getUserSelfID())
            job1.put("receiverId", userid)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        mSocket.emit("seen", job1)
    }

    fun getChatData() {
Log.i("canLoadData","Page_no $page_no")
        val responseCall: Call<ChatsGetSet> =
            apiInterface.getChatHistory(sessionManager.getToken(), userid, page_no)
        responseCall.enqueue(object : Callback<ChatsGetSet> {
            override fun onResponse(call: Call<ChatsGetSet>, response: Response<ChatsGetSet>) {
                Log.d("check", response.toString())
                if (response.isSuccessful()) {
                    Log.d("check", response.toString())
                   val chat_list1 = response.body()!!.data!!
                    chat_list.addAll(chat_list1)
                    if (chat_list1.size > 0) {
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
                } else {
                    myApplication.showToast("Something went wrong!!")
                    finish()
                }
            }

            override fun onFailure(call: Call<ChatsGetSet>, t: Throwable) {
                myApplication.printLogD("text", t.message!!)
            }
        })
    }

    override fun onStart() {
        super.onStart()

        userid = intent.getStringExtra("userid").toString()
        val image = intent.getStringExtra("image")
        val username = intent.getStringExtra("name")
        val url204 = intent.getStringExtra("url204")

        Log.i("intent_test","image : $image")
        Log.i("intent_test","username : $username")
        Log.i("intent_test","userid : $userid")

        viewBinding.name.text = username
        viewBinding.uname.text = username

        try {
            if (url204!=null){
                if (url204.isNotEmpty()){
                    viewBinding.messageET.setText(url204.toString())
                }
            }
            Glide.with(this@MessageActivity).load(image).placeholder(R.drawable.person_ic).into(viewBinding.img)
        }catch (e:Exception){
            e.printStackTrace()
        }


    }

}