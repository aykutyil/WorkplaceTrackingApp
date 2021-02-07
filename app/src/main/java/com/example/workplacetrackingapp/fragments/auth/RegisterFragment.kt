package com.example.workplacetrackingapp.fragments.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.workplacetrackingapp.MainActivity
import com.example.workplacetrackingapp.R
import com.example.workplacetrackingapp.databinding.FragmentProfilBinding
import com.example.workplacetrackingapp.databinding.FragmentRegisterBinding
import kotlinx.android.synthetic.main.fragment_register.*

class RegisterFragment : Fragment(),AuthListener {

    private lateinit var viewModel:AuthViewModel

    private lateinit var binding:FragmentRegisterBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_register,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as MainActivity).viewModel
        binding.viewModel = viewModel

        viewModel.authListener = this
    }

    override fun onLoading() {
        binding.pbLoading.show()
    }

    override fun onSuccess() {
        binding.pbLoading.visibility = View.GONE
        val action = RegisterFragmentDirections.actionRegisterFragmentToLoginFragment()
        Navigation.findNavController(requireView()).navigate(action)
    }

    override fun onGoogleLogin() {
        //empty
    }

    override fun onError(message: String) {
        binding.pbLoading.hide()
        Toast.makeText(context?.applicationContext,message,Toast.LENGTH_LONG).show()
    }
}