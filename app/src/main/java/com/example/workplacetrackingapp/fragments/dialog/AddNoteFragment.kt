package com.example.workplacetrackingapp.fragments.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.workplacetrackingapp.MainActivity
import com.example.workplacetrackingapp.R
import com.example.workplacetrackingapp.fragments.note.NoteViewModel
import com.example.workplacetrackingapp.model.User
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.dialog_add_note.*

class AddNoteFragment : DialogFragment() {

    lateinit var viewModel : NoteViewModel

    lateinit var userInfo: User

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewModel = (activity as MainActivity).noteViewModel

        viewModel.readUser()

        return inflater.inflate(R.layout.dialog_add_note, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.userLiveData.observe(viewLifecycleOwner,{
            userInfo = it
        })

        btnSendNote.setOnClickListener {
            viewModel.writeNote(userInfo,etTitle.text.toString(),etNoteArea.text.toString())
            dialog?.dismiss()
        }
    }


}