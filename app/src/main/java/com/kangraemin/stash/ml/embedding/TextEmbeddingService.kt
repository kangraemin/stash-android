package com.kangraemin.stash.ml.embedding

import com.kangraemin.stash.domain.repository.EmbeddingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

@Singleton
class TextEmbeddingService @Inject constructor(
    private val onnxModelService: OnnxModelService,
    private val tokenizer: BasicTokenizer,
) : EmbeddingService {

    override suspend fun generateEmbedding(text: String): List<Float> = withContext(Dispatchers.Default) {
        val tokenized = tokenizer.tokenize(text)
        val session = onnxModelService.getSession()

        val inputIds = onnxModelService.createTensor(arrayOf(tokenized.inputIds))
        val attentionMask = onnxModelService.createTensor(arrayOf(tokenized.attentionMask))
        val tokenTypeIds = onnxModelService.createTensor(arrayOf(tokenized.tokenTypeIds))

        val inputs = mapOf(
            "input_ids" to inputIds,
            "attention_mask" to attentionMask,
            "token_type_ids" to tokenTypeIds,
        )

        val results = session.run(inputs)

        @Suppress("UNCHECKED_CAST")
        val outputTensor = results[0].value as Array<Array<FloatArray>>
        val tokenEmbeddings = outputTensor[0]
        val mask = tokenized.attentionMask

        val pooled = meanPooling(tokenEmbeddings, mask)
        val normalized = normalize(pooled)

        inputIds.close()
        attentionMask.close()
        tokenTypeIds.close()
        results.close()

        normalized.toList()
    }

    private fun meanPooling(tokenEmbeddings: Array<FloatArray>, attentionMask: LongArray): FloatArray {
        val embeddingDim = tokenEmbeddings[0].size
        val result = FloatArray(embeddingDim)
        var maskSum = 0f

        for (i in tokenEmbeddings.indices) {
            val maskValue = attentionMask[i].toFloat()
            maskSum += maskValue
            for (j in 0 until embeddingDim) {
                result[j] += tokenEmbeddings[i][j] * maskValue
            }
        }

        if (maskSum > 0f) {
            for (j in result.indices) {
                result[j] /= maskSum
            }
        }

        return result
    }

    private fun normalize(vector: FloatArray): FloatArray {
        var sumSquared = 0f
        for (v in vector) {
            sumSquared += v * v
        }
        val norm = sqrt(sumSquared)
        if (norm == 0f) return vector
        return FloatArray(vector.size) { vector[it] / norm }
    }
}
