package com.walltext.app

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.walltext.app.databinding.ItemThoughtBinding

class ThoughtAdapter(
    private val onApplyClick: (Thought) -> Unit,
    private val onDeleteClick: (Thought) -> Unit,
) : ListAdapter<Thought, ThoughtAdapter.VH>(DIFF) {

    class VH(val binding: ItemThoughtBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemThoughtBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val thought = getItem(position)
        holder.binding.thoughtText.text = thought.text
        holder.binding.thoughtMeta.text = "${TextStyle.fromKey(thought.styleKey).displayName} · ${formatTime(thought.timestamp)}"
        holder.binding.thoughtApply.setOnClickListener { onApplyClick(thought) }
        holder.binding.thoughtDelete.setOnClickListener { onDeleteClick(thought) }
    }

    private fun formatTime(ts: Long): String {
        return DateUtils.getRelativeTimeSpanString(
            ts,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
        ).toString()
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Thought>() {
            override fun areItemsTheSame(o: Thought, n: Thought) = o.id == n.id
            override fun areContentsTheSame(o: Thought, n: Thought) = o == n
        }
    }
}