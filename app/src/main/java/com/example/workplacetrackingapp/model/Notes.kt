package com.example.workplacetrackingapp.model

class Notes {

    var name: String? = null
    var surname: String? = null
    var userId: String? = null
    var title: String? = null
    var message: String? = null
    var noteId: String? = null
    var acceptingId: String? = null
    var acceptingName: String? = null
    var acceptingSurname: String? = null

    constructor(
        name: String,
        surname: String,
        userId: String?,
        title: String,
        message: String,
        noteId: String,
        acceptingId: String,
        acceptingName: String,
        acceptingSurname: String
    ) {
        this.name = name
        this.surname = surname
        this.userId = userId
        this.title = title
        this.message = message
        this.noteId = noteId
        this.acceptingId = acceptingId
        this.acceptingName = acceptingName
        this.acceptingSurname = acceptingSurname
    }

    constructor()
}