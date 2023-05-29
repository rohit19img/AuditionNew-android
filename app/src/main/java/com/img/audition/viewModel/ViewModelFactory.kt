package com.img.audition.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.img.audition.network.ApiInterface
import java.lang.IllegalArgumentException

class ViewModelFactory(private val token:String?,private var apiService: ApiInterface) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(MainViewModel::class.java)){
            return MainViewModel(token,apiService) as T
        }
        throw IllegalArgumentException("Unknown class name")
    }
}