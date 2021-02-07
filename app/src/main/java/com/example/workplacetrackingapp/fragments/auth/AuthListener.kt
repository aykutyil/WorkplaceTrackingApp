package com.example.workplacetrackingapp.fragments.auth

interface AuthListener {

    fun onLoading()
    fun onSuccess()
    fun onGoogleLogin()
    fun onError(message : String)
}