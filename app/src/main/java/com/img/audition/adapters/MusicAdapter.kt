package com.img.audition.adapters


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.img.audition.R
import com.img.audition.dataModel.MusicData
import com.img.audition.databinding.MusiclistrecycledesignBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.SessionManager
import com.img.audition.screens.CompilerActivity
import com.img.audition.videoWork.PlayPauseAudio
import com.img.audition.videoWork.VideoCacheWork
import com.masoudss.lib.SeekBarOnProgressChanged
import com.masoudss.lib.WaveformSeekBar
import java.io.File
import java.io.IOException
import java.util.*
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlaybackException.TYPE_SOURCE
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode

@UnstableApi
class MusicAdapter(val contextFromActivity: Context, private var musicList: ArrayList<MusicData>) : RecyclerView.Adapter<MusicAdapter.MyMusicHolder>() {

    val songTimeHandler = Handler()
    val playerExo = ExoPlayer.Builder(contextFromActivity).build()
    private val myApplication by lazy { MyApplication(contextFromActivity) }
    private val sessionManager by lazy {
        SessionManager(contextFromActivity)
    }
    private val mediaSource by lazy {
        ProgressiveMediaSource.Factory(
            CacheDataSource.Factory()
                .setCache(VideoCacheWork.simpleCache)
                .setUpstreamDataSourceFactory(
                    DefaultHttpDataSource.Factory()
                        .setUserAgent("ExoPlayer"))
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR))
    }
    val TAG = "MusicAdapter"
    inner class MyMusicHolder(itemView: MusiclistrecycledesignBinding) : RecyclerView.ViewHolder(itemView.root) {

        val mImage = itemView.img
        val mName = itemView.songname
        val sName = itemView.singername
        val playBtn = itemView.playMusic
        val pauseBtn = itemView.pauseMusic
         val audioExoPlayer = itemView.audioPlayerView

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyMusicHolder {
        val itemBinding = MusiclistrecycledesignBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyMusicHolder(itemBinding)
    }

    override fun getItemCount(): Int {
        return musicList.size
    }


    override fun onBindViewHolder(holder: MyMusicHolder, position: Int) {
        holder.apply {
            val audioData = musicList[position]
            Glide.with(contextFromActivity).load(ConstValFile.BASEURL+audioData.Image).placeholder(R.drawable.ic_music).into(mImage)
            mName.text = audioData.title.toString()
            sName.text  = audioData.subtitle.toString()

            playBtn.setOnClickListener {
                    for (play in musicList){
                        play.isPlay = false
                    }
                audioData.isPlay = true
                notifyDataSetChanged()




                val mediaItem = MediaItem.fromUri(Uri.parse(ConstValFile.BASEURL+audioData.trackAacFormat.toString()))
                val audioMediaSource = mediaSource.createMediaSource(mediaItem)
                audioExoPlayer.player = playerExo
                playerExo.setMediaSource(audioMediaSource)
                playerExo.prepare()
                playerExo.playWhenReady = true
                playBtn.visibility = View.GONE
                pauseBtn.visibility = View.VISIBLE
            }

            if (musicList[position].isPlay){
                playBtn.visibility = View.GONE
                pauseBtn.visibility = View.VISIBLE
            }else{
                playBtn.visibility = View.VISIBLE
                pauseBtn.visibility = View.GONE
            }

            pauseBtn.setOnClickListener {
                if (playerExo.isPlaying){
                    playerExo.stop()
                    playBtn.visibility = View.VISIBLE
                    pauseBtn.visibility = View.GONE
                }
            }


            itemView.setOnClickListener {

                playerExo.seekTo(0)
                playerExo.pause()
                playerExo.stop()
                playerExo.playWhenReady = false
                playBtn.visibility = View.VISIBLE
                pauseBtn.visibility = View.GONE
                myApplication.printLogD(ConstValFile.BASEURL+audioData.trackAacFormat.toString(),"Audio url")
                showAudioTrimDialog(audioData.title.toString(),audioData.subtitle.toString(),ConstValFile.BASEURL+audioData.Image,ConstValFile.BASEURL+audioData.trackAacFormat.toString())
            }

        }
    }

    fun onActivityStateChanged() : PlayPauseAudio {
        return object : PlayPauseAudio {
            override fun stop() {
                myApplication.printLogD("stop: ",TAG)
                playerExo.seekTo(0)
                playerExo.pause()
                playerExo.playWhenReady = false
                playerExo.release()
            }
            override fun play() {
                myApplication.printLogD("play: ",TAG)
            }

        }
    }

    private fun showAudioTrimDialog(title: String, subTitle: String, image: String,audioFile:String) {
        val audioSheet = BottomSheetDialog(contextFromActivity,R.style.CustomBottomSheetDialogTheme)
        audioSheet.setContentView(R.layout.audio_trim_sheet_design)

        val audioPlayer = MediaPlayer()
        audioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)

        var audioTrimFromSec = 0L
        val audioImage = audioSheet.findViewById<ImageView>(R.id.audioImage)
        val audioName = audioSheet.findViewById<TextView>(R.id.audioName)
        val audioSingerName = audioSheet.findViewById<TextView>(R.id.audioSingerName)
        val closeAudioSheetBtn = audioSheet.findViewById<ImageView>(R.id.closeAudioSheetBtn)
        val trimAndUseBtn = audioSheet.findViewById<TextView>(R.id.trimAndUseBtn)
        val audio15SecFrame = audioSheet.findViewById<HorizontalScrollView>(R.id.audio15SecFrame)
        val audioWaveSeekbar = audioSheet.findViewById<WaveformSeekBar>(R.id.audioWaveSeekbar)
        val progressTrimMusic = audioSheet.findViewById<CircularProgressIndicator>(R.id.progressTrimMusic)
        val playPauseMusic = audioSheet.findViewById<ImageView>(R.id.playPauseMusic)
        val trimFrom = audioSheet.findViewById<TextView>(R.id.trimFrom)
        val trimTo = audioSheet.findViewById<TextView>(R.id.trimTo)

        Glide.with(contextFromActivity).load(image).placeholder(R.drawable.ic_music).into(audioImage!!)
        audioName!!.text = title
        audioSingerName!!.text = subTitle

        val byteThread = Thread {
            try { audioWaveSeekbar!!.setSampleFrom( audioFile) }
            catch (e: Exception) { e.printStackTrace() }
        }
        byteThread.start()

        audioPlayer.setDataSource(audioFile)
        audioPlayer.prepareAsync()
        audioPlayer.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.start()
            audioWaveSeekbar!!.maxProgress = audioPlayer.duration.toFloat()
        }

        audioWaveSeekbar!!.progress = audioPlayer.currentPosition.toFloat()


        try {
           myApplication.printLogI(audioPlayer.duration.toString(),TAG+"duration")
           audioWaveSeekbar!!.onProgressChanged = object : SeekBarOnProgressChanged {
               override fun onProgressChanged(waveformSeekBar: WaveformSeekBar, progress: Float, fromUser: Boolean) {
                   val fromTrimDuration = getTimeString(audioPlayer.currentPosition.toLong())!!
                   val toTrimDuration = getTimeString(audioPlayer.currentPosition.toLong()+sessionManager.getCreateVideoDuration())
                   audioPlayer.seekTo(progress.toInt())
                   trimFrom!!.text = fromTrimDuration
                   trimTo!!.text = toTrimDuration
               }
           }
       }catch (e:java.lang.Exception){
           myApplication.printLogD(e.toString(),TAG)
       }

        audioPlayer.setOnCompletionListener {m->
            m.start()
        }

        closeAudioSheetBtn!!.setOnClickListener {
            progressTrimMusic!!.visibility = View.GONE
            audioPlayer.pause()
            audioPlayer.stop()
            audioPlayer.release()
            audioSheet.dismiss()
        }

        trimAndUseBtn!!.setOnClickListener {

            val fromTrimDuration = audioPlayer.currentPosition.toLong()
            val totalAudioDuration = audioPlayer.duration
            val trimAudioLength = audioPlayer.currentPosition.toLong()+sessionManager.getCreateVideoDuration()
            if (trimAudioLength>totalAudioDuration){
                Toast.makeText(contextFromActivity,"Reselect Audio Trim Position",Toast.LENGTH_SHORT).show()
            }else{
                progressTrimMusic!!.visibility = View.VISIBLE
                TrimAudio(audioFile,fromTrimDuration ,sessionManager.getCreateVideoDuration())
                audioPlayer.pause()
                audioPlayer.stop()
                audioPlayer.release()
            }
        }
        audioSheet.show()
    }



    @SuppressLint("DefaultLocale")
    private fun getTimeString(millis: Long): String? {
        val buf = StringBuffer()
        val minutes = (millis % (1000 * 60 * 60) / (1000 * 60)).toInt()
        val seconds = (millis % (1000 * 60 * 60) % (1000 * 60) / 1000).toInt()
        buf
            .append(String.format("%02d", minutes))
            .append(".")
            .append(String.format("%02d", seconds))
        return buf.toString()
    }

    private fun TrimAudio(
        audioFile: String,
        audioTrimFromSec: Long,
        createVideoDuration: Long
    )
    {
        val trimFilePath = createFileAndFolder()

        val endPosition = audioTrimFromSec+createVideoDuration
        val firstPosition = audioTrimFromSec/1000
        myApplication.printLogD("VideoLength ${createVideoDuration/1000}","TrimAudio")
        myApplication.printLogD("firstPosition $firstPosition","TrimAudio")
        myApplication.printLogD("endPosition ${endPosition/1000}","TrimAudio")
        myApplication.printLogD("trimFilePath $trimFilePath","TrimAudio")


        val cmd =
            "-y -i $audioFile -ss $firstPosition -t ${createVideoDuration/1000} -vcodec copy $trimFilePath"

        /*EpEditor.execCmd(cmd,0,object : OnEditorListener {
            override fun onSuccess() {
                myApplication.printLogD("TrimAudio Complete","TrimAudio")
                sessionManager.setCreateAudioSession(trimFilePath)
                val bundle = Bundle()
                bundle.putString(ConstValFile.CompileTask,ConstValFile.TaskMuxing)
                contextFromActivity.startActivity(Intent(contextFromActivity.applicationContext,CompilerActivity::class.java)
                    .putExtra(ConstValFile.Bundle,bundle))

            }
            override fun onFailure() {
                myApplication.printLogD("TrimAudio : onFailure","TrimAudio")
            }
            override fun onProgress(progress: Float) {
                myApplication.printLogD("TrimAudio onProgress : $progress","TrimAudio")
            }
        })*/

        FFmpegKit.executeAsync(cmd,
            { session ->
                val state = session.state
                val returnCode = session.returnCode
                if (ReturnCode.isSuccess(returnCode)){
                    myApplication.printLogD("TrimAudio Complete","TrimAudio")
                    sessionManager.setCreateAudioSession(trimFilePath)
                    val bundle = Bundle()
                    bundle.putString(ConstValFile.CompileTask,ConstValFile.TaskMuxing)
                    contextFromActivity.startActivity(Intent(contextFromActivity.applicationContext,CompilerActivity::class.java)
                        .putExtra(ConstValFile.Bundle,bundle))
                }
                // CALLED WHEN SESSION IS EXECUTED
                Log.i("TrimAudio", String.format("FFmpeg process exited with state %s and rc %s.%s",
                    state, returnCode,  session.failStackTrace))
            },
            {
                myApplication.printLogD("log : $it","TrimAudio")
            })
        {
            myApplication.printLogD("statistics : $it","TrimAudio")
        }
    }

    private fun createFileAndFolder():String{
        val timestamp = System.currentTimeMillis()
        val filename = "$timestamp.aac"
        val appData = contextFromActivity.getExternalFilesDir(null)
        myApplication.printLogD(appData!!.absolutePath,TAG)

        val createFile = File(appData,filename)
        if (!(createFile.exists())){
            try {
                createFile.createNewFile()
                myApplication.printLogD(createFile.absolutePath,TAG)
            }catch (i: IOException){
                myApplication.printLogE(i.toString(),TAG)
            }
        }

        return createFile.absolutePath

    }

    fun filterList(filterList: ArrayList<MusicData>) {
        musicList = filterList
        notifyDataSetChanged()
    }
}


