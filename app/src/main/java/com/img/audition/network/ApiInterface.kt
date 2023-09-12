package com.img.audition.network

import com.google.gson.JsonObject
import com.img.audition.dataModel.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiInterface {

    @POST(APITags.GuestLogin)
    suspend fun guestLogin(@Body guestRequest: GuestLoginRequest): LoginResponse

    @POST(APITags.Login)
    suspend fun userLogin(@Body loginRequest: NumLoginRequest): CommanResponse

    @POST(APITags.OTPLogin)
    suspend fun userOtpVerify(@Body otpRequest: OTPRequest): LoginResponse

    @GET(APITags.GetForYouVideo)
    suspend fun getForYouReelsVideo(
        @Header(APITags.AUTHORIZATION) Auth: String,
        @Query("language") language: String?,
        @Query("lat") lat: Double?,
        @Query("long") lang: Double?
    ): VideoResponse

    @GET(APITags.GetVideoByID)
    suspend fun getVideoByID(@Path("videoID") videoID: String?): VideoResponse

    @GET(APITags.GetLiveContestVideo)
    suspend fun getLiveContestReelsVideo(
        @Header(APITags.AUTHORIZATION) Auth: String,
        @Query("language") language: String?,
        @Query("lat") lat: Double?,
        @Query("long") lang: Double?
    ): VideoResponse

    @GET(APITags.GetUserVideo)
    suspend fun getUserVideo(
        @Header(APITags.AUTHORIZATION) Auth: String?,
        @Query("id") userID: String?
    ): VideoResponse

    @GET(APITags.GetHashTagVideo)
    suspend fun getHashTagVideo(
        @Header(APITags.AUTHORIZATION) Auth: String?,
        @Query("id") hashTag: String
    ): VideoResponse

    @GET(APITags.GetMusicVideo)
    suspend fun getMusicVideo(
        @Header(APITags.AUTHORIZATION) Auth: String?,
        @Query("id") musicId: String
    ): SongVideoResponse

    @GET(APITags.TrendingVideo)
    suspend fun getTrendingVideo(@Header("Authorization") Auth: String?): TrendingVideoResponse

    @GET(APITags.ContestVideo)
    suspend fun getContestVideo(
        @Header(APITags.AUTHORIZATION) Auth: String?,
        @Query("userid") userID: String?,
        @Query("challengeid") challengeId: String?
    ): VideoResponse

    @GET(APITags.GetUserSelfFullDetails)
    suspend fun getUserSelfDetails(@Header(APITags.AUTHORIZATION) Auth: String?): UserSelfProfileResponse

    @GET(APITags.GetChatHistory)
    suspend fun getChatHistory(
        @Header(APITags.AUTHORIZATION) Auth: String?,
        @Query("receiverId") receiverId: String?,
        @Query("page") page_no: Int
    ): ChatsGetSet

    @GET("get-all-chat")
    suspend fun getChatUser(
        @Header(APITags.AUTHORIZATION) Auth: String?,
        @Query("page") page_no: Int
    ): ChatUserDataResponse

    @GET(APITags.GetMusicList)
    suspend fun getMusicList(@Header(APITags.AUTHORIZATION) Auth: String): AllCategoryMusicDataResponse

    @GET(APITags.GetFavMusicList)
    suspend fun getFavMusicList(@Header(APITags.AUTHORIZATION) Auth: String): MusicDataResponse

    @Multipart
    @POST(APITags.UploadMusic)
    fun uploadVideoMusic(
        @Header(APITags.AUTHORIZATION) Auth: String?,
        @Part typename: MultipartBody.Part,
        @Part audioFile: MultipartBody.Part
    ): Call<UploadMusicResponse>


    @GET(APITags.likeUnlike)
    fun viedolike(
        @Header(APITags.AUTHORIZATION) Auth: String?,
        @Query("videoId") videoId: String?,
        @Query("status") status: String?
    ): Call<LikeResponse>

    @GET(APITags.FollowUnfollow)
    fun followFollowing(
        @Header(APITags.AUTHORIZATION) Auth: String?,
        @Query("followingId") userID: String?,
        @Query("status") status: String?
    ): Call<FollowFollowingResponse>

    @GET(APITags.GetUserList)
    fun getOtherUser(
        @Header(APITags.AUTHORIZATION) Auth: String?,
        @Query("userId") userID: String?
    ): Call<GetOtherUserResponse>

    @GET(APITags.GetUserByAuditionId)
    fun getUserByAuditionId(
        @Header(APITags.AUTHORIZATION) Auth: String?,
        @Query("audition_id") auditionID: String?
    ): Call<GetOtherUserResponse>

    @GET(APITags.GetLanguages)
    fun getLanguages(@Header(APITags.AUTHORIZATION) Auth: String?): Call<LanguageResponse?>


    @GET(APITags.LogoutUser)
    fun logoutUser(@Header(APITags.AUTHORIZATION) Auth: String?): Call<CommanResponse>

    @POST(APITags.VerifyMobileNumber)
    fun verifyMobileNumber(
        @Header(APITags.AUTHORIZATION) Auth: String?,
        @Body loginRequest: NumLoginRequest
    ): Call<CommanResponse>

    @POST(APITags.VerifyEmailAddress)
    fun verifyEmailAddress(
        @Header(APITags.AUTHORIZATION) Auth: String?,
        @Body loginRequest: EmailLoginRequest
    ): Call<CommanResponse>

    @POST(APITags.VerifyOTP)
    fun verifyOTP(
        @Header(APITags.AUTHORIZATION) Auth: String?,
        @Body loginRequest: JsonObject
    ): Call<CommanResponse>

    @GET(APITags.AllVerify)
    fun getAllVerificationsData(@Header(APITags.AUTHORIZATION) Auth: String?): Call<UserVerificationResponse>

    @GET(APITags.GetAllLiveContest)
    fun getAllLiveContest(@Header(APITags.AUTHORIZATION) Auth: String?): Call<GetLiveContestDataResponse>

    @GET(APITags.GetJoinedContest)
    fun getJoinedContest(@Header(APITags.AUTHORIZATION) Auth: String?): Call<GetJoinedContestDataResponse>

    @GET(APITags.WatchLater)
    fun saveIntoWatchLater(
        @Header(APITags.AUTHORIZATION) Auth: String?,
        @Path("id") id: String?
    ): Call<CommanResponse>

    @GET(APITags.RemoveWatchLater)
    fun remove_watch_later(
        @Header(APITags.AUTHORIZATION) Auth: String?,
        @Path("id") id: String?
    ): Call<CommanResponse>

    @GET(APITags.FollowFollowingList)
    fun getFollowFollowingList(
        @Header(APITags.AUTHORIZATION) Auth: String?,
        @Query("id") id: String?
    ): Call<FollowerFollowingListResponse>

    @GET(APITags.GetReportCategory)
    fun getReportCategory(@Header(APITags.AUTHORIZATION) Auth: String?): Call<ReportCategoryResponse>

    @GET(APITags.GetNotInterestedCategory)
    fun getNotInterestedCategory(@Header(APITags.AUTHORIZATION) Auth: String?): Call<ReportCategoryResponse>

    @GET(APITags.ReportVideo)
    fun reportTheVideo(
        @Header(APITags.AUTHORIZATION) Auth: String?,
        @Query("reportId") reportID: String?,
        @Query("reportvideoid") videoID: String?
    ): Call<CommanResponse>

    @GET("addReportUser")
    fun reportUser(
        @Header(APITags.AUTHORIZATION) Auth: String?,
        @Query("reportId") reportID: String?,
        @Query("reportuserid") reportUserID: String?
    ): Call<CommanResponse>

    @GET(APITags.addIntoNotInterested)
    fun addIntoNotInterestedVideo(
        @Header(APITags.AUTHORIZATION) Auth: String?,
        @Query("catId") contestId: String?,
        @Query("videoId") videoID: String?
    ): Call<CommanResponse>

    @GET(APITags.GetSavedVideos)
    fun getSavedVideo(@Header(APITags.AUTHORIZATION) Auth: String?): Call<VideoResponse>


    @Multipart
    @POST(APITags.EditUserProfile)
    fun editProfile(
        @Header(APITags.AUTHORIZATION) auth: String?,
        @Part("typename") typename: RequestBody?,
        @Part("name") name: RequestBody?,
        @Part("audition_id") suditionid: RequestBody?,
        @Part image: MultipartBody.Part?,
        @Part("bio") bio: RequestBody?,
        @Part("gender") gender: RequestBody?,
        @Part("dob") dob: RequestBody?
    ): Call<CommanResponse>

    @Multipart
    @POST(APITags.EditUserProfile)
    fun editProfile(
        @Header(APITags.AUTHORIZATION) auth: String?,
        @Part("typename") typename: RequestBody?,
        @Part("name") name: RequestBody?,
        @Part("audition_id") suditionid: RequestBody?,
        @Part("bio") bio: RequestBody?,
        @Part("gender") gender: RequestBody?,
        @Part("dob") dob: RequestBody?
    ): Call<CommanResponse>

    @GET(APITags.blockUnblock)
    fun blockUnblockUser(
        @Header(APITags.AUTHORIZATION) Auth: String?,
        @Query("blockId") videoid: String?,
        @Query("status") status: String?
    ): Call<CommanResponse>

    @GET(APITags.GetAddCashOffer)
    fun getOfferDetails(@Header(APITags.AUTHORIZATION) Auth: String?): Call<OfferDataResponse>

    @GET(APITags.GetUserTransactions)
    fun getTransactions(@Header(APITags.AUTHORIZATION) Auth: String?): Call<TransactionReportResponse>

    @POST(APITags.UploadNormalVideoToServer)
    fun uploadNormalVideoToServer(
        @Header(APITags.AUTHORIZATION) Auth: String?,
        @Body postVideoObj: JsonObject?
    ): Call<CommanResponse>

    @POST(APITags.UploadContestVideoToServer)
    fun uploadContestVideoToServer(
        @Header(APITags.AUTHORIZATION) Auth: String?,
        @Body postVideoObj: JsonObject?
    ): Call<CommanResponse>

    @POST("search")
    suspend fun search(
        @Header("Authorization") Auth: String?,
        @Body searchObj: JsonObject?
    ): SearchResponse

    @GET("leagueDetails")
    fun getSingleContestDetails(
        @Header("Authorization") Auth: String?,
        @Query("contestId") contestId: String?
    ): Call<SingleContestDetailsResponse>

    @GET("myLeaderboard")
    fun getLeaderboardDetails(
        @Header("Authorization") Auth: String?,
        @Query("contestId") contestId: String?
    ): Call<LeaderboardDataResponse>

    @GET(APITags.LiveRanksLeaderboard)
    fun getLiveRanksLeaderboardDetails(
        @Header("Authorization") Auth: String?,
        @Query("contestId") contestId: String?
    ): Call<LiveRanksLeaderboardResponse>

    @GET("boost-rate")
    fun getBoostRate(@Header("Authorization") Auth: String?): Call<JsonObject>

    @POST("boost-video")
    fun boostPost(
        @Header("Authorization") Auth: String?,
        @Body boostPost: JsonObject?
    ): Call<BoostPostGetSet>

    @GET("getLikeCategory")
    fun getVoteCategory(@Header("Authorization") Auth: String?): Call<VoteDataResponse>

    @POST("userGiveVote")
    fun voteToUserVideo(
        @Header("Authorization") Auth: String?,
        @Body voteObj: JsonObject?
    ): Call<CommanResponse>

    @GET("get_notification")
    fun getNotification(@Header("Authorization") Auth: String?): Call<NotificationDataResponse>

    @GET("delete_video")
    fun deleteUserSelfVideo(
        @Header("Authorization") Auth: String?,
        @Query("id") contestId: String?
    ): Call<CommanResponse>

    @POST("socialauthentication")
    fun socialLogin(@Body socialLoginObj: JsonObject?): Call<LoginResponse>

    @POST("requestwithdraw")
    fun withdrawRequest(
        @Header("Authorization") Auth: String?,
        @Body jsonObject: JsonObject
    ): Call<CommanResponse>

    @GET("getBlockedUser")
    fun getBlockedUser(@Header("Authorization") Auth: String?): Call<BlockedUserResponse>

    @POST("addFavMusic")
    fun addFavMusic(
        @Header("Authorization") Auth: String?,
        @Body favMusicObj: JsonObject?
    ): Call<CommanResponse>

    @GET("terms-and-conditions")
    fun getTermsAndConditions(@Header("Authorization") Auth: String?): Call<TermAboutPrivacyResponse>

    @GET("privacy-policy")
    fun getPrivacyPolicy(@Header("Authorization") Auth: String?): Call<TermAboutPrivacyResponse>

    @GET("about-us")
    fun getAboutUs(@Header("Authorization") Auth: String?): Call<TermAboutPrivacyResponse>

    @GET("get_location_video")
    fun getPostLocationVideo(
        @Header(APITags.AUTHORIZATION) Auth: String?,
        @Query("location") hashTag: String
    ): Call<VideoResponse>

    @GET("get-web-slider")
    fun getWebSliderBanner(@Header(APITags.AUTHORIZATION) Auth: String?): Call<WebSliderResponse>

    @GET("user-votes")
    fun getVoterList(
        @Header("Authorization") Auth: String?,
        @Query("videoid") videoId: String?
    ): Call<LeaderboardDataResponse>

    @GET("get_usable_balance")
    fun getUsableBalance(
        @Header("Authorization") Auth: String?,
        @Query("id") contestID: String?
    ): Call<JoinUsableBalanceResponse>
}
