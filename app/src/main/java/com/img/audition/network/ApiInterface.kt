package com.img.audition.network

import com.img.audition.dataModel.*
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
    fun Login(@Body loginRequest:NumLoginRequest): Call<CommonResponse>

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
    fun logoutUser(@Header(APITags.AUTHORIZATION) Auth: String?): Call<CommonResponse>

    @POST(APITags.VerifyMobileNumber)
    fun verifyMobileNumber(@Header(APITags.AUTHORIZATION) Auth: String?, @Body loginRequest:NumLoginRequest): Call<CommonResponse>

    @GET(APITags.AllVerify)
    fun getAllVerificationsData(@Header(APITags.AUTHORIZATION)Auth: String?) : Call<UserVerificationResponse>

    @GET(APITags.GetAllLiveContest)
    fun getAllLiveContest(@Header(APITags.AUTHORIZATION)Auth: String?) : Call<GetLiveContestDataResponse>

    @GET(APITags.WatchLater)
    fun saveIntoWatchLater(@Header("Authorization") Auth: String?, @Path("id") id: String?): Call<CommonResponse>

    @GET()
    fun remove_watch_later(@Header("Authorization") Auth: String?, @Path("id") id: String?): Call<CommonResponse>
}