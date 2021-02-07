package com.example.workplacetrackingapp.fragments.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.workplacetrackingapp.MainActivity
import com.example.workplacetrackingapp.R
import com.example.workplacetrackingapp.fragments.profile.ProfileListener
import com.example.workplacetrackingapp.fragments.profile.ProfileViewModel
import kotlinx.android.synthetic.main.dialog_reset_password.*

class PasswordResetFragment : DialogFragment(),ProfileListener {

    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewModel = (activity as MainActivity).profileViewModel

        if (dialog != null && dialog?.window != null){
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        }
        viewModel.profileListener = this
        return inflater.inflate(R.layout.dialog_reset_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        changePassword.setOnClickListener {
            val password = password.text.toString()
            val rePassword = rePassword.text.toString()
            if (password.isNotEmpty() && rePassword.isNotEmpty()){
                if (password == rePassword){
                    viewModel.updateUserPassword(password)
                }else{
                    Toast.makeText(context?.applicationContext,"Your passwords are not the same.",Toast.LENGTH_LONG).show()
                }

            }else{
                Toast.makeText(context?.applicationContext,"Enter your information.",Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onLoading() {

    }

    override fun onSuccess(message: String) {

    }

    override fun onError(message: String) {
        Toast.makeText(context?.applicationContext,message, Toast.LENGTH_LONG).show()
    }

    override fun onLogOut(message: String) {
        dialog?.dismiss()
        Toast.makeText(context?.applicationContext,message, Toast.LENGTH_LONG).show()
    }
}