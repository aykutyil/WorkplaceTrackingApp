package com.example.workplacetrackingapp.adapter

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.workplacetrackingapp.R
import com.example.workplacetrackingapp.model.Notes
import com.example.workplacetrackingapp.model.UserWorkInformation
import kotlinx.android.synthetic.main.fragment_note.view.*
import kotlinx.android.synthetic.main.item_note_preview.view.*

class NoteAdapter: RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    inner class NoteViewHolder(itemView: View):RecyclerView.ViewHolder(itemView)

    private val differCallback = object : DiffUtil.ItemCallback<Notes>(){
        override fun areItemsTheSame(oldItem: Notes, newItem: Notes): Boolean {
            return oldItem.noteId == newItem.noteId
        }

        override fun areContentsTheSame(oldItem: Notes, newItem: Notes): Boolean {
            return  newItem.acceptingName == null
        }

        override fun getChangePayload(oldItem: Notes, newItem: Notes): Any? {
            val diff = Bundle()
            if (oldItem.acceptingId == newItem.acceptingId){
                diff.putString("acceptingName",newItem.acceptingName.toString())
                diff.putString("acceptingSurname",newItem.acceptingSurname.toString())
                return diff
            }else
                return super.getChangePayload(oldItem, newItem)
        }

    }

    val differ = AsyncListDiffer(this,differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        return NoteViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_note_preview,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: NoteViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if(payloads.isEmpty()){
            super.onBindViewHolder(holder, position, payloads)
        }else{
            val set = payloads[0] as Bundle
            for (key in set.keySet()){
                if (key == "acceptingName"){
                    if (set.get("acceptingName").toString().isNotEmpty()){
                        holder.itemView.setBackgroundColor(Color.GREEN)
                        holder.itemView.tvAcceptingTitle.visibility = View.VISIBLE
                        holder.itemView.tvAcceptedName.visibility = View.VISIBLE
                        holder.itemView.tvAcceptedName.text =
                            set.get("acceptingName").toString() + " " + set.get("acceptingSurname").toString()
                        holder.itemView.setOnClickListener {
                            onClickListener?.let {

                            }
                        }
                    }else{

                    }
                }
            }
            set.clear()
        }
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val currentNote = differ.currentList[position]
        holder.itemView.apply {
            tvNoteWriterName.text = currentNote.name+ " " + currentNote.surname
            tvTitleMessage.text = currentNote.title
            tvMessage.text = currentNote.message
            if (currentNote.acceptingId.isNullOrEmpty() && currentNote.acceptingName.isNullOrEmpty()){
                tvAcceptingTitle.visibility = View.INVISIBLE
                tvAcceptedName.visibility = View.INVISIBLE
                holder.itemView.setBackgroundColor(Color.GRAY)
            }else{
                holder.itemView.setBackgroundColor(Color.GREEN)
                tvAcceptingTitle.visibility = View.VISIBLE
                tvAcceptedName.text = currentNote.acceptingName.toString() + " " + currentNote.acceptingSurname.toString()
            }

            setOnClickListener {
                onClickListener?.let {
                    it(currentNote)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private var onClickListener :((Notes)->Unit)? = null

    fun setOnItemClickListener(listener : (Notes)->Unit){
        onClickListener = listener
    }
}