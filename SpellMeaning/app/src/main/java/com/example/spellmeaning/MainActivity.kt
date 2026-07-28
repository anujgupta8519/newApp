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
            Toast.makeText(this, getString(R.string.spellmeaning_toast_enter_word), Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)
        binding.textResult.text = getString(R.string.spellmeaning_searching_for, word)
        binding.textResult.alpha = 0f

        lifecycleScope.launch {
            val meaning = GoogleMeaningFetcher.fetchMeaning(word)
            setLoading(false)

            if (meaning.isNullOrBlank()) {
                binding.textResult.text = getString(R.string.spellmeaning_no_definition, word)
                binding.textResult.animate().alpha(1f).setDuration(300).start()
                return@launch
            }

            binding.textResult.text = meaning
            binding.textResult.animate().alpha(1f).setDuration(300).start()

            StorageHelper.saveEntry(this@MainActivity, WordEntry(word, meaning))
            Toast.makeText(this@MainActivity, getString(R.string.spellmeaning_toast_saved), Toast.LENGTH_SHORT).show()
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnSearch.isEnabled = !loading
    }
}
