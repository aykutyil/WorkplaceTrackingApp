package com.example.workplacetrackingapp.data.repository

import com.example.workplacetrackingapp.data.firebase.FirebaseSource
import com.example.workplacetrackingapp.model.Notes
import com.example.workplacetrackingapp.model.User
import com.example.workplacetrackingapp.model.UserWorkInformation

class UserRepository(
    private val firebase: FirebaseSource
) {

    fun login(email: String, password: String) = firebase.login(email, password)

    fun register(email: String, password: String) = firebase.register(email, password)

    fun currentUser() = firebase.currentUser()

    fun logOut() = firebase.logOut()

    fun sendVerificationEmail() = firebase.sendVerificationEmail()

    fun writeUserInformation(user: User) = firebase.writeUserInformation(user)

    fun readUserInfo() = firebase.readUser()

    fun uploadPPImage( imageByteArray: ByteArray) =
        firebase.uploadUserProfilePhoto( imageByteArray)

    fun updateUserEmail(userMail: String) = firebase.updateUserEmail(userMail)

    fun updateUserPassword(newPassword:String) = firebase.updateUserPassword(newPassword)

    fun writeWorkInfo(workInfo : UserWorkInformation) = firebase.writeWorkInformation(workInfo)

    fun readWorkerInfo() = firebase.readWorkerInfo()

    fun readUserWorkInfo() = firebase.readUserWorkInfo()

    fun writeNote(notes:Notes) = firebase.writeNote(notes)

    fun readNotes() = firebase.readNotes()

    fun updateNote(note:Notes) = firebase.updateNote(note)

    fun sendNotification(title:String,message:String) = firebase.readUsersAndSendNotification(title,message)

    fun firebaseAuthWithGoogle(idToken: String) = firebase.firebaseAuthWithGoogle(idToken)

    fun writeUserInfoForGoogle(user: User) = firebase.writeUserForGoogle(user)

}