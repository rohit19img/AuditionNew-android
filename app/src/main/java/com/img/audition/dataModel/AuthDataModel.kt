package com.img.audition.dataModel

import com.google.gson.annotations.SerializedName

data class GuestLoginRequest(
    @SerializedName("deviceId" ) var deviceId : String? = null,
    @SerializedName("appId"    ) var fcmToken : String? = null
)
data class LoginResponse(
    @SerializedName("success" ) var success : Boolean? = null,
    @SerializedName("message" ) var message : String?  = null,
    @SerializedName("data"    ) var data    : LoginData?    = LoginData()
)
data class LoginData (
    @SerializedName("token" ) var token : String? = null,
    @SerializedName("id"    ) var id    : String? = null
)

data class NumLoginRequest(
    @SerializedName("mobile" ) var mobile : String? = null
)

data class CommonResponse(
    @SerializedName("success" ) var success : Boolean? = null,
    @SerializedName("message" ) var message : String?  = null,
//    @SerializedName("data"    ) var data    : Data?    = Data()
)

data class OTPRequest(
    @SerializedName("mobile" ) var mobile : String? = null,
    @SerializedName("otp"    ) var otp    : Int?    = null,
    @SerializedName("appid" ) var fcmToken : String? = null

)
data class UserLatLang(var lat:Double? = null,var long:Double? = null)