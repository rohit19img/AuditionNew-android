package com.img.audition.adapters


import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.img.audition.R
import com.img.audition.dataModel.CommonResponse
import com.img.audition.dataModel.FollowFollowingResponse
import com.img.audition.dataModel.LikeResponse
import com.img.audition.dataModel.VideoData
import com.img.audition.databinding.VideoLayoutBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.screens.LoginActivity
import com.img.audition.screens.OtherUserProfileActivity
import com.img.audition.screens.fragment.VideoReportDialog
import com.img.audition.videoWork.VideoCacheWork
import com.img.audition.videoWork.VideoItemPlayPause
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


@UnstableApi class VideoAdapter(val context:Context, val videoList: ArrayList<VideoData>) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {
    val TAG = "VideoAdapter"

    var Ar = arrayOf(-1,0)
    val  sessionManager = SessionManager(context.applicationContext)
    val apiInterface = RetrofitClient.getInstance().create(ApiInterface::class.java)
    private val myApplication by lazy {
        MyApplication(context.applicationContext)
    }
    var cPos = 0
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
        var exoPlayer = ExoPlayer.Builder(context.applicationContext).build()
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
        holder.apply {
            val list = videoList[position]
            myApplication.printLogD("onBindViewHolder: ${list.file}","videoUrl")

            if (list.likeStatus!!){
                likeBtn.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.liked_ic))
                likeBtn.setColorFilter(context.resources.getColor(R.color.likeHeartRed))
            }else{
                likeBtn.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.like_ic))
                likeBtn.setColorFilter(context.resources.getColor(R.color.white))
            }
            if (list.followStatus!!){
                followBtn.text = ConstValFile.Following
                followBtn.setTypeface(followBtn.typeface, Typeface.ITALIC)
            }else{
                followBtn.text = ConstValFile.Follow
                followBtn.setTypeface(followBtn.typeface, Typeface.NORMAL)
            }
            if (list.caption.toString()!=""){
                caption.visibility = View.VISIBLE
                caption.text = list.caption
            }else{
                caption.visibility = View.GONE
            }
            likeBtn.setOnClickListener {
                if (list.likeStatus!!){
                    likeBtn.isSelected = false
                    likeBtn.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.like_ic))
                    likeBtn.setColorFilter(context.resources.getColor(R.color.bgColorWhite))
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
                    likeBtn.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.liked_ic))
                    likeBtn.setColorFilter(context.resources.getColor(R.color.likeHeartRed))
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
                    bundle.putBoolean(ConstValFile.UserFollowStatus, list.followStatus!!)
                    sendToVideoUserProfile(bundle)
                }else{
                    showToast("Self Profile")
                }
            }

            shareBtn.setOnClickListener {
                shareDialog(position)
            }

            moreBtn.setOnClickListener {
                moredialog(position)
            }


            likeCount.text = list.likeCount.toString()
            commentCount.text = list.commentCount.toString()
            shareCount.text = list.shares.toString()
            Glide.with(context).load(list.image).placeholder(R.drawable.person_ic).into(userProfile)
            userName.text = list.auditionId
            audioName.text = list.auditionId +" - Original Audio"
        }
    }

    private fun moredialog(i: Int) {
        val dialog1 = BottomSheetDialog(context)
        dialog1.setContentView(R.layout.more_dialog)
        val tv_watch_later = dialog1.findViewById<TextView>(R.id.tv_watch_later)
        val iv_share_not_intersested = dialog1.findViewById<ImageView>(R.id.iv_share_not_intersested)
        val iv_share_report = dialog1.findViewById<ImageView>(R.id.iv_share_report)
        val iv_share_duet = dialog1.findViewById<ImageView>(R.id.iv_share_duet)
        val iv_share_download = dialog1.findViewById<LinearLayout>(R.id.iv_share_download)
        val iv_share_watch_later = dialog1.findViewById<LinearLayout>(R.id.iv_share_watch_later)

        //BoostPost
        val boostPost = dialog1.findViewById<LinearLayout>(R.id.boost_post)
        if (videoList[i].userId.equals(sessionManager.getUserSelfID())) {
            boostPost!!.visibility = View.VISIBLE
        } else {
            boostPost!!.visibility = View.GONE
        }

        boostPost.setOnClickListener(View.OnClickListener {
            if (!sessionManager.isUserLoggedIn()) {
                val intent = Intent(context.applicationContext, LoginActivity::class.java)
                context.startActivity(intent)
            } else {
               /* val intent = Intent(context.applicationContext, BoostPostActivity::class.java)
                intent.putExtra("videoID", list.get(i).get_id())
                Log.d("videoID", "onClick:" + list.get(i).get_id())
                context.startActivity(intent)*/

                myApplication.showToast("Post Boost..")
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
                    Intent(context.applicationContext, LoginActivity::class.java)
                context.startActivity(intent)
            } else {
                /*val BottomSheet = Notinterstedpopup_Fragment()
                val link: String = list.get(i).get_id()
                Log.e("Check256", "!!  $link")
                if (link != null) {
                    val bundle = Bundle()
                    bundle.putString("vid", link)
                    BottomSheet.setArguments(bundle)
                    BottomSheet.show(
                        (context as AppCompatActivity).supportFragmentManager,
                        BottomSheet.getTag()
                    )
                }*/
                myApplication.showToast("Soon..")
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
                    Intent(context.applicationContext, LoginActivity::class.java)
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "Coming Soon..", Toast.LENGTH_SHORT).show()
            }
        }

        iv_share_watch_later!!.setOnClickListener {
            if (!sessionManager.isUserLoggedIn()) {
                val intent = Intent(context.applicationContext, LoginActivity::class.java)
                context.startActivity(intent)
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
                val intent = Intent(context.applicationContext, LoginActivity::class.java)
                context.startActivity(intent)
            } else {
                val manager: FragmentManager = (context as AppCompatActivity).supportFragmentManager

                val showReportDialog = VideoReportDialog(videoList[i].Id.toString())
                showReportDialog.show(manager,showReportDialog.tag)
            }
            dialog1.dismiss()
        }

        dialog1.show()

    }

    private fun RemoveWatchLater(id: String) {
        val removeWatchLaterReq = apiInterface.remove_watch_later(sessionManager.getToken(),id)

        removeWatchLaterReq.enqueue(object :Callback<CommonResponse>{
            override fun onResponse(call: Call<CommonResponse>, response: Response<CommonResponse>) {
                if (response.isSuccessful && response.body()!!.success!!){
                    myApplication.printLogD(response.toString(),TAG)
                }else{
                    myApplication.printLogE(response.toString(),TAG)
                }
            }
            override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
                myApplication.printLogE(t.toString(),TAG)
            }

        })
    }

    private fun watchLater(id: String) {
        val watchLaterReq = apiInterface.saveIntoWatchLater(sessionManager.getToken(),id)

        watchLaterReq.enqueue(object :Callback<CommonResponse>{
            override fun onResponse(call: Call<CommonResponse>, response: Response<CommonResponse>) {
                if (response.isSuccessful && response.body()!!.success!!){
                    myApplication.printLogD(response.toString(),TAG)
                }else{
                    myApplication.printLogE(response.toString(),TAG)
                }
            }
            override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
                myApplication.printLogE(t.toString(),TAG)
            }

        })

    }

    private fun shareDialog(i: Int) {
        val dialog1 = BottomSheetDialog(context)
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
           /* val intent = Intent(context.applicationContext, Message_Activity::class.java)
            val bundle = Bundle()
            bundle.putString("url204", shareBody + " userid " + list.get(i).getUserId())
            bundle.putString("userid", list.get(i).getAudition_id())
            bundle.putString("img", list.get(i).getImage())
            intent.putExtras(bundle)
            context.startActivity(intent)*/
            myApplication.showToast("Send To Message Activity..")
        }

        iv_share_whatsapp!!.setOnClickListener { v: View? ->
            val intent = Intent(Intent.ACTION_SEND)
            val shareBody: String = videoList.get(i).file.toString()
            intent.type = "text/plain"
            intent.setPackage("com.whatsapp")
            intent.putExtra(Intent.EXTRA_TEXT, shareBody)
            context.startActivity(Intent.createChooser(intent, "Audition"))
            dialog1.dismiss()
        }

        iv_share_facebook!!.setOnClickListener { v: View? ->
            val intent = Intent(Intent.ACTION_SEND)
            val shareBody: String = videoList.get(i).file.toString()
            intent.type = "text/plain"
            intent.setPackage("com.facebook.katana")
            intent.putExtra(Intent.EXTRA_TEXT, shareBody)
            context.startActivity(Intent.createChooser(intent, "Audition"))
            dialog1.dismiss()
        }

        iv_share_instagram!!.setOnClickListener { v: View? ->
            val intent = Intent(Intent.ACTION_SEND)
            val shareBody: String = videoList.get(i).file.toString()
            intent.type = "text/plain"
            intent.setPackage("com.instagram.android")
            intent.putExtra(Intent.EXTRA_TEXT, shareBody)
            context.startActivity(Intent.createChooser(intent, "Audition"))
            dialog1.dismiss()
        }

        iv_share_link!!.setOnClickListener { v: View? ->
            Toast.makeText(context, "Copied..", Toast.LENGTH_LONG).show()
            val clipboard =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
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
            context.startActivity(Intent.createChooser(intent, "Audition"))
            dialog1.dismiss()
        }

        iv_share_twitter!!.setOnClickListener { v: View? ->
            val intent = Intent(Intent.ACTION_SEND)
            val shareBody: String = videoList.get(i).file.toString()
            intent.type = "text/plain"
            intent.setPackage("com.twitter.android")
            intent.putExtra(Intent.EXTRA_TEXT, shareBody)
            context.startActivity(Intent.createChooser(intent, "Audition"))
            dialog1.dismiss()
        }

        iv_share_more!!.setOnClickListener { v: View? ->
            val intent = Intent(Intent.ACTION_SEND)
            val shareBody: String = videoList.get(i).file.toString()
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, shareBody)
            context.startActivity(Intent.createChooser(intent, "Audition"))
            dialog1.dismiss()
        }

        dialog1.show()
    }

    private fun sendToVideoUserProfile(bundle: Bundle) {
        val intent = Intent(context, OtherUserProfileActivity::class.java)
        intent.putExtra(ConstValFile.Bundle,bundle)
        context.startActivity(intent)
    }


    private fun showToast(msg: String) {
        Toast.makeText(context.applicationContext,msg,Toast.LENGTH_SHORT).show()
    }

    override fun onViewAttachedToWindow(holder: VideoViewHolder) {
        holder.apply {


            val list = videoList[position]
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


            playPauseVolumeBtn.setOnClickListener {
                playPauseIc.visibility = View.GONE
                if (exoPlayer.isPlaying){
                    exoPlayer.pause()
                    playPauseIc.visibility = View.VISIBLE
                    playPauseIc.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.play_ic))

                }else{
                    exoPlayer.prepare()
                    exoPlayer.play()
                    playPauseIc.visibility = View.GONE
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
            })

        }
        super.onViewAttachedToWindow(holder)
    }


    fun onActivityStateChanged() : VideoItemPlayPause{
        return object : VideoItemPlayPause{
            override fun onPause(holder: VideoViewHolder,cPos:Int) {
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

                    Log.i("positionTest","visible : $visiblePosition")
                    Log.i("positionTest","last : $lastPosition")

                    if (visiblePosition >= 0) {
                        val holder_current: VideoViewHolder =
                            recyclerView.findViewHolderForAdapterPosition(visiblePosition) as VideoViewHolder
                        holder_current.exoPlayer.seekTo(0)
                        holder_current.exoPlayer.playWhenReady = true
                    }

                    if (lastPosition >= 0) {
                        val holder_previous: VideoViewHolder =
                            recyclerView.findViewHolderForAdapterPosition(lastPosition) as VideoViewHolder
                        holder_previous.exoPlayer.seekTo(0)
                        if (holder_previous.exoPlayer.isPlaying)
                            holder_previous.exoPlayer.stop()
                        holder_previous.exoPlayer.playWhenReady = false
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
        return videoList.size
    }

    fun sendToLoginScreen(){
        val intent = Intent(context, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
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

        val timestamp = System.currentTimeMillis()
        val request = DownloadManager.Request(Uri.parse(url))
        request.allowScanningByMediaScanner()
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,ConstValFile.APP_NAME+"/"+timestamp.toString()+".mp4")
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
        Toast.makeText(context, "Video Download started", Toast.LENGTH_SHORT).show()
    }

}


