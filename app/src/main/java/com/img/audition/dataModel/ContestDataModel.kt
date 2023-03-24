package com.img.audition.dataModel

import com.google.gson.annotations.SerializedName

data class GetLiveContestDataResponse(
    @SerializedName("success" ) var success : Boolean?        = null,
    @SerializedName("message" ) var message : String?         = null,
    @SerializedName("data"    ) var data    : ArrayList<ContestData> = arrayListOf()
)

data class ContestData(
    @SerializedName("_id"                  ) var Id                 : String?           = null,
    @SerializedName("mega_status"          ) var megaStatus         : Int?              = null,
    @SerializedName("contest_name"         ) var contestName        : String?           = null,
    @SerializedName("file"                 ) var file               : String?           = null,
    @SerializedName("fileType"             ) var fileType           : String?           = null,
    @SerializedName("entryfee"             ) var entryfee           : Int?              = null,
    @SerializedName("win_amount"           ) var winAmount          : Int?              = null,
    @SerializedName("winning_percentage"   ) var winningPercentage  : Int?              = null,
    @SerializedName("maximum_user"         ) var maximumUser        : Int?              = null,
    @SerializedName("minimum_user"         ) var minimumUser        : Int?              = null,
    @SerializedName("contest_type"         ) var contestType        : String?           = null,
    @SerializedName("start_date"           ) var startDate          : String?           = null,
    @SerializedName("end_date"             ) var endDate            : String?           = null,
    @SerializedName("confirmed_challenge"  ) var confirmedChallenge : Int?              = null,
    @SerializedName("is_bonus"             ) var isBonus            : Int?              = null,
    @SerializedName("is_running"           ) var isRunning          : Int?              = null,
    @SerializedName("type"                 ) var type               : String?           = null,
    @SerializedName("status"               ) var status             : String?           = null,
    @SerializedName("final_status"         ) var finalStatus        : String?           = null,
    @SerializedName("joinedusers"          ) var joinedusers        : Int?              = null,
    @SerializedName("pricecard_type"       ) var pricecardType      : String?           = null,
    @SerializedName("freez"                ) var freez              : Int?              = null,
    @SerializedName("bonus_type"           ) var bonusType          : String?           = null,
    @SerializedName("winner_declared_type" ) var winnerDeclaredType : String?           = null,
    @SerializedName("bonus_percentage"     ) var bonusPercentage    : Int?              = null,
    @SerializedName("price_card"           ) var priceCard          : ArrayList<String> = arrayListOf(),
    @SerializedName("createdAt"            ) var createdAt          : String?           = null,
    @SerializedName("updatedAt"            ) var updatedAt          : String?           = null,
    @SerializedName("isJoined"             ) var isJoined           : Boolean?          = null

)