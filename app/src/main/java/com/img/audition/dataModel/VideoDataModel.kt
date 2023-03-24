package com.img.audition.dataModel

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName


data class VideoResponse(
    @SerializedName("message" ) var message : String?         = null,
    @SerializedName("success" ) var success : Boolean?        = null,
    @SerializedName("data"    ) var data    : ArrayList<VideoData> = arrayListOf()
)

data class VideoData(
    @SerializedName("_id"            ) var Id            : String?           = null,
    @SerializedName("caption"        ) var caption       : String?           = null,
    @SerializedName("language"       ) var language      : String?           = null,
    @SerializedName("location"       ) var location      : String?           = null,
    @SerializedName("userId"         ) var userId        : String?           = null,
    @SerializedName("file"           ) var file          : String?           = null,
    @SerializedName("users_like"     ) var usersLike     : ArrayList<String> = arrayListOf(),
    @SerializedName("like_count"     ) var likeCount     : Int?              = null,
    @SerializedName("status"         ) var status        : String?           = null,
    @SerializedName("enabled"        ) var enabled       : Boolean?          = null,
    @SerializedName("contest_status" ) var contestStatus : String?           = null,
    @SerializedName("checked"        ) var checked       : Boolean?          = null,
    @SerializedName("wrongContent"   ) var wrongContent  : Boolean?          = null,
    @SerializedName("comment"        ) var comment       : ArrayList<String> = arrayListOf(),
    @SerializedName("private"        ) var private       : Boolean?          = null,
    @SerializedName("shares"         ) var shares        : Int?              = null,
    @SerializedName("postId"         ) var postId        : String?           = null,
    @SerializedName("views"          ) var views         : Int?              = null,
    @SerializedName("vId"            ) var vId           : String?           = null,
    @SerializedName("hashtag"        ) var hashtag       : ArrayList<String> = arrayListOf(),
    @SerializedName("hashtagId"      ) var hashtagId     : ArrayList<String> = arrayListOf(),
    @SerializedName("createdAt"      ) var createdAt     : String?           = null,
    @SerializedName("updatedAt"      ) var updatedAt     : String?           = null,
    @SerializedName("string"         ) var string        : String?           = null,
    @SerializedName("vote_status"    ) var voteStatus    : Boolean?          = null,
    @SerializedName("is_self"        ) var isSelf        : Boolean?          = null,
    @SerializedName("comment_count"  ) var commentCount  : Int?              = null,
    @SerializedName("like_status"    ) var likeStatus    : Boolean?          = null,
    @SerializedName("follow_status"  ) var followStatus  : Boolean?          = null,
    @SerializedName("audition_id"    ) var auditionId    : String?           = null,
    @SerializedName("image"          ) var image         : String?           = null,
    @SerializedName("isSaved"        ) var isSaved       : Boolean?          = null
) : java.io.Serializable
