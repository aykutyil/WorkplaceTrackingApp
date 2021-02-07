package com.example.workplacetrackingapp.fragments.home

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.workplacetrackingapp.MainActivity
import com.example.workplacetrackingapp.R
import com.example.workplacetrackingapp.adapter.UserAdapter
import com.example.workplacetrackingapp.model.User
import com.example.workplacetrackingapp.model.UserWorkInformation
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.fragment_home.*


class HomeFragment : Fragment(R.layout.fragment_home), SwipeRefreshLayout.OnRefreshListener , HomeListener {

    private lateinit var viewModel: HomeViewModel

    lateinit var mAdapter: UserAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as MainActivity).homeViewModel
        viewModel.readWorkerInfo()
        setupAdapter()
        swipeLayout.setOnRefreshListener(this)
        pbLoading.show()

        homeConstraint.setOnClickListener {
            fabCheck()
        }

        swipeLayout.setOnClickListener {
            fabCheck()
        }

        rvWorkInfo.setOnClickListener {
            fabCheck()
        }
        viewModel.workerLiveData.observe(viewLifecycleOwner, { list ->
            mAdapter.submitList(list.toList())
            rvWorkInfo.scrollToPosition(list.size -1 )
            pbLoading.hide()
        })


        mAdapter.setOnItemClickListener {userInfo->
            viewModel.readUser()
            viewModel.readWorkerInfo()
            viewModel.userLiveData.observe(viewLifecycleOwner,{liveUser->
                    sendReport(userInfo,liveUser)
            })

            fabCheck()

        }

    }

    private fun setupAdapter() {
        (activity as MainActivity).initFCM()
        mAdapter = UserAdapter()
        rvWorkInfo.apply {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(activity)

        }
    }

    private fun sendReport(userWorkInformation:UserWorkInformation, user:User){
        if (userWorkInformation.userWorkStatus == true ){
            if (user.statusWork){
                if (user.userName != userWorkInformation.userName){
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Report")
                        .setMessage("${userWorkInformation.userName + " " +userWorkInformation.userSurname} is not here?")
                        .setNeutralButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .setPositiveButton("Accept") { dialog, _ ->
                            viewModel.sendReportNotification(
                                userWorkInformation,
                                "Report",
                                userWorkInformation.userName.toString() + " " + userWorkInformation.userSurname.toString() +
                                        " " + "is a liar."
                            )
                            dialog.dismiss()
                        }
                        .show()
                }
            }else{
                Toast.makeText(requireContext(),"You are not in workplace",Toast.LENGTH_LONG).show()
            }

        }else{
            Toast.makeText(requireContext(),"${userWorkInformation.userName} is not here anyway.",Toast.LENGTH_LONG).show()
        }
    }

    override fun onRefresh() {
        setupAdapter()
        viewModel.readWorkerInfo()
        viewModel.workerLiveData.observe(viewLifecycleOwner, { list ->
            mAdapter.submitList(list.toList())
        })
        swipeLayout.isRefreshing = false
    }

    fun fabCheck(){
        var clicked = (activity as MainActivity).clicked
        if (clicked)
            (activity as MainActivity).onFabClicked()
    }

    override fun onLoading() {
        pbLoading.show()
    }

    override fun onSuccess(message: String) {
        Toast.makeText(requireContext(),message,Toast.LENGTH_SHORT).show()
        pbLoading.hide()
    }

    override fun onError(message: String) {
        Toast.makeText(requireContext(),message,Toast.LENGTH_SHORT).show()
        pbLoading.hide()
    }
}