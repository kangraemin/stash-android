package com.kangraemin.stash.share

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kangraemin.stash.domain.repository.ContentRepository
import com.kangraemin.stash.domain.repository.EmbeddingService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class EmbeddingWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val contentRepository: ContentRepository,
    private val embeddingService: EmbeddingService,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val contentId = inputData.getString(KEY_CONTENT_ID) ?: return Result.failure()

        val content = contentRepository.getById(contentId) ?: return Result.failure()

        val text = buildString {
            append(content.title)
            content.description?.let { append(" ").append(it) }
        }

        if (text.isBlank()) return Result.success()

        return runCatching {
            val embedding = embeddingService.generateEmbedding(text)
            contentRepository.update(content.copy(embedding = embedding))
            Result.success()
        }.getOrElse {
            if (runAttemptCount < MAX_RETRY_COUNT) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val KEY_CONTENT_ID = "content_id"
        private const val MAX_RETRY_COUNT = 3
    }
}
