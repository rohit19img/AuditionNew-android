package com.img.audition.dataModel

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
    var auditionID : String = ""
    var comment : String? = ""
    var commentBy : String? = ""
    var createdAt : String? = ""
    var postID : String? = ""
    var userID : String? = ""
    var userImage : String? = ""
}
