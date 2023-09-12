package com.img.audition.dataModel

import com.google.gson.annotations.SerializedName

data class SearchResponse(
    @SerializedName("success" ) var success : Boolean? = null,
    @SerializedName("message" ) var message : String?  = null,
    @SerializedName("data"    ) var data    : SearchResponseData?    = SearchResponseData()
)

data class SearchUserData (

    @SerializedName("_id"             ) var Id             : String?  = null,
    @SerializedName("image"           ) var image          : String?  = null,
    @SerializedName("name"            ) var name           : String?  = null,
    @SerializedName("audition_id"     ) var auditionId     : String?  = null,
    @SerializedName("is_self"         ) var isSelf         : Boolean? = null,
    @SerializedName("follow_status"   ) var followStatus   : Boolean? = null,
    @SerializedName("followers_count" ) var followersCount : Int?     = null

)


data class SearchHashtagsData (

    @SerializedName("_id"    ) var Id     : String? = null,
    @SerializedName("Name"   ) var Name   : String? = null,
    @SerializedName("Videos" ) var Videos : Int?    = null

)

data class SearchResponseData (

    @SerializedName("users"    ) var users    : ArrayList<SearchUserData>    = arrayListOf(),
    @SerializedName("hashtags" ) var hashtags : ArrayList<SearchHashtagsData> = arrayListOf(),
    @SerializedName("data"     ) var data     : ArrayList<VideoData>     = arrayListOf()

)


data class TermAboutPrivacyResponse (
    @SerializedName("success" ) var success : Boolean? = null,
    @SerializedName("message" ) var message : String?  = null,
    @SerializedName("data"    ) var data    : TermAboutPrivacyData?    = TermAboutPrivacyData()
)

data class TermAboutPrivacyData(
    @SerializedName("_id"         ) var Id          : String?  = null,
    @SerializedName("title"       ) var title       : String?  = null,
    @SerializedName("description" ) var description : String?  = null,
    @SerializedName("is_deleted"  ) var isDeleted   : Boolean? = null,
    @SerializedName("createdAt"   ) var createdAt   : String?  = null,
    @SerializedName("updatedAt"   ) var updatedAt   : String?  = null
)