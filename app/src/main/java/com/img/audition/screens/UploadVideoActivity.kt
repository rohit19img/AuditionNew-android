package com.img.audition.screens

import android.app.AlertDialog
import android.content.ContentUris
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import com.arthenica.ffmpegkit.FFmpegKit
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.img.audition.databinding.ActivityUploadVideoBinding
import com.img.audition.globalAccess.ConstValFile
import com.img.audition.globalAccess.MyApplication
import com.img.audition.network.SessionManager
import java.io.File
import java.io.IOException


@UnstableApi class UploadVideoActivity : AppCompatActivity() {

    val TAG = "UploadVideoActivity"
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityUploadVideoBinding.inflate(layoutInflater)
    }
    private val sessionManager by lazy {
        SessionManager(this@UploadVideoActivity)
    }

    private val myApplication by lazy {
        MyApplication(this@UploadVideoActivity)
    }

    private val bundle by lazy {
        intent.getBundleExtra(ConstValFile.Bundle)
    }

    private var orignalPath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        if (bundle!=null){
            orignalPath = bundle!!.getString(ConstValFile.VideoFilePath).toString()
            Glide.with(this@UploadVideoActivity).load(orignalPath)
                .diskCacheStrategy(DiskCacheStrategy.ALL).into(viewBinding.videoThumbnail)
        }else{
            myApplication.printLogD("File Path Null",TAG)
        }

        viewBinding.backPressIC.setOnClickListener {
            onBackPressed()
        }

        viewBinding.uploadVideoBtn.setOnClickListener {
            if(!(sessionManager.isUserLoggedIn())){
                sendToLoginActivity()
            }else{
                compressVideo()
            }
        }
    }

    private fun sendToLoginActivity() {
        val intent = Intent(this@UploadVideoActivity,LoginActivity::class.java)
        startActivity(intent)
    }

    override fun onBackPressed() {
        val dialogBuilder = AlertDialog.Builder(this@UploadVideoActivity)
        dialogBuilder.setTitle("Discard Video")
        dialogBuilder.setMessage("Do you want discard the video")
            .setCancelable(false)
            .setPositiveButton("Discard", DialogInterface.OnClickListener {
                    _, _ -> sendToMain()
            })
            .setNegativeButton("NO") { dialog, _ ->
                dialog.cancel()
            }
        val alert = dialogBuilder.create()
        alert.show()
//        super.onBackPressed()
    }

    fun sendToMain(){
        val intent = Intent(this@UploadVideoActivity,HomeActivity::class.java)
        intent.flags =Intent.FLAG_ACTIVITY_CLEAR_TOP
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    fun getNewFilePath():String{
        val fileRandomName  = System.currentTimeMillis() / 1000
        val fileName =  fileRandomName.toString()+ConstValFile.VideoFileExt
        val newPath = File(getVideoFolderPath(),fileName)
        myApplication.printLogD(newPath.absolutePath,"New Path")
        if (!(newPath.exists())){
            try {
                newPath.createNewFile()
            } catch (e: IOException) {
                myApplication.printLogE(e.toString(),TAG)
            }
        }
        return newPath.absolutePath
    }


    private fun compressVideo() {
        val inputPath = getPathFromUri(this@UploadVideoActivity,Uri.parse(orignalPath))
        val outputPath = getNewFilePath()
        val compressCMD = "-y -i $inputPath -vcodec libx264 -crf 22 $outputPath"//Its Working Fine
        /*val speedCMD1 = "-y -i $inputPath  -map 0:v -c:v copy -bsf:v hevc_mp4toannexb raw.h265"

        val complexCommand =
            "-y -i $inputPath -filter_complex [0:v]setpts=2.0*PTS[v];[0:a]atempo=0.5[a] -map [v] -map [a] -b:v 2097k -r 60 -vcodec mpeg4 $outputPath"

        val aaudiIth = "-i $inputPath -vf setpts=1.5*PTS -c:a copy $outputPath"
        val removeAudio = "-i $inputPath -c copy -an $outputPath"*/

/*
        val session = FFmpegKit.execute(speedCMD1)
        val state = session.state
        val returnCode = session.returnCode

        if (ReturnCode.isSuccess(session.returnCode)){
            val speedCMD2 =  "-y -i $inputPath -filter:v setpts=0.5*PTS $outputPath"
            val session = FFmpegKit.execute(speedCMD2)
            val state = session.state
            val returnCode = session.returnCode

            if (ReturnCode.isSuccess(session.returnCode)){
                myApplication.showToast("Complete...")
            }
        }*/

        FFmpegKit.executeAsync(compressCMD,
            { session ->
                val state = session.state
                val returnCode = session.returnCode

                // CALLED WHEN SESSION IS EXECUTED
                Log.d(
                    "check 100",
                    String.format(
                        "FFmpeg process exited with state %s and rc %s.%s",
                        state,
                        returnCode,
                        session.failStackTrace
                    )
                )
            }, {
                // CALLED WHEN SESSION PRINTS LOGS
                myApplication.printLogD(it.message,"check 100")
            }) {
            myApplication.printLogD(it.toString(),"check 100")
        }
    }

    fun getVideoFolderPath(): String {
        if(!File(getExternalFilesDir(null)!!.path.replace("files","")+"raw_video/").exists())
            File(getExternalFilesDir(null)!!.path.replace("files","")+"raw_video/").mkdir()


        return   getExternalFilesDir(null)!!.path.replace("files","")+"raw_video/" /*+ SimpleDateFormat("yyyyMM_dd-HHmmss").format(Date()) + "cameraRecorder.mp4"*/
    }

    fun getPathFromUri(context: Context, uri: Uri): String? {
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString()+ "/" + split[1]
                }

                // TODO handle non-primary volumes
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)
                )
                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(
                    split[1]
                )
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {

            // Return the remote address
            return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(
                context,
                uri,
                null,
                null
            )
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
            column
        )
        try {
            cursor = context.getContentResolver().query(
                uri!!, projection, selection, selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index: Int = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            if (cursor != null) cursor.close()
        }
        return null
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }
}