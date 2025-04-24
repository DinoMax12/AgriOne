package com.agrione.app.viewmodel

import androidx.lifecycle.LiveData

interface AuthListener {
    fun onStarted()
    fun onSuccess(authRepo: LiveData<String>)
    fun onFailure(message: String)
}
