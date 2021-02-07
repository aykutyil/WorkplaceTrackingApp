package com.example.workplacetrackingapp.fragments.home

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.workplacetrackingapp.data.repository.UserRepository

class HomeViewModelFactory(
    val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return HomeViewModel(userRepository) as T
    }

}