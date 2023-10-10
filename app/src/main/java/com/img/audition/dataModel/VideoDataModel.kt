package com.img.audition.dataModel

import com.google.gson.annotations.SerializedName


data class VideoResponse(
    @SerializedName("message") var message: String? = null,
    @SerializedName("success") var success: Boolean? = null,
    @SerializedName("data") var data: ArrayList<VideoData> = arrayListOf()
) : java.io.Serializable


data class VideoData(
    @SerializedName("_id") var Id: String? = null,
    @SerializedName("caption") var caption: String? = null,
    @SerializedName("language") var language: String? = null,
    @SerializedName("location") var location: String? = null,
    @SerializedName("userId") var userId: String? = null,
    @SerializedName("file") var file: String? = null,
    @SerializedName("users_like") var usersLike: ArrayList<String> = arrayListOf(),
    @SerializedName("like_count") var likeCount: Int? = null,
    @SerializedName("status") var status: String? = null,
    @SerializedName("songLink") var songLink: String? = null,
    @SerializedName("enabled") var enabled: Boolean? = null,
    @SerializedName("contest_status") var contestStatus: String? = null,
    @SerializedName("isAllowComment") var allowComment: Boolean? = null,
    @SerializedName("isAllowSharing") var allowSharing: Boolean? = null,
    @SerializedName("isAllowDuet") var allowDuet: Boolean? = null,
    @SerializedName("checked") var checked: Boolean? = null,
    @SerializedName("wrongContent") var wrongContent: Boolean? = null,
    @SerializedName("comment") var comment: ArrayList<Comment> = arrayListOf(),
    @SerializedName("votes") var votes: ArrayList<Votes> = arrayListOf(),
    @SerializedName("private") var private: Boolean? = false,
    @SerializedName("shares") var shares: Int? = null,
    @SerializedName("postId") var postId: String? = null,
    @SerializedName("views") var views: Int? = null,
    @SerializedName("songId") var songId: String? = null,
    @SerializedName("song") var song: Song? = Song(),
    @SerializedName("vId") var vId: String? = null,
    @SerializedName("hashtag") var hashtag: ArrayList<String> = arrayListOf(),
//    @SerializedName("hashtagId"      ) var hashtagId     : ArrayList<String> = arrayListOf(),
    @SerializedName("createdAt") var createdAt: String? = null,
    @SerializedName("updatedAt") var updatedAt: String? = null,
    @SerializedName("string") var string: String? = null,
    @SerializedName("vote_status") var voteStatus: Boolean? = false,
    @SerializedName("is_self") var isSelf: Boolean? = false,
    @SerializedName("comment_count") var commentCount: Int? = null,
    @SerializedName("like_status") var likeStatus: Boolean? = false,
    @SerializedName("follow_status") var followStatus: Boolean? = false,
    @SerializedName("audition_id") var auditionId: String? = null,
    @SerializedName("image") var image: String? = null,
    @SerializedName("isSaved") var isSaved: Boolean? = false,
    @SerializedName("isBoosted") var isBoosted: Boolean? = false,
    @SerializedName("hashName") var hashName: String? = null,
    @SerializedName("is_blocked") var isBlocked: Boolean = false,
    var isSelected: Boolean = false,

    ) : java.io.Serializable

data class Votes(
    @SerializedName("uservotes") var uservotes: String? = null,
    @SerializedName("_id") var Id: String? = null,
    @SerializedName("like_category") var likeCategory: String? = null,
    @SerializedName("vote") var vote: String? = null,
    @SerializedName("__v") var _v: Int? = null,
    @SerializedName("emoji") var emoji: String? = null
) : java.io.Serializable

data class VoteDataResponse(
    @SerializedName("success") var success: Boolean? = null,
    @SerializedName("message") var message: String? = null,
    @SerializedName("data") var data: ArrayList<VoteData> = arrayListOf()
) : java.io.Serializable

data class VoteData(
    @SerializedName("_id") var Id: String? = null,
    @SerializedName("like_category") var likeCategory: String? = null,
    @SerializedName("vote") var vote: String? = null,
    @SerializedName("__v") var _v: Int? = null,
    @SerializedName("emoji") var emoji: String? = null
) : java.io.Serializable


data class TrendingVideoResponse(
    @SerializedName("success") var success: Boolean? = null,
    @SerializedName("message") var message: String? = null,
    @SerializedName("data") var data: TrendingVideoData? = TrendingVideoData()
) : java.io.Serializable

data class TrendingVideoData(
    @SerializedName("data") var data: ArrayList<TrendingVideo> = arrayListOf(),
    @SerializedName("images") var images: ArrayList<String> = arrayListOf()
) : java.io.Serializable

data class TrendingVideo(
    @SerializedName("_id") var Id: String? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("views") var views: Int? = null,
    @SerializedName("videos") var videos: ArrayList<VideoData> = arrayListOf()
) : java.io.Serializable

data class UploadMusicResponse(
    @SerializedName("message") var message: String? = null,
    @SerializedName("success") var success: Boolean? = null,
    @SerializedName("data") var data: UploadMusicData? = UploadMusicData()

) : java.io.Serializable

data class UploadMusicData(
    @SerializedName("title") var title: String? = null,
    @SerializedName("track_aac_format") var trackAacFormat: String? = null,
    @SerializedName("subtitle") var subtitle: String? = null,
    @SerializedName("file") var file: String? = null,
    @SerializedName("Image") var Image: String? = null,
    @SerializedName("userid") var userid: String? = null,
    @SerializedName("byUser") var byUser: Boolean? = null,
    @SerializedName("_id") var Id: String? = null,
    @SerializedName("createdAt") var createdAt: String? = null,
    @SerializedName("updatedAt") var updatedAt: String? = null
) : java.io.Serializable

data class Song(

    @SerializedName("_id") var Id: String? = null,
    @SerializedName("title") var title: String? = null,
    @SerializedName("track_aac_format") var trackAacFormat: String? = null,
    @SerializedName("subtitle") var subtitle: String? = null,
    @SerializedName("file") var file: String? = null,
    @SerializedName("Image") var Image: String? = null,
    @SerializedName("userid") var userid: String? = null,
    @SerializedName("byUser") var byUser: Boolean? = null,
    @SerializedName("createdAt") var createdAt: String? = null,
    @SerializedName("updatedAt") var updatedAt: String? = null,
    @SerializedName("track") var track: String? = null,
    @SerializedName("categoryId") var categoryId: String? = null,
    @SerializedName("userDetails") var userDetails: userDetails? = userDetails(),


    ) : java.io.Serializable

data class SongVideoResponse(

    @SerializedName("message") var message: String? = null,
    @SerializedName("success") var success: Boolean? = null,
    @SerializedName("data") var data: SongVideoData? = SongVideoData()
) : java.io.Serializable

data class SongVideoData(

    @SerializedName("_id") var Id: String? = null,
    @SerializedName("title") var title: String? = "",
    @SerializedName("track_aac_format") var trackAacFormat: String? = null,
    @SerializedName("subtitle") var subtitle: String? = "",
    @SerializedName("file") var file: String? = null,
    @SerializedName("Image") var Image: String? = null,
    @SerializedName("userid") var userid: String? = null,
    @SerializedName("byUser") var byUser: Boolean? = null,
    @SerializedName("createdAt") var createdAt: String? = null,
    @SerializedName("updatedAt") var updatedAt: String? = null,
    @SerializedName("musicDetails") var musicDetails: MusicDetails? = MusicDetails(),
    @SerializedName("videos") var videos: ArrayList<VideoData> = arrayListOf()

) : java.io.Serializable

data class MusicDetails(

    @SerializedName("_id") var Id: String? = null,
    @SerializedName("audition_id") var auditionId: String? = ""

) : java.io.Serializable

data class userDetails(
    @SerializedName("_id") var Id: String? = null,
    @SerializedName("image") var image: String? = "",
    @SerializedName("audition_id") var auditionId: String? = ""
) : java.io.Serializable

data class Comment(
    @SerializedName("user_id") var userId: String? = null,
    @SerializedName("comment") var comment: String? = null,
    @SerializedName("_id") var Id: String? = null,
    @SerializedName("createdAt") var createdAt: String? = null,
    @SerializedName("updatedAt") var updatedAt: String? = null
) : java.io.Serializable