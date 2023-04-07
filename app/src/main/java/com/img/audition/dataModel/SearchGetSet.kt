package com.img.audition.dataModel

import java.io.Serializable

class Searchgetset {
    var success: Boolean = true
    var message: String = ""
    var data: Data? = null

    class Data : Serializable {
        var users: ArrayList<User>? = null
        var hashtags: ArrayList<Hashtag>? = null
        var videos: ArrayList<VideoData>? = null
        var data: ArrayList<VideoData>? = null
    }

    class User {
        var item: Item? = null
        var refIndex: Int? = null
        private var _id: String? = null
        var image: String? = null
        var name: String? = null
        var audition_id: String? = null
        var is_self: Boolean? = null
        var followStatus: Boolean? = null
        var followers_count: Int? = null

        fun get_id(): String? {
            return _id
        }

        fun set_id(_id: String?) {
            this._id = _id
        }
    }

    class Item {
        private var _id: String? = null
        var image: String? = null
        var name: String? = null
        var audition_id: String? = null
        var is_self: Boolean? = null
        var followStatus: Boolean? = null
        var followers_count: Int? = null

        fun get_id(): String? {
            return _id
        }

        fun set_id(_id: String?) {
            this._id = _id
        }
    }

    class Hashtag {
        private var _id: String? = null
        var Name: String? = null
        var Videos: Int? = null

        fun get_id(): String? {
            return _id
        }

        fun set_id(_id: String?) {
            this._id = _id
        }
    }
}
