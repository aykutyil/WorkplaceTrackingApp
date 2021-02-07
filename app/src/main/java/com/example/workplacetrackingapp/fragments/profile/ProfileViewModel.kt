package com.example.workplacetrackingapp.fragments.profile

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workplacetrackingapp.data.repository.UserRepository
import com.example.workplacetrackingapp.model.User
import com.example.workplacetrackingapp.model.UserWorkInformation
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class ProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    var userInfo = MutableLiveData<User>()

    var workInfo = MutableLiveData<UserWorkInformation>()

    var profileListener: ProfileListener? = null

    private val disposables = CompositeDisposable()

    private lateinit var tokenUserInfo : User

    val user by lazy {
        userRepository.currentUser()
    }

    fun logOut() {
        userRepository.logOut()
    }

    fun readUser() {
        userInfo =  userRepository.readUserInfo()
    }

    fun readWorkInfo(){
        workInfo = userRepository.readUserWorkInfo()
    }

     fun writeUser(userInfo : User) {
        userInfo.token = ""

        val disposable = userRepository.writeUserInformation(userInfo)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                userRepository.logOut()
                profileListener?.onLogOut("Exit access")
            }, {
                profileListener?.onError(it.message.toString())
            })
        disposables.add(disposable)
    }

    fun compressAndUploadImage(contentResolver: ContentResolver, data: Uri) =
        viewModelScope.launch {

            var bitMap: Bitmap
            bitMap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(contentResolver, data)
            } else {
                val source = ImageDecoder.createSource(contentResolver, data)
                ImageDecoder.decodeBitmap(source)
            }

            var stream = ByteArrayOutputStream()
            var imageByteArray: ByteArray

            bitMap.compress(Bitmap.CompressFormat.JPEG, 100 / 90, stream)

            imageByteArray = stream.toByteArray()

            val disposable = userRepository.uploadPPImage(imageByteArray)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    profileListener?.onSuccess("The picture has been uploaded successfully.\n ")
                }, {
                    profileListener?.onError(it.message.toString())
                })
            disposables.add(disposable)


        }


    fun updateUserData(changedUserInfo: User, currentUserInfo: User) {
        if (currentUserInfo.userName != changedUserInfo.userName ||
            currentUserInfo.userSurname != changedUserInfo.userSurname
            || currentUserInfo.userMail != changedUserInfo.userMail
        ) {
            if (currentUserInfo.userMail != changedUserInfo.userMail) {
                val disposable = userRepository.updateUserEmail(changedUserInfo.userMail!!)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({
                        val disposable = userRepository.sendVerificationEmail()
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe({
                                currentUserInfo.userName = changedUserInfo.userName
                                currentUserInfo.userSurname = changedUserInfo.userSurname
                                currentUserInfo.userMail = changedUserInfo.userMail
                                val disposable =
                                    userRepository.writeUserInformation(currentUserInfo)
                                        .observeOn(Schedulers.io())
                                        .subscribeOn(AndroidSchedulers.mainThread())
                                        .subscribe({
                                            userRepository.logOut()
                                        }, {
                                            profileListener?.onError(it.message.toString())
                                        }).also {
                                            profileListener?.onLogOut("Update Email And Information")
                                        }
                                disposables.add(disposable)
                            }, {
                                profileListener?.onError(it.message.toString())
                            })
                        disposables.add(disposable)

                    }, {
                        profileListener?.onError("Cannot update email")
                    })
                disposables.add(disposable)
            } else {
                currentUserInfo.userName = changedUserInfo.userName
                currentUserInfo.userSurname = changedUserInfo.userSurname
                val disposable = userRepository.writeUserInformation(currentUserInfo)
                    .observeOn(Schedulers.io())
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                    }.also {
                        profileListener?.onSuccess("Update your information.")
                    }, {
                        profileListener?.onError(it.message.toString())
                    })
                disposables.add(disposable)
            }

        } else {
            profileListener?.onError("Noting changed")
        }
    }

    fun updateUserPassword(newPassword: String) {
        val disposable = userRepository.updateUserPassword(newPassword)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                profileListener?.onLogOut("Password successfully changed ")
            }, {
                profileListener?.onError(it.message.toString())
            })
        disposables.add(disposable)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}