package com.example.sufragio.ui.mypage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sufragio.R

class VoteAdapter(
    private val onItemClick: (Vote) -> Unit
) : ListAdapter<Vote, VoteAdapter.VoteViewHolder>(VoteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.vote_item_d, parent, false)
        return VoteViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: VoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class VoteViewHolder(
        itemView: View,
        private val onItemClick: (Vote) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.textTitle)
        private val description: TextView = itemView.findViewById(R.id.textDescription)
        private val sum: TextView = itemView.findViewById(R.id.textSum)

        fun bind(vote: Vote) {
            title.text = vote.title
            description.text = vote.description
            sum.text = "참여자 ${vote.participants}명 • 마감일 ${vote.deadline}"

            itemView.setOnClickListener {
                onItemClick(vote)
            }
        }
    }

    class VoteDiffCallback : DiffUtil.ItemCallback<Vote>() {
        override fun areItemsTheSame(oldItem: Vote, newItem: Vote): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: Vote, newItem: Vote): Boolean {
            return oldItem == newItem
        }
    }
}
