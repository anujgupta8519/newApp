package com.example.spellmeaning

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.spellmeaning.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSearch.setOnClickListener { doSearch() }
        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

    private fun doSearch() {
        val word = binding.editWord.text.toString().trim()
        if (word.isEmpty()) {
            Toast.makeText(this, "Type a word first", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)
        binding.textResult.text = ""

        lifecycleScope.launch {
            val meaning = GoogleMeaningFetcher.fetchMeaning(word)
            setLoading(false)

            if (meaning.isNullOrBlank()) {
                binding.textResult.text =
                    "Couldn't find a definition for \"$word\" right now. " +
                    "Google may not have shown a dictionary result, or blocked the request. Try again shortly."
                return@launch
            }

            binding.textResult.text = meaning

            // Auto-save: every successful search is stored immediately, no extra tap needed
            StorageHelper.saveEntry(this@MainActivity, WordEntry(word, meaning))
            Toast.makeText(this@MainActivity, "Saved to your list", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnSearch.isEnabled = !loading
    }
}
