package com.img.audition.screens



import VideoHandle.EpEditor
import VideoHandle.OnEditorListener
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.media.*
import android.net.Uri
import android.os.Bundle
import android.text.TextPaint
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import cn.pedant.SweetAlert.SweetAlertDialog
import com.img.audition.databinding.ActivityCompilerBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.SessionManager
import com.img.audition.snapCameraKit.SnapPreviewActivity
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer


@UnstableApi
class CompilerActivity : AppCompatActivity() {
    val TAG = "CompilerActivity"
    val TARCK = "check 100"
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityCompilerBinding.inflate(layoutInflater)
    }
    private val sessionManager by lazy {
        SessionManager(this@CompilerActivity)
    }

    private val myApplication by lazy {
        MyApplication(this@CompilerActivity)
    }

    private val bundle by lazy {
        intent.getBundleExtra(ConstValFile.Bundle)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
    }

    override fun onResume() {
        when(bundle!!.getString(ConstValFile.CompileTask)){
            ConstValFile.TaskMuxing ->{
                audioVideoMuxing()
            }
            ConstValFile.ColorFilter ->{
                applyColorFilter()
            }
            ConstValFile.AddText ->{
                addTextInVideo()

            }
            ConstValFile.SlowVideo->{
                slowVideo()
            }
            ConstValFile.FastVideo->{
                fastVideo()
            }ConstValFile.NormalVideo->{
                normalVideo()
            }
            ConstValFile.CompressVideo->{
                compressVideo()
            }
        }
        super.onResume()
    }

    private fun normalVideo() {
        val outputPath = createFileAndFolder()
        val originalVideoPath = sessionManager.getCreateVideoPath()

        val cmd2 = "-y -i $originalVideoPath -filter_complex [0:v]setpts=1.0*PTS[v];[0:a]atempo=0.25[a] -map [v] -map [a] -preset veryfast -threads 6 $outputPath"

        val cmd = "-y -i $originalVideoPath -filter:v setpts=1.0*PTS  $outputPath"
        funComipler(cmd2,outputPath)
    }

    private fun fastVideo() {
        val outputPath = createFileAndFolder()
        val originalVideoPath = sessionManager.getCreateVideoPath()
        val cmd2 = "-y -i $originalVideoPath -filter_complex [0:v]setpts=0.5*PTS[v];[0:a]atempo=2.0[a] -map [v] -map [a] $outputPath"

        val cmd = "-y -i $originalVideoPath -filter:v setpts=0.5*PTS  $outputPath"
        funComipler(cmd2,outputPath)
    }

    private fun slowVideo() {
        val outputPath = createFileAndFolder()
        val originalVideoPath = sessionManager.getCreateVideoPath()

//        val cmd1 = "-y -fflags +genpts -r 15 -i raw.h264 -i $originalVideoPath -map 0:v -c:v copy -map 1:a -af atempo=0.5 -movflags faststart $outputPath"

        val cmd2 = "-y -i $originalVideoPath -filter_complex [0:v]setpts=2.0*PTS[v];[0:a]atempo=0.5[a] -map [v] -map [a] -preset veryfast -threads 6 $outputPath"
//        val cmd = "-y -i $originalVideoPath -filter:v setpts=2.0*PTS  $outputPath"
        funComipler(cmd2,outputPath)
    }

    private fun addTextInVideo() {
        val outputPath = createFileAndFolder()
        val videoDuration = sessionManager.getCreateVideoDuration()
        val originalVideoPath = sessionManager.getCreateVideoPath()
        val fontPath = filesDir.absolutePath +  File.separator + ConstValFile.FONT + File.separator + ConstValFile.DEFAULT_FONT
        val text = bundle!!.getString(ConstValFile.VideoText)
        val textSize = bundle!!.getString(ConstValFile.VideoTextSize)
        val textColor = bundle!!.getString(ConstValFile.VideoTextColor)
        val x = bundle!!.getString(ConstValFile.VideoTextXpos)
        val y = bundle!!.getString(ConstValFile.VideoTextYpos)
        val textAlignCmd = bundle!!.getString(ConstValFile.TextAlignCmd)
        myApplication.printLogD("originalVideoPath :$originalVideoPath","AddTextInVideo")
        myApplication.printLogD("outputPath :$outputPath","AddTextInVideo")
        myApplication.printLogD("fontPath :$fontPath","AddTextInVideo")
        myApplication.printLogD("text :$text","AddTextInVideo")
        myApplication.printLogD("textSize :$textSize","AddTextInVideo")
        myApplication.printLogD("textColor :$textColor","AddTextInVideo")
        myApplication.printLogD("VideoTextXpos :$x","AddTextInVideo")
        myApplication.printLogD("VideoTextYpos :$y","AddTextInVideo")

               val cmdww = "-y -i $originalVideoPath -i $text -filter_complex [0:v][1:v]overlay=x=(W-w)/2:y=(H-h)/2[out] -map [out] -c:a copy $outputPath"

//        val cmd = "-y -i $originalVideoPath -vf drawtext=fontfile=$fontPath:text=$text:fontsize=$textSize:fontcolor=$textColor:$textAlignCmd -c:v libx264 -preset fast -crf 12 -c:a copy $outputPath"
//       val cmd = "-y -i $originalVideoPath -i $text -filter_complex [0:v][1:v]overlay=x=$x:y=$y[out] -map [out] -c:a copy $outputPath"
//      val tOcmd = "-y -loop 1 -i $text -filter_complex [1:v]overlay=x=$x:y=$y -t ${videoDuration/1000}  -pix_fmt yuv420p $outputPath"

        val cmd2 = "-y -i $originalVideoPath -i $text -filter_complex [0:v][1:v]overlay=x=$x:y=$y[out] -map [out] -preset veryfast -threads 6 $outputPath"

        val cmmmmddd = "-y -i $originalVideoPath -i $text -filter_complex overlay=$x:$y -preset veryfast -threads 6 $outputPath"
//        addTextCompiler(tOcmd,outputPath)

        funComipler(cmmmmddd,outputPath)

    }

    private fun applyColorFilter() {
        val outputPath = createFileAndFolder()
        val originalVideoPath = sessionManager.getCreateVideoPath()
        //                                                        "R    :R    :R"    ":G    :G    :G"   ":B     :B    :B"
        val cmd = "-y -i $originalVideoPath -vf colorchannelmixer=0.393:0.769:0.189:0.349:0.686:0.168:0.272:0.534:0.131 $outputPath"


        funComipler(cmd,outputPath)
    }

    private fun audioVideoMuxing() {
        val outputPath = createFileAndFolder()
        val trimAudioPath = sessionManager.getTrimAudioPath()
        val originalVideoPath = sessionManager.getCreateVideoPath()


        val cmd1  =   "-y -i $originalVideoPath -i $trimAudioPath -c copy -map 0:v:0 -map 1:a:0 $outputPath"


        val cmd = "-y -i $originalVideoPath -i $trimAudioPath -c:v copy -c:a aac -map 0:v:0 -map 1:a:0 $outputPath"
        funComipler(cmd1,outputPath)
    }


    fun addTextCompiler(cmd: String, overTextPath: String){
       /* EpEditor.execCmd(cmd,0, object : OnEditorListener {
            override fun onSuccess() {
                myApplication.printLogD("Add Text Compile Complete",TAG)
                val outputPath = createFileAndFolder()
                val videoDuration = sessionManager.getCreateVideoDuration()
                val x = bundle!!.getString(ConstValFile.VideoTextXpos)
                val y = bundle!!.getString(ConstValFile.VideoTextYpos)
                val originalVideoPath = sessionManager.getCreateVideoPath()
                val cmd2 = "-y -i $originalVideoPath -i $overTextPath -filter_complex [0:v][1:v] overlay=enable=between(t,0,${videoDuration/1000}):x=$x:y=$y [out] -map [out] -map 0:a -c:a copy -c:v libx264 -preset veryfast -crf 18 -pix_fmt yuv420p $outputPath"
                val cmd1 = "-y -i $originalVideoPath -i $overTextPath -filter_complex [0:v][1:v] overlay=enable='between(t,1,${videoDuration/1000})' [out] -map [out] -map 0:a? -c:a copy -c:v libx264 -preset veryfast -crf 18 -pix_fmt yuv420p $outputPath"
                funComipler(cmd2,outputPath)
            }
            override fun onFailure() {
                myApplication.printLogD("Add Text  Compile : onFailure",TAG)
            }
            override fun onProgress(progress: Float) {
                myApplication.printLogD(" Add Text Compile onProgress : $progress",TAG)
            }
        })*/

         /*FFmpegKit.executeAsync(cmd,
           { session ->
               val state = session.state
               val returnCode = session.returnCode
               if (ReturnCode.isSuccess(returnCode)){
                   myApplication.printLogD("Compile Complete",TAG)
                   sessionManager.setCreateVideoPath(outputPath)
                   startActivity(Intent(this@CompilerActivity,PreviewActivity::class.java))
                   finish()
               }
               // CALLED WHEN SESSION IS EXECUTED
               Log.d(TAG, String.format("FFmpeg process exited with state %s and rc %s.%s",
                   state, returnCode,  session.failStackTrace))
           },
           {
               myApplication.printLogD("log : $it","ffmpeg")
           })
       {
           myApplication.printLogD("statistics : $it","ffmpeg")
       }*/
    }

    fun funComipler(cmd: String, outputPath: String){
        EpEditor.execCmd(cmd,0, object : OnEditorListener {
            override fun onSuccess() {
                myApplication.printLogD("Compile Complete",TAG)

                val retriever =  MediaMetadataRetriever()
                retriever.setDataSource(this@CompilerActivity, Uri.parse(outputPath));
                val  time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                retriever.release()
                myApplication.printLogD("selectVideoDuration : $time" ,TAG)
                val selectVideoDuration = time!!.toLong()
                sessionManager.setCreateVideoPath(outputPath)
                sessionManager.setCreateVideoDuration(selectVideoDuration)
                startActivity(Intent(this@CompilerActivity, SnapPreviewActivity::class.java))
                finish()
            }
            override fun onFailure() {
                myApplication.printLogD("Compile : onFailure",TAG)
                myApplication.showToast("Failed, Try Again..")
                startActivity(Intent(this@CompilerActivity,SnapPreviewActivity::class.java))
                finish()
            }
            override fun onProgress(progress: Float) {
                myApplication.printLogD("Compile onProgress : $progress",TAG)
            }
        })

        /*FFmpegKit.executeAsync(cmd,
          { session ->
              val state = session.state
              val returnCode = session.returnCode
              if (ReturnCode.isSuccess(returnCode)){
                  myApplication.printLogD("Compile Complete",TAG)
                  sessionManager.setCreateVideoPath(outputPath)
                  startActivity(Intent(this@CompilerActivity,PreviewActivity::class.java))
                  finish()
              }
              // CALLED WHEN SESSION IS EXECUTED
              Log.d(TAG, String.format("FFmpeg process exited with state %s and rc %s.%s",
                  state, returnCode,  session.failStackTrace))
          },
          {
              myApplication.printLogD("log : $it","ffmpeg")
          })
      {
          myApplication.printLogD("statistics : $it","ffmpeg")
      }*/
    }

    private fun createFileAndFolder():String{
        val timestamp = System.currentTimeMillis()
        val filename = "$timestamp.mp4"
        val appData = getExternalFilesDir(null)

        val createFile = File(appData,filename)
        if (!(createFile.exists())){
            try {
                createFile.createNewFile()
            }catch (i: IOException){
                myApplication.printLogE(i.toString(), TAG)
            }
        }

        return createFile.absolutePath

    }


    override fun onBackPressed() {
        val sweetAlertDialog = SweetAlertDialog(this@CompilerActivity, SweetAlertDialog.WARNING_TYPE)
        sweetAlertDialog.titleText = "Please wait "
        sweetAlertDialog.contentText = "While processing"
        sweetAlertDialog.show()
    }


    private fun addTextMediaCodec(){
        val outputPath = createFileAndFolder()
        val videoDuration = sessionManager.getCreateVideoDuration()
        val originalVideoPath = sessionManager.getCreateVideoPath()
        val extractor = MediaExtractor()
        extractor.setDataSource(originalVideoPath!!)
        val muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)


        var videoTrackIndex = -1
        var audioTrackIndex = -1
        var videoFormat = MediaFormat()
        for (i in 0 until extractor.trackCount) {
            videoFormat = extractor.getTrackFormat(i)
            val mime = videoFormat.getString(MediaFormat.KEY_MIME)
            if (mime!!.startsWith("video/")) {
                extractor.selectTrack(i)
                videoTrackIndex = muxer.addTrack(videoFormat)
            } else if (mime!!.startsWith("audio/")) {
                extractor.selectTrack(i)
                audioTrackIndex = muxer.addTrack(videoFormat)
            }
        }

        muxer.start();
        val textPaint = TextPaint()
        textPaint.color = Color.WHITE
        textPaint.textSize = 30f
        textPaint.typeface = Typeface.DEFAULT_BOLD

        val bitmap = Bitmap.createBitmap(720, 1280, Bitmap.Config.ARGB_8888)
        val canvas = Canvas()
        canvas.setBitmap(bitmap)
        canvas.drawText("YOUR TEXT HERE", 431F, 429F, textPaint)

        val byteBuffer: ByteBuffer = ByteBuffer.allocate(bitmap.byteCount)
        bitmap.copyPixelsToBuffer(byteBuffer)
        byteBuffer.rewind()

        val encoder = MediaCodec.createEncoderByType(videoFormat.getString(MediaFormat.KEY_MIME).toString())
        encoder.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        encoder.start()


        val info = MediaCodec.BufferInfo()
        val inputBufferIndex = encoder.dequeueInputBuffer(0)
        if (inputBufferIndex >= 0) {
            val inputBuffer = encoder.getInputBuffer(inputBufferIndex)
            inputBuffer!!.clear()
            inputBuffer.put(byteBuffer)
            encoder.queueInputBuffer(inputBufferIndex, 0, byteBuffer.limit(), 0, 0)
        }


        val outputBufferIndex = encoder.dequeueOutputBuffer(info, 0)
        if (outputBufferIndex >= 0) {
            val outputBuffer = encoder.getOutputBuffer(outputBufferIndex)
            muxer.writeSampleData(videoTrackIndex, outputBuffer!!, info)
            encoder.releaseOutputBuffer(outputBufferIndex, false)
        }


        encoder.stop();
        encoder.release();
        muxer.stop();
        muxer.release();

        sessionManager.setCreateVideoPath(outputPath)
        startActivity(Intent(this@CompilerActivity,SnapPreviewActivity::class.java))
        finish()
    }


    fun compressVideo(){

        val outputPath = createFileAndFolder()
        val inputPath = sessionManager.getCreateVideoPath().toString()
        myApplication.printLogD("InSide Compress Video",TARCK)
        val cmd = "-y -i $inputPath -vcodec libx264 -preset veryfast -threads 6 -crf 28 $outputPath"


        EpEditor.execCmd(cmd,0,object : OnEditorListener {
            override fun onSuccess() {
                myApplication.printLogD("log : onSuccess",TARCK)
                myApplication.printLogD("Video Compress Complete",TARCK)
                if (!sessionManager.getIsVideoFromGallery()){
                    if (File(inputPath).exists()){
                        File(inputPath).delete()
                    }
                }
                val retriever =  MediaMetadataRetriever()
                retriever.setDataSource(this@CompilerActivity, Uri.parse(outputPath));
                val  time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                retriever.release()
                myApplication.printLogD("selectVideoDuration : $time" ,TAG)
                val selectVideoDuration = time!!.toLong()
                sessionManager.setCreateVideoPath(outputPath)
                sessionManager.setCreateVideoDuration(selectVideoDuration)
                sendToUploadVideoActivity()
            }

            override fun onFailure() {
                myApplication.printLogD("log : onFailure",TARCK)
            }

            override fun onProgress(progress: Float) {
                myApplication.printLogD("log onProgress : $progress",TARCK)
            }

        })

        /*FFmpegKit.executeAsync(cmd,
            { session ->
                val state = session.state
                val returnCode = session.returnCode
                if (ReturnCode.isSuccess(returnCode)){
                    myApplication.printLogD("Video Compress Complete",TARCK)
                    if (File(videoOriginalPath).exists()){
                        File(videoOriginalPath).delete()
                    }
                    videoOriginalPath = outputPath

                    myApplication.printLogD("Call uploadVideoToS3 Fun",TARCK)
                    uploadVideoToS3()
                }
                // CALLED WHEN SESSION IS EXECUTED
                Log.d(TAG, String.format("FFmpeg process exited with state %s and rc %s.%s",
                    state, returnCode,  session.failStackTrace))
            },
            {
                myApplication.printLogD("log : $it","ffmpeg")
            })
        {
            myApplication.printLogD("statistics : $it","ffmpeg")
        }*/
    }

    private fun sendToUploadVideoActivity() {
        val intent = Intent(this@CompilerActivity,UploadVideoActivity::class.java)
        startActivity(intent)
        finish()
    }
}