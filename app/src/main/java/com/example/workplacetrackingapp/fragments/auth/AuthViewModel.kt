package com.example.workplacetrackingapp.fragments.auth

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import com.example.workplacetrackingapp.R
import com.example.workplacetrackingapp.data.repository.UserRepository
import com.example.workplacetrackingapp.model.User
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class AuthViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    var email: String? = null
    var password: String? = null
    var repassword: String? = null
    var name: String? = null
    var surName: String? = null

    var authListener: AuthListener? = null

    lateinit var user: User

    private val disposables = CompositeDisposable()

    lateinit var userInfo : MutableLiveData<User>


    fun login() {
        if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
            authListener?.onError("Please enter your email or password.")
            return
        } else {
            authListener?.onLoading()

            userRepository.currentUser()

            email?.let { mEmail ->
                password?.let { mPassword ->
                    val disposable = userRepository.login(mEmail, mPassword)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            if (userRepository.currentUser()?.isEmailVerified == true) {
                                authListener?.onSuccess()
                                clearVariable()
                            } else {
                                authListener?.onError("Please verified your account")
                                userRepository.logOut()
                            }
                        }, {
                            authListener?.onError(it.message.toString())
                        })

                    disposables.add(disposable)
                }
            }
        }
    }

    fun goToRegisterFragment(v: View) {
        v.findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
    }

    fun register() {
        if (email.isNullOrEmpty() || password.isNullOrEmpty() ||
            name.isNullOrEmpty() || surName.isNullOrEmpty() ||
            repassword.isNullOrEmpty()
        ) {
            authListener?.onError("Please enter your information")
            return
        } else {
            authListener?.onLoading()
            if (password == repassword) {
                email?.let { mEmail ->
                    password?.let { mPassword ->
                        val disposable = userRepository.register(mEmail, mPassword)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                sendVerificationEmail()
                            }, {
                                authListener?.onError(it.message.toString())
                            })
                        disposables.add(disposable)
                    }
                }
            } else {
                authListener?.onError("Enter the passwords the same.")
            }

        }
    }


    private fun sendVerificationEmail() {
        if (email.isNullOrEmpty() || password.isNullOrEmpty()
        ) {
            authListener?.onError("Please enter your information")
            return
        } else {
            if (email != null && password != null && repassword != null && name != null && surName != null
                && password == repassword
            ) {
                authListener?.onLoading()

                val disposable = userRepository.login(email!!, password!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        val disposable = userRepository.sendVerificationEmail()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                writeUser()
                            }, {
                                authListener?.onError(it.message.toString())
                            })
                        disposables.add(disposable)

                    }, {})

                disposables.add(disposable)
            }
        }
    }

    private fun writeUser() {
        user = User()
        user.userId = userRepository.currentUser()?.uid
        user.userName = name
        user.userMail = email
        user.userSurname = surName
        user.userProfilePhoto = ""

        val disposable = userRepository.writeUserInformation(user)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                userRepository.logOut()
                authListener?.onSuccess()
                clearVariable()
            }, {
                authListener?.onError(it.message.toString())
            })
        disposables.add(disposable)
    }

    private fun clearVariable() {
        email = ""
        password = ""
        repassword = ""
        name = ""
        surName = ""
    }

    fun readUserInfoForGoogleLogin(){
        userInfo = userRepository.readUserInfo()
    }

    fun firebaseAuthWithGoogle(idToken: String) {
        authListener?.onLoading()
        val disposable =
            userRepository.firebaseAuthWithGoogle(idToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    authListener?.onGoogleLogin()
                }, {
                    authListener?.onError("Google SignIn Error")
                })
        disposables.add(disposable)
    }

    fun writeUserInfoForGoogle(user: User){
        authListener?.onLoading()
        user.userId = userRepository.currentUser()?.uid
        user.userMail = userRepository.currentUser()?.email
        user.userName = userRepository.currentUser()?.displayName
        val disposable = userRepository.writeUserInfoForGoogle(user)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                authListener?.onSuccess()
            },{
                authListener?.onError("Google SignIn Error")
            })
        disposables.add(disposable)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }

}