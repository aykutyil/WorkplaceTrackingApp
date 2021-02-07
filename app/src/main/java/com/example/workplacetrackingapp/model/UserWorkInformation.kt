package com.example.workplacetrackingapp.model

class UserWorkInformation {
    var userId: String? = null
    var userName: String? = null
    var userSurname: String? = null
    var userPP: String? = null
    var userWorkStatus: Boolean? = null
    var checkInTime: String? = null
    var checkOutTime: String? = null
    var userReported:Boolean?=null

    constructor(
        userId: String,
        userName: String,
        userSurname: String,
        userPP: String,
        userWorkState: Boolean,
        checkInTime : String,
        checkOutTime : String,
        userReported : Boolean
    ) {
        this.userId = userId
        this.userName = userName
        this.userSurname = userSurname
        this.userPP = userPP
        this.userWorkStatus = userWorkState
        this.checkInTime = checkInTime
        this.checkOutTime = checkOutTime
        this.userReported = userReported
    }

    constructor()
}