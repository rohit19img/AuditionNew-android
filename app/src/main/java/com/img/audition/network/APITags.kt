package com.img.audition.network

import com.amazonaws.auth.BasicAWSCredentials


class APITags {
    companion object{
        //S3Client
        const val s3Key = "DO00MEHYAAAQQ3UMDXBU"
        const val s3SecretKey = "nOQSayk36LetSj8wdI058GYRxlAujpj0ETtLE+I2VaU"
        const val s3EndpointUrl = "https://sgp1.digitaloceanspaces.com/"
        const val digitalOceanBaseUrl =  "https://audition.sgp1.digitaloceanspaces.com/"
        //


        const val APIBASEURL = "http://139.59.30.125:6060/api/"
        const val AUTHORIZATION = "Authorization"
        const val GuestLogin = "guest-login"
        const val GetChatHistory = "getChatHistory"
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
        const val GetNotInterestedCategory = "interest-categories"
        const val ReportVideo = "report_video"
        const val addIntoNotInterested = "not-intrested"
        const val GetSavedVideos = "watch-later-videos"
        const val blockUnblock = "userBlockUnblock"
        const val GetAddCashOffer = "getOffers"
        const val EditUserProfile = "editProfile"
        const val GetUserTransactions = "getTransactions"
        const val UploadNormalVideoToServer = "upload_video"
        const val UploadContestVideoToServer = "join_contest"


    }
}