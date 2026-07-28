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
        binding.textMeaning.text = meaning
        binding.textDate.text = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            .format(Date(timestamp))

        binding.btnDelete.setOnClickListener {
            StorageHelper.deleteEntry(this, word)
            finish()
        }
    }
}
