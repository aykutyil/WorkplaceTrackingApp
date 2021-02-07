package com.example.workplacetrackingapp.data.firebase

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.workplacetrackingapp.model.*
import com.example.workplacetrackingapp.retrofit.RetrofitInstance
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import io.reactivex.Completable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class FirebaseSource {

    private val firebaseAuth :FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }


    private val firebaseDatabase: DatabaseReference by lazy {
        Firebase.database.reference
    }

    private val firebaseStorage: StorageReference by lazy {
        FirebaseStorage.getInstance().reference
    }

    fun firebaseAuthWithGoogle(idToken: String) = Completable.create { emitter->
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (!emitter.isDisposed){
                    if (task.isSuccessful) {
                        emitter.onComplete()
                    } else {
                        task.exception?.let { exception -> emitter.onError(exception) }
                    }
                }


            }
    }

    fun writeUserForGoogle(user: User) = Completable.create { emitter->
        firebaseAuth.currentUser?.uid?.let {
            firebaseDatabase.child("users")
                .child(it)
                .setValue(user)
                .addOnCompleteListener {task->
                    if (!emitter.isDisposed){
                        if (task.isSuccessful){
                            emitter.onComplete()
                        }else
                            task.exception?.let { exception -> emitter.onError(exception) }
                    }
                }
        }
    }

    fun login(email: String, password: String) = Completable.create { emitter ->
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { autResult ->
                if (!emitter.isDisposed) {
                    if (autResult.isSuccessful)
                        emitter.onComplete()
                    else
                        autResult.exception?.let { exception -> emitter.onError(exception) }
                }
            }
    }

    fun register(email: String, password: String) = Completable.create { emitter ->
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { autResult ->
                if (!emitter.isDisposed) {
                    if (autResult.isSuccessful){
                        emitter.onComplete()
                    }

                    else
                        autResult.exception?.let { exception -> emitter.onError(exception) }
                }
            }
    }

    fun logOut() = firebaseAuth.signOut()

    fun currentUser() = firebaseAuth.currentUser

    fun sendVerificationEmail() = Completable.create { emitter ->
        firebaseAuth.currentUser?.sendEmailVerification()?.addOnCompleteListener {
            if (!emitter.isDisposed) {
                if (it.isSuccessful) {
                    emitter.onComplete()
                } else
                    it.exception?.let { exception -> emitter.onError(exception) }
            }
        }
    }

    fun writeUserInformation(user: User) = Completable.create { emitter ->
        firebaseAuth.uid?.let { userUid ->
            firebaseDatabase.child("users")
                .child(userUid)
                .setValue(user)
                .addOnCompleteListener {
                    if (!emitter.isDisposed) {
                        if (it.isSuccessful)
                            emitter.onComplete()
                        else
                            it.exception?.let { exception -> emitter.onError(exception) }
                    }
                }
        }
    }

    fun readUser(): MutableLiveData<User> {
        val liveData = MutableLiveData<User>()
        firebaseDatabase.child("users")
            .orderByKey()
            .equalTo(firebaseAuth.currentUser?.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var userInfo = User()
                    for (i in snapshot.children) {
                         userInfo = i.getValue(User::class.java)!!
                    }
                    liveData.postValue(userInfo)
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        return liveData

    }

    fun uploadUserProfilePhoto( imageByteArray: ByteArray) = Completable.create { emitter ->

            val photoReference = firebaseStorage.child("image/users")
                .child(firebaseAuth.currentUser?.uid.toString())
                .child("profile_photo")

            photoReference
                .putBytes(imageByteArray)
                .addOnCompleteListener {
                    if (!emitter.isDisposed) {
                        if (it.isSuccessful) {
                            photoReference.downloadUrl.addOnSuccessListener { uri ->
                                firebaseDatabase.child("users")
                                    .child(firebaseAuth.currentUser?.uid.toString())
                                    .child("userProfilePhoto")
                                    .setValue(uri.toString())
                                    .addOnCompleteListener {
                                        firebaseDatabase.child("workinfo")
                                            .child(firebaseAuth.currentUser?.uid.toString())
                                            .child("userPP")
                                            .setValue(uri.toString())
                                        emitter.onComplete()
                                    }
                            }
                        } else {
                            it.exception?.let { exception -> emitter.onError(exception) }
                        }
                    }
                }
        }

    fun updateUserEmail(userMail: String) = Completable.create { emitter ->
        firebaseAuth.currentUser!!.updateEmail(userMail)
            .addOnCompleteListener {
                if (!emitter.isDisposed) {
                    if (it.isSuccessful) {
                        emitter.onComplete()
                    } else
                        it.exception?.let { exception -> emitter.onError(exception) }
                }
            }
    }

    fun updateUserPassword(userNewPassword:String) = Completable.create { emitter->
        firebaseAuth.currentUser!!.updatePassword(userNewPassword)
            .addOnCompleteListener {
                if (!emitter.isDisposed){
                    if (it.isSuccessful){
                        emitter.onComplete()
                    }else{
                        it.exception?.let { exception -> emitter.onError(exception) }
                    }
                }
            }
    }

    //-----------------------------------------------------------------------------

    fun writeWorkInformation(userWorkInfo : UserWorkInformation) = Completable.create { emitter->
            firebaseDatabase.child("workinfo")
                .child(userWorkInfo.userId.toString())
                .setValue(userWorkInfo)
                .addOnCompleteListener {
                    if (!emitter.isDisposed){
                        if (it.isSuccessful)
                            emitter.onComplete()
                        else
                            it.exception?.let { exception -> emitter.onError(exception) }
                    }
                }
    }

    fun readWorkerInfo():MutableLiveData<List<UserWorkInformation>>{
        var liveData = MutableLiveData<List<UserWorkInformation>>()
        firebaseDatabase.child("workinfo")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = ArrayList<UserWorkInformation>()
                    for (i in snapshot.children){
                        i.getValue(UserWorkInformation::class.java)?.let { list.add(it) }
                    }
                    liveData.postValue(list)

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        return liveData
    }

    fun readUserWorkInfo():MutableLiveData<UserWorkInformation>{
        var liveData = MutableLiveData<UserWorkInformation>()
        firebaseDatabase.child("workinfo")
            .orderByKey()
            .equalTo(firebaseAuth.currentUser?.uid)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var worker = UserWorkInformation()
                    for (i in snapshot.children){
                        worker = i.getValue(UserWorkInformation::class.java)!!
                    }
                    liveData.postValue(worker)
                }
                override fun onCancelled(error: DatabaseError) {

                }
            })
        return liveData
    }

    //-------------------------------------------------------------------------------

    fun writeNote(notes: Notes) = Completable.create { emitter->
        val noteId = firebaseDatabase.push().key.toString()
        notes.noteId = noteId
        firebaseDatabase.child("notes")
            .child(noteId)
            .setValue(notes)
            .addOnCompleteListener {
                if (!emitter.isDisposed){
                    if (it.isSuccessful){
                        emitter.onComplete()
                    }else{
                        it.exception?.let { exception -> emitter.onError(exception) }
                    }
                }
            }
    }

    fun updateNote(note:Notes) = Completable.create { emitter->
        firebaseDatabase.child("notes")
            .child(note.noteId.toString())
            .setValue(note)
            .addOnCompleteListener {
                if (!emitter.isDisposed){
                    if (it.isSuccessful)
                        emitter.onComplete()
                    else
                        it.exception?.let { exception -> emitter.onError(exception) }
                }
            }
    }

    fun readNotes() : MutableLiveData<List<Notes>>{
        var liveData = MutableLiveData<List<Notes>>()

        firebaseDatabase.child("notes")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var list = ArrayList<Notes>()
                    for (i in snapshot.children){
                        i.getValue(Notes::class.java)?.let { list.add(it) }
                    }
                    liveData.postValue(list)
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

        return liveData
    }

    fun readUsersAndSendNotification(title:String,message:String){
        firebaseDatabase.child("users")
            .orderByKey()
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    for (i in snapshot.children){
                        var id = i.key

                        if (id.toString() != FirebaseAuth.getInstance().currentUser?.uid.toString()){
                            firebaseDatabase.child("users")
                                .orderByKey()
                                .equalTo(id)
                                .addListenerForSingleValueEvent(object : ValueEventListener{
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        var user = snapshot.children.iterator().next()
                                        var messageToken = user.getValue(User::class.java)?.token
                                        CoroutineScope(Dispatchers.IO).launch {
                                            try {
                                                var data = NotificationData(title,message)
                                                val notification = PushNotification(data,messageToken.toString())
                                                val response = RetrofitInstance.api.postNotification(notification)
                                                if (response.isSuccessful)
                                                    Log.d("Retrofit",response.message().toString())
                                                else
                                                    Log.e("Retrofit", response.errorBody().toString())
                                            }catch (e: Exception){
                                                Log.e("Retrofit",e.message.toString())
                                            }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {

                                    }
                                })
                        }
                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    fun tokenSave(token:String) = Completable.create { emitter->
        firebaseDatabase.child("users")
            .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
            .child("token")
            .setValue(token).addOnCompleteListener {
                if (!emitter.isDisposed){
                    if (it.isSuccessful){
                        emitter.onComplete()
                    }else
                        it.exception?.let { exception -> emitter.onError(exception) }
                }
            }
    }
}