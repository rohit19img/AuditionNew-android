package com.img.audition.dataModel

import com.google.gson.annotations.SerializedName

data class LikeResponse (
    @SerializedName("message" ) var message : String?  = null,
    @SerializedName("success" ) var success : Boolean? = null,
    @SerializedName("likes"   ) var likes   : Int?     = null
        )

data class FollowFollowingResponse(
    @SerializedName("message" ) var message : String?  = null,
    @SerializedName("success" ) var success : Boolean? = null,
    @SerializedName("data"    ) var data    : FollowFollowingData?    = FollowFollowingData()
)

data class FollowFollowingData(
    @SerializedName("userFollowings"     ) var userFollowings     : Int? = null,
    @SerializedName("otherUserFollowers" ) var otherUserFollowers : Int? = null
)

data class ReportCategoryResponse(
    @SerializedName("message" ) var message : String?         = null,
    @SerializedName("success" ) var success : Boolean?        = null,
    @SerializedName("data"    ) var data    : ArrayList<ReportCategoryData> = arrayListOf()
)

data class ReportCategoryData(
    @SerializedName("_id"  ) var Id   : String? = null,
    @SerializedName("name" ) var name : String? = null,
                             var isSelected :Boolean = false
)

