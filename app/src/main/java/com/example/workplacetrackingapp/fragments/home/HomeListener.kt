package com.example.workplacetrackingapp.fragments.home

interface HomeListener {
    fun onLoading()
    fun onSuccess(message: String)
    fun onError(message : String)
}