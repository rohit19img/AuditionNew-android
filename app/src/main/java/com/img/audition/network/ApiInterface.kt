package com.img.audition.network

import com.google.gson.JsonObject
import com.img.audition.dataModel.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiInterface {
    @POST(APITags.GuestLogin)
    fun guestLogin(@Body guestRequest: GuestLoginRequest): Call<LoginResponse>

    @GET(APITags.GetVideo)
    fun getVideo(
        @Header(APITags.AUTHORIZATION) Auth: String,
        @Query("language") language: String?,
        @Query("lat") lat: Double?,
        @Query("long") lang: Double?
    ): Call<VideoResponse>

    @POST(APITags.Login)
    fun Login(@Body loginRequest:NumLoginRequest): Call<CommanResponse>

    @POST(APITags.OTPLogin)
    fun OTP_Login(@Body otpRequest: OTPRequest): Call<LoginResponse>

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

    @GET(APITags.GetUserVideo)
    fun getOtherUserVideo(
        @Header(APITags.AUTHORIZATION) Auth: String?,
        @Query("id") userID: String?
    ): Call<VideoResponse>

    @GET(APITags.GetLanguages)
    fun getLanguages(@Header(APITags.AUTHORIZATION) Auth: String?): Call<LanguageResponse?>

    @GET(APITags.GetUserSelfFullDetails)
    fun getUserSelfDetails(@Header(APITags.AUTHORIZATION) Auth: String?): Call<UserSelfProfileResponse>

    @GET(APITags.GetUserVideo)
    fun getUserSelfVideo(@Header(APITags.AUTHORIZATION) Auth: String?): Call<VideoResponse>

    @GET(APITags.LogoutUser)
    fun logoutUser(@Header(APITags.AUTHORIZATION) Auth: String?): Call<CommanResponse>

    @POST(APITags.VerifyMobileNumber)
    fun verifyMobileNumber(@Header(APITags.AUTHORIZATION) Auth: String?, @Body loginRequest:NumLoginRequest): Call<CommanResponse>

    @GET(APITags.AllVerify)
    fun getAllVerificationsData(@Header(APITags.AUTHORIZATION)Auth: String?) : Call<UserVerificationResponse>

    @GET(APITags.GetAllLiveContest)
    fun getAllLiveContest(@Header(APITags.AUTHORIZATION)Auth: String?) : Call<GetLiveContestDataResponse>

    @GET(APITags.GetJoinedContest)
    fun getJoinedContest(@Header(APITags.AUTHORIZATION) Auth: String?): Call<GetJoinedContestDataResponse>

    @GET(APITags.WatchLater)
    fun saveIntoWatchLater(@Header(APITags.AUTHORIZATION) Auth: String?, @Path("id") id: String?): Call<CommanResponse>

    @GET(APITags.RemoveWatchLater)
    fun remove_watch_later(@Header(APITags.AUTHORIZATION) Auth: String?, @Path("id") id: String?): Call<CommanResponse>

    @GET(APITags.FollowFollowingList)
    fun getFollowFollowingList(@Header(APITags.AUTHORIZATION) Auth: String?): Call<FollowerFollowingListResponse>

    @GET(APITags.GetMusicList)
    fun getMusicList(@Header(APITags.AUTHORIZATION) Auth:String) : Call<MusicDataResponse>

    @GET(APITags.GetReportCategory)
    fun getReportCategory(@Header(APITags.AUTHORIZATION) Auth: String?): Call<ReportCategoryResponse>

    @GET(APITags.GetNotInterestedCategory)
    fun getNotInterestedCategory(@Header(APITags.AUTHORIZATION) Auth: String?): Call<ReportCategoryResponse>

    @GET(APITags.ReportVideo)
    fun reportTheVideo(@Header(APITags.AUTHORIZATION) Auth: String?, @Query("reportId") reportID: String?, @Query("reportvideoid") videoID: String?): Call<CommanResponse>

    @GET(APITags.addIntoNotInterested)
    fun addIntoNotInterestedVideo(@Header(APITags.AUTHORIZATION) Auth: String?, @Query("catId") contestId: String?, @Query("videoId") videoID: String?): Call<CommanResponse>

    @GET(APITags.GetChatHistory)
    fun getChatHistory(
        @Header(APITags.AUTHORIZATION) Auth: String?,
        @Query("receiverId") contestId: String?,
        @Query("page") page_no: Int
    ): Call<ChatsGetSet>

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
    fun search(
        @Header("Authorization") Auth: String?,
        @Body login: JsonObject?
    ): Call<Searchgetset>

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

    @GET("boost-rate")
    fun getBoostRate(@Header("Authorization") Auth: String?): Call<JsonObject>

    @POST("boost-video")
    fun boostPost(
        @Header("Authorization") Auth: String?,
        @Body boostPost: JsonObject?
    ): Call<BoostPostGetSet>

    @GET("userFullDetails")
    fun userprofile(@Header("Authorization") Auth: String?): Call<Profilegetset>

    @GET("getLikeCategory")
    fun getVoteCategory(@Header("Authorization") Auth: String?): Call<VoteDataResponse>

    @POST("userGiveVote")
    fun voteToUserVideo(@Header("Authorization") Auth: String?, @Body login: JsonObject?): Call<CommanResponse>
}
