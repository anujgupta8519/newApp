package com.example.spellmeaning

/**
 * One saved search result: the word, its meaning, and when it was looked up.
 */
data class WordEntry(
    val word: String,
    val meaning: String,
    val timestamp: Long = System.currentTimeMillis()
)
