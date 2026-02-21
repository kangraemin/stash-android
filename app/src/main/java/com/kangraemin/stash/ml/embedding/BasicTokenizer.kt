package com.kangraemin.stash.ml.embedding

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BasicTokenizer @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val vocab: Map<String, Long> by lazy { loadVocab() }

    private fun loadVocab(): Map<String, Long> {
        val map = mutableMapOf<String, Long>()
        context.assets.open(VOCAB_FILE_NAME).bufferedReader().useLines { lines ->
            lines.forEachIndexed { index, token ->
                map[token] = index.toLong()
            }
        }
        return map
    }

    fun tokenize(text: String, maxLength: Int = MAX_SEQUENCE_LENGTH): TokenizedInput {
        val tokens = mutableListOf<Long>()
        tokens.add(vocab[CLS_TOKEN] ?: 101L)

        val words = text.lowercase().split(WHITESPACE_REGEX)
        for (word in words) {
            if (tokens.size >= maxLength - 1) break
            val wordTokens = tokenizeWord(word)
            for (token in wordTokens) {
                if (tokens.size >= maxLength - 1) break
                tokens.add(token)
            }
        }

        tokens.add(vocab[SEP_TOKEN] ?: 102L)

        val inputIds = LongArray(maxLength)
        val attentionMask = LongArray(maxLength)
        val tokenTypeIds = LongArray(maxLength)

        for (i in tokens.indices) {
            inputIds[i] = tokens[i]
            attentionMask[i] = 1L
        }

        return TokenizedInput(
            inputIds = inputIds,
            attentionMask = attentionMask,
            tokenTypeIds = tokenTypeIds,
        )
    }

    private fun tokenizeWord(word: String): List<Long> {
        if (word.isEmpty()) return emptyList()

        val id = vocab[word]
        if (id != null) return listOf(id)

        val tokens = mutableListOf<Long>()
        var start = 0
        while (start < word.length) {
            var end = word.length
            var matched = false
            while (start < end) {
                val substr = if (start == 0) word.substring(start, end) else "##${word.substring(start, end)}"
                val tokenId = vocab[substr]
                if (tokenId != null) {
                    tokens.add(tokenId)
                    start = end
                    matched = true
                    break
                }
                end--
            }
            if (!matched) {
                tokens.add(vocab[UNK_TOKEN] ?: 100L)
                break
            }
        }
        return tokens
    }

    data class TokenizedInput(
        val inputIds: LongArray,
        val attentionMask: LongArray,
        val tokenTypeIds: LongArray,
    )

    companion object {
        const val VOCAB_FILE_NAME = "vocab.txt"
        const val MAX_SEQUENCE_LENGTH = 128
        private const val CLS_TOKEN = "[CLS]"
        private const val SEP_TOKEN = "[SEP]"
        private const val UNK_TOKEN = "[UNK]"
        private val WHITESPACE_REGEX = "\\s+".toRegex()
    }
}
