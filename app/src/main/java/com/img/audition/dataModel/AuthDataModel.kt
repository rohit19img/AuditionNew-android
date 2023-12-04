package com.img.audition.dataModel

import com.google.gson.annotations.SerializedName

data class RootResponse<T>(
    @SerializedName("success" ) var success : Boolean? = null,
    @SerializedName("message" ) var message : String?  = null,
    @SerializedName("data"    ) var data    : T?    = null
)

data class VersionInfo(
    @SerializedName("version" ) var version : Int?    = null,
    @SerializedName("point"   ) var point   : String? = null
)

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
    @SerializedName("id"    ) var id    : String? = null,
    @SerializedName("audition_id"    ) var auditionID    : String? = null,
)
data class NumLoginRequest(
    @SerializedName("mobile" ) var mobile : String? = null,
    @SerializedName("refer_code" ) var refer_code : String? = null
)
data class EmailLoginRequest(
    @SerializedName("email" ) var email : String? = null
)
data class CommanResponse(
    @SerializedName("success" ) var success : Boolean? = null,
    @SerializedName("message" ) var message : String?  = null,
//    @SerializedName("data"    ) var data    : Data?    = Data()
)
data class OTPRequest(
    @SerializedName("mobile" ) var mobile : String? = null,
    @SerializedName("otp"    ) var otp    : Int?    = null,
    @SerializedName("appid" ) var fcmToken : String? = null,

)
data class UserLatLang(var lat:Double? = null,var long:Double? = null)

data class OfferDataResponse(
    @SerializedName("success" ) var success : Boolean?        = null,
    @SerializedName("message" ) var message : String?         = null,
    @SerializedName("data"    ) var data    : ArrayList<OfferData> = arrayListOf()
)
data class OfferData(
    @SerializedName("_id"         ) var Id          : String? = null,
    @SerializedName("min_amount"  ) var minAmount   : Int?    = null,
    @SerializedName("max_amount"  ) var maxAmount   : Int?    = null,
    @SerializedName("bonus"       ) var bonus       : Int?    = null,
    @SerializedName("offer_code"  ) var offerCode   : String? = null,
    @SerializedName("bonus_type"  ) var bonusType   : String? = null,
    @SerializedName("title"       ) var title       : String? = null,
    @SerializedName("start_date"  ) var startDate   : String? = null,
    @SerializedName("image"       ) var image       : String? = null,
    @SerializedName("expire_date" ) var expireDate  : String? = null,
    @SerializedName("user_time"   ) var userTime    : Int?    = null,
    @SerializedName("type"        ) var type        : String? = null,
    @SerializedName("amt_limit"   ) var amtLimit    : Int?    = null,
    @SerializedName("description" ) var description : String? = null
)
data class TransactionReportResponse(
    @SerializedName("success" ) var success : Boolean?        = null,
    @SerializedName("message" ) var message : String?         = null,
    @SerializedName("data"    ) var data    : ArrayList<TransactionData> = arrayListOf()
)
data class TransactionData(
    @SerializedName("_id"            ) var Id            : String? = null,
    @SerializedName("type"           ) var type          : String? = null,
    @SerializedName("transaction_id" ) var transactionId : String? = null,
    @SerializedName("amount"         ) var amount        : Double?    = null,
    @SerializedName("paymentstatus"  ) var paymentstatus : String? = null
)

data class StatusGetSet(
    @SerializedName("success") var success : Boolean ?= null,
    @SerializedName("message") var message : String ?= null,
    @SerializedName("data") var data : ArrayList<StatusData> ?= null
) : java.io.Serializable

data class StatusData(
    @SerializedName("_id") var _id : String ?= null,
    @SerializedName("isSelf") var isSelf : Boolean ?= null,
    @SerializedName("name") var name : String ?= null,
    @SerializedName("image") var image : String ?= null,
    @SerializedName("audition_id") var audition_id : String ?= null,
    @SerializedName("status") var status : ArrayList<Status_statusData> ?= null
) : java.io.Serializable

data class Status_statusData(
    @SerializedName("_id") var _id : String ?= null,
    @SerializedName("userId") var userId : String ?= null,
    @SerializedName("text") var text : String ?= null,
    @SerializedName("media") var media : String ?= null,
    @SerializedName("is_deleted") var is_deleted : Boolean ?= null,
    @SerializedName("isSeen") var isSeen : Boolean ?= null,
    @SerializedName("seenBy") var seenBy : ArrayList<String> ?= null,
    @SerializedName("createdAt") var createdAt : String ?= null,
    @SerializedName("updatedAt") var updatedAt : String ?= null,
) : java.io.Serializable