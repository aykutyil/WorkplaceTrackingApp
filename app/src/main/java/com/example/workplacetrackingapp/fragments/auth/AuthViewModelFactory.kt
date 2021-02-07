package com.example.workplacetrackingapp.fragments.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.workplacetrackingapp.data.repository.UserRepository

class AuthViewModelFactory(
    val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AuthViewModel(userRepository) as T
    }

}