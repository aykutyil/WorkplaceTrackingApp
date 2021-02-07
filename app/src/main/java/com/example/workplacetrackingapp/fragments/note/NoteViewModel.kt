package com.example.workplacetrackingapp.fragments.note

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.workplacetrackingapp.data.repository.UserRepository
import com.example.workplacetrackingapp.model.Notes
import com.example.workplacetrackingapp.model.NotificationData
import com.example.workplacetrackingapp.model.PushNotification
import com.example.workplacetrackingapp.model.User
import com.example.workplacetrackingapp.retrofit.RetrofitInstance
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class NoteViewModel(private var userRepository: UserRepository) : ViewModel() {

    private var disposables = CompositeDisposable()

    lateinit var noteLiveData : MutableLiveData<List<Notes>>

    var userLiveData = MutableLiveData<User>()

    private var noteListener: NoteListener? = null

    val currentUser:MutableLiveData<User> by lazy {
        userRepository.readUserInfo()
    }

    fun writeNote(user: User,title : String,message : String) {
        var note = Notes()
        note.userId = user.userId
        note.name = user.userName
        note.surname = user.userSurname
        note.title = title
        note.message = message
        val disposable = userRepository.writeNote(note)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                userRepository.sendNotification(title,message)
            }, {
                noteListener?.onError("There is a problem.")
            })
        disposables.add(disposable)
    }

    fun readUser(){
        userLiveData = userRepository.readUserInfo()
    }

    fun readNotes(){
        noteLiveData = MutableLiveData()
        noteLiveData = userRepository.readNotes()
    }

    fun takeNote(note: Notes, user: User?) {
        note.acceptingId = user?.userId
        note.acceptingName = user?.userName
        note.acceptingSurname = user?.userSurname
        val disposable = userRepository.updateNote(note)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                noteListener?.onSuccess("You have received the order.")
            },{
                noteListener?.onError("There is a problem.")
            })
        disposables.add(disposable)
    }


    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

}