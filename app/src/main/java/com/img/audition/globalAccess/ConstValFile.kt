package com.img.audition.globalAccess

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi

class ConstValFile {

    companion object{

        const val BASEURL = "https://api.biggee.in/"
        const val SOCKET_URL = "https://api.biggee.in/"
        const val VideoHashTag = "VideoHashTag"
        const val SongUrl = "SongUrl"
        const val PostLocation = "PostLocation"
        const val SongID = "SongID"
        const val AppAudio = "AppAudio"
        const val AppSongID = "AppSongID"
        const val AudioDuration = "audioDuration"
        const val isFromTryAudio = "isFromTryAudio"
        const val isFromGallery ="VideoFrom"
        const val VideoText = "VideoText"
        const val VideoTextSize = "VideoTextSize"
        const val VideoTextColor = "VideoTextColor"
        const val VideoTextXpos = "VideoTextXpos"
        const val VideoTextYpos = "VideoTextYpos"
        const val TextAlignCmd = "textAlignCmd"
        const val SlowVideo = "SlowVideo"
        const val FastVideo = "FastVideo"
        const val NormalVideo = "NormalVideo"
        const val CompressVideo = "CompressVideo"
        const val MergeVideo = "MergeVideo"
        const val ColorFilter = "ColorFilter"
        const val AddText = "AddText"
        const val VideoOriginalPath ="videoOriginalPath"
        const val TYPE_IMAGE = "image"
        const val UpContestComingList = "UpContestList"
        const val ForYouVideoList = "ForYouVideoList"
        const val LiveContestVideoList = "LiveContestVideoList"
        const val LiveContestList = "LiveContestList"
        const val MusicList = "MusicList"
        const val VideoList = "VideoList"
        const val CompletedContestList = "CompletedContestList"
        const val FromCamera = "FromCamera"
        const val CompileTask = "compileTask"
        const val TaskMuxing = "TaskMuxing"
        const val UploadSuccess = "Uploaded Successfully."
        const val UploadFailed = "Uploading Failed."
        const val Block = "blocked"
        const val Unblock = "unblocked"
        const val NotInterestedDialogView  = "NotInterestedView"
        const val ReportDialogView  = "ReportDialogView"
        const val ReportUserView  = "ReportUserView"
        const val FirebaseCommentDB: String = "Comments"
        const val FirebasePostDB: String = "Post"
        const val UserImage: String = "UserImage"
        const val VideoFileExt = ".mp4"
        const val UserName = "Username"
        const val VideoTempUrl = "videoTempUrl"
        const val PagePosition =  "PagePosition"
        const val RemoveFromWatch = "Remove From Watch Later"
        const val WatchLater = "Watch Later"
        const val VideoPosition  = "VideoPosition"
        const val UserPositionInList  = "UserPositionInList"
        const val VideoFilePath = "videoFilePath"
        const val DuetVideoUrl = "DuetVideoUrl"
        const val DuetCaption = "DuetCaption"
        const val isFromDuet = "isFromDuet"
        const val duetVideoUrl = "videoFilePath"
        const val TrimAudioUrl = "trimAudioUrl"
        const val VideoSpeedState = "videoSpeedState"
        const val VideoDuration = "videoDuration"
        const val LoginMsg: String = "Login Required.."
        const val IS_GUEST = "isGuest"
        const val ContestID = "contestID"
        const val ContestStatus = "ContestStatus"
        const val IsContestJoin = "isContestJoin"
        const val ContestEntryFee = "contestEntryFee"
        const val GUEST_TOKEN = "GuestToken"
        const val GUEST_ID = "GuestID"
        const val Net_Connected = "Connected.."
        const val Check_Connection = "Please Check Internet"
        const val APP_NAME = "Audition"
        const val NOTIFICATION_TOKEN = "Notification_Token"
        const val PREFER_MAIN  = "AuditionMain"
        const val PREFER_LANG  = "AuditionLang"
        const val PREFER_CONTEST  = "AuditionContest"
        const val PREFER_VIDEO  = "AuditionVideo"
        const val DUET_VIDEO  = "DuetVideo"
        const val SELECTED_LANG  = "Selected_Lang"
        const val PRIVATE_MODE = 0
        const val IS_LOGIN = "IsLoggedIn"
        const val TOKEN = "UserToken"
        const val NUMBER = "UserNumber"
        const val USER_ID = "UserSelfID"
        const val AuditionID = "auditionid"
        const val isSearchAuditionID = "isSearchAuditionID"
        const val AllUserID = "userid"
        const val VideoID = "vId"
        const val PostID = "postId"
        const val CommentList = "comment"
        const val ContestType = "imageOrVideo"
        const val ContestFile = "contestFile"
        const val IsFromContest = "isFromContest"
        const val USER_IDFORIntent = "UserSelfID"
        const val UserFollowStatus = "followStatus"
        const val Bundle = "bundle"
        const val ContestBundle = "contestBundle"
        const val Following = "Following"
        const val Unfollow = "Unfollow"
        const val Follow = "Follow"
        const val MobileVerified = "MobileVerified"
        const val EmailVerified = "EmailVerified"
        const val PanVerified = "PanVerified"
        const val BankVerified = "BankVerified"
        const val REQUEST_PERMISSION_CODE_LOCATION = 1001
        const val REQUEST_PERMISSION_CODE_CAMERA = 1002
        const val REQUEST_PERMISSION_CODE_AUDIO = 1003
        const val REQUEST_PERMISSION_CODE_STORAGE = 1004
        const val REQUEST_PERMISSION_CODE = 1000


        const val FONT = "Font"
        const val DEFAULT_FONT = "notosans_regular.ttf"
        const val FontName = "notosans_regular"

        val PERMISSION_LIST = listOf<String>(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

}