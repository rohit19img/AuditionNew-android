package com.img.audition.adapters


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
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlaybackException.TYPE_SOURCE
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.img.audition.R
import com.img.audition.dataModel.*
import com.img.audition.databinding.VideoLayoutBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.screens.*
import com.img.audition.screens.fragment.CommentBottomSheet
import com.img.audition.screens.fragment.ProfileFragment
import com.img.audition.screens.fragment.VideoReportDialog
import com.img.audition.videoWork.VideoCacheWork
import com.img.audition.videoWork.VideoItemPlayPause
import io.socket.client.Socket
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File


class VideoAdapter(val contextFromActivity:Context, val videoList: ArrayList<VideoData>) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {
    val TAG = "VideoAdapter"

    var Ar = arrayOf(-1,0)
    val  sessionManager = SessionManager(contextFromActivity.applicationContext)
    val apiInterface = RetrofitClient.getInstance().create(ApiInterface::class.java)
    var firestore = FirebaseFirestore.getInstance()
    lateinit var commentList : ArrayList<CommentData>

    private val myApplication by lazy {
        MyApplication(contextFromActivity.applicationContext)
    }
    var cPos = 0

    var mSocket: Socket = VideoCacheWork.mSocket!!

    inner class VideoViewHolder(itemView: VideoLayoutBinding) : RecyclerView.ViewHolder(itemView.root) {
        val playerViewExo = itemView.videoExoView
        val likeBtn = itemView.likeButton
        val likeCount = itemView.likeCount
        val followBtn = itemView.followButton
        val userProfile = itemView.userImage
        val viewVidUserProBtn = itemView.viewVidUserProBtn
        val voteBtn = itemView.voteButton
        val userName = itemView.userName
        val caption = itemView.videoCaption
        val audioName = itemView.audioName
        val audioImage = itemView.audioImage
        val shareCount = itemView.shareCount
        val shareBtn = itemView.shareButton
        val moreBtn = itemView.moreButton
        val commentBtn = itemView.commentButton
        val commentCount = itemView.commentCount
        val playPauseVolumeBtn = itemView.playPauseVolume
        val playPauseIc = itemView.videoPlayPause
        val volumeOnOffIc = itemView.volumeOnOff


        //Video Cache
        val mediaSource = ProgressiveMediaSource.Factory(
        CacheDataSource.Factory()
        .setCache(VideoCacheWork.simpleCache)
        .setUpstreamDataSourceFactory(
        DefaultHttpDataSource.Factory()
        .setUserAgent("ExoPlayer"))
        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR))
        var exoPlayer = ExoPlayer.Builder(contextFromActivity.applicationContext).build()
        //
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VideoViewHolder {
        val itemBinding = VideoLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VideoViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {

        val list = videoList[position]
        holder.apply {
            //vote
            if (!(list.userId.equals(sessionManager.getUserSelfID()))){
                if (list.voteStatus!=null)
                {
                    if(list.voteStatus!!){
                        voteBtn.visibility = View.VISIBLE
                    }else{
                        voteBtn.visibility = View.GONE
                    }
                }
                voteBtn.setOnClickListener {
                    if (!(sessionManager.isUserLoggedIn())){
                        sendToLoginScreen()
                    }else{
                        showVoteDialog(list.Id)
                    }
                }
            }

            //commentCount
            firestore.collection(ConstValFile.FirebaseCommentDB).whereEqualTo("post_id", list.postId)
                .get()
                .addOnSuccessListener { documentSnapshots ->
                    commentList = java.util.ArrayList<CommentData>()
                    for (documentSnapshot1 in documentSnapshots) {
                        val note: CommentData =
                            documentSnapshot1.toObject(CommentData::class.java)
                        commentList.add(note)
                    }
                    myApplication.printLogD("commentCount "+commentList.size.toString(),TAG)
                    commentCount.text = commentList.size.toString()
                }
            //

            myApplication.printLogD("onBindViewHolder: ${list.file}","videoUrl")

            if (list.likeStatus!!){
                likeBtn.setImageDrawable(ContextCompat.getDrawable(contextFromActivity, R.drawable.liked_ic))
                likeBtn.setColorFilter(contextFromActivity.resources.getColor(R.color.likeHeartRed))
            }else{
                likeBtn.setImageDrawable(ContextCompat.getDrawable(contextFromActivity, R.drawable.like_ic))
                likeBtn.setColorFilter(contextFromActivity.resources.getColor(R.color.white))
            }
            if (list.followStatus!!){
                followBtn.text = ConstValFile.Following
                followBtn.setTypeface(followBtn.typeface, Typeface.ITALIC)
            }else{
                followBtn.text = ConstValFile.Follow
                followBtn.setTypeface(followBtn.typeface, Typeface.NORMAL)
            }
            if (list.caption.toString().isNotEmpty()){
                caption.visibility = View.VISIBLE
                caption.text = list.caption
            }else{
                caption.visibility = View.GONE
            }

            likeBtn.setOnClickListener {
                if (list.likeStatus!!){
                    likeBtn.isSelected = false
                    likeBtn.setImageDrawable(ContextCompat.getDrawable(contextFromActivity, R.drawable.like_ic))
                    likeBtn.setColorFilter(contextFromActivity.resources.getColor(R.color.bgColorWhite))
                    list.likeStatus = false
                    likeVideo(list.Id,"unlike")
                    val newlikeCount = list.likeCount?.minus(1)
                    if (newlikeCount != null && newlikeCount>1) {
                        list.likeCount = newlikeCount
                    }else{
                        list.likeCount = 0
                    }
                    likeCount.text = newlikeCount.toString()
                }else{
                    likeBtn.setImageDrawable(ContextCompat.getDrawable(contextFromActivity, R.drawable.liked_ic))
                    likeBtn.setColorFilter(contextFromActivity.resources.getColor(R.color.likeHeartRed))
                    likeBtn.isSelected = true
                    likeVideo(list.Id,"liked")
                    list.likeStatus = true
                    val newlikeCount = list.likeCount?.plus(1)
                    list.likeCount = newlikeCount
                    likeCount.text = newlikeCount.toString()

                }
            }

            followBtn.setOnClickListener {
                if (!(sessionManager.isUserLoggedIn())){
                    showToast(ConstValFile.LoginMsg)
                    sendToLoginScreen()
                }else{
                    if (!(list.followStatus!!)){
                        for (uList in videoList) {
                            if (uList.userId.equals(videoList[position].userId)) {
                                uList.followStatus = true
                            }
                        }
                        list.followStatus = true
                        notifyDataSetChanged()
                        followUserApi(list.userId,"followed")
                        followBtn.text = ConstValFile.Following
                        followBtn.setTypeface(followBtn.typeface, Typeface.ITALIC)
                    }
                }
            }

            viewVidUserProBtn.setOnClickListener{
                if (!(list.isSelf)!!){

                    val bundle = Bundle()
                    bundle.putString(ConstValFile.USER_IDFORIntent,list.userId)
                    bundle.putInt(ConstValFile.UserPositionInList,position)
                    bundle.putSerializable("list", videoList)
                    bundle.putBoolean(ConstValFile.UserFollowStatus, list.followStatus!!)
                    sendToVideoUserProfile(bundle)
                }else{
                    sendToUserSelfProfile()
                }
            }

            shareBtn.setOnClickListener {
                shareDialog(position)
            }

            moreBtn.setOnClickListener {
                moredialog(position)
            }


            likeCount.text = list.likeCount.toString()
            shareCount.text = list.shares.toString()
            Glide.with(contextFromActivity).load(list.image).placeholder(R.drawable.person_ic).into(userProfile)
            userName.text = list.auditionId
            audioName.text = list.auditionId +" - Original Audio"

            commentBtn.setOnClickListener {
                val bundle = Bundle()
                bundle.putString(ConstValFile.AuditionID,list.auditionId)
                bundle.putString(ConstValFile.VideoID,list.auditionId)
                bundle.putSerializable(ConstValFile.CommentList, list.comment)
                bundle.putString(ConstValFile.AllUserID, list.userId)
                bundle.putString(ConstValFile.PostID, list.postId)
                bundle.putString(ConstValFile.VideoID, list.vId)
                if (!(sessionManager.getUserProfileImage().isNullOrEmpty())){
                    bundle.putString(ConstValFile.UserImage,sessionManager.getUserProfileImage())
                }else{
                    bundle.putString(ConstValFile.UserImage,"")
                }
                bundle.putInt(ConstValFile.UserPositionInList, position)
                bundle.putSerializable("list", videoList)
                showCommentDialog(bundle)
            }

            audioName.isSelected = true
            holder.audioName.movementMethod = ScrollingMovementMethod()

        }

    }

    override fun onViewAttachedToWindow(holder: VideoViewHolder) {

        val list = videoList[holder.position]
        holder.apply {



            val mediaItem = MediaItem.fromUri(list.file.toString())
            val videoMediaSource = mediaSource.createMediaSource(mediaItem)
            playerViewExo.player = exoPlayer
            exoPlayer.setMediaSource(videoMediaSource)
            exoPlayer.prepare()

            if (cPos>=0){

                if (cPos == position){
                    myApplication.printLogD("onViewAttachedToWindow: Posotion ${cPos}","check 100")
                    myApplication.printLogD("onViewAttachedToWindow: Url ${videoList[cPos].file}","check 100")
                    exoPlayer.seekTo(0)

                    exoPlayer.playWhenReady = true
                }else{
                    exoPlayer.seekTo(0)
                    exoPlayer.playWhenReady = false
                }
            }

            val jsonObject = JSONObject()
            try {
                jsonObject.put("userId", sessionManager.getUserSelfID())
                jsonObject.put("videoId", list.Id)
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            mSocket.emit("view", jsonObject)


            playPauseVolumeBtn.setOnClickListener {
                playPauseIc.visibility = View.GONE
                if (exoPlayer.isPlaying){
                    exoPlayer.pause()
                    playPauseIc.visibility = View.VISIBLE
                    playPauseIc.setImageDrawable(ContextCompat.getDrawable(contextFromActivity, R.drawable.play_ic))

                }else{
                    exoPlayer.prepare()
                    exoPlayer.play()
                    playPauseIc.visibility = View.GONE
                }
            }

            audioImage.setOnClickListener {
                if (exoPlayer.volume == 0F){
                    exoPlayer.volume = 1F
                    audioImage.setImageDrawable(ContextCompat.getDrawable(contextFromActivity, R.drawable.volume_on_ic))
                }else{
                    exoPlayer.volume = 0F
                    audioImage.setImageDrawable(ContextCompat.getDrawable(contextFromActivity, R.drawable.volume_off_ic))
                }
            }
            exoPlayer.addListener(object : Player.Listener{
                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    when (playbackState) {
                        ExoPlayer.STATE_ENDED -> {
                            exoPlayer.seekTo(0)
                            exoPlayer.prepare()
                            exoPlayer.play()
                        }
                        ExoPlayer.STATE_BUFFERING ->{
                            myApplication.printLogD("STATE_BUFFERING",TAG)
                        }
                        ExoPlayer.STATE_READY ->{
                            myApplication.printLogD("STATE_READY",TAG)
                        }
                        else -> {
                            myApplication.printLogD(playbackState.toString(),"currentState")
                        }
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    when(error.errorCode){
                        TYPE_SOURCE ->{
                            myApplication.showToast("Source Error..")
                            val list = videoList[position]
                            val mediaItem = MediaItem.fromUri(list.file.toString())
                            val videoMediaSource = mediaSource.createMediaSource(mediaItem)
                            playerViewExo.player = exoPlayer
                            exoPlayer.setMediaSource(videoMediaSource)
                            exoPlayer.prepare()
                            exoPlayer.play()
                        }

                    }
                }

            })

        }
        try {

        }catch (e:java.lang.Exception){
            myApplication.printLogE(e.toString(),TAG)
        }
        super.onViewAttachedToWindow(holder)
    }

    fun onActivityStateChanged() : VideoItemPlayPause{
        return object : VideoItemPlayPause{
            override fun onPause(holder: VideoAdapter.VideoViewHolder, cPos:Int) {
                myApplication.printLogD("onPause: Position ${cPos}",TAG)

                holder.apply {
                    if (cPos>=0){
                        if (cPos == position){
                            exoPlayer.pause()
                            exoPlayer.playWhenReady = false
                        }else{
                            exoPlayer.pause()
                            exoPlayer.playWhenReady = false
                        }
                    }
                    exoPlayer.playWhenReady = false
                    exoPlayer.pause()
                }
            }

            override fun onResume(holder: VideoViewHolder,cPos:Int) {
                myApplication.printLogD("onResume: Position ${cPos}",TAG)

                holder.apply {
                    if (cPos>=0){
                        if (cPos == position){
                            exoPlayer.playWhenReady = true
                        }else{
                            exoPlayer.seekTo(0)
                            exoPlayer.playWhenReady = false
                            exoPlayer.stop()
                        }
                    }

                }
            }

            override fun onRestart(holder: VideoViewHolder,cPos:Int) {}

            override fun onStop(holder: VideoViewHolder,cPos:Int) {
                myApplication.printLogD("onStop: Position ${cPos}",TAG)

                holder.apply {
                    if (cPos>=0){
                        if (cPos == position){
                            exoPlayer.playWhenReady = false
                            exoPlayer.seekTo(0)
                            exoPlayer.stop()

                        }else{
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
            playerViewExo.player!!.playWhenReady = false
            playerViewExo.player!!.seekTo(0)
            playerViewExo.player!!.pause()
            playerViewExo.player!!.stop()

        }
        super.onViewDetachedFromWindow(holder)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        val layoutManager = recyclerView.layoutManager
        if (layoutManager is LinearLayoutManager && itemCount > 0){
            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)


                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val visiblePosition = layoutManager.findFirstCompletelyVisibleItemPosition()
//                    var lastPosition = layoutManager.findLastCompletelyVisibleItemPosition()


                    var lastPosition = Ar[0]
                    Ar[0] = Ar[1]
                    Ar[1] = visiblePosition

                    /* Log.i("positionTest","visible : $visiblePosition")
                     Log.i("positionTest","last : $lastPosition")*/



                    if (visiblePosition >= 0) {
                        val holder_current: VideoViewHolder =
                            recyclerView.findViewHolderForAdapterPosition(visiblePosition) as VideoViewHolder
                        holder_current.exoPlayer.seekTo(0)
//                        holder_current.exoPlayer.playWhenReady = true

                        if(holder_current.playPauseIc.isVisible){
                            holder_current.exoPlayer.playWhenReady = false
                            myApplication.printLogD("video pause","isPlaying")
                        }else{
                            holder_current.exoPlayer.playWhenReady = true
                            myApplication.printLogD("video play","isPlaying")
                        }
                    }

                    if (lastPosition >= 0) {
                        try {
                            val holder_previous: VideoViewHolder =
                                recyclerView.findViewHolderForAdapterPosition(lastPosition) as VideoViewHolder
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

                            Log.i("SocketCheck", "Scroll : $jsonObject")

                            mSocket.emit("scroll", jsonObject)
                        }catch (e:Exception){
                            myApplication.printLogE(e.toString(),TAG)
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
        myApplication.printLogD(videoList.size.toString(),"Video List Size")
        return videoList.size
    }

    private fun sendToUserSelfProfile() {
        if (contextFromActivity is CommanVideoPlayActivity){
            val activity = contextFromActivity
            val myFragment: Fragment = ProfileFragment(contextFromActivity)
            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.viewContainer, myFragment).addToBackStack(null).commit()
        }else{
            val activity = contextFromActivity as HomeActivity
            val myFragment: Fragment = ProfileFragment(contextFromActivity)
            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.viewContainer, myFragment).addToBackStack(null).commit()
        }

    }

    private fun moredialog(i: Int) {
        val dialog1 = BottomSheetDialog(contextFromActivity,R.style.CustomBottomSheetDialogTheme)
        dialog1.setContentView(R.layout.more_dialog)
        val tv_watch_later = dialog1.findViewById<TextView>(R.id.tv_watch_later)
        val iv_share_not_intersested = dialog1.findViewById<ImageView>(R.id.iv_share_not_intersested)
        val iv_share_report = dialog1.findViewById<ImageView>(R.id.iv_share_report)
        val iv_share_duet = dialog1.findViewById<ImageView>(R.id.iv_share_duet)
        val iv_share_download = dialog1.findViewById<LinearLayout>(R.id.iv_share_download)
        val iv_share_watch_later = dialog1.findViewById<LinearLayout>(R.id.iv_share_watch_later)

        //BoostPost
        val boostPost = dialog1.findViewById<LinearLayout>(R.id.boost_post)
        val boostText = dialog1.findViewById<TextView>(R.id.boostText)
        if (videoList[i].userId.equals(sessionManager.getUserSelfID())) {
            boostPost!!.visibility = View.VISIBLE
            if (videoList[i].isBoosted!!){
                boostText!!.text = "Post Boosted"
            }else{
                boostText!!.text = "Boost Post"
            }
        } else {
            boostPost!!.visibility = View.GONE
        }

        boostPost.setOnClickListener(View.OnClickListener {
            if (!sessionManager.isUserLoggedIn()) {
                val intent = Intent(contextFromActivity.applicationContext, LoginActivity::class.java)
                contextFromActivity.startActivity(intent)
                dialog1.dismiss()
            } else {
                if (videoList[i].isBoosted!!){
                   showToast("You Already Boost This Video..")
                }else{
                    val intent = Intent(contextFromActivity.applicationContext, BoostPostActivity::class.java)
                    intent.putExtra("videoID", videoList[i].Id)
                    dialog1.dismiss()
                    contextFromActivity.startActivity(intent)
                }

            }
        })

        //
        if (videoList[i].isSaved!!) {
            tv_watch_later!!.text = ConstValFile.RemoveFromWatch
        } else {
            tv_watch_later!!.text = ConstValFile.WatchLater
        }

        iv_share_not_intersested!!.setOnClickListener { view: View? ->
            if (!sessionManager.isUserLoggedIn()) {
                val intent =
                    Intent(contextFromActivity.applicationContext, LoginActivity::class.java)
                contextFromActivity.startActivity(intent)
            } else {
                val manager: FragmentManager = (contextFromActivity as AppCompatActivity).supportFragmentManager
                val showReportDialog = VideoReportDialog(contextFromActivity,videoList[i].Id.toString(),ConstValFile.NotInterestedDialogView)
                showReportDialog.show(manager,showReportDialog.tag)
                dialog1.dismiss()
            }
        }

        iv_share_download!!.setOnClickListener {
            val shareBody: String = videoList[i].file.toString()
            Log.e("check1452", " $shareBody")
            savevideotointernalmemory(shareBody)
            dialog1.dismiss()
        }

        iv_share_duet!!.setOnClickListener {
            if (!sessionManager.isUserLoggedIn()) {
                val intent =
                    Intent(contextFromActivity.applicationContext, LoginActivity::class.java)
                contextFromActivity.startActivity(intent)
            } else {
                Toast.makeText(contextFromActivity, "Coming Soon..", Toast.LENGTH_SHORT).show()
            }
        }

        iv_share_watch_later!!.setOnClickListener {
            if (!sessionManager.isUserLoggedIn()) {
                val intent = Intent(contextFromActivity.applicationContext, LoginActivity::class.java)
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
        }

        iv_share_report!!.setOnClickListener {
            if (!sessionManager.isUserLoggedIn()) {
                val intent = Intent(contextFromActivity.applicationContext, LoginActivity::class.java)
                contextFromActivity.startActivity(intent)
            } else {
                val manager: FragmentManager = (contextFromActivity as AppCompatActivity).supportFragmentManager

                val showReportDialog = VideoReportDialog(contextFromActivity,videoList[i].Id.toString(),ConstValFile.ReportDialogView)
                showReportDialog.show(manager,showReportDialog.tag)
            }
            dialog1.dismiss()
        }

        dialog1.show()

    }

    private fun RemoveWatchLater(id: String) {
        val removeWatchLaterReq = apiInterface.remove_watch_later(sessionManager.getToken(),id)

        removeWatchLaterReq.enqueue(object :Callback<CommanResponse>{
            override fun onResponse(call: Call<CommanResponse>, response: Response<CommanResponse>) {
                if (response.isSuccessful && response.body()!!.success!!){
                    myApplication.printLogD(response.toString(),TAG)
                }else{
                    myApplication.printLogE(response.toString(),TAG)
                }
            }
            override fun onFailure(call: Call<CommanResponse>, t: Throwable) {
                myApplication.printLogE(t.toString(),TAG)
            }

        })
    }

    private fun watchLater(id: String) {
        val watchLaterReq = apiInterface.saveIntoWatchLater(sessionManager.getToken(),id)

        watchLaterReq.enqueue(object :Callback<CommanResponse>{
            override fun onResponse(call: Call<CommanResponse>, response: Response<CommanResponse>) {
                if (response.isSuccessful && response.body()!!.success!!){
                    myApplication.printLogD(response.toString(),TAG)
                }else{
                    myApplication.printLogE(response.toString(),TAG)
                }
            }
            override fun onFailure(call: Call<CommanResponse>, t: Throwable) {
                myApplication.printLogE(t.toString(),TAG)
            }
        })

    }

    private fun shareDialog(i: Int) {
        val dialog1 = BottomSheetDialog(contextFromActivity,R.style.CustomBottomSheetDialogTheme)
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
                .putExtra("name",videoList[i].auditionId)
                .putExtra("userid", videoList[i].userId.toString())
                .putExtra("image",videoList[i].image)
            contextFromActivity.startActivity(intent)
        }

        iv_share_whatsapp!!.setOnClickListener { v: View? ->
            val intent = Intent(Intent.ACTION_SEND)
            val shareBody: String = videoList.get(i).file.toString()
            intent.type = "text/plain"
            intent.setPackage("com.whatsapp")
            intent.putExtra(Intent.EXTRA_TEXT, shareBody)
            contextFromActivity.startActivity(Intent.createChooser(intent, "Audition"))
            dialog1.dismiss()
        }

        iv_share_facebook!!.setOnClickListener { v: View? ->
            val intent = Intent(Intent.ACTION_SEND)
            val shareBody: String = videoList.get(i).file.toString()
            intent.type = "text/plain"
            intent.setPackage("com.facebook.katana")
            intent.putExtra(Intent.EXTRA_TEXT, shareBody)
            contextFromActivity.startActivity(Intent.createChooser(intent, "Audition"))
            dialog1.dismiss()
        }

        iv_share_instagram!!.setOnClickListener { v: View? ->
            val intent = Intent(Intent.ACTION_SEND)
            val shareBody: String = videoList.get(i).file.toString()
            intent.type = "text/plain"
            intent.setPackage("com.instagram.android")
            intent.putExtra(Intent.EXTRA_TEXT, shareBody)
            contextFromActivity.startActivity(Intent.createChooser(intent, "Audition"))
            dialog1.dismiss()
        }

        iv_share_link!!.setOnClickListener { v: View? ->
            Toast.makeText(contextFromActivity, "Copied..", Toast.LENGTH_LONG).show()
            val clipboard =
                contextFromActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val shareBody: String = videoList.get(i).file.toString()
            val check = ClipData.newPlainText("label", shareBody)
            clipboard.setPrimaryClip(check)
            dialog1.dismiss()
        }

        iv_share_snapchat!!.setOnClickListener { v: View? ->
            val intent = Intent(Intent.ACTION_SEND)
            val shareBody: String = videoList.get(i).file.toString()
            intent.type = "text/plain"
            intent.setPackage("com.snapchat.android")
            intent.putExtra(Intent.EXTRA_TEXT, shareBody)
            contextFromActivity.startActivity(Intent.createChooser(intent, "Audition"))
            dialog1.dismiss()
        }

        iv_share_twitter!!.setOnClickListener { v: View? ->
            val intent = Intent(Intent.ACTION_SEND)
            val shareBody: String = videoList.get(i).file.toString()
            intent.type = "text/plain"
            intent.setPackage("com.twitter.android")
            intent.putExtra(Intent.EXTRA_TEXT, shareBody)
            contextFromActivity.startActivity(Intent.createChooser(intent, "Audition"))
            dialog1.dismiss()
        }

        iv_share_more!!.setOnClickListener { v: View? ->
            val intent = Intent(Intent.ACTION_SEND)
            val shareBody: String = videoList.get(i).file.toString()
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, shareBody)
            contextFromActivity.startActivity(Intent.createChooser(intent, "Audition"))
            dialog1.dismiss()
        }

        dialog1.show()
    }

    private fun sendToVideoUserProfile(bundle: Bundle) {
        val intent = Intent(contextFromActivity, OtherUserProfileActivity::class.java)
        intent.putExtra(ConstValFile.Bundle,bundle)
        contextFromActivity.startActivity(intent)
    }


    private fun showToast(msg: String) {
        Toast.makeText(contextFromActivity.applicationContext,msg,Toast.LENGTH_SHORT).show()
    }



    fun sendToLoginScreen(){
        val intent = Intent(contextFromActivity, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        contextFromActivity.startActivity(intent)
    }

    private fun likeVideo(id: String?, status: String) {
        myApplication.printLogD("likeVideo: $id $status",TAG)
        val liekeReq = apiInterface.viedolike(sessionManager.getToken(),id,status)
        liekeReq.enqueue(object : Callback<LikeResponse>{
            override fun onResponse(call: Call<LikeResponse>, response: Response<LikeResponse>) {
                if (response.isSuccessful){
                    myApplication.printLogD("onResponse: Video Like ${response.toString()}",TAG)
                }else{
                    myApplication.printLogE("onResponse: Video Like ${response.toString()}",TAG)

                }
            }
            override fun onFailure(call: Call<LikeResponse>, t: Throwable) {
                myApplication.printLogE("onFailure: Video Like ${t.toString()}",TAG)

            }

        })
    }

    private fun followUserApi(userId: String?, status: String) {
        myApplication.printLogD("followUserApi: $userId $status",TAG)
        val ffReq = apiInterface.followFollowing(sessionManager.getToken(),userId,status)
        ffReq.enqueue(object : Callback<FollowFollowingResponse>{
            override fun onResponse(call: Call<FollowFollowingResponse>, response: Response<FollowFollowingResponse>) {
                if (response.isSuccessful){
                    myApplication.printLogD("onResponse: FollowFollowing ${response.toString()}",TAG)

                }else{
                    myApplication.printLogE("onResponse: FollowFollowing ${response.toString()}",TAG)

                }
            }
            override fun onFailure(call: Call<FollowFollowingResponse>, t: Throwable) {
                myApplication.printLogE("onFailure: FollowFollowing ${t.toString()}",TAG)

            }
        })
    }

    private fun savevideotointernalmemory(url: String) {
        val file =  File(File(Environment.getExternalStorageDirectory(), "Audition"), "Audition")
        val timestamp = System.currentTimeMillis()
        val request = DownloadManager.Request(Uri.parse(url))
        request.allowScanningByMediaScanner()
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,file.absolutePath+"/"+timestamp.toString()+".mp4")
        val manager = contextFromActivity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
        Toast.makeText(contextFromActivity, "Video Download started", Toast.LENGTH_SHORT).show()
    }

    private fun showCommentDialog(bundle: Bundle) {
        val showCommentDialog = CommentBottomSheet()
        showCommentDialog.arguments = bundle
        showCommentDialog.show((contextFromActivity as AppCompatActivity).
        supportFragmentManager, showCommentDialog.getTag())
    }

    private fun showVoteDialog(id: String?) {
        val voteDialog = Dialog(contextFromActivity)
        voteDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        voteDialog.setContentView(R.layout.vote_dialog_sheet)

        val voteCycle = voteDialog.findViewById<RecyclerView>(R.id.voteCycle)
        voteDialog.window!!.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        voteDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        voteDialog.window!!.setGravity(Gravity.RIGHT)


        val voteCatReq = apiInterface.getVoteCategory(sessionManager.getToken())

        voteCatReq.enqueue(object : Callback<VoteDataResponse>{
            override fun onResponse(call: Call<VoteDataResponse>, response: Response<VoteDataResponse>) {
               if (response.isSuccessful && response.body()!!.success!!){
                   val data = response.body()!!.data
                   voteCycle.adapter = VoteAdapter(contextFromActivity,data,id!!,voteDialog)

               }else{
                   myApplication.printLogE(response.toString(),TAG)

               }
            }

            override fun onFailure(call: Call<VoteDataResponse>, t: Throwable) {
                myApplication.printLogE(t.toString(),TAG)
            }

        })

        voteDialog.show()
    }
}


