package com.img.audition.network



class APITags {
    companion object{
        const val APIBASEURL = "http://139.59.30.125:6060/api/"
        const val AUTHORIZATION = "Authorization"
        const val GuestLogin = "guest-login"
        const val GetVideo = "get_video"
        const val Login = "login"
        const val OTPLogin = "otp-login"
        const val likeUnlike = "likeUnlike"
        const val FollowUnfollow = "followUnfollow"
        const val GetUserList = "getUserList"
        const val GetUserVideo = "get_user_video"
        const val GetLanguages = "languages"
        const val GetUserSelfFullDetails = "userFullDetails"
        const val LogoutUser = "logoutUser"
        const val VerifyMobileNumber = "verifyMobileNumber"
        const val AllVerify = "allverify"
        const val GetAllLiveContest = "get_all_contest"
        const val WatchLater = "watch-later/{id}"
        const val RemoveWatchLater = "remove-watch-later/{id}"
        const val FollowFollowingList = "getFollowerList"
        const val GetMusicList = "get_music_Uploaded"
        const val GetReportCategory = "getReportCategory"
        const val ReportVideo = "report_video"


    }
}