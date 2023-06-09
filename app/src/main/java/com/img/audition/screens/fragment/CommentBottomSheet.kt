package com.img.audition.screens.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.img.audition.R
import com.img.audition.adapters.CommentAdapter
import com.img.audition.dataModel.CommentData
import com.img.audition.dataModel.VideoData
import com.img.audition.databinding.CommentBottomSheetBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.network.SessionManager
import com.img.audition.screens.LoginActivity
import com.img.audition.videoWork.VideoCacheWork
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

@UnstableApi class CommentBottomSheet : BottomSheetDialogFragment() {

    private val sessionManager by lazy {
        SessionManager(requireActivity().applicationContext)
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

    private val commentDataList = ArrayList<CommentData>()
    private var adapter: CommentAdapter? = null

    lateinit var commentCycle : RecyclerView
    lateinit var commentLayoutManager: LinearLayoutManager

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

        commentCycle = view.commentCycle
        commentLayoutManager = LinearLayoutManager(view1.context,LinearLayoutManager.VERTICAL,false)
        commentCycle.layoutManager = commentLayoutManager
        val auditionID = bundle!!.getString(ConstValFile.AuditionID)
        val userID = bundle!!.getString(ConstValFile.AllUserID)
        val videoID = bundle!!.getString(ConstValFile.VideoID)
        val uIm = bundle!!.getString(ConstValFile.UserImage).toString()
        var userImage = ""
        if (uIm.isNotEmpty()){
            userImage = uIm
        }
        val postID = bundle!!.getString(ConstValFile.PostID)

        getAllComments(postID)
        view.sendCommentBtn.setOnClickListener {
            if ((!sessionManager.isUserLoggedIn())){
                sendToLoginScreen()
            }else{
                val commentText = view.commentET.text.toString().trim()
                val position = bundle!!.getInt(ConstValFile.UserPositionInList)
                val videoList = bundle!!.getSerializable("list") as ArrayList<VideoData>
                if (commentText.isNotEmpty()){
                    view.commentET.text.clear()
                    videoList[position].commentCount = videoList[position].commentCount?.plus(1)
                    writeNewComment(auditionID, userID,postID,videoID,userImage,commentText)
                }else{
                    Toast.makeText(view1.context,"Please write something..", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }


    private fun writeNewComment(auditionID: String?, userID: String?,postID: String?,videoID: String?,
                                userImage: String?,commentText: String?)
    {

       /* view.noCommentView.visibility = View.GONE
        commentCycle.visibility = View.VISIBLE
        val commentData =  JSONObject()
        commentData.put("video_id",videoID)
        commentData.put("user_id",userID)
        commentData.put("comment",commentText)
        mSocket.emit("post-comment",commentData)


       val mapData = HashMap<String, Any>()
        mapData.put("comment_by",sessionManager.getUserName()!!)
        mapData.put("auditionid", auditionID!!)
        mapData.put("comment",commentText!!)
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
        val date = dateFormat.format(calendar.time)
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val time = timeFormat.format(calendar.time)
        val now = "$date $time"

        val comment_id = UUID.randomUUID().toString()
        mapData.put("created_at", now.toString())
        mapData.put("user_id", userID.toString())
        mapData.put("userimage",userImage.toString())
        mapData.put("post_id",postID.toString())
        mapData.put("comment_id",comment_id)

        val cData = CommentData()
        cData.auditionID = auditionID.toString()
        cData.comment_id = comment_id
        cData.commentBy = sessionManager.getUserName()!!
        cData.comment = commentText.toString()
        cData.createdAt = now.toString()
        cData.userImage = userImage.toString()
        cData.postID = postID.toString()

        commentDataList.add(0,cData)
        if(adapter!=null){
            adapter = CommentAdapter(requireContext(), commentDataList)
            commentCycle.adapter = adapter

            adapter!!.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    super.onItemRangeInserted(positionStart, itemCount)
                    val totalItemCount: Int = commentLayoutManager.itemCount
                    val lastVisibleItemPosition: Int = commentLayoutManager.findLastVisibleItemPosition()
                    // If the last visible item is the same as the last item in the adapter, scroll to the bottom
                    if (lastVisibleItemPosition == totalItemCount - 1) {
                        commentCycle.scrollToPosition(lastVisibleItemPosition)
                    }
                }
            })
            adapter!!.notifyDataSetChanged()
        }else{

            adapter = CommentAdapter(requireContext(), commentDataList, this)
            commentCycle.adapter = adapter

            adapter!!.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    super.onItemRangeInserted(positionStart, itemCount)
                    val totalItemCount: Int = commentLayoutManager.itemCount
                    val lastVisibleItemPosition: Int = commentLayoutManager.findLastVisibleItemPosition()
                    // If the last visible item is the same as the last item in the adapter, scroll to the bottom
                    if (lastVisibleItemPosition == totalItemCount - 1) {
                        commentCycle.scrollToPosition(lastVisibleItemPosition)
                    }
                }
            })
        }


        firebaseDB.collection(ConstValFile.FirebaseCommentDB)
            .add(mapData)
            .addOnSuccessListener(OnSuccessListener<DocumentReference> { documentReference ->
            }).addOnFailureListener(OnFailureListener { e ->
                e.printStackTrace()
            })
*/
    }

    fun sendToLoginScreen(){
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        requireContext().startActivity(intent)
    }

    private fun getAllComments(postID: String?) {
        /*firebaseDB.collection(ConstValFile.FirebaseCommentDB).whereEqualTo("post_id",postID)
            .get().addOnSuccessListener {
                for (snap in it){
                    val cData = CommentData()

                    cData.comment_id = snap.get("comment_id").toString()
                    Log.i("DocumentId","Snap_values : ${snap.get("auto-id").toString()}")
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
                    commentCycle.visibility = View.VISIBLE
                    val adapter = CommentAdapter(requireContext(), commentDataList, this)
                    commentCycle.adapter = adapter
                }else{
                    view.noCommentView.visibility = View.VISIBLE
                    commentCycle.visibility = View.GONE
                }

            }*/
    }

    override fun onDestroyView() {
        try {
            commentCycle.adapter = null
            commentDataList.clear()
            adapter = null
        }catch (e:Exception){
            e.printStackTrace()
        }
        getView()?.destroyDrawingCache()
        super.onDestroyView()
    }

}