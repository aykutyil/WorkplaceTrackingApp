package com.example.workplacetrackingapp.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
class User {
    var userId: String? = null
    var userName: String? = null
    var userMail: String? = null
    var userProfilePhoto: String? = null
    var userSurname: String? = null
    var statusWork: Boolean = false
    var token: String? = null

    constructor(
        userId: String?,
        userName: String?,
        userMail: String?,
        userProfilePhoto: String?,
        userSurname: String?,
        statusWork: Boolean,
        token:String?
    ) {
        this.userId = userId
        this.userName = userName
        this.userSurname = userSurname
        this.userProfilePhoto = userProfilePhoto
        this.userMail = userMail
        this.statusWork = statusWork
        this.token = token
    }

    constructor()
}
