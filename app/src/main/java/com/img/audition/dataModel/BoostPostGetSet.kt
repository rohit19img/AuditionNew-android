package com.img.audition.dataModel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class BoostPostGetSet {
    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("success")
    @Expose
    var success: Boolean? = null

    @SerializedName("data")
    @Expose
    var data: DataBoost? = null
}

class DataBoost {
    @SerializedName("userId")
    @Expose
    var userId: String? = null

    @SerializedName("videoId")
    @Expose
    var videoId: String? = null

    @SerializedName("radius")
    @Expose
    var radius: Int? = null

    @SerializedName("coordinates")
    @Expose
    var coordinates: List<Float>? = null

    @SerializedName("days")
    @Expose
    var days: Int? = null

    @SerializedName("seenByUsers")
    @Expose
    var seenByUsers: List<Any>? = null

    @SerializedName("is_deleted")
    @Expose
    var isDeleted: Boolean? = null

    @SerializedName("_id")
    @Expose
    var id: String? = null

    @SerializedName("createdAt")
    @Expose
    var createdAt: String? = null

    @SerializedName("updatedAt")
    @Expose
    var updatedAt: String? = null
}