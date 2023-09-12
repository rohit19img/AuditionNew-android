package com.img.audition.adapters


import android.annotation.SuppressLint
import android.app.Dialog
import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.method.ScrollingMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlaybackException.TYPE_SOURCE
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.img.audition.R
import com.img.audition.dataModel.*
import com.img.audition.databinding.TestLayoutForMainBinding
import com.img.audition.databinding.VideoLayoutBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.screens.*
import com.img.audition.screens.fragment.ProfileFragment
import com.img.audition.screens.fragment.VideoReportDialog
import com.img.audition.videoWork.FollowFollowingTrack
import com.img.audition.videoWork.VideoCacheWork
import com.img.audition.videoWork.VideoItemPlayPause
import io.socket.client.Socket
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

@UnstableApi
class VideoAdapter() : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>(), FollowFollowingTrack {

    lateinit var contextFromActivity:Context
    lateinit var videoList:ArrayList<VideoData>
    private val TAG = "VideoAdapter"
    lateinit var sessionManager : SessionManager
    lateinit var apiInterface :ApiInterface
    lateinit var firestore :FirebaseFirestore
    var commentList: ArrayList<CommentData> = arrayListOf()
    private var commentAdapter: CommentAdapter? = null
    lateinit var mSocket: Socket

    var Ar = arrayOf(-1, 0)
    var cPos = 0


    private var videoPosition = 0

    private val myApplication by lazy {
        MyApplication(contextFromActivity)
    }
    constructor(contextFromActivity: Context,videoList: ArrayList<VideoData>):this()
    {
        this.videoList = videoList
        this.contextFromActivity = contextFromActivity
        sessionManager = SessionManager(contextFromActivity.applicationContext)
        apiInterface = RetrofitClient.getInstance().create(ApiInterface::class.java)
        firestore = FirebaseFirestore.getInstance()
        mSocket = VideoCacheWork.mSocket!!
    }

    override fun onIntentReceived(followStatus: Boolean, userID: String, position: Int) {
        Log.d("onIntentReceived","Intent $TAG : $followStatus")
        for (dd in videoList) {
            if (dd.userId == userID) {
                dd.followStatus = followStatus
                notifyDataSetChanged()
            }
        }
    }

    inner class VideoViewHolder(itemView: TestLayoutForMainBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val playerViewExo = itemView.videoExoView
        val likeBtn = itemView.likeButton
        val likeCount = itemView.likeCount
        val followBtn = itemView.followButton
        val userProfile = itemView.userImage
        val userProfile2 = itemView.userImage1
        val viewVidUserProBtn = itemView.viewVidUserProBtn
        val voteBtn = itemView.voteButton
        val userName = itemView.userName
        val caption = itemView.videoCaption
        val audioName = itemView.audioName
        val audioImage = itemView.audioImage
        val shareBtn = itemView.shareButton
        val moreBtn = itemView.moreButton
        val commentBtn = itemView.commentButton
        val postLocation = itemView.postLocation
        val commentCount = itemView.commentCount
        val viewCount = itemView.viewCount
        val playPauseIc = itemView.videoPlayPause
        val playPauseVideoBtn = itemView.playPauseVideoBtn

         val MAX_LINES_COLLAPSED = 3
         val INITIAL_IS_COLLAPSED = true

         var isCollapsed = INITIAL_IS_COLLAPSED

        //Video Cache

        val mediaSource = ProgressiveMediaSource.Factory(CacheDataSource.Factory().setCache(VideoCacheWork.simpleCache)
                .setUpstreamDataSourceFactory(
                    DefaultHttpDataSource.Factory().setUserAgent("ExoPlayer")
                )
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        )


        val trackSelector = DefaultTrackSelector(contextFromActivity).apply {
            setParameters(buildUponParameters()
                .setRendererDisabled(C.TRACK_TYPE_VIDEO,true)
                .setAllowVideoMixedDecoderSupportAdaptiveness(true)
                .setAllowAudioMixedDecoderSupportAdaptiveness(true)
                .setPreferredVideoMimeType("video/avc")
                .setPreferredAudioMimeType("audio/mp4a-latm")
                .setAllowAudioMixedChannelCountAdaptiveness(true)
                .setAllowMultipleAdaptiveSelections(true)
            )
        }

        val exoPlayer = ExoPlayer.Builder(contextFromActivity.applicationContext)
            .setTrackSelector(trackSelector).setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
            .build()
        //

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val itemBinding =
            TestLayoutForMainBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VideoViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, @SuppressLint("RecyclerView") position: Int) {

        val list = videoList[position]
        holder.apply {

            if (list.location!!.isNotEmpty()) {
//                postLocation.visibility = View.VISIBLE
                postLocation.visibility = View.GONE // remove it, if you want to location
                postLocation.text = list.location.toString()
            } else {
                postLocation.visibility = View.GONE
            }

            viewCount.text = formatCount(list.views!!)

            postLocation.setOnClickListener {
                sendPostLocationVideoActivity(list.location.toString())
            }

            if (list.allowComment == false) {
                commentCount.visibility = View.GONE
                commentBtn.visibility = View.GONE
            } else {
                commentCount.visibility = View.VISIBLE
                commentBtn.visibility = View.VISIBLE
            }

            if (list.allowSharing == false) {
                shareBtn.visibility = View.GONE
            } else {
                shareBtn.visibility = View.VISIBLE
            }

            if (list.allowDuet == false) {
                shareBtn.visibility = View.GONE
            } else {
                shareBtn.visibility = View.VISIBLE
            }

            //vote

            if (list.userId.equals(sessionManager.getUserSelfID())) {
                followBtn.visibility = View.GONE
            } else {
                followBtn.visibility = View.VISIBLE
            }


            if (list.status!!.equals("contest",true)) {
                voteBtn.visibility = View.VISIBLE
            } else {
                voteBtn.visibility = View.GONE
            }

           /* if (list.voteStatus!!){
              voteBtn.setBtnColor(contextFromActivity.getColor(R.color.darkYellow))
            }else{
                voteBtn.setBtnColor(contextFromActivity.getColor(R.color.bgColorWhite))
            }*/

            voteBtn.setOnClickListener {
                if (myApplication.isNetworkConnected()){
                    if (list.contestStatus.equals("started",true)){
                        if (!(sessionManager.isUserLoggedIn())) {
                            sendToLoginScreen()
                        } else {
                            if (list.voteStatus!!){
                                Toast.makeText(contextFromActivity,"You already vote this video",Toast.LENGTH_SHORT).show()
                            }else{
                                showVoteDialog(list.Id,position)
                            }
                        }
                    }else{
                        Toast.makeText(contextFromActivity,"Contest Close",Toast.LENGTH_SHORT).show()
                    }
                }else{
                    showToast(ConstValFile.Check_Connection)
                }
            }

            //commentCount
            firestore.collection(ConstValFile.FirebaseCommentDB)
                .whereEqualTo("post_id", list.postId)
                .get()
                .addOnSuccessListener { documentSnapshots ->
                    commentList = ArrayList<CommentData>()
                    for (documentSnapshot1 in documentSnapshots) {
                        val note: CommentData =
                            documentSnapshot1.toObject(CommentData::class.java)
                        commentList.add(note)
                    }
                    videoList[position].commentCount = commentList.size
                    commentCount.text = formatCount(list.commentCount!!)
                    Log.d("commentCount", "c list size: ${commentList.size.toString()}")

                }
            //

            Log.d("commentCount", "c count: ${list.commentCount.toString()}")
            if (list.likeStatus!!) {
                likeBtn.setImageDrawable(ContextCompat.getDrawable(contextFromActivity, R.drawable.ic_line_new_selected))
            } else {
                likeBtn.setImageDrawable(ContextCompat.getDrawable(contextFromActivity, R.drawable.ic_like_new))
            }
            if (list.followStatus!!) {
                followBtn.text = ConstValFile.Following
                followBtn.setTypeface(followBtn.typeface, Typeface.ITALIC)
            } else {
                followBtn.text = ConstValFile.Follow
                followBtn.setTypeface(followBtn.typeface, Typeface.NORMAL)
            }
            if (list.caption.toString().isNotEmpty()) {
                caption.visibility = View.VISIBLE
                val longString = list.caption.toString()
                val spannable = SpannableStringBuilder(longString)

                // Find all hashtags in the string using a regular expression
                val hashPattern = Pattern.compile("#(\\w+)")
                val hashMatcher = hashPattern.matcher(longString)

                while (hashMatcher.find()) {
                    val hashtag = hashMatcher.group()
                    val start = longString.indexOf(hashtag)
                    val end = start + hashtag.length
                    // Create a clickable span for each hashtag
                    val clickableSpan = object : ClickableSpan() {
                        override fun onClick(view: View) {
                            // Handle hashtag click here
                            val clickedString = (view as TextView).text.toString()
                            val clickedHashtag = clickedString.substring(start, end)

                            sendHashTagVideo(clickedHashtag)
                        }

                        override fun updateDrawState(textPaint: TextPaint) {
                            super.updateDrawState(textPaint)
                            // Customize the appearance of the clickable text
                            textPaint.color = Color.WHITE
                            textPaint.isUnderlineText = false
                            textPaint.typeface = Typeface.DEFAULT_BOLD
                        }
                    }

                    // Set the clickable span on the corresponding text range
                    spannable.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

                val tagPattern = Pattern.compile("@(\\w+)")
                val tagMatcher = tagPattern.matcher(longString)

                while (tagMatcher.find()) {
                    val tag = tagMatcher.group()
                    val start = longString.indexOf(tag)
                    val end = start + tag.length
                    // Create a clickable span for each hashtag
                    val clickableSpan = object : ClickableSpan() {
                        override fun onClick(view: View) {
                            if (list.isBlocked){
                                val sweetAlertDialog = SweetAlertDialog(contextFromActivity, SweetAlertDialog.WARNING_TYPE)
                                sweetAlertDialog.titleText = "Blocked User"
                                sweetAlertDialog.contentText = "Do you want Unblock this user"
                                sweetAlertDialog.confirmText = "Yes"
                                sweetAlertDialog.setConfirmClickListener {
                                    sweetAlertDialog.dismiss()
                                    blockUnblockUser(ConstValFile.Unblock,list.userId.toString())
                                }
                                sweetAlertDialog.cancelText = "No"
                                sweetAlertDialog.setCancelClickListener {
                                    sweetAlertDialog.dismiss()
                                }
                                sweetAlertDialog.show()
                            }else{
                                val clickedString = (view as TextView).text.toString()
                                val clickedHashtag = clickedString.substring(start, end)
                                Log.d("check100", "onClick: $clickedHashtag == ${sessionManager.getUserAuditionID()}")
                                if (clickedHashtag == sessionManager.getUserAuditionID()){
                                    sendToUserSelfProfile()
                                }else{
                                    val bundle = Bundle()
                                    bundle.putString(ConstValFile.USER_IDFORIntent, list.userId)
                                    bundle.putString(ConstValFile.AuditionID, clickedHashtag)
                                    bundle.putBoolean(ConstValFile.isSearchAuditionID, true)
                                    bundle.putInt(ConstValFile.UserPositionInList, position)
                                    bundle.putSerializable("list", videoList)
                                    bundle.putBoolean(ConstValFile.UserFollowStatus, list.followStatus!!)
                                    sendToVideoUserProfile(bundle)
                                }

                            }
                        }

                        override fun updateDrawState(textPaint: TextPaint) {
                            super.updateDrawState(textPaint)
                            // Customize the appearance of the clickable text
                            textPaint.color = Color.WHITE
                            textPaint.isUnderlineText = false
                            textPaint.typeface = Typeface.SANS_SERIF
                        }

                    }

                    // Set the clickable span on the corresponding text range
                    spannable.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                }


                // Set the modified text with clickable hashtags on the TextView
                caption.text = spannable
                // Make sure the clickable links work by enabling movement method
                caption.movementMethod = LinkMovementMethod.getInstance()

            } else {
                caption.visibility = View.GONE
            }

            caption.setOnClickListener {
                if (isCollapsed){
                    caption.maxLines = Int.MAX_VALUE
                }else{
                    caption.maxLines = MAX_LINES_COLLAPSED
                }

                isCollapsed = !isCollapsed
            }


            likeBtn.setOnClickListener {
                if (myApplication.isNetworkConnected()){
                    if (list.likeStatus!!) {
                        likeBtn.isSelected = false
                        likeBtn.setImageDrawable(
                            ContextCompat.getDrawable(
                                contextFromActivity,
                                R.drawable.ic_like_new
                            )
                        )
                        list.likeStatus = false
                        likeVideo(list.Id, "unlike")
                        val newlikeCount = list.likeCount?.minus(1)
                        if (newlikeCount != null && newlikeCount > 1) {
                            list.likeCount = newlikeCount
                        } else {
                            list.likeCount = 0
                        }
                        likeCount.text = formatCount(newlikeCount!!)
                    } else {
                        likeBtn.setImageDrawable(
                            ContextCompat.getDrawable(
                                contextFromActivity,
                                R.drawable.ic_line_new_selected
                            )
                        )
                        likeBtn.isSelected = true
                        likeVideo(list.Id, "liked")
                        list.likeStatus = true
                        val newlikeCount = list.likeCount?.plus(1)
                        list.likeCount = newlikeCount
                        likeCount.text = formatCount(newlikeCount!!)

                    }
                }else{
                    showToast(ConstValFile.Check_Connection)
                }

            }

            followBtn.setOnClickListener {
                if (myApplication.isNetworkConnected()){
                    if (!(sessionManager.isUserLoggedIn())) {
                        showToast(ConstValFile.LoginMsg)
                        sendToLoginScreen()
                    } else {
                        if (!(list.followStatus!!)) {
                            for (uList in videoList) {
                                if (uList.userId.equals(videoList[position].userId)) {
                                    uList.followStatus = true
                                }
                            }
                            list.followStatus = true
                            followUserApi(list.userId, "followed")
                            followBtn.text = ConstValFile.Following
                            followBtn.setTypeface(followBtn.typeface, Typeface.ITALIC)
                        }
                    }
                }else{
                    showToast(ConstValFile.Check_Connection)
                }

            }

            viewVidUserProBtn.setOnClickListener {
                if (myApplication.isNetworkConnected()){
                    if (list.isBlocked){
                        val sweetAlertDialog = SweetAlertDialog(contextFromActivity, SweetAlertDialog.WARNING_TYPE)
                        sweetAlertDialog.titleText = "Blocked User"
                        sweetAlertDialog.contentText = "Do you want Unblock this user"
                        sweetAlertDialog.confirmText = "Yes"
                        sweetAlertDialog.setConfirmClickListener {
                            sweetAlertDialog.dismiss()
                            blockUnblockUser(ConstValFile.Unblock,list.userId.toString())
                        }
                        sweetAlertDialog.cancelText = "No"
                        sweetAlertDialog.setCancelClickListener {
                            sweetAlertDialog.dismiss()
                        }
                        sweetAlertDialog.show()
                    }else{
                        if (!(list.isSelf)!!) {
                            val bundle = Bundle()
                            bundle.putString(ConstValFile.USER_IDFORIntent, list.userId)
                            bundle.putString("auditionID", list.auditionId)
                            bundle.putInt(ConstValFile.UserPositionInList, position)
                            bundle.putSerializable("list", videoList)
                            bundle.putBoolean(ConstValFile.UserFollowStatus, list.followStatus!!)
                            sendToVideoUserProfile(bundle)
                        } else {
                            sendToUserSelfProfile()
                        }
                    }
                }else{
                    showToast(ConstValFile.Check_Connection)
                }
            }

            shareBtn.setOnClickListener {
//                       val videoUrl = "https://audition.com/video/${videoList[position].Id}"
                val videoUrl = "https://biggee.in/video/${videoList[position].Id}"
                shareDialog(position, videoUrl)
            }

            moreBtn.setOnClickListener {
                moredialog(position)
            }

            userName.setOnClickListener {
                if (myApplication.isNetworkConnected()){
                    if (list.isBlocked){
                        val sweetAlertDialog = SweetAlertDialog(contextFromActivity, SweetAlertDialog.WARNING_TYPE)
                        sweetAlertDialog.titleText = "Blocked User"
                        sweetAlertDialog.contentText = "Do you want Unblock this user"
                        sweetAlertDialog.confirmText = "Yes"
                        sweetAlertDialog.setConfirmClickListener {
                            sweetAlertDialog.dismiss()
                            blockUnblockUser(ConstValFile.Unblock,list.userId.toString())
                        }
                        sweetAlertDialog.cancelText = "No"
                        sweetAlertDialog.setCancelClickListener {
                            sweetAlertDialog.dismiss()
                        }
                        sweetAlertDialog.show()
                    }else{
                        if (!(list.isSelf)!!) {
                            val bundle = Bundle()
                            bundle.putString(ConstValFile.USER_IDFORIntent, list.userId)
                            bundle.putInt(ConstValFile.UserPositionInList, position)
                            bundle.putSerializable("list", videoList)
                            bundle.putBoolean(ConstValFile.UserFollowStatus, list.followStatus!!)
                            sendToVideoUserProfile(bundle)
                        } else {
                            sendToUserSelfProfile()
                        }
                    }
                }else{
                    showToast(ConstValFile.Check_Connection)
                }
            }

            likeCount.text = formatCount(list.likeCount!!)

            Glide.with(contextFromActivity).load(list.image).placeholder(R.drawable.person_ic)
                .into(userProfile)
            Glide.with(contextFromActivity).load(list.image).placeholder(R.drawable.person_ic)
                .into(userProfile2)
            userName.text = list.auditionId
            if (list.songId != null && list.songId!!.isNotEmpty()) {
                audioName.text =
                    list.song?.title + "  by ${list.song?.userDetails?.auditionId}  " + list.song?.subtitle
            } else {
                audioName.text = "Audio Not Available"
            }


            audioName.setOnClickListener {
                if (list.songId != null && list.songId!!.isNotEmpty()) {
                    sendSongVideoActivity(list.song!!.Id!!, list.songLink.toString())
                } else {
                    showToast("This Audio Not Available")
                }
            }

            commentBtn.setOnClickListener {
                if (myApplication.isNetworkConnected()){
                    videoPosition = position
                    val bundle = Bundle()
                    bundle.putString(ConstValFile.AuditionID, list.auditionId)
                    bundle.putSerializable(ConstValFile.CommentList, list.comment)
                    bundle.putString(ConstValFile.AllUserID, list.userId)
                    bundle.putString(ConstValFile.VideoID, list.Id)
                    bundle.putString(ConstValFile.PostID, list.postId)
                    val userImage = if (!(sessionManager.getUserProfileImage().isNullOrEmpty())) {
                        sessionManager.getUserProfileImage()
                    } else {
                        ""
                    }
                    bundle.putInt(ConstValFile.UserPositionInList, position)
                    bundle.putSerializable("list",videoList)
//                showCommentDialog(bundle)
                    showCommentDialog2(list.postId!!,list.auditionId!!,list.userId!!,list.Id,userImage!!,position)
                }else{
                    showToast(ConstValFile.Check_Connection)
                }

            }
            audioName.isSelected = true
            holder.audioName.movementMethod = ScrollingMovementMethod()

        }
    }

    override fun onViewAttachedToWindow(holder: VideoViewHolder) {

        val list = videoList[holder.position]

        holder.apply {

            val mediaItem =  MediaItem.Builder().setMimeType("video/avc").setUri(list.file.toString()).build()
            val videoMediaSource = mediaSource.createMediaSource(mediaItem)
            playerViewExo.player = exoPlayer
            exoPlayer.setMediaSource(videoMediaSource)
            exoPlayer.prepare()

            if (cPos >= 0) {

                if (cPos == position) {

                    exoPlayer.seekTo(0)
                    exoPlayer.playWhenReady = true
                } else {
                    exoPlayer.seekTo(0)
                    exoPlayer.playWhenReady = false
                }
            }
            playPauseVideoBtn.setOnClickListener {
                playPauseIc.visibility = View.GONE
                if (exoPlayer.isPlaying) {
                    exoPlayer.pause()
                    playPauseIc.visibility = View.VISIBLE
                    playPauseIc.setImageDrawable(
                        ContextCompat.getDrawable(
                            contextFromActivity,
                            R.drawable.play_ic
                        )
                    )

                } else {
                    exoPlayer.play()
                    playPauseIc.visibility = View.GONE
                }
            }

            audioImage.setOnClickListener {
                if (exoPlayer.volume == 0F) {
                    exoPlayer.volume = 1F
                    audioImage.setImageDrawable(
                        ContextCompat.getDrawable(
                            contextFromActivity,
                            R.drawable.volume_on_ic
                        )
                    )
                } else {
                    exoPlayer.volume = 0F
                    audioImage.setImageDrawable(
                        ContextCompat.getDrawable(
                            contextFromActivity,
                            R.drawable.volume_off_ic
                        )
                    )
                }
            }
            exoPlayer.addListener(object : Player.Listener {
                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    when (playbackState) {
                        ExoPlayer.STATE_ENDED -> {
                            exoPlayer.seekTo(0)
                            exoPlayer.prepare()
                            exoPlayer.play()
                        }
                        ExoPlayer.STATE_BUFFERING -> {}
                        ExoPlayer.STATE_READY -> {}
                        else -> {}
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    when (error.errorCode) {
                        TYPE_SOURCE -> {
                            val list = videoList[position]
                            val mediaItem = MediaItem.fromUri(list.file.toString())
                            val videoMediaSource = mediaSource.createMediaSource(mediaItem)
                            playerViewExo.player = exoPlayer
                            exoPlayer.setMediaSource(videoMediaSource)
                            exoPlayer.prepare()
                            exoPlayer.play()
                        }
                        else -> {}

                    }
                }
            })
        }
        super.onViewAttachedToWindow(holder)
    }


    fun onActivityStateChanged(): VideoItemPlayPause {
        return object : VideoItemPlayPause {
            override fun onPause(holder: VideoAdapter.VideoViewHolder, cPos: Int) {

                holder.apply {
                    if (cPos >= 0) {
                        if (cPos == position) {
                            exoPlayer.pause()
                            exoPlayer.playWhenReady = false
                        } else {
                            exoPlayer.pause()
                            exoPlayer.playWhenReady = false
                        }
                    }
                    exoPlayer.playWhenReady = false
                    exoPlayer.pause()
                }
            }

            override fun onResume(holder: VideoViewHolder, cPos: Int) {
                holder.apply {
                    if (cPos >= 0) {
                        if (cPos == position) {
                            exoPlayer.prepare()
                            exoPlayer.playWhenReady = true
                        } else {
                            exoPlayer.seekTo(0)
                            exoPlayer.playWhenReady = false
                            exoPlayer.stop()
                        }
                    }

                }
            }

            override fun onRestart(holder: VideoViewHolder, cPos: Int) {}

            override fun onStop(holder: VideoViewHolder, cPos: Int) {

                holder.apply {
                    if (cPos >= 0) {
                        if (cPos == position) {
                            exoPlayer.playWhenReady = false
                            exoPlayer.seekTo(0)
                            exoPlayer.stop()
                        } else {
                            exoPlayer.playWhenReady = false
                            exoPlayer.seekTo(0)
                            exoPlayer.stop()
                        }
                    }
                    exoPlayer.playWhenReady = false
                    exoPlayer.seekTo(0)
                    exoPlayer.stop()
                }
            }
        }

    }

    override fun onViewDetachedFromWindow(holder: VideoViewHolder) {
        holder.apply {
            try {
                Log.d("videoUrl ", "onViewDetachedFromWindow: ${videoList[absoluteAdapterPosition].file}")

            val duration = exoPlayer.contentDuration

            val jsonObject = JSONObject()
            try {
                jsonObject.put("userId", sessionManager.getUserSelfID())
                jsonObject.put("time", duration)
                jsonObject.put("videoId", videoList[absoluteAdapterPosition].Id)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            mSocket.emit("scroll", jsonObject)
            Log.d("socket", "scrollSocket: videoId ${videoList[absoluteAdapterPosition].Id}")
            Log.d("socket", "scrollSocket: userId ${sessionManager.getUserSelfID()}")
            Log.d("socket", "scrollSocket: time ${duration}")
        } catch (e: Exception) {
            Log.e(TAG, "onScrolled: ", e)
        }
            playerViewExo.player!!.playWhenReady = false
            playerViewExo.player!!.seekTo(0)
            playerViewExo.player!!.pause()
            playerViewExo.player!!.stop()
            playerViewExo.player = null
        }
        super.onViewDetachedFromWindow(holder)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        val layoutManager = recyclerView.layoutManager
        if (layoutManager is LinearLayoutManager && itemCount > 0) {
            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)

                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val visiblePosition = layoutManager.findFirstCompletelyVisibleItemPosition()
                    var lastPosition = layoutManager.findLastCompletelyVisibleItemPosition()

                     lastPosition = Ar[0]
                    Ar[0] = Ar[1]
                    Ar[1] = visiblePosition

                    /* Log.i("positionTest","visible : $visiblePosition")
                     Log.i("positionTest","last : $lastPosition")*/



                    if (visiblePosition >= 0) {
                        val holder_current: VideoViewHolder =
                            recyclerView.findViewHolderForAdapterPosition(visiblePosition) as VideoViewHolder
                        holder_current.exoPlayer.seekTo(0)
//                        holder_current.exoPlayer.playWhenReady = false

                        if (holder_current.playPauseIc.isVisible) {
                            holder_current.exoPlayer.playWhenReady = false
                        } else {
                            Log.d("videoUrl ", "onScrolled: ${videoList[visiblePosition].file}")
                            holder_current.playerViewExo.player!!.prepare()
                            holder_current.playerViewExo.player!!.play()
                            val jsonObject = JSONObject()
                            try {
                                jsonObject.put("userId", sessionManager.getUserSelfID())
                                jsonObject.put("videoId", videoList[visiblePosition].Id)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Log.e(TAG, "viewSocket: ", e)
                            }

                            mSocket.emit("view", jsonObject)
                            Log.d("socket", "viewSocket: userId ${sessionManager.getUserSelfID()}")
                            Log.d("socket", "viewSocket: videoId ${videoList[visiblePosition].Id}")
                        }
                    }
//                    Log.d("socket", "lastPosition: $lastPosition")

                    if (lastPosition >= 0) {
                        try {
                            val holder_previous =  recyclerView.findViewHolderForAdapterPosition(lastPosition) as VideoViewHolder
                            holder_previous.exoPlayer.seekTo(0)
                            if (holder_previous.exoPlayer.isPlaying)
                                holder_previous.exoPlayer.stop()
                            holder_previous.exoPlayer.playWhenReady = false

                            val duration: Long = holder_previous.exoPlayer.contentDuration

                            val jsonObject = JSONObject()
                            try {
                                jsonObject.put("userId", sessionManager.getUserSelfID())
                                jsonObject.put("time", duration)
                                jsonObject.put("videoId", videoList[lastPosition].Id)
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                            mSocket.emit("scroll", jsonObject)
                           /* Log.d("socket", "scrollSocket: videoId ${videoList[visiblePosition].Id}")
                            Log.d("socket", "scrollSocket: userId ${sessionManager.getUserSelfID()}")
                            Log.d("socket", "scrollSocket: time ${duration}")*/
                        } catch (e: Exception) {
                            Log.e("TAG", "onScrolled: ", e)
                        }
                    }
                    if (visiblePosition >= 0) {
                        cPos = visiblePosition
//                        notifyDataSetChanged()
                    }
                }
            })
        }
    }

    override fun getItemCount(): Int {
        Log.d("videoListSize:", "getItemCount: ${videoList.size}")
        return videoList.size
    }

    private fun sendToUserSelfProfile() {
        if (contextFromActivity is CommanVideoPlayActivity) {
            val activity = contextFromActivity as CommanVideoPlayActivity
            val myFragment: Fragment = ProfileFragment(contextFromActivity)
            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.viewContainer, myFragment).addToBackStack(null).commit()
        } else {
            val activity = contextFromActivity as HomeActivity

            val myFragment: Fragment = ProfileFragment(contextFromActivity)
            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.viewContainer, myFragment).addToBackStack(null).commit()
        }
    }

    private fun sendToDuetCameraActivity(bundle: Bundle) {
        val intent = Intent(contextFromActivity.applicationContext, DuetCameraActivity::class.java)
        intent.putExtra(ConstValFile.Bundle, bundle)
        contextFromActivity.startActivity(intent)
    }

    private fun moredialog(i: Int) {
        val dialog1 = BottomSheetDialog(contextFromActivity, R.style.CustomBottomSheetDialogTheme)
        dialog1.setContentView(R.layout.more_dialog)
        val tv_watch_later = dialog1.findViewById<TextView>(R.id.tv_watch_later)
        val iv_share_not_intersested =
            dialog1.findViewById<LinearLayout>(R.id.iv_share_not_intersested)
        val iv_share_report = dialog1.findViewById<LinearLayout>(R.id.iv_share_report)
        val iv_share_duet = dialog1.findViewById<LinearLayout>(R.id.iv_share_duet)
        val iv_share_download = dialog1.findViewById<LinearLayout>(R.id.iv_share_download)
        val iv_share_watch_later = dialog1.findViewById<LinearLayout>(R.id.iv_share_watch_later)

        if ((videoList[i].userId.equals(sessionManager.getUserSelfID()))) {
            iv_share_report!!.visibility = View.GONE
            iv_share_not_intersested!!.visibility = View.GONE

        } else {
            iv_share_report!!.visibility = View.VISIBLE
            iv_share_not_intersested!!.visibility = View.VISIBLE
        }


        if (!(videoList[i].allowDuet!!)) {
            iv_share_duet!!.visibility = View.GONE
        } else {
            iv_share_duet!!.visibility = View.VISIBLE
        }

        //BoostPost
        val boostPost = dialog1.findViewById<LinearLayout>(R.id.boost_post)
        val boostText = dialog1.findViewById<TextView>(R.id.boostText)
        if (videoList[i].userId.equals(sessionManager.getUserSelfID())) {
            boostPost!!.visibility = View.VISIBLE
            if (videoList[i].isBoosted!!) {
                boostText!!.text = "Post Boosted"
            } else {
                boostText!!.text = "Boost Post"
            }
        } else {
            boostPost!!.visibility = View.GONE
        }

        boostPost.setOnClickListener(View.OnClickListener {
            if (myApplication.isNetworkConnected()){
                if (!sessionManager.isUserLoggedIn()) {
                    val intent = Intent(contextFromActivity.applicationContext, LoginActivity::class.java)
                    contextFromActivity.startActivity(intent)
                    dialog1.dismiss()
                } else {
                    if (videoList[i].isBoosted!!) {
                        showToast("You Already Boost This Video..")
                    } else {
                        val intent = Intent(
                            contextFromActivity.applicationContext,
                            BoostPostActivity::class.java
                        )
                        intent.putExtra("videoID", videoList[i].Id)
                        dialog1.dismiss()
                        contextFromActivity.startActivity(intent)
                    }
                }
            }else{
                showToast(ConstValFile.Check_Connection)
            }
        })

        //
        if (videoList[i].isSaved!!) {
            tv_watch_later!!.text = ConstValFile.RemoveFromWatch
        } else {
            tv_watch_later!!.text = ConstValFile.WatchLater
        }

        iv_share_not_intersested!!.setOnClickListener { view: View? ->

            if (myApplication.isNetworkConnected()){
                if (!sessionManager.isUserLoggedIn()) {
                    val intent = Intent(contextFromActivity.applicationContext, LoginActivity::class.java)
                    contextFromActivity.startActivity(intent)
                } else {
                    val manager: FragmentManager = (contextFromActivity as AppCompatActivity).supportFragmentManager
                    val showReportDialog = VideoReportDialog(contextFromActivity, videoList[i].Id.toString(), ConstValFile.NotInterestedDialogView,this@VideoAdapter,i)
                    showReportDialog.show(manager, showReportDialog.tag)
                    dialog1.dismiss()
                }
            }else{
                showToast(ConstValFile.Check_Connection)
            }
        }

        iv_share_download!!.setOnClickListener {
            if (myApplication.isNetworkConnected()){
                val shareBody: String = videoList[i].file.toString()
                Log.e("check1452", " $shareBody")
                savevideotointernalmemory(shareBody)
                dialog1.dismiss()
            }else{
                showToast(ConstValFile.Check_Connection)
            }
        }

        iv_share_duet.setOnClickListener {
            if (myApplication.isNetworkConnected()){
                if (!sessionManager.isUserLoggedIn()) {
                    dialog1.dismiss()
                    val intent = Intent(contextFromActivity.applicationContext, LoginActivity::class.java)
                    contextFromActivity.startActivity(intent)
                } else {
                    val bundle = Bundle()
                    bundle.putString(ConstValFile.DuetVideoUrl, videoList[i].file)
                    bundle.putString(ConstValFile.AuditionID, videoList[i].auditionId)
                    sendToDuetCameraActivity(bundle)
                    dialog1.dismiss()
                }
            }else{
                showToast(ConstValFile.Check_Connection)
            }
        }

        iv_share_watch_later!!.setOnClickListener {
            if (myApplication.isNetworkConnected()){
                if (!sessionManager.isUserLoggedIn()) {
                    val intent =
                        Intent(contextFromActivity.applicationContext, LoginActivity::class.java)
                    contextFromActivity.startActivity(intent)
                } else {
                    if (!(videoList[i].isSaved)!!) {
                        tv_watch_later.text = ConstValFile.RemoveFromWatch
                        videoList[i].isSaved = true
                        notifyDataSetChanged()
                        watchLater(videoList[i].Id.toString())
                    } else {
                        tv_watch_later.text = ConstValFile.WatchLater
                        videoList[i].isSaved = false
                        notifyDataSetChanged()
                        RemoveWatchLater(videoList[i].Id.toString())
                    }
                }
                dialog1.dismiss()
            }else{
                showToast(ConstValFile.Check_Connection)
            }

        }

        iv_share_report.setOnClickListener {
            if (myApplication.isNetworkConnected()){
                if (!sessionManager.isUserLoggedIn()) {
                    val intent =
                        Intent(contextFromActivity.applicationContext, LoginActivity::class.java)
                    contextFromActivity.startActivity(intent)
                } else {
//                videoReportDialog2(videoList[i].Id.toString(),ConstValFile.ReportDialogView)

                    val manager: FragmentManager =
                        (contextFromActivity as AppCompatActivity).supportFragmentManager

                    val showReportDialog = VideoReportDialog(contextFromActivity, videoList[i].Id.toString(), ConstValFile.ReportDialogView,this@VideoAdapter,i)
                    showReportDialog.show(manager, showReportDialog.tag)
                }
                dialog1.dismiss()
            }else{
                showToast(ConstValFile.Check_Connection)
            }
        }

        dialog1.show()

    }

    private fun RemoveWatchLater(id: String) {
        val removeWatchLaterReq = apiInterface.remove_watch_later(sessionManager.getToken(), id)

        removeWatchLaterReq.enqueue(object : Callback<CommanResponse> {
            override fun onResponse(call: Call<CommanResponse>, response: Response<CommanResponse>) {
                if (response.isSuccessful && response.body()!!.success!!) {
                    Log.d(TAG, "onResponse: ${response.body()!!.message}")
                } else {
                    Log.d(TAG, "onResponse: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<CommanResponse>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    private fun watchLater(id: String) {
        val watchLaterReq = apiInterface.saveIntoWatchLater(sessionManager.getToken(), id)

        watchLaterReq.enqueue(object : Callback<CommanResponse> {
            override fun onResponse(call: Call<CommanResponse>, response: Response<CommanResponse>) {
                if (response.isSuccessful && response.body()!!.success!!) {
                    Log.d(TAG, "onResponse: ${response.body()!!.message}")
                } else {
                    Log.d(TAG, "onResponse: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<CommanResponse>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    private fun shareDialog(i: Int, videoUrl: String) {
        val dialog1 = BottomSheetDialog(contextFromActivity, R.style.CustomBottomSheetDialogTheme)
        dialog1.setContentView(R.layout.share_dailog)
        val iv_share_chat = dialog1.findViewById<ImageView>(R.id.iv_share_chat)
        val iv_share_whatsapp = dialog1.findViewById<ImageView>(R.id.iv_share_whatsapp)
        val iv_share_facebook = dialog1.findViewById<ImageView>(R.id.iv_share_facebook)
        val iv_share_instagram = dialog1.findViewById<ImageView>(R.id.iv_share_instagram)
        val iv_share_link = dialog1.findViewById<ImageView>(R.id.iv_share_link)
        val iv_share_snapchat = dialog1.findViewById<ImageView>(R.id.iv_share_snapchat)
        val iv_share_twitter = dialog1.findViewById<ImageView>(R.id.iv_share_twitter)
        val iv_share_more = dialog1.findViewById<ImageView>(R.id.iv_share_more)

        iv_share_chat!!.setOnClickListener {
            val shareBody: String = videoList[i].file.toString()
            val intent = Intent(contextFromActivity.applicationContext, MessageActivity::class.java)
                .putExtra("url204", shareBody + " userid " + videoList[i].userId.toString())
                .putExtra("name", videoList[i].auditionId)
                .putExtra("userid", videoList[i].userId.toString())
                .putExtra("image", videoList[i].image)
                .putExtra("auditionID", videoList[i].auditionId)
            contextFromActivity.startActivity(intent)
        }

        iv_share_whatsapp!!.setOnClickListener { v: View? ->
            val intent = Intent(Intent.ACTION_SEND)
            val shareBody: String = videoUrl
            intent.type = "text/plain"
            intent.setPackage("com.whatsapp")
            intent.putExtra(Intent.EXTRA_TEXT, shareBody)
            contextFromActivity.startActivity(Intent.createChooser(intent, "Biggee"))
            dialog1.dismiss()
        }

        iv_share_facebook!!.setOnClickListener { v: View? ->
            val intent = Intent(Intent.ACTION_SEND)
            val shareBody: String = videoUrl
            intent.type = "text/plain"
            intent.setPackage("com.facebook.katana")
            intent.putExtra(Intent.EXTRA_TEXT, shareBody)
            contextFromActivity.startActivity(Intent.createChooser(intent, "Biggee"))
            dialog1.dismiss()
        }

        iv_share_instagram!!.setOnClickListener { v: View? ->
            val intent = Intent(Intent.ACTION_SEND)
            val shareBody: String = videoUrl
            intent.type = "text/plain"
            intent.setPackage("com.instagram.android")
            intent.putExtra(Intent.EXTRA_TEXT, shareBody)
            contextFromActivity.startActivity(Intent.createChooser(intent, "Biggee"))
            dialog1.dismiss()
        }

        iv_share_link!!.setOnClickListener { v: View? ->
            Toast.makeText(contextFromActivity, "Copied..", Toast.LENGTH_LONG).show()
            val clipboard =
                contextFromActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val shareBody: String = videoUrl
            val check = ClipData.newPlainText("label", shareBody)
            clipboard.setPrimaryClip(check)
            dialog1.dismiss()
        }

        iv_share_snapchat!!.setOnClickListener { v: View? ->
            val intent = Intent(Intent.ACTION_SEND)
            val shareBody: String = videoUrl
            intent.type = "text/plain"
            intent.setPackage("com.snapchat.android")
            intent.putExtra(Intent.EXTRA_TEXT, shareBody)
            contextFromActivity.startActivity(Intent.createChooser(intent, "Biggee"))
            dialog1.dismiss()
        }

        iv_share_twitter!!.setOnClickListener { v: View? ->
            val intent = Intent(Intent.ACTION_SEND)
            val shareBody: String = videoUrl
            intent.type = "text/plain"
            intent.setPackage("com.twitter.android")
            intent.putExtra(Intent.EXTRA_TEXT, shareBody)
            contextFromActivity.startActivity(Intent.createChooser(intent, "Biggee"))
            dialog1.dismiss()
        }

        iv_share_more!!.setOnClickListener { v: View? ->
            val intent = Intent(Intent.ACTION_SEND)
            val shareBody: String = videoUrl
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, shareBody)
            contextFromActivity.startActivity(Intent.createChooser(intent, "Biggee"))
            dialog1.dismiss()
        }
        dialog1.show()
    }

    private fun sendToVideoUserProfile(bundle: Bundle) {
        val intent = Intent(contextFromActivity, OtherUserProfileActivity::class.java)
        intent.putExtra(ConstValFile.Bundle, bundle)
        contextFromActivity.startActivity(intent)
    }


    private fun showToast(msg: String) {
        Toast.makeText(contextFromActivity.applicationContext, msg, Toast.LENGTH_SHORT).show()
    }


    fun sendToLoginScreen() {
        val intent = Intent(contextFromActivity, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        contextFromActivity.startActivity(intent)
    }

    fun sendHashTagVideo(hashTag: String) {
        val bundle = Bundle()
        bundle.putString(ConstValFile.VideoHashTag, hashTag)
        sessionManager.setVideoHashTag(hashTag)
        val intent = Intent(contextFromActivity, HashtagVideoActivity::class.java)
        intent.putExtra(ConstValFile.Bundle, bundle)
        contextFromActivity.startActivity(intent)
    }

    private fun likeVideo(id: String?, status: String) {
        val liekeReq = apiInterface.viedolike(sessionManager.getToken(), id, status)
        liekeReq.enqueue(object : Callback<LikeResponse> {
            override fun onResponse(call: Call<LikeResponse>, response: Response<LikeResponse>) {
                if (response.isSuccessful && response.body()!!.success!!) {
                    Log.d(TAG, "onResponse: ${response.body()!!.message}")
                } else {
                    Log.d(TAG, "onResponse: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<LikeResponse>, t: Throwable) {
                t.printStackTrace()
            }

        })
    }

    private fun followUserApi(userId: String?, status: String) {
        val ffReq = apiInterface.followFollowing(sessionManager.getToken(), userId, status)
        ffReq.enqueue(object : Callback<FollowFollowingResponse> {
            override fun onResponse(
                call: Call<FollowFollowingResponse>,
                response: Response<FollowFollowingResponse>
            ) {
                if (response.isSuccessful && response.body()!!.success!!) {
                    Log.d(TAG, "onResponse: ${response.body()!!.message}")
                } else {
                    Log.d(TAG, "onResponse: ${response.message()}")

                }
            }

            override fun onFailure(call: Call<FollowFollowingResponse>, t: Throwable) {
                t.printStackTrace()

            }
        })
    }

    private fun savevideotointernalmemory(url: String) {
        val file = File(File(Environment.getExternalStorageDirectory(), "Biggee"), "Biggee")
        val timestamp = System.currentTimeMillis()
        val request = DownloadManager.Request(Uri.parse(url))
        request.allowScanningByMediaScanner()
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            file.absolutePath + "/" + timestamp.toString() + ".mp4"
        )
        val manager =
            contextFromActivity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
        Toast.makeText(contextFromActivity, "Video Download started", Toast.LENGTH_SHORT).show()
    }

    private fun showCommentDialog(bundle: Bundle) {
      /*  val showCommentDialog = CommentBottomSheet()
        showCommentDialog.arguments = bundle
        showCommentDialog.show((contextFromActivity as AppCompatActivity).supportFragmentManager, showCommentDialog.tag)*/
    }


    private fun showVoteDialog(id: String?, position: Int) {
        val voteDialog = Dialog(contextFromActivity)
        voteDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        voteDialog.setContentView(R.layout.vote_dialog_sheet)

        val voteCycle = voteDialog.findViewById<RecyclerView>(R.id.voteCycle)
        voteDialog.window!!.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        voteDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        voteDialog.window!!.setGravity(Gravity.END)


        val voteCatReq = apiInterface.getVoteCategory(sessionManager.getToken())

        voteCatReq.enqueue(object : Callback<VoteDataResponse> {
            override fun onResponse(
                call: Call<VoteDataResponse>,
                response: Response<VoteDataResponse>
            ) {
                if (response.isSuccessful && response.body()!!.success!!) {
                    val data = response.body()!!.data
                    voteCycle.adapter = VoteAdapter(contextFromActivity, data, id!!, voteDialog,this@VideoAdapter,position)
                    /*if (!(videoList[position].voteStatus!!)) {
                        for (uList in videoList) {
                            if (uList.userId.equals(videoList[position].userId)) {
                                uList.voteStatus = true
                            }
                        }
                    }*/
                } else {
                    Log.e(TAG, "onResponse: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<VoteDataResponse>, t: Throwable) {
                t.printStackTrace()
            }
        })

        voteDialog.show()
    }

    private fun sendSongVideoActivity(songId: String, songUrl: String) {
        val bundle = Bundle()
        bundle.putString(ConstValFile.SongID, songId)
        bundle.putString(ConstValFile.SongUrl, songUrl)
        val intent = Intent(contextFromActivity, TryAudioActivity::class.java)
        intent.putExtra(ConstValFile.Bundle, bundle)
        contextFromActivity.startActivity(intent)
    }

    private fun sendPostLocationVideoActivity(postLocation: String) {
        val bundle = Bundle()
        bundle.putString(ConstValFile.PostLocation, postLocation)
        val intent = Intent(contextFromActivity, PostLocationVideoActivity::class.java)
        intent.putExtra(ConstValFile.Bundle, bundle)
        contextFromActivity.startActivity(intent)
    }

    private fun blockUnblockUser(status: String,userID:String) {
        val blockUnblockReq = apiInterface.blockUnblockUser(sessionManager.getToken(), userID, status)
        blockUnblockReq.enqueue(object : Callback<CommanResponse> {
            override fun onResponse(call: Call<CommanResponse>, response: Response<CommanResponse>) {
                if (response.isSuccessful && response.body()?.success!!) {
                    showToast("Successfully Unblock..")
                } else {
                    Log.e("BlockedUser", "onResponse: ${response.message()}")
                }
            }
            override fun onFailure(call: Call<CommanResponse>, t: Throwable) {
                Log.e("BlockedUser", "onFailure: $t")
            }
        })
    }


    private fun showCommentDialog2(
        postId: String?,
        auditionId: String,
        userId: String,
        videoID: String?,
        userImage: String,
        position: Int
    ) {
        val dialog = BottomSheetDialog(contextFromActivity, R.style.CustomBottomSheetDialogTheme)
        dialog.setContentView(R.layout.comment_bottom_sheet)

        val sendCommentBtn = dialog.findViewById<ImageView>(R.id.sendCommentBtn)
        val commentET = dialog.findViewById<EditText>(R.id.commentET)
        val userImageView = dialog.findViewById<ImageView>(R.id.userImage)
        val commentCycle = dialog.findViewById<RecyclerView>(R.id.commentCycle)
        val noCommentView = dialog.findViewById<TextView>(R.id.noCommentView)

        Glide.with(contextFromActivity).load(userImage).placeholder(R.drawable.person_ic).into(userImageView!!)

        sendCommentBtn!!.setOnClickListener {
            if (myApplication.isNetworkConnected())
            {
                if ((!sessionManager.isUserLoggedIn())){
                    sendToLoginScreen()
                }else{
                    val commentText = commentET!!.text.toString().trim()
                    if (commentText.isNotEmpty()){
                        commentET.text.clear()
                        videoList[position].commentCount = videoList[position].commentCount?.plus(1)
                        notifyItemChanged(position)
                        writeNewComment(auditionId, userId,postId,videoID,userImage,commentText,noCommentView!!,commentCycle!!)
                    }else{
                        showToast("Please write something..")
                    }
                }
            }else{
                showToast(ConstValFile.Check_Connection)
            }
        }

        getAllComments(postID = postId,noCommentView!!,commentCycle!!)
        dialog.show()
    }

    private fun getAllComments(postID: String?,noCommentView:TextView,commentCycle:RecyclerView) {
        commentList.clear()
        firestore.collection(ConstValFile.FirebaseCommentDB).whereEqualTo("post_id",postID)
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
                    commentList.add(cData)
                }

                if (commentList.size>0){
                    noCommentView.visibility = View.GONE
                    commentCycle.visibility = View.VISIBLE
                    commentAdapter = CommentAdapter(contextFromActivity, commentList, this)
                    commentCycle.adapter = commentAdapter
                }else{
                    noCommentView.visibility = View.VISIBLE
                    commentCycle.visibility = View.GONE
                }
            }
    }

    private fun writeNewComment(auditionID: String?, userID: String?,postID: String?,videoID: String?,
                                userImage: String?,commentText: String?,noCommentView:TextView,commentCycle:RecyclerView)
    {

        noCommentView.visibility = View.GONE
        commentCycle.visibility = View.VISIBLE
        val commentData =  JSONObject()
        commentData.put("video_id",videoID)
        commentData.put("user_id",sessionManager.getUserSelfID())
        commentData.put("comment",commentText)
        mSocket.emit("post-comment",commentData)

        Log.d(TAG, "writeNewComment : video_id: ${videoID}")
        Log.d(TAG, "writeNewComment : user_id: ${sessionManager.getUserSelfID()}")
        Log.d(TAG, "writeNewComment : comment: ${commentText}")



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
        mapData.put("user_id", sessionManager.getUserSelfID().toString())
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

        commentList.add(cData)
        commentAdapter = CommentAdapter(contextFromActivity,commentList,this)
        commentCycle.adapter = commentAdapter
        commentCycle.scrollToPosition(commentList.lastIndex)

        firestore.collection(ConstValFile.FirebaseCommentDB)
            .add(mapData)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "writeNewComment: ${documentReference.id}")
            }.addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    fun deleteComment(commentID : String,commentPosition:Int){

        val sweetAlertDialog = SweetAlertDialog(contextFromActivity, SweetAlertDialog.WARNING_TYPE)
        sweetAlertDialog.titleText = "Delete Comment"
        sweetAlertDialog.contentText = "Do you want Delete this Comment"
        sweetAlertDialog.confirmText = "Yes"
        sweetAlertDialog.setConfirmClickListener {
            sweetAlertDialog.dismiss()
            Log.i("DocumentId", "Document id : ${commentID}")
            val query = firestore.collection(ConstValFile.FirebaseCommentDB)
                .whereEqualTo("comment_id", commentID)
            query.get().addOnSuccessListener {
                for (document in it.documents) {
                    document.reference.delete()
                        .addOnSuccessListener {
                            videoList[videoPosition].commentCount = videoList[videoPosition].commentCount?.minus(1)
                            notifyItemChanged(videoPosition)
                            commentList.removeAt(commentPosition)
                            commentAdapter?.notifyDataSetChanged()
                        }
                        .addOnFailureListener {
                            Log.e("deleteCom", "onBindViewHolder: $it")
                        }
                }
            }
        }
        sweetAlertDialog.cancelText = "No"
        sweetAlertDialog.setCancelClickListener {
            sweetAlertDialog.dismiss()
        }
        sweetAlertDialog.show()
    }

    private fun formatCount(count: Int): String {
        val suffix = charArrayOf(' ', 'k', 'M', 'B', 'T', 'P', 'E')
        val numValue: Long = count.toLong()
        val value = Math.floor(Math.log10(numValue.toDouble())).toInt()
        val base = value / 3
        return if (value >= 3 && base < suffix.size) {
            DecimalFormat("#0.0").format(
                numValue / Math.pow(
                    10.0,
                    (base * 3).toDouble()
                )
            ) + suffix[base]
        } else {
            DecimalFormat("#,##0").format(numValue)
        }
        /*return when {
            count < 1000 -> count.toString()
            count < 10000 -> String.format("%.1fk", Math.floor(count / 100.0) / 10)
            else -> (count / 1000).toString() + "k"
        }*/
    }

}



