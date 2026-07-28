package com.example.spellmeaning

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spellmeaning.databinding.ActivityHistoryBinding

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var adapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = HistoryAdapter(mutableListOf()) { entry ->
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra("word", entry.word)
                putExtra("meaning", entry.meaning)
                putExtra("timestamp", entry.timestamp)
            }
            startActivity(intent)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val all = StorageHelper.loadAll(this)
        adapter.updateData(all)
        binding.textEmpty.visibility = if (all.isEmpty()) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (all.isEmpty()) View.GONE else View.VISIBLE
    }
}
