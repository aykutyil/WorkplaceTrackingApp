package com.example.workplacetrackingapp.fragments.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.example.workplacetrackingapp.MainActivity
import com.example.workplacetrackingapp.R
import com.example.workplacetrackingapp.fragments.auth.AuthViewModel
import com.example.workplacetrackingapp.model.User
import kotlinx.android.synthetic.main.google_login_layout.*

class GoogleLogin : DialogFragment() {
    lateinit var viewModel:AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.google_login_layout,container,false)

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as MainActivity).viewModel

        googleBtn.setOnClickListener {
            if (googleName.text.toString().isNotEmpty() && googleSurname.text.toString().isNotEmpty()){
                findNavController().navigate(R.id.homeFragment)
                var user = User()
                user.userName = googleName.text.toString()
                user.userSurname = googleSurname.text.toString()
                viewModel.writeUserInfoForGoogle(user)
                (activity as MainActivity).initFCM()
                dialog?.dismiss()
            }
        }

    }
}