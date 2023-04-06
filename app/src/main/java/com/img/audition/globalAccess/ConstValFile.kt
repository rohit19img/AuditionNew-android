package com.img.audition.globalAccess

import android.Manifest

class ConstValFile {

    companion object{

        const val Block = "blocked"
        const val Unblock = "unblocked"
        const val NotInterestedDialogView  = "NotInterestedView"
        const val ReportDialogView  = "ReportDialogView"
        const val ReportUserView  = "ReportUserView"
        const val FirebaseCommentDB: String = "Comments"
        const val UserImage: String = "UserImage"
        const val VideoFileExt = ".mp4"
        const val UserName = "Username"
        const val PagePosition =  "PagePosition"
        const val RemoveFromWatch = "Remove From Watch Later"
        const val WatchLater = "Watch Later"
        const val VideoPosition  = "VideoPosition"
        const val UserPositionInList  = "UserPositionInList"
        const val VideoList = "VideoList"
        const val VideoFilePath = "videoFilePath"
        const val BASEURL = "http://139.59.30.125:6060"
        const val SOCKET_URL = "http://139.59.30.125:6060"
        const val LoginMsg: String = "Login Required.."
        const val IS_GUEST = "isGuest"
        const val ContestID = "contestID"
        const val GUEST_TOKEN = "GuestToken"
        const val GUEST_ID = "GuestID"
        const val Net_Connected = "Connected.."
        const val Check_Connection = "Please Check Internet"
        const val APP_NAME = "Audition"
        const val NOTIFICATION_TOKEN = "Notification_Token"
        const val PREFER_MAIN  = "AuditionMain"
        const val PREFER_LANG  = "AuditionLang"
        const val SELECTED_LANG  = "Selected_Lang"
        const val PRIVATE_MODE = 0
        const val IS_LOGIN = "IsLoggedIn"
        const val TOKEN = "UserToken"
        const val NUMBER = "UserNumber"
        const val USER_ID = "UserSelfID"
        const val AuditionID = "auditionid"
        const val AllUserID = "userid"
        const val VideoID = "vId"
        const val PostID = "postId"
        const val CommentList = "comment"
        const val TYPE_IMAGE = "image"
        const val USER_IDFORIntent = "UserSelfID"
        const val UserFollowStatus = "followStatus"
        const val Bundle = "bundle"
        const val Following = "Following"
        const val Unfollow = "Unfollow"
        const val Follow = "Follow"
        const val MobileVerified = "MobileVerified"
        const val PanVerified = "PanVerified"
        const val BankVerified = "BankVerified"
        const val REQUEST_PERMISSION_CODE_LOCATION = 1001
        const val REQUEST_PERMISSION_CODE_CAMERA = 1002
        const val REQUEST_PERMISSION_CODE_AUDIO = 1003
        const val REQUEST_PERMISSION_CODE_STORAGE = 1004
        const val REQUEST_PERMISSION_CODE = 1000

        val PERMISSION_LIST = listOf<String>(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)

    }

}