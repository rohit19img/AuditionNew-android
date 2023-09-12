package com.img.audition.adapters

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlaybackException
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.img.audition.R
import com.img.audition.dataModel.JoinUsableBalanceResponse
import com.img.audition.dataModel.LiveContestData
import com.img.audition.databinding.LiveContestItemLayoutBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.APITags
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.screens.AddAmountActivity
import com.img.audition.screens.ContestDetailsActivity
import com.img.audition.screens.LoginActivity
import com.img.audition.snapCameraKit.SnapCameraActivity
import com.img.audition.videoWork.PlayPauseContestVideo
import com.img.audition.videoWork.VideoCacheWork
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


@UnstableApi
class ContestLiveAdapter(private val context: Context,private val contestList: ArrayList<LiveContestData>) : RecyclerView.Adapter<ContestLiveAdapter.MyViewHolder>() {

    private val TAG = "ContestLiveAdapter"
    private var cPos = 0

    private var sDate = ""
    private var eDate = ""
    private var startDate: Date? = null
    private var endDate:Date? = null

    private val sessionManager by lazy {
        SessionManager(context.applicationContext)
    }
    private val myApplication by lazy {
        MyApplication(context.applicationContext)
    }

    inner class MyViewHolder(itemView: LiveContestItemLayoutBinding) :
        RecyclerView.ViewHolder(itemView.root) {

        val contestImage = itemView.contestImage
        val contestName = itemView.contestName
        val playerViewExo = itemView.contestVideo
        val contestWinPrize = itemView.contestWiningPrize
        val contestJoinBtn = itemView.contestJoinBtn
        val contestProgressBar = itemView.contestProgress
        val contestJoinedUser = itemView.contestJoinUser
        val contestMaxJoinUser = itemView.contestMaxJoinUser
        val contestStartDate = itemView.contestStartDate
        val contestEndDate = itemView.contestEndDate
        val contestBonus = itemView.contestBonus
        val contestTimer = itemView.contestTimer
        val bonusLL = itemView.bonusLL
        val winner = itemView.winner

        lateinit var cT:CountDownTimer


        //Video Cache
        val mediaSource = ProgressiveMediaSource.Factory(CacheDataSource.Factory().setCache(VideoCacheWork.simpleCache)
                .setUpstreamDataSourceFactory(
                    DefaultHttpDataSource.Factory().setUserAgent("ExoPlayer")
                )
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        )
        private val trackSelector = DefaultTrackSelector(context).apply {
            setParameters(buildUponParameters()
                .setRendererDisabled(C.TRACK_TYPE_VIDEO,true)
                .setAllowVideoMixedDecoderSupportAdaptiveness(true)
                .setAllowAudioMixedDecoderSupportAdaptiveness(true)
                .setPreferredVideoMimeType("video/avc")
                .setAllowAudioMixedChannelCountAdaptiveness(true)
                .setAllowMultipleAdaptiveSelections(true)
            )
        }

        val exoPlayer = ExoPlayer.Builder(context.applicationContext)
            .setTrackSelector(trackSelector)
            .build()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemBinding =
            LiveContestItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(itemBinding)
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.apply {

            val contest = contestList[position]

            contestName.text = contest.contestName.toString()
            winner.text = "WINNERS : ${contest.total_winners.toString()}"
            if (VideoCacheWork.simpleCache.cacheSpace >= (20 * 1024 * 1024)){
                Log.d("checkCache", "cache Full ${VideoCacheWork.simpleCache.cacheSpace}")
            }else{
                Log.d("checkCache", "cache have space ${VideoCacheWork.simpleCache.cacheSpace}")
            }
            if(contest.isBonus == 1) {
                bonusLL.visibility = View.VISIBLE
                contestBonus.text = "BOUNS : ${contest.bonusPercentage}%"
            } else
                bonusLL.visibility = View.GONE

            val c: Calendar = Calendar.getInstance()
            c.timeZone = TimeZone.getTimeZone("Asia/Calcutta")
            val hour: Int = c.get(Calendar.HOUR_OF_DAY)
            val minute: Int = c.get(Calendar.MINUTE)
            val sec: Int = c.get(Calendar.SECOND)
            val mYear1: Int = c.get(Calendar.YEAR)
            val mMonth1: Int = c.get(Calendar.MONTH)
            val mDay1: Int = c.get(Calendar.DAY_OF_MONTH)

            sDate = mYear1.toString() + "-" + (mMonth1 + 1) + "-" + mDay1 + " " + hour + ":" + minute + ":" + sec
            eDate = contest.startDate
            Log.i("matchtime","Start "+ sDate)
            Log.i("matchtime", "End "+eDate)

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val dateFormat =  SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            val dateFormat1 =  SimpleDateFormat("MMM dd  hh:mm a")
            try {
                 startDate = sdf.parse(sDate)
                 endDate =  dateFormat.parse(eDate)
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            var diffInMs : Long = 0
           try {
               diffInMs = endDate!!.time - startDate!!.time
               Log.d("checkTimer", "onBindViewHolder: $diffInMs")
           }catch (e:Exception){
               e.printStackTrace()
           }

            val hours1 = (1*60*60 * 1000).toLong()
            val hours4 = (4*60*60 * 1000).toLong()
            val hours48 =(48*60*60 * 1000).toLong()


            cT = object : CountDownTimer(diffInMs, 1000) {
                @SuppressLint("DefaultLocale", "SetTextI18n")
                override fun onTick(millisUntilFinished: Long) {
                    if (diffInMs < hours1) {
                        contestTimer.text =  "Starts in : "+ String.format(
                            java.lang.String.format(
                                "%02d",
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                            ) + "m:"
                                    + java.lang.String.format(
                                "%02d",
                                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                                )
                            ) + "s"
                        )
                    } else if (diffInMs < hours4) {
                        contestTimer.text =  "Starts in : "+String.format(
                            (java.lang.String.format(
                                "%02d",
                                TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                            ) + "h:"
                                    + java.lang.String.format(
                                "%02d",
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                    TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                                )
                            ) + " m")
                        )
                    } else if (diffInMs < hours48) {
                        contestTimer.text =  "Starts in : "+String.format(
                            java.lang.String.format(
                                "%02d",
                                TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                            ) + " h"
                        )
                    } else {
                        contestTimer.text = "Starts in : "+String.format(
                            java.lang.String.format(
                                "%02d",
                                TimeUnit.MILLISECONDS.toDays(millisUntilFinished)
                            ) + " days"
                        )
                    }
                }

                override fun onFinish() {
                    if (contest.status == "started" && contest.finalStatus == "pending"){
                        contestTimer.text = "Contest Started"
                    } else if(contest.status == "completed" && contest.finalStatus == "IsReviewed"){
                        contestTimer.text = "Under Review"
                    } else if(contest.status == "completed" && contest.finalStatus == "winnerdeclared"){
                        contestTimer.text = "Contest Completed"
                    }
                    cT.cancel()
                }
            }
            cT.start()

            contestStartDate.text = contest.startDate
            contestEndDate.text = contest.endDate

            try{
                contestStartDate.text = "${dateFormat1.format(dateFormat.parse(contest.startDate)!!)}"
            } catch (e : java.lang.Exception){
               Log.i("Exception"," ${ e.message}")
            }
            try{
                contestEndDate.text = "${dateFormat1.format(dateFormat.parse(contest.endDate)!!)}"
            } catch (e : java.lang.Exception){
               Log.i("Exception"," ${ e.message}")
            }

            contestWinPrize.text = "₹ " + contest.winAmount.toString()
            contestJoinBtn.text = "₹ " + contest.entryfee.toString()
            contestProgressBar.max = contest.maximumUser!!
            contestProgressBar.progress = contest.joinedusers!!
            contestMaxJoinUser.text = "${contest.maximumUser.toString()} "
            contestJoinedUser.text = "${contest.joinedusers.toString()}"
            contestJoinBtn.setOnClickListener {
                if (myApplication.isNetworkConnected()){
                    if (!(sessionManager.isUserLoggedIn())) {
                        sendToLoginScreen()
                    } else {
                        if (contest.status == "notstarted"){
                            if (!(contest.isJoined!!)) {
                                getUsableBalance(contest)
                            } else {
                                Toast.makeText(context,"You Already join this contest",Toast.LENGTH_SHORT).show()
                                Log.d(TAG, "You Already join this contest")
                            }
                        }
                    }
                }else{
                    checkInternetDialog()
                }
            }

            itemView.setOnClickListener {
                if (myApplication.isNetworkConnected()){
                    Log.d("contestID", "onBindViewHolder: ${contest.Id}")
                    sessionManager.createContestSession(contest.entryfee!!, contest.Id, contest.fileType, contest.file, true)
                    val bundle = Bundle()
                    bundle.putString(ConstValFile.ContestID,contest.Id)
                    bundle.putString(ConstValFile.ContestStatus,contest.status)
                    if (contest.status == "notstarted"){
                        bundle.putBoolean(ConstValFile.IsContestJoin,contest.isJoined!!)
                    }else{
                        bundle.putBoolean(ConstValFile.IsContestJoin,true)
                    }
                    sendToContestDetailsActivity(bundle)
                }else{
                    checkInternetDialog()
                }
            }
        }
    }

    private fun getUsableBalance(contest: LiveContestData) {

        val apiInterface = RetrofitClient.getInstance().create(ApiInterface::class.java)

        val voterListReq = apiInterface.getUsableBalance(SessionManager(context).getToken(),contest.Id)

        voterListReq.enqueue(object : Callback<JoinUsableBalanceResponse> {
            override fun onResponse(call: Call<JoinUsableBalanceResponse>, response: Response<JoinUsableBalanceResponse>) {
                if (response.isSuccessful && response.body()?.success!!){

                        val res = response.body()!!.data

                        val dialog = Dialog(context)
                        dialog.setContentView(R.layout.join_contest_dailog)
                        val availableBalance: TextView = dialog.findViewById(R.id.availableBalance)
                        val entryFee: TextView = dialog.findViewById(R.id.entryFee)
                        val btnJoinContest: TextView = dialog.findViewById(R.id.btnJoinContest)
                        val usableBalance: TextView = dialog.findViewById(R.id.usableBalance)
                        val bonus_amount: TextView = dialog.findViewById(R.id.bonus_amount)

                        availableBalance.text = "₹${res!!.usertotalbalance}"
                        entryFee.text = "₹${res!!.entryfee}"
                        usableBalance.text = "₹${res.usablebalance}"
                        bonus_amount.text = "(Bonus :${res!!.bonus}%)"
                        btnJoinContest.setOnClickListener {
                            dialog.dismiss()
                            if(res.usablebalance!!.toDouble() <  res.entryfee!!.toDouble()){
//                                val amount : Double = res.entryfee!!.toDouble() - res.usablebalance!!.toDouble()
                                sendToAddAmountActivity()
                            } else{
                                sessionManager.createContestSession(contest.entryfee!!, contest.Id, contest.fileType, contest.file, true)
                                Thread.sleep(100)
                                sendForCreateVideo()
                            }
                        }
                        dialog.window!!.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        dialog.show()
                }else{
                    Log.e(TAG, "onResponse: $response")
                }
            }
            override fun onFailure(call: Call<JoinUsableBalanceResponse>, t: Throwable) {
                Log.e(TAG, "onFailure: $t")
            }
        })
    }


    override fun onViewAttachedToWindow(holder: MyViewHolder) {
        holder.apply {
            val contest = contestList[position]
            if (contest.fileType.equals(ConstValFile.TYPE_IMAGE)) {
                val imageUrl = APITags.ADMINBASEURL + contest.file.toString()
                Log.d("url", "contestImage : $imageUrl")

                playerViewExo.visibility = View.GONE
                contestImage.visibility = View.VISIBLE
                Glide.with(context).load(imageUrl).placeholder(R.drawable.splash_icon).into(contestImage)
            } else {
                playerViewExo.visibility = View.VISIBLE
                contestImage.visibility = View.GONE
                Log.d("url", "contestVideo : ${APITags.ADMINBASEURL + contest.file.toString()}")
                val mediaItem = MediaItem.Builder().setMimeType("video/avc").setUri(APITags.ADMINBASEURL + contest.file.toString()).build()
                val videoMediaSource = mediaSource.createMediaSource(mediaItem)
                playerViewExo.player = exoPlayer
                exoPlayer.setMediaSource(videoMediaSource)
                exoPlayer.prepare()
                exoPlayer.play()
                /* if (cPos>=0){
                     if (cPos == position){
                         exoPlayer.seekTo(0)
                         exoPlayer.playWhenReady = true
                     }else{
                         exoPlayer.seekTo(0)
                         exoPlayer.playWhenReady = false
                     }
                 }*/

                exoPlayer.addListener(object : Player.Listener {
                    @Deprecated("Deprecated in Java")
                    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                        when (playbackState) {
                            ExoPlayer.STATE_ENDED -> {
                                exoPlayer.seekTo(0)
                                exoPlayer.prepare()
                                exoPlayer.play()
                            }
                            ExoPlayer.STATE_BUFFERING -> {}
                            else -> {}
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        super.onPlayerError(error)
                        when (error.errorCode) {
                            ExoPlaybackException.TYPE_SOURCE -> {
                                val mediaItem = MediaItem.Builder().setMimeType("video/avc").setUri(APITags.ADMINBASEURL + contest.file.toString()).build()
                                val videoMediaSource = mediaSource.createMediaSource(mediaItem)
                                playerViewExo.player = exoPlayer
                                exoPlayer.setMediaSource(videoMediaSource)
                                exoPlayer.prepare()
                                exoPlayer.play()
                            }
                            else -> {
                                Log.e("currentState",APITags.ADMINBASEURL + contest.file.toString())

                            }
                        }
                    }
                })
            }
        }
        super.onViewAttachedToWindow(holder)
    }

    fun onActivityStateChanged(): PlayPauseContestVideo {
        return object : PlayPauseContestVideo {
            override fun onPause(holder: MyViewHolder, cPos: Int) {
                holder.apply {
                    Log.d("check 400", "onPause: Inside Adapter")
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

            override fun onResume(holder: MyViewHolder, cPos: Int) {
                holder.apply {
                    Log.d("check 400", "onResume: Inside Adapter")
                    if (cPos >= 0) {
                        if (cPos == position) {
                            exoPlayer.prepare()
                            exoPlayer.playWhenReady = true
                            Log.d("check 400", "onResume: Inside Adapter 444")
                        } else {
                            exoPlayer.seekTo(0)
                            exoPlayer.playWhenReady = false
                            exoPlayer.stop()
                        }
                    }
                }
            }

            override fun onStop(holder: MyViewHolder, cPos: Int) {

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

    override fun onViewDetachedFromWindow(holder: MyViewHolder) {
        holder.apply {
            val contest = contestList[position]
            cT.cancel()
            if (!(contest.fileType.equals(ConstValFile.TYPE_IMAGE))) {
                if (exoPlayer.isPlaying) {
                    playerViewExo.player!!.playWhenReady = false
                    playerViewExo.player!!.seekTo(0)
                    playerViewExo.player!!.pause()
                    playerViewExo.player!!.stop()
                }
            }
        }
        super.onViewDetachedFromWindow(holder)
    }

    override fun getItemCount(): Int {
        return contestList.size
    }

    override fun onViewRecycled(holder: MyViewHolder) {
        super.onViewRecycled(holder)
        holder.cT.cancel()
    }

    private fun sendToLoginScreen() {
        val intent = Intent(context, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
    }

    private fun sendForCreateVideo() {
        val bundle = Bundle()
        bundle.putBoolean(ConstValFile.IsFromContest, true)
        bundle.putBoolean(ConstValFile.isFromDuet, false)
        sessionManager.setIsFromContest(true)
        val intent = Intent(context, SnapCameraActivity::class.java)
        intent.putExtra(ConstValFile.Bundle, bundle)
        context.startActivity(intent)
    }

    private fun sendToContestDetailsActivity(bundle: Bundle) {
        val intent = Intent(context, ContestDetailsActivity::class.java)
        intent.putExtra(ConstValFile.Bundle,bundle)
        context.startActivity(intent)
    }

    private fun checkInternetDialog() {
        val sweetAlertDialog = SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
        sweetAlertDialog.titleText = "Internet"
        sweetAlertDialog.contentText = ConstValFile.Check_Connection
        sweetAlertDialog.confirmText = "OK"
        sweetAlertDialog.setConfirmClickListener {
            sweetAlertDialog.dismiss()
        }
        sweetAlertDialog.show()
    }
    private fun sendToAddAmountActivity() {
        val intent = Intent(context, AddAmountActivity::class.java)
        context.startActivity(intent)
    }
}