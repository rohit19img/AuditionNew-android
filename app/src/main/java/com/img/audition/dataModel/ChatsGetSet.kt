package com.img.audition.dataModel

import com.google.gson.annotations.SerializedName

class ChatsGetSet {
    var isSuccess = false
    var message: String = ""
    var data: ArrayList<ChatsGetSet> = ArrayList()
    var _id: String = ""
    var room: String = ""
    var senderId: String = ""
    var receiverId: String = ""
    var seen: String = ""
    var createdAt: String = ""
    var updatedAt: String = ""
}

class CommentData{
    var comment_id : String? = ""
    var auditionID : String = ""
    var comment : String? = ""
    var commentBy : String? = ""
    var createdAt : String? = ""
    var postID : String? = ""
    var userID : String? = ""
    var userImage : String? = ""
}

data class ChatUserDataResponse(
    @SerializedName("success" ) var success : Boolean?        = null,
    @SerializedName("message" ) var message : String?         = null,
    @SerializedName("data"    ) var data    : ArrayList<ChatUserData> = arrayListOf()
)

data class ChatUserData(
    @SerializedName("_id"         ) var Id          : String?      = null,
    @SerializedName("lastMessage" ) var lastMessage : ChatLastMessage? = ChatLastMessage(),
    @SerializedName("name"        ) var name        : String?      = null,
    @SerializedName("image"       ) var image       : String?      = null,
    @SerializedName("audition_id" ) var auditionId  : String?      = null
)

data class ChatLastMessage(
    @SerializedName("message" ) var message : String?  = null,
    @SerializedName("seen"    ) var seen    : Boolean? = null
)