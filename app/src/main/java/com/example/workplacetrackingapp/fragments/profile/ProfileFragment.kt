package com.example.workplacetrackingapp.fragments.profile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.workplacetrackingapp.MainActivity
import com.example.workplacetrackingapp.R
import com.example.workplacetrackingapp.databinding.FragmentProfilBinding
import com.example.workplacetrackingapp.fragments.dialog.PasswordResetFragment
import com.example.workplacetrackingapp.model.User
import com.example.workplacetrackingapp.model.UserWorkInformation
import com.example.workplacetrackingapp.util.Constant.Companion.IMAGE_PICK_CODE
import com.example.workplacetrackingapp.util.Constant.Companion.PERMISSION_CODE
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_profil.*

class ProfileFragment : Fragment(),ProfileListener {

    private lateinit var binding: FragmentProfilBinding

    private lateinit var viewModel: ProfileViewModel

    private lateinit var userInfo:User

    private lateinit var changedUserInfo:User

    lateinit var userWorkInfo : UserWorkInformation


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profil, container, false)
        binding.root.setOnClickListener {
            fabCheck()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        viewModel = (activity as MainActivity).profileViewModel

        binding.viewModel = viewModel

        viewModel.profileListener = this

        viewModel.readUser()

        viewModel.readWorkInfo()

        viewModel.workInfo.observe(viewLifecycleOwner,{
            userWorkInfo = it
        })

        binding.pbLoading.show()
        viewModel.userInfo.observe(viewLifecycleOwner, {
            userInfo = User()
            userInfo = it
            binding.etName.setText(it.userName)
            binding.etEmail.setText(viewModel.user!!.email)
            binding.etSurname.setText(it.userSurname)
            if (it.userProfilePhoto !=""){
                Glide.with(this)
                    .load(it.userProfilePhoto)
                    .into(binding.imgPP)
            }
            binding.pbLoading.hide()
        })

        binding.imgPP.setOnClickListener {
            if (userWorkInfo.checkInTime.isNullOrEmpty() && userWorkInfo.checkOutTime.isNullOrEmpty()){
                Toast.makeText(requireContext(),"Please enter or exit the office to select a picture. ",Toast.LENGTH_SHORT).show()
            }else{
                somethingPermission(it)
                fabCheck()
            }

        }
        binding.btnUpdate.setOnClickListener {
            fabCheck()
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Update")
                .setMessage("Are you sure?")
                .setNeutralButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton("Accept"){ dialog,_->
                    updateUserData()
                    dialog.dismiss()
                }
                .show()
        }

        binding.btnSignOut.setOnClickListener {
            fabCheck()
            viewModel.writeUser(userInfo)
        }


    }

    private fun fabCheck(){
        var clicked = (activity as MainActivity).clicked
        if (clicked)
            (activity as MainActivity).onFabClicked()
    }

    fun updateUserData(){
        if (binding.etName.toString().isEmpty() || binding.etSurname.toString().isEmpty()
            || binding.etEmail.toString().isEmpty()){
            Toast.makeText(context,"Information cannot be left blank. ",Toast.LENGTH_LONG).show()
            return
        }else{
            changedUserInfo = User()
            changedUserInfo.userName = binding.etName.text.toString()
            changedUserInfo.userSurname = binding.etSurname.text.toString()
            changedUserInfo.userMail = binding.etEmail.text.toString()
            viewModel.updateUserData(changedUserInfo,userInfo)
        }
    }

    fun somethingPermission(v: View) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    v.context.applicationContext,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) ==
                PackageManager.PERMISSION_DENIED
            ) {
                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                requestPermissions(permissions, PERMISSION_CODE)

            } else {
                pickImageFromGallery()
            }
        } else {

        }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery()
                } else {
                    Toast.makeText(
                        context, "You must accept the permissions for the profile photo.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            if (data != null) {
                binding.imgPP.setImageURI(data.data)
                this@ProfileFragment.activity?.contentResolver?.let {contentResolver->
                    data.data?.let {uri->
                        viewModel.compressAndUploadImage(
                            contentResolver, uri
                        )
                    }
                }

            }
        }
    }

    override fun onLoading() {

    }

    override fun onSuccess(message: String) {
        Toast.makeText(activity?.applicationContext,message,Toast.LENGTH_LONG).show()
    }

    override fun onError(message: String) {
        Toast.makeText(activity?.applicationContext,message,Toast.LENGTH_LONG).show()
    }

    override fun onLogOut(message: String) {
        Toast.makeText(context?.applicationContext,message,Toast.LENGTH_LONG).show()
        findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.reset_menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.resetPassword -> {
                PasswordResetFragment().show(this@ProfileFragment.childFragmentManager,"SHOWRESETFRAGMENT")
            }
        }

        return super.onOptionsItemSelected(item)
    }


}