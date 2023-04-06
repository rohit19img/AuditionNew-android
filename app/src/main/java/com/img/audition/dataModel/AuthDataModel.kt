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