package com.img.audition.screens.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.img.audition.R
import com.img.audition.adapters.CommentAdapter
import com.img.audition.dataModel.CommentData
import com.img.audition.databinding.CommentBottomSheetBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.screens.LoginActivity
import com.img.audition.videoWork.VideoCacheWork
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class CommentBottomSheet : BottomSheetDialogFragment() {
    val TAG = "CommentBottomSheet"

    private val sessionManager by lazy {
        SessionManager(requireActivity().applicationContext)
    }
    private val apiInterface by lazy {
        RetrofitClient.getInstance().create(ApiInterface::class.java)
    }
    private val myApplication by lazy {
        MyApplication(requireActivity().applicationContext)
    }

    private val bundle by lazy {
        arguments
    }

    private val mSocket by lazy {
        VideoCacheWork.mSocket!!
    }

    private val firebaseDB by lazy {
        FirebaseFirestore.getInstance()
    }

    val commentDataList = ArrayList<CommentData>()
    lateinit var adapter: CommentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    lateinit var view: CommentBottomSheetBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        view = CommentBottomSheetBinding.inflate(inflater,container,false)
        return view.root
    }

    override fun onViewCreated(view1: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view1, savedInstanceState)

        val auditionID = bundle!!.getString(ConstValFile.AuditionID)
        val userID = bundle!!.getString(ConstValFile.AllUserID)
        val videoID = bundle!!.getString(ConstValFile.VideoID)
        val userImage = bundle!!.getString(ConstValFile.UserImage)
        val postID = bundle!!.getString(ConstValFile.PostID)

        getAllComments(postID)
        view.sendCommentBtn.setOnClickListener {
            if ((!sessionManager.isUserLoggedIn())){
                sendToLoginScreen()
            }else{
                val commentText = view.commentET.text.toString().trim()
               /* val list = (VideoFragment).videoList2
                val adapter = (VideoFragment).videoAdapter*/
                val position = bundle!!.getInt(ConstValFile.UserPositionInList)
               if (commentText.isNotEmpty()){
                   view.commentET.text.clear()
                  /* list[position].commentCount =+ 1
                   adapter.notifyDataSetChanged()*/
                   writeNewComment(auditionID, userID,postID,videoID,userImage,commentText)
               }else{
                   myApplication.showToast("Please write something..")
               }
            }
        }

    }


    private fun writeNewComment(auditionID: String?, userID: String?,postID: String?,videoID: String?,
                                userImage: String?,commentText: String?)
    {

        view.noCommentView.visibility = View.GONE
        view.commentCycle.visibility = View.VISIBLE
       val commentData =  JSONObject()
        commentData.put("video_id",videoID)
        commentData.put("user_id",userID)
        commentData.put("comment",commentText)
        mSocket.emit("post-comment",commentData)


       val mapData = HashMap<String, Any>()
        mapData.put("comment_by",auditionID!!)
        mapData.put("auditionid", auditionID)
        mapData.put("comment",commentText!!)
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
            now =
                dateFormat.parse(year.toString() + "-" + month + "-" + day + "T" + hour + ":" + minutes + ":" + seconds)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        mapData.put("created_at", now.toString())
        mapData.put("user_id", userID!!)
        mapData.put("userimage",userImage!!)
        mapData.put("post_id",postID!!)

        val cData = CommentData()
        cData.auditionID = auditionID.toString()
        cData.commentBy = auditionID.toString()
        cData.comment = commentText.toString()
        cData.createdAt = now.toString()
        cData.userImage = userImage.toString()
        cData.postID = postID.toString()

        commentDataList.add(cData)
        if(this::adapter.isInitialized){
            adapter!!.notifyDataSetChanged()
        }else{
            val adapter = CommentAdapter(requireContext(),commentDataList)
            view.commentCycle.adapter = adapter
        }


        firebaseDB.collection(ConstValFile.FirebaseCommentDB)
            .add(mapData)
            .addOnSuccessListener(OnSuccessListener<DocumentReference> { documentReference ->

            }).addOnFailureListener(OnFailureListener { e ->
                myApplication.printLogE("Firebase Failure",TAG)
            })

    }

    fun sendToLoginScreen(){
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        requireContext().startActivity(intent)
    }

    private fun getAllComments(postID: String?) {
        firebaseDB.collection(ConstValFile.FirebaseCommentDB).whereEqualTo("post_id",postID)
            .get().addOnSuccessListener {

                for (snap in it){
                    val cData = CommentData()
                    cData.auditionID = snap.get("auditionid").toString()
                    cData.commentBy = snap.get("comment_by").toString()
                    cData.comment = snap.get("comment").toString()
                    cData.createdAt = snap.get("created_at").toString()
                    cData.userImage = snap.get("userimage").toString()
                    cData.postID = snap.get("post_id").toString()
                    commentDataList.add(cData)
                }
                if (commentDataList.size>0){
                    view.noCommentView.visibility = View.GONE
                    view.commentCycle.visibility = View.VISIBLE
                    val adapter = CommentAdapter(requireContext(),commentDataList)
                    view.commentCycle.adapter = adapter
                }else{
                    view.noCommentView.visibility = View.VISIBLE
                    view.commentCycle.visibility = View.GONE
                }

            }
    }
}