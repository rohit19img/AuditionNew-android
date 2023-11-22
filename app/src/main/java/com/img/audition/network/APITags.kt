package com.img.audition.network

import com.amazonaws.auth.BasicAWSCredentials


class APITags {
    companion object{

        //S3Client
        const val s3Key = "DO00AFMRUZGMUVCVNCX3"
        const val s3SecretKey = "WL7tdc+5fvGdbCpRJx+lpGe49jxYoffxtmj+D2HAoS8"
        const val s3EndpointUrl = "https://blr1.digitaloceanspaces.com/"
        const val digitalOceanBaseUrl =  "https://biggee.blr1.digitaloceanspaces.com/"
        const val bucketName =  "biggee"
        //

//        const val APIBASEURL = "http://64.227.157.121:6060/api/"
//        const val ADMINBASEURL = "http://64.227.157.121:12345/admin/"
        const val APIBASEURL = "https://api.biggee.in/api/"
        const val ADMINBASEURL = "http://admin.biggee.in/"
        const val AUTHORIZATION = "Authorization"
        const val GuestLogin = "guest-login"
        const val GetChatHistory = "getChatHistory"
        const val GetForYouVideo = "get_video"
        const val GetVideoByID = "video/{videoID}"
        const val GetLiveContestVideo = "contest_video"
        const val Login = "login"
        const val OTPLogin = "otp-login"
        const val likeUnlike = "likeUnlike"
        const val FollowUnfollow = "followUnfollow"
        const val GetUserList = "getUserList"
        const val GetUserByAuditionId = "getUserByAuditionId"
        const val GetUserVideo = "get_user_video"
        const val ContestVideo = "contest-video"
        const val GetHashTagVideo = "get_hashtag_video"
        const val GetMusicVideo = "get_music_videos"
        const val TrendingVideo = "trending"
        const val UploadMusic = "upload_music"
        const val GetLanguages = "languages"
        const val GetUserSelfFullDetails = "userFullDetails"
        const val LogoutUser = "logoutUser"
        const val VerifyEmailAddress = "verifyEmail"
        const val VerifyMobileNumber = "verifyMobileNumber"
        const val LiveRanksLeaderboard = "liveRanksLeaderboard"
        const val VerifyOTP = "verifyCode"
        const val AllVerify = "allverify"
        const val GetAllLiveContest = "get_all_contest"
        const val GetJoinedContest = "myJoinedContests"
        const val WatchLater = "watch-later/{id}"
        const val RemoveWatchLater = "remove-watch-later/{id}"
        const val FollowFollowingList = "getFollowerList"
        const val GetMusicList = "get_music_Uploaded"
        const val GetFavMusicList = "favMusicList"
        const val GetReportCategory = "getReportCategory"
        const val GetNotInterestedCategory = "interest-categories"
        const val ReportVideo = "report_video"
        const val addIntoNotInterested = "not-intrested"
        const val GetSavedVideos = "watch-later-videos"
        const val blockUnblock = "userBlockUnblock"
        const val GetAddCashOffer = "getOffers"
        const val EditUserProfile = "editProfile"
        const val UploadStatus = "upload-status"
        const val GetUserTransactions = "getTransactions"
        const val UploadNormalVideoToServer = "upload_video"
        const val UploadContestVideoToServer = "join_contest"
        const val MAPS_API_KEY = "AIzaSyBmniloMXEznkrAL6k0VfoFsJJFAfcRBgg"

    }
}