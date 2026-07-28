package com.example.spellmeaning

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.spellmeaning.databinding.ItemWordBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    private val items: MutableList<WordEntry>,
    private val onClick: (WordEntry) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    private val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    inner class ViewHolder(val binding: ItemWordBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = items[position]
        holder.binding.textWord.text = entry.word
        holder.binding.textMeaningPreview.text = entry.meaning
        holder.binding.textDate.text = sdf.format(Date(entry.timestamp))
        holder.binding.root.setOnClickListener { onClick(entry) }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<WordEntry>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
