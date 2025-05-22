package com.example.sufragio.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sufragio.R

class SearchAdapter(
    private var voteList: List<VoteSearchData>,
    private val onItemClick: (VoteSearchData) -> Unit
) : RecyclerView.Adapter<SearchAdapter.VoteViewHolder>() {

    inner class VoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.voteTitleText)
        val infoText: TextView = itemView.findViewById(R.id.voteInfoText)

        fun bind(vote: VoteSearchData) {
            titleText.text = vote.title
            infoText.text = "참여자 ${vote.participants}명 • 마감일 ${vote.deadline}"
            itemView.setOnClickListener { onItemClick(vote) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.search_vote_item, parent, false)
        return VoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: VoteViewHolder, position: Int) {
        holder.bind(voteList[position])
    }

    override fun getItemCount(): Int = voteList.size

    fun updateList(newList: List<VoteSearchData>) {
        voteList = newList
        notifyDataSetChanged()
    }
}
