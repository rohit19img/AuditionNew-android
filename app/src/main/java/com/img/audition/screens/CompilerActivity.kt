package com.img.audition.screens

import VideoHandle.EpEditor
import VideoHandle.OnEditorListener
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.img.audition.databinding.ActivityCompilerBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.SessionManager
import java.io.File
import java.io.IOException

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
        }
        super.onResume()
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
        EpEditor.execCmd(cmd,0,object : OnEditorListener {
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
        })
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
}