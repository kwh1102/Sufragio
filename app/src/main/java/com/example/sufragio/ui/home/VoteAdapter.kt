package com.example.sufragio.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sufragio.R

class VoteAdapter(
    private val voteList: List<VoteItem>,
    private val onItemClick: (VoteItem) -> Unit
) : RecyclerView.Adapter<VoteAdapter.VoteViewHolder>() {

    inner class VoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.voteTitle)
        val info: TextView = itemView.findViewById(R.id.voteInfo)

        fun bind(vote: VoteItem) {
            title.text = vote.title
            info.text = "참여자 ${vote.participants}명 • 마감일 ${vote.deadline}"

            itemView.setOnClickListener {
                onItemClick(vote)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.vote_item, parent, false)
        return VoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: VoteViewHolder, position: Int) {
        holder.bind(voteList[position])
    }

    override fun getItemCount() = voteList.size
}
