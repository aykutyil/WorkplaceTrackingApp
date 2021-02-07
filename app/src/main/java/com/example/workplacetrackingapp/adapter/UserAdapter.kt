package com.example.workplacetrackingapp.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.workplacetrackingapp.R
import com.example.workplacetrackingapp.model.UserWorkInformation
import kotlinx.android.synthetic.main.item_user_preview.view.*

class UserAdapter : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {}

    private val differCallback = object : DiffUtil.ItemCallback<UserWorkInformation>() {
        override fun areItemsTheSame(
            oldItem: UserWorkInformation,
            newItem: UserWorkInformation
        ): Boolean {
            return oldItem.userId == newItem.userId
        }

        override fun areContentsTheSame(
            oldItem: UserWorkInformation,
            newItem: UserWorkInformation
        ): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun getChangePayload(
            oldItem: UserWorkInformation,
            newItem: UserWorkInformation
        ): Any? {

            val diff = Bundle()
            diff.putString("name", newItem.userName)
            diff.putString("workStatus", newItem.userWorkStatus.toString())
            diff.putString("reported", newItem.userReported.toString())
            diff.putString("url",newItem.userPP.toString())
            return diff;
        }
    }

    private val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAdapter.UserViewHolder {
        return UserViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_user_preview,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: UserViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {

            val set = payloads[0] as Bundle
            val currentList = differ.currentList[position]

            for (key in set.keySet()) {
                if (key == "workStatus") {
                    if (set.get(key).toString() == "true") {
                        holder.itemView.tvWorkState.text =
                            set.get("name").toString() + "'s at work now."
                        currentList.userWorkStatus = true.also {
                            holder.itemView.setOnClickListener {
                                onClickListener?.let {
                                    it(currentList)
                                }
                            }
                        }

                    } else{
                        holder.itemView.tvWorkState.text =
                            set.get("name").toString() + " is not at work right now."
                        currentList.userWorkStatus = false.also {
                            holder.itemView.setOnClickListener {
                                onClickListener?.let {
                                    it(currentList)
                                }
                            }
                        }
                    }

                }
                if (key == "reported") {
                    if (set.get(key).toString() == "true") {
                        holder.itemView.imgReported.visibility = View.VISIBLE
                    } else {
                        holder.itemView.imgReported.visibility = View.GONE
                    }
                }

                if (key=="url"){
                    holder.itemView.apply {
                        Glide.with(this).load(set.get(key).toString()).into(userPP)
                    }
                }
            }


        }
    }


    override fun onBindViewHolder(holder: UserAdapter.UserViewHolder, position: Int) {
        var currentUser = differ.currentList[position]
        holder.itemView.apply {
            Glide.with(this).load(currentUser.userPP).into(userPP)
            tvUserName.text = currentUser.userName + " " + currentUser.userSurname
            if (currentUser.userReported == true)
                imgReported.visibility = View.VISIBLE
            else
                imgReported.visibility = View.GONE
            if (currentUser.userWorkStatus == true)
                tvWorkState.text = "${currentUser.userName}'s at work now."
            else
                tvWorkState.text = "${currentUser.userName} is not at work right now"
            setOnClickListener {
                onClickListener?.let {
                    it(currentUser)
                }
            }
        }
    }

    //Kullanıcı iş Bilgileri gönderilmesini isteniyorum. Belki işlemler yapılacak
    private var onClickListener: ((UserWorkInformation) -> Unit)? = null

    fun setOnItemClickListener(listener: (UserWorkInformation) -> Unit) {
        onClickListener = listener
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: List<UserWorkInformation>) {
        differ.submitList(list)
    }

}