package com.example.workplacetrackingapp.fragments.note

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.workplacetrackingapp.MainActivity
import com.example.workplacetrackingapp.R
import com.example.workplacetrackingapp.adapter.NoteAdapter
import com.example.workplacetrackingapp.fragments.dialog.AddNoteFragment
import com.example.workplacetrackingapp.model.Notes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_note.*

class NoteFragment : Fragment(R.layout.fragment_note) {

    lateinit var viewModel : NoteViewModel

    lateinit var mAdapter: NoteAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as MainActivity).noteViewModel
        setupAdapter()

        viewModel.readUser()
        viewModel.readNotes()

        extendedFab.setOnClickListener {
            AddNoteFragment().show(this@NoteFragment.childFragmentManager,"NOTEFRAGMENT")
        }

        mAdapter.setOnItemClickListener {note->
            if (note.acceptingId.isNullOrEmpty()){
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Note")
                    .setMessage("Are you sure to take the note?")
                    .setNeutralButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setPositiveButton("Accept"){ dialog,_->
                        viewModel.currentUser.observe(viewLifecycleOwner,{user->
                            viewModel.takeNote(note,user)
                        }).also {
                            viewModel.readNotes()
                        }
                        dialog.dismiss()
                    }
                    .show()
            }else{
                Toast.makeText(requireContext().applicationContext,"The order has already been taken.",Toast.LENGTH_SHORT).show()
            }

        }

        viewModel.noteLiveData.observe(viewLifecycleOwner,{
            mAdapter.differ.submitList(it)
            rvNotes.scrollToPosition(it.size -1 )
        })
    }

    private fun setupAdapter() {
        mAdapter = NoteAdapter()
        rvNotes.apply {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }
}