package com.example.workplacetrackingapp.fragments.home

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.workplacetrackingapp.data.repository.UserRepository
import com.example.workplacetrackingapp.fragments.note.NoteListener
import com.example.workplacetrackingapp.model.NotificationData
import com.example.workplacetrackingapp.model.PushNotification
import com.example.workplacetrackingapp.model.User
import com.example.workplacetrackingapp.model.UserWorkInformation
import com.example.workplacetrackingapp.retrofit.RetrofitInstance
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class HomeViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    lateinit var workerLiveData: MutableLiveData<List<UserWorkInformation>>

    lateinit var userLiveData : MutableLiveData<User>

    private val homeListener: HomeListener? = null

    private val disposables = CompositeDisposable()

    fun readWorkerInfo() {
        workerLiveData = MutableLiveData()
        workerLiveData = userRepository.readWorkerInfo()
    }

    fun readUser(){
        userLiveData = MutableLiveData()
        userLiveData = userRepository.readUserInfo()
    }

    fun sendReportNotification(
        userWorkInformation: UserWorkInformation,
        title: String,
        message: String
    ) {
        homeListener?.onLoading()
        userRepository.sendNotification(title,message)
        var userWorkInfo = userWorkInformation
        userWorkInfo.userReported = true
        val disposable = userRepository.writeWorkInfo(userWorkInfo)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                homeListener?.onSuccess("User reported")
            }, {
                homeListener?.onError("User is not reported.")
            })
        disposables.add(disposable)

    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}