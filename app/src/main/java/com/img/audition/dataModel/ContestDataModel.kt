package com.img.audition.dataModel

import com.google.gson.annotations.SerializedName

data class GetLiveContestDataResponse(
    @SerializedName("success" ) var success : Boolean?        = null,
    @SerializedName("message" ) var message : String?         = null,
    @SerializedName("data"    ) var data    : ArrayList<LiveContestData> = arrayListOf()
)

data class LiveContestData(
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
    @SerializedName("start_date"           ) var startDate          : String          = "",
    @SerializedName("end_date"             ) var endDate            : String           = "",
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
    @SerializedName("price_card"           ) var priceCard : ArrayList<LiveContestPriceCard> = arrayListOf(),
    @SerializedName("createdAt"            ) var createdAt          : String?           = null,
    @SerializedName("updatedAt"            ) var updatedAt          : String?           = null,
    @SerializedName("isJoined"             ) var isJoined           : Boolean?          = null,
    @SerializedName("total_winners"        ) var total_winners      : Int?              =null

) : java.io.Serializable

data class GetJoinedContestDataResponse(
    @SerializedName("success" ) var success : Boolean?        = null,
    @SerializedName("message" ) var message : String?         = null,
    @SerializedName("data"    ) var data    : ArrayList<JoinedContestData> = arrayListOf()
        )

data class JoinedContestData (
    @SerializedName("userrank"            ) var userrank           : Int?                 = null,
    @SerializedName("win_amount_str"      ) var winAmountStr       : String?              = null,
    @SerializedName("joinedleaugeId"      ) var joinedleaugeId     : String?              = null,
    @SerializedName("challengeid"         ) var challengeid        : String?              = null,
    @SerializedName("refercode"           ) var refercode          : String?              = null,
    @SerializedName("file"                ) var file               : String?              = null,
    @SerializedName("fileType"            ) var fileType           : String?              = null,
    @SerializedName("start_date"          ) var startDate          : String?              = null,
    @SerializedName("end_date"            ) var endDate            : String?              = null,
    @SerializedName("winamount"           ) var winamount          : Int?                 = null,
    @SerializedName("is_bonus"            ) var isBonus            : Int?                 = null,
    @SerializedName("bonus_percentage"    ) var bonusPercentage    : Int?                 = null,
    @SerializedName("winning_percentage"  ) var winningPercentage  : Int?                 = null,
    @SerializedName("contest_type"        ) var contestType        : String?              = null,
    @SerializedName("confirmed_challenge" ) var confirmedChallenge : Int?                 = null,
    @SerializedName("joinedusers"         ) var joinedusers        : Int?                 = null,
    @SerializedName("entryfee"            ) var entryfee           : Int?                 = null,
    @SerializedName("pricecard_type"      ) var pricecardType      : String?              = null,
    @SerializedName("maximum_user"        ) var maximumUser        : Int?                 = null,
    @SerializedName("Finalstatus"         ) var Finalstatus        : String?              = null,
    @SerializedName("ChallengeStatus"     ) var ChallengeStatus    : String?              = null,
    @SerializedName("totalwinning"        ) var totalwinning       : String?              = null,
    @SerializedName("isselected"          ) var isselected         : Boolean?             = null,
    @SerializedName("totalwinners"        ) var totalwinners       : Int?                 = null,
    @SerializedName("price_card"          ) var priceCard          : ArrayList<JoinedPriceCard> = arrayListOf(),
    @SerializedName("pricecardstatus"     ) var pricecardstatus    : Int?                 = null,
    @SerializedName("plus"                ) var plus               : String?              = null
        )

data class JoinedPriceCard (

    @SerializedName("start_position" ) var startPosition : Int?    = null,
    @SerializedName("price"          ) var price         : String? = null

)

data class SingleContestDetailsResponse(
    @SerializedName("success" ) var success : Boolean? = null,
    @SerializedName("message" ) var message : String?  = null,
    @SerializedName("data"    ) var data    : SingleContestDetailsData?    = SingleContestDetailsData()
)

data class SingleContestDetailsData(
    @SerializedName("matchchallengeid"    ) var matchchallengeid   : String?              = null,
    @SerializedName("winning_percentage"  ) var winningPercentage  : Int?                 = null,
    @SerializedName("entryfee"            ) var entryfee           : Int?                 = null,
    @SerializedName("win_amount"          ) var winAmount          : Int?                 = null,
    @SerializedName("contest_type"        ) var contestType        : String?              = null,
    @SerializedName("maximum_user"        ) var maximumUser        : Int?                 = null,
    @SerializedName("joinedusers"         ) var joinedusers        : Int?                 = null,
    @SerializedName("contest_name"        ) var contestName        : String?              = null,
    @SerializedName("confirmed_challenge" ) var confirmedChallenge : Int?                 = null,
    @SerializedName("is_running"          ) var isRunning          : Int?                 = null,
    @SerializedName("is_bonus"            ) var isBonus            : Int?                 = null,
    @SerializedName("start_date"          ) var startDate          : String?              = null,
    @SerializedName("end_date"            ) var endDate            : String?              = null,
    @SerializedName("bonus_percentage"    ) var bonusPercentage    : Int?                 = null,
    @SerializedName("pricecard_type"      ) var pricecardType      : String?              = null,
    @SerializedName("isselected"          ) var isselected         : Boolean?             = null,
    @SerializedName("bonus_date"          ) var bonusDate          : String?              = null,
    @SerializedName("isselectedid"        ) var isselectedid       : String?              = null,
    @SerializedName("refercode"           ) var refercode          : String?              = null,
    @SerializedName("totalwinners"        ) var totalwinners       : Int?                 = null,
    @SerializedName("price_card"          ) var priceCard          : ArrayList<SingleContestPriceCard> = arrayListOf(),
    @SerializedName("status"              ) var status             : Int?                 = null
)

data class SingleContestPriceCard(
    @SerializedName("id"                ) var id               : String?    = null,
    @SerializedName("winners"           ) var winners          : String? = null,
    @SerializedName("price"             ) var price            : String? = null,
    @SerializedName("total"             ) var total            : String? = null,
    @SerializedName("start_position"    ) var startPosition    : String? = null,
    @SerializedName("distribution_type" ) var distributionType : String? = null
)

data class LeaderboardDataResponse(
    @SerializedName("success" ) var success : Boolean?        = null,
    @SerializedName("message" ) var message : String?         = null,
    @SerializedName("data"    ) var data    : ArrayList<LeaderboardData> = arrayListOf()
)

data class LeaderboardData(
    @SerializedName("usernumber"   ) var usernumber   : Int?    = null,
    @SerializedName("joinleaugeid" ) var joinleaugeid : String? = null,
    @SerializedName("joinVideosId" ) var joinVideosId : String? = null,
    @SerializedName("videoid" ) var videoID : String? = null,
    @SerializedName("userid"       ) var userid       : String? = null,
    @SerializedName("name"         ) var name         : String? = null,
    @SerializedName("auditionId"   ) var auditionId   : String? = null,
    @SerializedName("image"        ) var image        : String? = null,
    @SerializedName("status"       ) var status        : String? = null,
    @SerializedName("votes"       ) var voteCount        : Int? = null
)

data class LiveContestPriceCard (

    @SerializedName("challengeId"   ) var challengeId  : String? = null,
    @SerializedName("winners"       ) var winners      : String? = null,
    @SerializedName("price"         ) var price        : Int?    = null,
    @SerializedName("price_percent" ) var pricePercent : Float?    = null,
    @SerializedName("min_position"  ) var minPosition  : Int?    = null,
    @SerializedName("max_position"  ) var maxPosition  : Int?    = null,
    @SerializedName("total"         ) var total        : Int?    = null,
    @SerializedName("type"          ) var type         : String? = null,
    @SerializedName("_id"           ) var Id           : String? = null

) : java.io.Serializable

data class LiveRanksLeaderboardResponse(
    @SerializedName("success" ) var success : Boolean? = null,
    @SerializedName("message" ) var message : String?  = null,
    @SerializedName("data"    ) var data    : LiveRanksLeaderboardData?    = LiveRanksLeaderboardData()
)

data class LiveRanksLeaderboardData(
    @SerializedName("userrank"  ) var userrank  : Int?                 = null,
    @SerializedName("pdfname"   ) var pdfname   : String?              = null,
    @SerializedName("jointeams" ) var jointeams : ArrayList<Jointeams> = arrayListOf()
)

data class Jointeams(
    @SerializedName("userjoinid"     ) var userjoinid     : String?  = null,
    @SerializedName("userid"         ) var userid         : String?  = null,
    @SerializedName("joinVideosId"   ) var joinVideosId   : String?  = null,
    @SerializedName("joinleaugeid"   ) var joinleaugeId   : String?  = null,
    @SerializedName("videoid"   ) var videoId   : String?  = null,
    @SerializedName("vote"           ) var vote           : Int?     = null,
    @SerializedName("teamname"       ) var teamname       : String?  = null,
    @SerializedName("getcurrentrank" ) var getcurrentrank : Int?     = null,
    @SerializedName("image"          ) var image          : String?  = null,
    @SerializedName("userno"         ) var userno         : Int?     = null,
    @SerializedName("is_show"        ) var isShow         : Boolean? = null,
    @SerializedName("winingamount"   ) var winingamount   : String?  = null,
    @SerializedName("audition_id"   ) var auditionId   : String?  = null,
    @SerializedName("status"   ) var status   : String?  = null,
    @SerializedName("winningcrown"   ) var winningcrown   : String?  = null
)

data class JoinUsableBalanceResponse(
    @SerializedName("success" ) var success : Boolean? = null,
    @SerializedName("message" ) var message : String?  = null,
    @SerializedName("data"    ) var data    : JoinUsableBalanceData?    = JoinUsableBalanceData()
)

data class JoinUsableBalanceData(
    @SerializedName("usablebalance"    ) var usablebalance    : String? = null,
    @SerializedName("usertotalbalance" ) var usertotalbalance : String? = null,
    @SerializedName("entryfee"         ) var entryfee         : String? = null,
    @SerializedName("bonus"            ) var bonus            : String? = null
)