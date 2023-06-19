package com.img.audition.dataModel

import com.google.gson.annotations.SerializedName
import com.img.audition.globalAccess.ConstValFile

data class MusicDataResponse(
@SerializedName("message" ) var message : String?         = null,
@SerializedName("success" ) var success : Boolean?        = null,
@SerializedName("data"    ) var data    : ArrayList<MusicData> = arrayListOf()
)

data class MusicData(
    @SerializedName("_id"              ) var Id             : String?     = null,
    @SerializedName("title"            ) var title          : String?     = null,
    @SerializedName("track"            ) var track          : String?     = null,
    @SerializedName("track_aac_format" ) var trackAacFormat : String?     = null,
    @SerializedName("subtitle"         ) var subtitle       : String?     = null,
    @SerializedName("categoryId"       ) var categoryId     : CategoryId? = CategoryId(),
    @SerializedName("file"             ) var file           : String?     = null,
    @SerializedName("Image"            ) var Image          : String?     = null,
    @SerializedName("userid"           ) var userid         : String?     = null,
    @SerializedName("createdAt"        ) var createdAt      : String?     = null,
    @SerializedName("updatedAt"        ) var updatedAt      : String?     = null,
    @SerializedName("isFav"            ) var isFav          : Boolean     = false,
                                         var isPlay         : Boolean     = false
)

data class CategoryId(
    @SerializedName("_id"       ) var Id        : String? = null,
    @SerializedName("title"     ) var title     : String? = null,
    @SerializedName("image"     ) var image     : String? = null,
    @SerializedName("subtitle"  ) var subtitle  : String? = null,
    @SerializedName("createdAt" ) var createdAt : String? = null,
    @SerializedName("updatedAt" ) var updatedAt : String? = null
)

data class AllCategoryMusicDataResponse(
    @SerializedName("message" ) var message : String?         = null,
    @SerializedName("success" ) var success : Boolean?        = null,
    @SerializedName("data"    ) var data    : ArrayList<AllCategoryMusicData> = arrayListOf()
)

data class AllCategoryMusicData(
//    @SerializedName("title"    ) var catTitle    : String? = null,
//    @SerializedName("subtitle" ) var catSubtitle : String? = null,
    @SerializedName("image"    ) var catImage    : String? = null,
    @SerializedName("type"    ) var type    : String? = null,
    @SerializedName("_id"              ) var Id             : String?     = null,
    @SerializedName("title"            ) var title          : String?     = null,
    @SerializedName("track"            ) var track          : String?     = null,
    @SerializedName("track_aac_format" ) var trackAacFormat : String?     = null,
    @SerializedName("subtitle"         ) var subtitle       : String?     = null,
    @SerializedName("categoryId"       ) var categoryId     : CategoryId? = CategoryId(),
    @SerializedName("file"             ) var file           : String?     = null,
    @SerializedName("Image"            ) var Image          : String?     = null,
    @SerializedName("userid"           ) var userid         : String?     = null,
    @SerializedName("createdAt"        ) var createdAt      : String?     = null,
    @SerializedName("updatedAt"        ) var updatedAt      : String?     = null,
    @SerializedName("isFav"            ) var isFav          : Boolean     = false,
    var isPlay         : Boolean     = false


)