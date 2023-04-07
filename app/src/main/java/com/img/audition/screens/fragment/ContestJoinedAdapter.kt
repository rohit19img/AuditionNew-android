package com.img.audition.screens.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.img.audition.dataModel.JoinedContestData
import com.img.audition.databinding.JoinedContestCycleDesignBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.SessionManager
import com.img.audition.screens.ContestDetailsActivity
import com.img.audition.videoWork.VideoCacheWork
import java.text.SimpleDateFormat

@UnstableApi
class ContestJoinedAdapter(val context: Context,val contestList:ArrayList<JoinedContestData>) :
    RecyclerView.Adapter<ContestJoinedAdapter.MyViewHolder>()
{


    val TAG = "ContestJoinedAdapter"
    private var cPos = 0
    private val myApplication by lazy {
        MyApplication(context.applicationContext)
    }
    private val sessionManager by lazy {
        SessionManager(context.applicationContext)
    }
    inner class MyViewHolder(itemView: JoinedContestCycleDesignBinding) : RecyclerView.ViewHolder(itemView.root) {

       val priceText = itemView.prizetxt
       val contestStart = itemView.start
       val contestEndDate = itemView.enddate
       val contestLeaderboard = itemView.leaderboard
       val maxUser = itemView.maxuser
       val winAmount = itemView.winamount
       val joinUser = itemView.joinuser
       val winner = itemView.winner
       val teamEnteredProBar = itemView.teamEnteredPB
       val contestName = itemView.contestName
       val playerViewExo = itemView.contestVideo
       val contestImage = itemView.contestImage


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


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContestJoinedAdapter.MyViewHolder {
        val itemBinding = JoinedContestCycleDesignBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(itemBinding)
    }

    override fun getItemCount(): Int {
        return contestList.size
    }

    override fun onBindViewHolder(holder: ContestJoinedAdapter.MyViewHolder, position: Int) {
        val data = contestList[position]

        holder.apply {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
            val simpleDateFormat1 = SimpleDateFormat("MMM dd, HH:mm aa")

           try {
               contestEndDate.text = simpleDateFormat1.format(simpleDateFormat.format(data.endDate!!))
               contestStart.text = simpleDateFormat1.format(simpleDateFormat.format(data.startDate!!))
           }catch (e:Exception){
               myApplication.printLogE(e.toString(),TAG)
           }
            maxUser.text = data.maximumUser.toString()
            winAmount.text = data.winAmountStr.toString()
            joinUser.text = data.joinedusers.toString()

            winner.text = data.totalwinners.toString()

            teamEnteredProBar.max = data.maximumUser!!
            teamEnteredProBar.progress = data.joinedusers!!

            itemView.setOnClickListener {
                val bundle = Bundle()
                bundle.putString(ConstValFile.ContestID,data.challengeid)
                bundle.putString("contest","join")
                sendToContestDetailsActivity(bundle)
            }
        }
    }

    private fun sendToContestDetailsActivity(bundle: Bundle) {
        val intent = Intent(context,ContestDetailsActivity::class.java)
        intent.putExtra(ConstValFile.Bundle,bundle)
        context.startActivity(intent)
    }


    override fun onViewAttachedToWindow(holder: ContestJoinedAdapter.MyViewHolder) {
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

                val mediaItem = MediaItem.fromUri(contest.file.toString())
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


}