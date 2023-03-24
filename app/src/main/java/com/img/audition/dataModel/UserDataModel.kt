package com.img.audition.dataModel

import com.google.gson.annotations.SerializedName

data class GetOtherUserResponse(
    @SerializedName("message" ) var message : String?         = null,
    @SerializedName("success" ) var success : Boolean?        = null,
    @SerializedName("data"    ) var data    : ArrayList<GetOtherUserData> = arrayListOf()
)

data class GetOtherUserData(
    @SerializedName("_id"             ) var Id              : String?           = null,
    @SerializedName("image"           ) var image           : String?           = null,
    @SerializedName("name"            ) var name            : String?           = null,
    @SerializedName("audition_id"     ) var auditionId      : String?           = null,
    @SerializedName("email"           ) var email           : String?           = null,
    @SerializedName("bio"             ) var bio             : String?           = null,
    @SerializedName("profile_link"    ) var profileLink     : String?           = null,
    @SerializedName("followers_count" ) var followersCount  : Int?              = null,
    @SerializedName("followers"       ) var followers       : ArrayList<String> = arrayListOf(),
    @SerializedName("following_count" ) var followingCount  : Int?              = null,
    @SerializedName("following"       ) var following       : ArrayList<String> = arrayListOf(),
    @SerializedName("refer_code"      ) var referCode       : String?           = null,
    @SerializedName("special_refer"   ) var specialRefer    : String?           = null,
    @SerializedName("createdAt"       ) var createdAt       : String?           = null,
    @SerializedName("updatedAt"       ) var updatedAt       : String?           = null,
    @SerializedName("totalLike"       ) var totalLike       : Int?              = null,
    @SerializedName("follow_status"   )  var followStatus : Boolean?          = false
)

data class LanguageResponse(
    @SerializedName("success" ) var success : Boolean?        = null,
    @SerializedName("message" ) var message : String?         = null,
    @SerializedName("data"    ) var data    : ArrayList<languageData> = arrayListOf()
)
data class languageData(
    @SerializedName("languages" ) var languages : ArrayList<String> = arrayListOf()
)

class Languages {
    var language: String? = ""
    var isSelected = false
}

data class UserSelfProfileResponse(
    @SerializedName("message" ) var message : String?  = null,
    @SerializedName("success" ) var success : Boolean? = null,
    @SerializedName("data"    ) var data    : UserSelfData?    = UserSelfData()
)

data class UserSelfData(
    @SerializedName("id"                ) var id               : String? = null,
    @SerializedName("name"              ) var name             : String? = null,
    @SerializedName("mobile"            ) var mobile           : Long?    = null,
    @SerializedName("email"             ) var email            : String? = null,
    @SerializedName("audition_id"       ) var auditionId       : String? = null,
    @SerializedName("followers_count"   ) var followersCount   : Int?    = null,
    @SerializedName("following_count"   ) var followingCount   : Int?    = null,
    @SerializedName("bio"               ) var bio              : String? = null,
    @SerializedName("pincode"           ) var pincode          : String? = null,
    @SerializedName("address"           ) var address          : String? = null,
    @SerializedName("dob"               ) var dob              : String? = null,
    @SerializedName("DayOfBirth"        ) var DayOfBirth       : String? = null,
    @SerializedName("MonthOfBirth"      ) var MonthOfBirth     : String? = null,
    @SerializedName("YearOfBirth"       ) var YearOfBirth      : String? = null,
    @SerializedName("gender"            ) var gender           : String? = null,
    @SerializedName("image"             ) var image            : String? = null,
    @SerializedName("activation_status" ) var activationStatus : String? = null,
    @SerializedName("state"             ) var state            : String? = null,
    @SerializedName("city"              ) var city             : String? = null,
    @SerializedName("team"              ) var team             : String? = null,
    @SerializedName("teamfreeze"        ) var teamfreeze       : Int?    = null,
    @SerializedName("refer_code"        ) var referCode        : String? = null,
    @SerializedName("totalbalance"      ) var totalbalance     : String? = null,
    @SerializedName("totalwon"          ) var totalwon         : String? = null,
    @SerializedName("totalbonus"        ) var totalbonus       : String? = null,
    @SerializedName("totalcheers"       ) var totalcheers      : String? = null,
    @SerializedName("totalmints"        ) var totalmints       : String? = null,
    @SerializedName("walletamaount"     ) var walletamaount    : Int?    = null,
    @SerializedName("verified"          ) var verified         : Int?    = null,
    @SerializedName("downloadapk"       ) var downloadapk      : Int?    = null,
    @SerializedName("emailfreeze"       ) var emailfreeze      : Int?    = null,
    @SerializedName("mobilefreeze"      ) var mobilefreeze     : Int?    = null,
    @SerializedName("mobileVerified"    ) var mobileVerified   : Int?    = null,
    @SerializedName("emailVerified"     ) var emailVerified    : Int?    = null,
    @SerializedName("PanVerified"       ) var PanVerified      : Int?    = null,
    @SerializedName("BankVerified"      ) var BankVerified     : Int?    = null,
    @SerializedName("statefreeze"       ) var statefreeze      : Int?    = null,
    @SerializedName("dobfreeze"         ) var dobfreeze        : Int?    = null,
    @SerializedName("totalLike"         ) var totalLike        : Int?    = null
)

data class UserVerificationResponse(
    @SerializedName("message" ) var message : String?  = null,
    @SerializedName("success" ) var success : Boolean? = null,
    @SerializedName("data"    ) var data    : UserVerificationData?    = UserVerificationData()
)

data class UserVerificationData(
    @SerializedName("mobile_verify"        ) var mobileVerify       : Int?    = null,
    @SerializedName("email_verify"         ) var emailVerify        : Int?    = null,
    @SerializedName("bank_verify"          ) var bankVerify         : Int?    = null,
    @SerializedName("pan_verify"           ) var panVerify          : Int?    = null,
    @SerializedName("profile_image_verify" ) var profileImageVerify : Int?    = null,
    @SerializedName("image"                ) var image              : String? = null,
    @SerializedName("email"                ) var email              : String? = null,
    @SerializedName("mobile"               ) var mobile             : Int?    = null,
    @SerializedName("pan_comment"          ) var panComment         : String? = null,
    @SerializedName("bank_comment"         ) var bankComment        : String? = null
)

class WatchLaterResponse()