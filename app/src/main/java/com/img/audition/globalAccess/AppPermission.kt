package com.img.audition.globalAccess

import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class AppPermission(val activity: Activity,val permissionList: List<String>,val reqCode:Int) {
    fun checkPermissions() {
        if (isPermissionsGranted() != PackageManager.PERMISSION_GRANTED) {
            requestPermissions()
        } else {
            Log.d("AppPermission", "Permissions already granted.")
        }
    }

    private fun isPermissionsGranted(): Int {
        // PERMISSION_GRANTED : Constant Value: 0
        // PERMISSION_DENIED : Constant Value: -1
        var counter = 0
        for (permission in permissionList) {
            counter += ContextCompat.checkSelfPermission(activity, permission)
        }
        return counter
    }

    private fun deniedPermission(): String {
        for (permission in permissionList) {
            if (ContextCompat.checkSelfPermission(activity, permission)
                == PackageManager.PERMISSION_DENIED) return permission
        }
        return ""
    }

    private fun showAlert() {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Please Allow Permission..")
        builder.setPositiveButton("OK", { dialog, which -> requestPermissions() })
        builder.setNeutralButton("Cancel", null)
        val dialog = builder.create()
        dialog.show()
    }

    private fun requestPermissions() {
        val permission = deniedPermission()
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            // Show an explanation asynchronously
//            Toast.makeText(activity, "Allow Permission", Toast.LENGTH_SHORT).show()
            ActivityCompat.requestPermissions(activity, permissionList.toTypedArray(), reqCode)
        } else {
            ActivityCompat.requestPermissions(activity, permissionList.toTypedArray(), reqCode)
        }
    }

    fun processPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray): Boolean {
        var result = 0
        if (grantResults.isNotEmpty()) {
            for (item in grantResults) {
                result += item
            }
        }
        if (result == PackageManager.PERMISSION_GRANTED) return true
        return false
    }

}