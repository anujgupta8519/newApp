package com.example.spellmeaning

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.spellmeaning.databinding.ActivityDetailBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val word = intent.getStringExtra("word") ?: ""
        val meaning = intent.getStringExtra("meaning") ?: ""
        val timestamp = intent.getLongExtra("timestamp", System.currentTimeMillis())

        binding.textWord.text = word
        binding.textMeaning.text = meaning.ifBlank { getString(R.string.detail_no_meaning) }
        binding.textDate.text = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            .format(Date(timestamp))

        binding.btnOpenBrowser.setOnClickListener {
            val query = java.net.URLEncoder.encode("define $word", "UTF-8")
            val url = "https://www.google.com/search?q=$query"
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse(url)
            }
            startActivity(intent)
        }

        binding.btnDelete.setOnClickListener {
            StorageHelper.deleteEntry(this, word)
            finish()
        }
    }
}
