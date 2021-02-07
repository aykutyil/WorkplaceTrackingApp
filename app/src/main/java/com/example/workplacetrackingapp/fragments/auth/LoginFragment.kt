package com.example.workplacetrackingapp.fragments.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.workplacetrackingapp.MainActivity
import com.example.workplacetrackingapp.R
import com.example.workplacetrackingapp.databinding.FragmentLoginBinding
import com.example.workplacetrackingapp.fragments.dialog.GoogleLogin
import com.example.workplacetrackingapp.model.User
import com.example.workplacetrackingapp.util.Constant.Companion.RC_SIGN_IN
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.fragment_login.*

class LoginFragment : Fragment(), AuthListener {

    private lateinit var binding:FragmentLoginBinding

    private lateinit var viewModel: AuthViewModel

    private lateinit var googleSignInClient : GoogleSignInClient

    private lateinit var  userInfo : User

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_login,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as MainActivity).viewModel

        binding.viewModel = viewModel

        viewModel.authListener = this



        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient((activity as MainActivity),gso)

        btnSignInGoogle.setOnClickListener {
            signIn()
        }

    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val exception = task.exception
            if (task.isSuccessful){
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    Log.d("LoginFragment", "firebaseAuthWithGoogle:" + account.id)
                    viewModel.firebaseAuthWithGoogle(account.idToken!!)

                } catch (e: ApiException) {
                    Log.w("LoginFragment", "Google sign in failed", e)
                }
            }else{
                Log.w("LoginFragment", exception.toString())
            }

        }
    }


    override fun onLoading() {
        binding.pbLogin.show()
    }

    override fun onSuccess() {
        binding.pbLogin.visibility = View.GONE
        findNavController().navigate(R.id.homeFragment)
    }

    override fun onGoogleLogin() {
        userInfo = User()
        viewModel.readUserInfoForGoogleLogin()

        viewModel.userInfo.observe(viewLifecycleOwner,{
            userInfo = it
            if (userInfo.userName.isNullOrEmpty()&&userInfo.userSurname.isNullOrEmpty()){
                GoogleLogin().show(this@LoginFragment.childFragmentManager,"LOGINFRAGMENT")
            }else{
                (activity as MainActivity).initFCM()
                findNavController().navigate(R.id.action_loginFragment_to_homeFragment,
                    null,NavOptions.Builder().setPopUpTo(R.id.loginFragment,true).build())
            }
        })
        binding.pbLogin.hide()

    }

    override fun onError(message: String) {
        Toast.makeText(context?.applicationContext,message,Toast.LENGTH_LONG).show()
        binding.pbLogin.hide()
    }


}


