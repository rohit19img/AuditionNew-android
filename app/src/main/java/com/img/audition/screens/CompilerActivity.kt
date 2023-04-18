package com.img.audition.screens


import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import cn.pedant.SweetAlert.SweetAlertDialog
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.img.audition.databinding.ActivityCompilerBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.SessionManager
import java.io.File
import java.io.IOException


@UnstableApi
class CompilerActivity : AppCompatActivity() {
    val TAG = "CompilerActivity"
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
                AudioVideoMuxing()
            }
            ConstValFile.ColorFilter ->{
                ApplyColorFilter()
            }
            ConstValFile.AddText ->{
                AddTextInVideo()
            }
            ConstValFile.SlowVideo->{
                slowVideo()
            }
            ConstValFile.FastVideo->{
                fastVideo()
            }ConstValFile.NormalVideo->{
                normalVideo()
            }
        }
        super.onResume()
    }

    private fun normalVideo() {
        val outputPath = createFileAndFolder()
        val originalVideoPath = sessionManager.getCreateVideoPath()

        val cmd2 = "-y -i $originalVideoPath -filter_complex [0:v]setpts=1.0*PTS[v];[0:a]atempo=0.25[a] -map [v] -map [a] $outputPath"

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

        val cmd2 = "-y -i $originalVideoPath -filter_complex [0:v]setpts=2.0*PTS[v];[0:a]atempo=0.5[a] -map [v] -map [a] $outputPath"
//        val cmd = "-y -i $originalVideoPath -filter:v setpts=2.0*PTS  $outputPath"
        funComipler(cmd2,outputPath)
    }

    private fun AddTextInVideo() {
        val outputPath = createFileAndFolder()
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

        val cmd = "-y -i $originalVideoPath -vf drawtext=fontfile=$fontPath:text=$text:fontsize=$textSize:fontcolor=$textColor:$textAlignCmd -c:v libx264 -preset fast -crf 12 -c:a copy $outputPath"
//       val cmd2 = "-y -i $originalVideoPath -i $text -filter_complex [0:v][1:v]overlay=$x:$y[out] -map [out] -c:a copy $outputPath"
        funComipler(cmd,outputPath)
    }

    private fun ApplyColorFilter() {
        val outputPath = createFileAndFolder()
        val originalVideoPath = sessionManager.getCreateVideoPath()
        //                                                        "R    :R    :R"    ":G    :G    :G"   ":B     :B    :B"
        val cmd = "-y -i $originalVideoPath -vf colorchannelmixer=0.393:0.769:0.189:0.349:0.686:0.168:0.272:0.534:0.131 $outputPath"
        funComipler(cmd,outputPath)
    }

    private fun AudioVideoMuxing() {
        val outputPath = createFileAndFolder()
        val trimAudioPath = sessionManager.getTrimAudioPath()
        val originalVideoPath = sessionManager.getCreateVideoPath()


        val cmd1  =   "-y -i $originalVideoPath -i $trimAudioPath -c copy -map 0:v:0 -map 1:a:0 $outputPath"


        val cmd = "-y -i $originalVideoPath -i $trimAudioPath -c:v copy -c:a aac -map 0:v:0 -map 1:a:0 $outputPath"
        funComipler(cmd1,outputPath)
    }


    fun funComipler(cmd: String, outputPath: String){
       /* EpEditor.execCmd(cmd,0, object : OnEditorListener {
            override fun onSuccess() {
                myApplication.printLogD("Compile Complete",TAG)
                val bundle = Bundle()
                sessionManager.setCreateVideoPath(outputPath)
                bundle.putString(ConstValFile.CompileTask,ConstValFile.TaskMuxing)
                startActivity(Intent(this@CompilerActivity,PreviewActivity::class.java))
                finish()
            }
            override fun onFailure() {
                myApplication.printLogD("Compile : onFailure",TAG)
            }
            override fun onProgress(progress: Float) {
                myApplication.printLogD("Compile onProgress : $progress",TAG)
            }
        })*/

         FFmpegKit.executeAsync(cmd,
           { session ->
               val state = session.state
               val returnCode = session.returnCode
               if (ReturnCode.isSuccess(returnCode)){
                   myApplication.printLogD("Compile Complete",TAG)
                   val bundle = Bundle()
                   sessionManager.setCreateVideoPath(outputPath)
                   bundle.putString(ConstValFile.CompileTask,ConstValFile.TaskMuxing)
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
       }
    }


    private fun createFileAndFolder():String{
        val timestamp = System.currentTimeMillis()
        val filename = "$timestamp.mp4"
        val appData = getExternalFilesDir(null)
        myApplication.printLogD(appData!!.absolutePath, TAG)

        val createFile = File(appData,filename)
        if (!(createFile.exists())){
            try {
                createFile.createNewFile()
                myApplication.printLogD(createFile.absolutePath, TAG)
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


}