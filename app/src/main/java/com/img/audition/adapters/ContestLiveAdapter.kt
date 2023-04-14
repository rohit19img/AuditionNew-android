package com.img.audition.adapters

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.img.audition.dataModel.LiveContestData
import com.img.audition.databinding.LiveContestItemLayoutBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.APITags
import com.img.audition.network.SessionManager
import com.img.audition.screens.CameraActivity
import com.img.audition.screens.LoginActivity
import com.img.audition.videoWork.VideoCacheWork

 class ContestLiveAdapter(val context: Context,val contestList:ArrayList<LiveContestData>) : RecyclerView.Adapter<ContestLiveAdapter.MyViewHolder>() {
    val TAG = "ContestLiveAdapter"
    private var cPos = 0
    private val myApplication by lazy {
        MyApplication(context.applicationContext)
    }
    private val sessionManager by lazy {
        SessionManager(context.applicationContext)
    }
    inner class MyViewHolder(itemView: LiveContestItemLayoutBinding) : RecyclerView.ViewHolder(itemView.root) {
        val contestImage = itemView.contestImage
        val playerViewExo = itemView.contestVideo
        val contestWinPrize = itemView.contestWiningPrize
        val contestJoinBtn = itemView.contestJoinBtn
        val contestProgressBar = itemView.contestPorgress
        val contestJoinedUser = itemView.contestJoinUser
        val contestMaxJoinUser = itemView.contestMaxJoinUser


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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemBinding = LiveContestItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
       holder.apply {
            val contest = contestList[position]
            contestWinPrize.text = "₹ "+contest.winAmount.toString()
            contestJoinBtn.text = "₹ "+contest.entryfee.toString()
            contestProgressBar.max = contest.maximumUser!!
            contestProgressBar.progress = contest.joinedusers!!
            contestMaxJoinUser.text = "Max.Join "+contest.maximumUser.toString()
            contestJoinedUser.text = "Joined "+contest.joinedusers.toString()

           contestJoinBtn.setOnClickListener {
              if (!(sessionManager.isUserLoggedIn())){
                    sendToLoginScreen()
              }else{
                  if (!(contest.isJoined!!)){
                      myApplication.printLogD("User Not Joined",TAG)
                      sessionManager.createContestSession(contest.entryfee!!,contest.Id,contest.fileType,contest.file,true)
                      sendForCreateVideo()
                  }else{
                      myApplication.printLogD("User Joined",TAG)
                      myApplication.showToast("You Already join this contest")

                  }
              }
           }
       }
    }



    override fun onViewAttachedToWindow(holder: MyViewHolder) {
        holder.apply {
            val contest = contestList[position]
            if (contest.fileType.equals(ConstValFile.TYPE_IMAGE)){
                val imageUrl = ConstValFile.BASEURL+contest.file.toString()
                playerViewExo.visibility = View.GONE
                contestImage.visibility = View.VISIBLE
                Glide.with(context).load(imageUrl).into(contestImage)
            }else{
                playerViewExo.visibility = View.VISIBLE
                contestImage.visibility = View.GONE
                myApplication.printLogD(APITags.APIBASEURL+contest.file, "contest video Url")
                val mediaItem = MediaItem.fromUri(APITags.APIBASEURL+contest.file.toString())
                val videoMediaSource = mediaSource.createMediaSource(mediaItem)
                playerViewExo.player = exoPlayer
                exoPlayer.setMediaSource(videoMediaSource)
                exoPlayer.prepare()
                exoPlayer.playWhenReady = false

               /* if (cPos>=0){
                    myApplication.printLogD("onViewAttachedToWindow: Posotion ${cPos}",TAG)
                    if (cPos == position){
                        exoPlayer.seekTo(0)
                        exoPlayer.playWhenReady = true
                    }else{
                        exoPlayer.seekTo(0)
                        exoPlayer.playWhenReady = false
                    }
                }*/

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
                            else -> {
                                myApplication.printLogD(playbackState.toString(),TAG)
                            }
                        }
                    }
                })
            }

        }
        super.onViewAttachedToWindow(holder)
    }

    override fun getItemCount(): Int {
      return contestList.size
    }

    fun sendToLoginScreen(){
        val intent = Intent(context, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
    }

    private fun sendForCreateVideo() {
        val bundle = Bundle()
        bundle.putBoolean(ConstValFile.IsFromContest,true)
        val intent = Intent(context, CameraActivity::class.java)
        intent.putExtra(ConstValFile.Bundle,bundle)
        context.startActivity(intent)
    }

}