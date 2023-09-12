package com.img.audition.adapters


import VideoHandle.EpEditor
import VideoHandle.OnEditorListener
import android.annotation.SuppressLint
import android.app.Activity
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
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.gson.JsonObject
import com.img.audition.R
import com.img.audition.dataModel.CommanResponse
import com.img.audition.dataModel.MusicData
import com.img.audition.databinding.MusiclistrecycledesignBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.APITags
import com.img.audition.network.ApiInterface
import com.img.audition.network.RetrofitClient
import com.img.audition.network.SessionManager
import com.img.audition.screens.CompilerActivity
import com.img.audition.snapCameraKit.SnapCameraActivity
import com.img.audition.videoWork.PlayPauseAudio
import com.img.audition.videoWork.VideoCacheWork
import com.masoudss.lib.SeekBarOnProgressChanged
import com.masoudss.lib.WaveformSeekBar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList


@UnstableApi
class MusicAdapter(val contextFromActivity: Context, private var musicList: ArrayList<MusicData>) :
    RecyclerView.Adapter<MusicAdapter.MyMusicHolder>() {

    val playerExo = ExoPlayer.Builder(contextFromActivity).build()
    private val sessionManager by lazy {
        SessionManager(contextFromActivity)
    }
    var startTrimFrom = 0
    private val mediaSource by lazy {
        ProgressiveMediaSource.Factory(
            CacheDataSource.Factory()
                .setCache(VideoCacheWork.simpleCache)
                .setUpstreamDataSourceFactory(
                    DefaultHttpDataSource.Factory()
                        .setUserAgent("ExoPlayer").setAllowCrossProtocolRedirects(true)
                )
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        )
    }
    private val TAG = "MusicAdapter"

    inner class MyMusicHolder(itemView: MusiclistrecycledesignBinding) :
        RecyclerView.ViewHolder(itemView.root) {

        val mImage = itemView.img
        val mName = itemView.songname
        val sName = itemView.singername
        val playBtn = itemView.playMusic
        val pauseBtn = itemView.pauseMusic
        val addFavMusic = itemView.addFavMusic
        val audioExoPlayer = itemView.audioPlayerView

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyMusicHolder {
        val itemBinding = MusiclistrecycledesignBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyMusicHolder(itemBinding)
    }

    override fun getItemCount(): Int {
        return musicList.size
    }


    override fun onBindViewHolder(holder: MyMusicHolder, position: Int) {
        holder.apply {
            val audioData = musicList[position]
            Glide.with(contextFromActivity).load(APITags.ADMINBASEURL + audioData.Image)
                .placeholder(R.drawable.ic_music).into(mImage)
            mName.text = audioData.title.toString()
            sName.text = audioData.subtitle.toString()

            playBtn.setOnClickListener {
                for (play in musicList) {
                    play.isPlay = false
                }
                audioData.isPlay = true
                notifyDataSetChanged()

                Log.d("urlAudioMA", APITags.ADMINBASEURL + audioData.trackAacFormat.toString())
                val mediaItem =
                    MediaItem.fromUri(Uri.parse(APITags.ADMINBASEURL + audioData.trackAacFormat.toString()))
                val audioMediaSource = mediaSource.createMediaSource(mediaItem)
                audioExoPlayer.player = playerExo
                playerExo.setMediaSource(audioMediaSource)
                playerExo.prepare()
                playerExo.playWhenReady = true
                playBtn.visibility = View.GONE
                pauseBtn.visibility = View.VISIBLE
            }

            if (musicList[position].isPlay) {
                playBtn.visibility = View.GONE
                pauseBtn.visibility = View.VISIBLE
            } else {
                playBtn.visibility = View.VISIBLE
                pauseBtn.visibility = View.GONE
            }

            if (audioData.isFav) {
                addFavMusic.setImageDrawable(
                    ContextCompat.getDrawable(
                        contextFromActivity,
                        R.drawable.liked_ic_music
                    )
                )
                addFavMusic.setColorFilter(contextFromActivity.resources.getColor(R.color.white))
            } else {
                addFavMusic.setImageDrawable(
                    ContextCompat.getDrawable(
                        contextFromActivity,
                        R.drawable.like_ic_music
                    )
                )
                addFavMusic.setColorFilter(contextFromActivity.resources.getColor(R.color.white))
            }

            addFavMusic.setOnClickListener {
                val songID = audioData.Id.toString()
                if (audioData.isFav) {
                    addFavMusic.setImageDrawable(
                        ContextCompat.getDrawable(
                            contextFromActivity,
                            R.drawable.like_ic_music
                        )
                    )
                    addFavMusic.setColorFilter(contextFromActivity.resources.getColor(R.color.white))
                    audioData.isFav = false
                    addFavMusic(songID)
                } else {
                    audioData.isFav = true
                    addFavMusic.setImageDrawable(
                        ContextCompat.getDrawable(
                            contextFromActivity,
                            R.drawable.liked_ic_music
                        )
                    )
                    addFavMusic.setColorFilter(contextFromActivity.resources.getColor(R.color.white))
                    addFavMusic(songID)
                }


            }

            pauseBtn.setOnClickListener {
                if (playerExo.isPlaying) {
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
                val songID = audioData.Id.toString()
                showAudioTrimDialog(
                    audioData.title.toString(),
                    audioData.subtitle.toString(),
                    APITags.ADMINBASEURL + audioData.Image,
                    APITags.ADMINBASEURL + audioData.trackAacFormat.toString(),
                    songID
                )
            }

        }
    }

    fun onActivityStateChanged(): PlayPauseAudio {
        return object : PlayPauseAudio {
            override fun stop() {
                playerExo.seekTo(0)
                playerExo.pause()
                playerExo.playWhenReady = false
                playerExo.release()

            }

            override fun play() {}

        }
    }

    private fun showAudioTrimDialog(
        title: String,
        subTitle: String,
        image: String,
        audioFile: String,
        songID: String
    ) {
        val audioSheet =
            BottomSheetDialog(contextFromActivity, R.style.CustomBottomSheetDialogTheme)
        audioSheet.setCanceledOnTouchOutside(false)
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

        progressTrimMusic!!.visibility = View.VISIBLE

        val songTimeHandler = Handler()

        Glide.with(contextFromActivity).load(image).placeholder(R.drawable.ic_music)
            .into(audioImage!!)
        audioName!!.text = title
        audioSingerName!!.text = subTitle

        val byteThread = Thread {
            try {
                audioWaveSeekbar!!.setSampleFrom(audioFile)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        byteThread.start()


        val audioThread = Thread {
            audioPlayer.setDataSource(audioFile)
            audioPlayer.prepareAsync()
            audioPlayer.setOnPreparedListener { mediaPlayer ->
                mediaPlayer.start()
                progressTrimMusic.visibility = View.GONE
                audioWaveSeekbar!!.maxProgress = audioPlayer.duration.toFloat()
                (contextFromActivity as Activity).runOnUiThread(object : Runnable {
                    override fun run() {

                        try {
                            if (mediaPlayer.isPlaying)
                                audioWaveSeekbar.progress = mediaPlayer.currentPosition.toFloat()
                        }catch (e:java.lang.Exception){
                            Log.e(TAG, "run: ${e.message}")
                        }


//                        if (trimFrom!!.text.trim() == trimTo!!.text.trim()){ }
                        songTimeHandler.postDelayed(this, 1000)
                    }

                })

            }
        }
        audioThread.start()



        audioSheet.setOnDismissListener {
            try {
                if (audioPlayer != null && audioPlayer.isPlaying) {
                    audioPlayer.stop()
                }
            } catch (e: Exception) {
                    e.printStackTrace()
            }
        }

        try {

            /* val map = hashMapOf<Float,String>()
             map[audioWaveSeekbar!!.maxProgress/2] = "The middle"
             audioWaveSeekbar.marker = map*/

            audioWaveSeekbar!!.onProgressChanged = object : SeekBarOnProgressChanged {
                override fun onProgressChanged(
                    waveformSeekBar: WaveformSeekBar,
                    progress: Float,
                    fromUser: Boolean
                ) {
                    val fromTrimDuration = getTimeString(audioPlayer.currentPosition.toLong())!!
                    trimFrom!!.text = fromTrimDuration

                    Log.d(TAG, "onProgressChanged: isAppAudio ${sessionManager.getIsAppAudio()}")

                    if (sessionManager.getIsAppAudio()){
                        val toTrimDuration = "${getTimeString(25000L)}"
                        trimTo!!.text = toTrimDuration
                    }else{
                        val toTrimDuration = "${getTimeString(sessionManager.getCreateVideoDuration())}"
                        trimTo!!.text = toTrimDuration
                    }

                    val map = hashMapOf<Float, String>()
                    map[audioWaveSeekbar.progress] = ""
                    audioWaveSeekbar.marker = map

                    /* val scrollX = calculateScrollPosition(audio15SecFrame!!,progress.toInt(),audioPlayer.duration)
                     audio15SecFrame.post {
                         audio15SecFrame.scrollTo(scrollX,0)
                     }*/
                    if (fromUser) {
                        audioPlayer.seekTo(progress.toInt())
                        startTrimFrom = progress.toInt()
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

//        audioPlayer.setOnCompletionListener { m ->
//            m.start()
//        }

        closeAudioSheetBtn!!.setOnClickListener {
            progressTrimMusic!!.visibility = View.GONE
            audioPlayer.release()
            audioSheet.dismiss()
            audioThread.interrupt()
            songTimeHandler.removeCallbacksAndMessages(null)
            byteThread.interrupt()
        }

        trimAndUseBtn!!.setOnClickListener {

            val fromTrimDuration = startTrimFrom
            val totalAudioDuration = audioPlayer.duration
            var toTrimDuration = sessionManager.getCreateVideoDuration()
            if (sessionManager.getIsAppAudio()){
                toTrimDuration =  25000L
            }

            if (toTrimDuration > totalAudioDuration) {
                Toast.makeText(contextFromActivity, "Reselect Audio Trim Position", Toast.LENGTH_SHORT).show()
            } else {
                progressTrimMusic!!.visibility = View.VISIBLE
                TrimAudio(audioFile, fromTrimDuration.toLong(), toTrimDuration, songID)
                audioPlayer.pause()
                audioPlayer.stop()
                songTimeHandler.removeCallbacksAndMessages(null)
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
        toTrimDuration: Long,
        songID: String
    ) {
        val trimFilePath = createFileAndFolder()
        val trimFrom = audioTrimFromSec / 1000
        val trimTo = toTrimDuration / 1000


        val cmd =
            "-y -i $audioFile -ss $trimFrom -t $trimTo -acodec copy -preset veryfast -threads 8 $trimFilePath"

        EpEditor.execCmd(cmd, 0, object : OnEditorListener {
            override fun onSuccess() {
                sessionManager.setCreateAudioSession(trimFilePath)
                sessionManager.setAppSongID(songID)

                if (sessionManager.getIsAppAudio()){
                    val bundle = Bundle()
                    bundle.putString(ConstValFile.AppAudio,trimFilePath)
                    contextFromActivity.startActivity(
                        Intent(contextFromActivity.applicationContext, SnapCameraActivity::class.java)
                            .putExtra(ConstValFile.Bundle, bundle).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    )
                }else{
                    val bundle = Bundle()
                    bundle.putString(ConstValFile.CompileTask, ConstValFile.TaskMuxing)
                    contextFromActivity.startActivity(
                        Intent(contextFromActivity.applicationContext, CompilerActivity::class.java)
                            .putExtra(ConstValFile.Bundle, bundle)
                    )
                }



            }

            override fun onFailure() {}

            override fun onProgress(progress: Float) {}
        })

        /*FFmpegKit.executeAsync(cmd,
            { session ->
                val state = session.state
                val returnCode = session.returnCode
                if (ReturnCode.isSuccess(returnCode)){
                    myApplication.printLogD("TrimAudio Complete","TrimAudio")
                    sessionManager.setCreateAudioSession(trimFilePath)
                    sessionManager.setAppSongID(songID)
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
        }*/
    }

    private fun createFileAndFolder(): String {
        val timestamp = System.currentTimeMillis()
        val filename = "$timestamp.aac"
        val appData = contextFromActivity.getExternalFilesDir(null)

        val createFile = File(appData, filename)
        if (!(createFile.exists())) {
            try {
                createFile.createNewFile()
            } catch (i: IOException) {
                i.printStackTrace()
            }
        }

        return createFile.absolutePath

    }

    fun filterList(filterList: ArrayList<MusicData>) {
        musicList = filterList
        notifyDataSetChanged()
    }

    fun getLE2(buffer: ByteArray): Long {
        var value = buffer[1].toLong() and 0xFF
        value = (value shl 8) + (buffer[0].toLong() and 0xFF)
        return value
    }

    private fun getByteArrayOfAudio(
        audioFile: String,
        audioWaveSeekbar: WaveformSeekBar?
    ): IntArray {
        var waveArray = intArrayOf()
        Log.d("audioArray", "LoadAudio: in fun " + waveArray.size)
        Thread {
            try {
                Log.d("audioArray", "LoadAudio: in fun " + waveArray.size)
                val inputStream = URL(audioFile).openStream()
                var read: Int
                val bytes_tmp = ByteArray(44)
                read = inputStream.read(bytes_tmp, 0, bytes_tmp.size)
                val bytes = ByteArray(2)
                var longtmp: Long
                while (read != -1) {
                    read = inputStream.read(bytes, 0, bytes.size)
                    longtmp = getLE2(bytes)
                    waveArray += longtmp.toInt()
                    Log.d("audioArray", "LoadAudio: in fun " + waveArray.size)
                }
                Log.d("audioArray", "LoadAudio: in fun " + waveArray.size)
                inputStream.close()
            } catch (e: java.lang.Exception) {
                Log.d("audioArray", "LoadAudio: in fun $e")
            }
        }.start()

        Log.d("audioArray", "LoadAudio: in fun " + waveArray.size)
        audioWaveSeekbar?.setSampleFrom(waveArray)
        return waveArray
    }

    fun addFavMusic(songID: String) {
        val favMusicobj = JsonObject()
        favMusicobj.addProperty("favMusic", songID)
        val apiInterface =  RetrofitClient.getInstance().create(ApiInterface::class.java)
        val favMusicReq = apiInterface.addFavMusic(sessionManager.getToken(), favMusicobj)

        favMusicReq.enqueue(object : Callback<CommanResponse> {
            override fun onResponse(
                call: Call<CommanResponse>,
                response: Response<CommanResponse>
            ) {

            }

            override fun onFailure(call: Call<CommanResponse>, t: Throwable) {
                    t.printStackTrace()
            }

        })
    }

    private fun calculateScrollPosition(
        horizontalScrollView: HorizontalScrollView,
        currentPosition: Int,
        audioDuration: Int
    ): Int {
        val contentWidth = horizontalScrollView.getChildAt(0).width
        val scrollViewWidth = horizontalScrollView.width
        val maxScroll = contentWidth - scrollViewWidth

        val positionRatio = currentPosition.toFloat() / audioDuration.toFloat()
        val scrollPosition = (maxScroll * positionRatio).toInt()

        return scrollPosition
    }

}

