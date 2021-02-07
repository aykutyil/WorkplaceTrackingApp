package com.example.workplacetrackingapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.workplacetrackingapp.data.firebase.FirebaseSource
import com.example.workplacetrackingapp.data.repository.UserRepository
import com.example.workplacetrackingapp.fragments.auth.AuthViewModel
import com.example.workplacetrackingapp.fragments.auth.AuthViewModelFactory
import com.example.workplacetrackingapp.fragments.home.HomeViewModel
import com.example.workplacetrackingapp.fragments.home.HomeViewModelFactory
import com.example.workplacetrackingapp.fragments.note.NoteViewModel
import com.example.workplacetrackingapp.fragments.note.NoteViewModelFactory
import com.example.workplacetrackingapp.fragments.profile.ProfileViewModel
import com.example.workplacetrackingapp.fragments.profile.ProfileViewModelFactory
import com.example.workplacetrackingapp.model.User
import com.example.workplacetrackingapp.model.UserWorkInformation
import com.example.workplacetrackingapp.util.Constant.Companion.PERMISSION_CODE
import com.google.firebase.messaging.FirebaseMessaging
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    lateinit var viewModel: AuthViewModel
    lateinit var profileViewModel: ProfileViewModel
    lateinit var homeViewModel: HomeViewModel
    lateinit var noteViewModel : NoteViewModel

    var clicked = false

    private var requestBoolean = false

    private val rotateForward: Animation by lazy {
        AnimationUtils.loadAnimation(
            applicationContext,
            R.anim.rotate_forward
        )
    }
    private val rotateBackward: Animation by lazy {
        AnimationUtils.loadAnimation(
            applicationContext,
            R.anim.rotate_backward
        )
    }

    var userLiveData = MutableLiveData<User>()

    private val disposables = CompositeDisposable()
    private lateinit var firebaseSource : FirebaseSource

    private lateinit var currentTime: String

    private var currentStatus = false

    private val centerLatitude = 39.8903814
    private val centerLongitude = 32.812001
    private var isWithin1km = false

    private lateinit var locationManager: LocationManager

    private lateinit var locationListener: LocationListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bottomNavigationView.background = null
        bottomNavigationView.menu.getItem(1).isEnabled = false
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        firebaseSource = FirebaseSource()

        FirebaseMessaging.getInstance().subscribeToTopic("notes")

        val autRepository = UserRepository(firebaseSource)
        val viewModelFactory = AuthViewModelFactory(autRepository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(AuthViewModel::class.java)

        val profileViewModelFactory = ProfileViewModelFactory(autRepository)
        profileViewModel =
            ViewModelProvider(this, profileViewModelFactory).get(ProfileViewModel::class.java)

        val homeViewModelFactory = HomeViewModelFactory(autRepository)
        homeViewModel = ViewModelProvider(this, homeViewModelFactory).get(HomeViewModel::class.java)

        val noteViewModelFactory = NoteViewModelFactory(autRepository)
        noteViewModel = ViewModelProvider(this,noteViewModelFactory).get(NoteViewModel::class.java)

        bottomNavigationView.setupWithNavController(navHostFragment.findNavController())

        navHostFragment.findNavController()
            .addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.profileFragment, R.id.homeFragment -> {
                        bottomNavigationView.visibility = View.VISIBLE
                        bar.visibility = View.VISIBLE
                        fabMain.show()
                        fabMain.isClickable = true
                        fabMain.isFocusable = true
                        if (clicked) {
                            onFabClicked()
                        }
                    }
                    R.id.loginFragment, R.id.registerFragment, R.id.noteFragment -> {
                        fabMain.hide()
                        fabMain.isClickable = false
                        fabMain.isFocusable = false
                        bottomNavigationView.visibility = View.GONE
                        bar.visibility = View.GONE
                    }
                }
            }

        fabMain.setOnClickListener {
            onFabClicked()
            checkPermission()
        }

        fabAccess.setOnClickListener {
            userLiveData = autRepository.readUserInfo()
            currentStatus = true
            writeUserWorkInformation(currentStatus)
        }

        fabNote.setOnClickListener {
            onFabClicked()
            val navController = findNavController(R.id.navHostFragment)
            navController.navigate(R.id.noteFragment)
        }

        fabExit.setOnClickListener {
            userLiveData = autRepository.readUserInfo()
            currentStatus = false
            isWithin1km = true
            writeUserWorkInformation(currentStatus)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channelId = "my_channel"
            val channelName = "approx"
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(
                NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_LOW)
            )
        }

    }


     fun initFCM() {
         FirebaseMessaging.getInstance().token.addOnCompleteListener {
             val disposable = firebaseSource.tokenSave(it.result.toString())
                 .subscribeOn(Schedulers.io())
                 .observeOn(AndroidSchedulers.mainThread())
                 .subscribe({},{})
             disposables.add(disposable)
         }
    }


    private fun writeUserWorkInformation(currentStatus: Boolean) {
        if (requestBoolean && isWithin1km) {
            pbLoading.show()
            getTime()
            CoroutineScope(Dispatchers.IO).launch {

                val userWorkInformation = UserWorkInformation()
                userLiveData.observe(this@MainActivity, {
                    userWorkInformation.userName = it.userName
                    userWorkInformation.userId = it.userId
                    userWorkInformation.userPP = it.userProfilePhoto
                    userWorkInformation.userWorkStatus = it.statusWork
                    userWorkInformation.userSurname = it.userSurname
                    userWorkInformation.userReported = false

                    if (userWorkInformation.userWorkStatus!! != currentStatus) {
                        if (!userWorkInformation.userWorkStatus!!)
                            userWorkInformation.checkInTime = currentTime
                        else
                            userWorkInformation.checkOutTime = currentTime
                        userWorkInformation.userWorkStatus = !it.statusWork

                        val disposable = firebaseSource.writeWorkInformation(userWorkInformation)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                val user = it
                                user.statusWork = !it.statusWork

                                val disposable = firebaseSource.writeUserInformation(user)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({
                                        Toast.makeText(
                                            this@MainActivity,
                                            "The information has been saved.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        pbLoading.hide()
                                        onFabClicked()
                                    }, { throwable ->
                                        Toast.makeText(
                                            this@MainActivity,
                                            throwable.message.toString(),
                                            Toast.LENGTH_LONG
                                        ).show()
                                        pbLoading.hide()
                                        onFabClicked()
                                    })
                                disposables.add(disposable)

                            }, { throwable ->
                                Toast.makeText(this@MainActivity, throwable.message.toString(), Toast.LENGTH_LONG)
                                    .show()
                                pbLoading.hide()
                                onFabClicked()
                            })
                        disposables.add(disposable)
                    } else {
                        Toast.makeText(this@MainActivity, "You are already at work right now.", Toast.LENGTH_SHORT)
                            .show()
                        pbLoading.hide()
                        onFabClicked()
                    }
                })

            }
        }else{
            Toast.makeText(this,"You are not close to your workplace right now",Toast.LENGTH_LONG).show()
        }
    }

    override fun onBackPressed() {
        when (Navigation.findNavController(this, R.id.navHostFragment)
            .currentDestination?.id) {
            R.id.homeFragment -> {
                moveTaskToBack(true)
                exitProcess(-1)
            }
            R.id.loginFragment -> {
                moveTaskToBack(true)
                exitProcess(-1)
            }
            R.id.registerFragment ->{
                navHostFragment.findNavController().navigate(R.id.loginFragment)
            }
            else -> navHostFragment.findNavController().navigate(R.id.homeFragment)
        }

    }

    override fun onResume() {
        super.onResume()
        bottomNavigationView.menu.getItem(0).isChecked = true
    }

    override fun onStart() {
        super.onStart()
        if (firebaseSource.currentUser() != null){
            findNavController(R.id.navHostFragment).navigate(R.id.homeFragment)
            initFCM()
            if (clicked) {
                onFabClicked()
            }
        }else{
            findNavController(R.id.navHostFragment).navigate(R.id.loginFragment)
        }

    }

    override fun onPause() {
        super.onPause()
        disposables.clear()
    }

    fun onFabClicked() {
        setVisibility(clicked)
        setAnimation(clicked)
        setClickable(clicked)
        clicked = !clicked
    }

    private fun setAnimation(clicked: Boolean) {
        if (!clicked) {
            fabMain.startAnimation(rotateForward)
        } else {
            fabMain.startAnimation(rotateBackward)
        }
    }

    private fun setVisibility(clicked: Boolean) {
        if (!clicked) {
            fabAccess.show()
            fabNote.show()
            fabExit.show()
        } else {
            fabAccess.hide()
            fabNote.hide()
            fabExit.hide()
        }
    }

    private fun setClickable(clicked: Boolean) {
        if (!clicked) {
            fabAccess.isClickable = true
            fabNote.isClickable = true
            fabExit.isClickable = true
        } else {
            fabAccess.isClickable = false
            fabNote.isClickable = false
            fabExit.isClickable = false
        }
    }

    private fun getTime() {
        val mCalender = Calendar.getInstance()
        currentTime =
            mCalender[Calendar.HOUR_OF_DAY].toString() + ":" +
                    mCalender[Calendar.MINUTE] + ":" +
                    mCalender[Calendar.SECOND]
    }

    private fun checkPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_DENIED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                PERMISSION_CODE
            )
            requestBoolean = false
        } else {
            requestBoolean = true
            locationListener = object : LocationListener{
                override fun onLocationChanged(location: Location) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val results = FloatArray(1)
                        Location.distanceBetween(
                            centerLatitude,
                            centerLongitude,
                            location.latitude,
                            location.longitude,
                            results
                        )
                        val distanceInMeters = results[0]
                        isWithin1km = distanceInMeters < 1000
                    }
                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

                }

                override fun onProviderEnabled(provider: String) {
                    super.onProviderEnabled(provider)
                }

                override fun onProviderDisabled(provider: String) {
                    super.onProviderDisabled(provider)
                }
            }/* LocationListener { location ->
                val results = FloatArray(1)
                Location.distanceBetween(
                    centerLatitude,
                    centerLongitude,
                    location.latitude,
                    location.longitude,
                    results
                )
                val distanceInMeters = results[0]
                isWithin1km = distanceInMeters < 1000
            }*/
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000,
                10f,
                locationListener
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestBoolean = true
                } else {
                    Toast.makeText(
                        this.applicationContext,
                        "You must accept the permissions for the profile photo.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
        }
    }

}