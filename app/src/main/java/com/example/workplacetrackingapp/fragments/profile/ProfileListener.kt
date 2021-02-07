package com.example.workplacetrackingapp.fragments.profile

interface ProfileListener {
    fun onLoading()
    fun onSuccess(message: String)
    fun onError(message : String)
    fun onLogOut(message: String)
}