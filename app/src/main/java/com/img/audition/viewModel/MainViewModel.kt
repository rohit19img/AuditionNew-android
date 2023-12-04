package com.img.audition.viewModel


    import android.util.Log
    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.liveData
    import com.google.gson.Gson
    import com.google.gson.JsonObject
import com.img.audition.dataModel.GuestLoginRequest
import com.img.audition.dataModel.NumLoginRequest
import com.img.audition.dataModel.OTPRequest
import com.img.audition.network.ApiInterface
import kotlinx.coroutines.Dispatchers
    import retrofit2.HttpException

class MainViewModel(private val token: String?,private val apiInterface: ApiInterface): ViewModel() {

    fun getForYouReelsVideo(language: String?,lat:Double?,long:Double?) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiInterface.getForYouReelsVideo(token!!,language,lat,long)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error occurred"))
        }
    }

    fun getVideoByID(videoID: String?) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiInterface.getVideoByID(videoID)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error occurred"))
        }
    }

    fun getLiveContestReelsVideo(language: String?,lat:Double?,long:Double?, _id : String?) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiInterface.getLiveContestReelsVideo(token!!,language,lat,long,_id)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error occurred"))
        }
    }

    fun getLiveContestReelsCategories(language: String?) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiInterface.getLiveContestReelsCategories(token!!,language)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error occurred"))
        }
    }


    fun guestLogin(guestRequest: GuestLoginRequest) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiInterface.guestLogin(guestRequest)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error occurred"))
        }
    }


    fun userLogin(numLoginRequest: NumLoginRequest) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            val response =apiInterface.userLogin(numLoginRequest)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    emit(Resource.success(data = body))
                } else {
                    emit(Resource.error(data = null, message = "Response body is null"))
                }
            } else if (response.code() == 400) {
                // Handle 400 response here and emit the data
                val body = response.errorBody()?.string()
                val jsonObject = Gson().fromJson(body, JsonObject::class.java)
                emit(Resource.error(data = jsonObject, message = "Error: ${response.code()}"))
            } else {
                emit(Resource.error(data = null, message = "Error: ${response.code()}"))
            }
        } catch (exception: HttpException) {
            emit(Resource.error(data = null, message = "HTTP error: ${exception.message}"))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error occurred"))
        }
    }

    fun getVersion() = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            val response = apiInterface.getVersion()

            if (response.isSuccessful) {
                val body = response.body()?.data
                if (body != null) {
                    emit(Resource.success(data = body))
                } else {
                    emit(Resource.error(data = null, message = "Response body is null"))
                }
            } else if (response.code() == 400) {
                // Handle 400 response here and emit the data
                val body = response.errorBody()?.string()
                val jsonObject = Gson().fromJson(body, JsonObject::class.java)
                emit(Resource.error(data = jsonObject, message = "Error: ${response.code()}"))
            } else {
                emit(Resource.error(data = null, message = "Error: ${response.code()}"))
            }
        } catch (exception: HttpException) {
            emit(Resource.error(data = null, message = "HTTP error: ${exception.message}"))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error occurred"))
        }
    }

    fun defaultLogin(numLoginRequest: NumLoginRequest) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            val response =apiInterface.defaultLogin(numLoginRequest)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    emit(Resource.success(data = body)) // Assuming RootResponse has a 'data' property
                } else {
                    emit(Resource.error(data = null, message = "Response body is null"))
                }
            } else if (response.code() == 400) {
                // Handle 400 response here and emit the data
                val body = response.errorBody()?.string()
                val jsonObject = Gson().fromJson(body, JsonObject::class.java)
                emit(Resource.error(data = jsonObject, message = "Error: ${response.code()}"))
            } else {
                emit(Resource.error(data = null, message = "Error: ${response.code()}"))
            }
        } catch (exception: HttpException) {
            emit(Resource.error(data = null, message = "HTTP error: ${exception.message}"))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error occurred"))
        }
    }

    fun userOtpVerify(otpRequest: OTPRequest) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            val response =apiInterface.userOtpVerify(otpRequest)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    emit(Resource.success(data = body)) // Assuming RootResponse has a 'data' property
                } else {
                    emit(Resource.error(data = null, message = "Response body is null"))
                }
            } else if (response.code() == 400) {
                // Handle 400 response here and emit the data
                val body = response.errorBody()?.string()
                val jsonObject = Gson().fromJson(body, JsonObject::class.java)
                emit(Resource.error(data = jsonObject, message = "Error: ${response.code()}"))
            } else {
                emit(Resource.error(data = null, message = "Error: ${response.code()}"))
            }
        } catch (exception: HttpException) {
            emit(Resource.error(data = null, message = "HTTP error: ${exception.message}"))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error occurred"))
        }
    }

    fun getUserVideo(id:String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiInterface.getUserVideo(token!!,id)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error occurred"))
        }
    }

    fun getHashTagVideo(id:String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiInterface.getHashTagVideo(token!!,id)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error occurred"))
        }
    }

    fun getMusicVideo(id:String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiInterface.getMusicVideo(token!!,id)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error occurred"))
        }
    }

    fun getTrendingVideo() = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiInterface.getTrendingVideo(token!!)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error occurred"))
        }
    }

    fun getContestVideo(userId:String,challengeId: String?) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiInterface.getContestVideo(token!!,userId,challengeId)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error occurred"))
        }
    }

    fun getSearchData(searchObj: JsonObject?) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiInterface.search(token!!,searchObj)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error occurred"))
        }
    }

    fun getUserSelfDetails() = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiInterface.getUserSelfDetails(token)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error occurred"))
        }
    }


    fun getChatHistory(receiverId: String?, page_no: Int) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiInterface.getChatHistory(token,receiverId,page_no)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error occurred"))
        }
    }

    fun getChatUser(page_no: Int) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiInterface.getChatUser(token,page_no)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error occurred"))
        }
    }

    fun getMusicList() = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiInterface.getMusicList(token!!)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error occurred"))
        }
    }

    fun getFavMusicList() = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiInterface.getFavMusicList(token!!)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error occurred"))
        }
    }
}