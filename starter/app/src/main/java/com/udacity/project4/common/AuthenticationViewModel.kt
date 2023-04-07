package com.udacity.project4.common

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.utils.FirebaseUserLiveData

class AuthenticationViewModel(app: Application) : BaseViewModel(app) {

    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED, INVALID_AUTHENTICATION
    }

    private val _signInSuccess = MutableLiveData<Boolean>()
    val signInSuccess: LiveData<Boolean>
        get() = _signInSuccess

    fun signInFailed() {
        _signInSuccess.value = false
    }

    val authenticationState = FirebaseUserLiveData().map { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        } else if (signInSuccess.value == false) {
            AuthenticationState.INVALID_AUTHENTICATION
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }
}