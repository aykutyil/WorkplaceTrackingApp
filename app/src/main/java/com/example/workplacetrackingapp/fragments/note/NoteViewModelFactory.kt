package com.example.workplacetrackingapp.fragments.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.workplacetrackingapp.data.repository.UserRepository

class NoteViewModelFactory(
    private val userRepository: UserRepository
):ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return NoteViewModel(userRepository) as T
    }
}