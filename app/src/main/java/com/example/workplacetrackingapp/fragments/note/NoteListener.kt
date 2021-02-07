package com.example.workplacetrackingapp.fragments.note

interface NoteListener {
    fun onLoading()
    fun onSuccess(message: String)
    fun onError(message : String)
}