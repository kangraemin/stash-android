package com.kangraemin.stash.share

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kangraemin.stash.domain.repository.ContentRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.net.HttpURLConnection
import java.net.URL

@HiltWorker
class MetadataWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val contentRepository: ContentRepository,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val contentId = inputData.getString(KEY_CONTENT_ID) ?: return Result.failure()

        val content = contentRepository.getById(contentId) ?: return Result.failure()

        val metadata = fetchOgMetadata(content.url) ?: return Result.success()

        val updated = content.copy(
            title = metadata.title ?: content.title,
            description = metadata.description,
            thumbnailUrl = metadata.imageUrl,
        )
        contentRepository.update(updated)

        return Result.success()
    }

    private fun fetchOgMetadata(url: String): OgMetadata? {
        return runCatching {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.setRequestProperty("User-Agent", USER_AGENT)

            val html = connection.inputStream.bufferedReader().use { reader ->
                val buffer = StringBuilder()
                val charArray = CharArray(READ_BUFFER_SIZE)
                var totalRead = 0
                var read: Int
                while (reader.read(charArray).also { read = it } != -1 && totalRead < MAX_READ_SIZE) {
                    buffer.append(charArray, 0, read)
                    totalRead += read
                }
                buffer.toString()
            }
            connection.disconnect()
            parseOgTags(html)
        }.getOrNull()
    }

    private fun parseOgTags(html: String): OgMetadata {
        val title = extractMetaContent(html, "og:title")
            ?: extractHtmlTitle(html)
        val description = extractMetaContent(html, "og:description")
        val imageUrl = extractMetaContent(html, "og:image")

        return OgMetadata(title = title, description = description, imageUrl = imageUrl)
    }

    private fun extractMetaContent(html: String, property: String): String? {
        val regex = Regex(
            """<meta[^>]*property\s*=\s*["']$property["'][^>]*content\s*=\s*["']([^"']*)["'][^>]*/?>""",
            RegexOption.IGNORE_CASE,
        )
        regex.find(html)?.groupValues?.get(1)?.let { return it }

        val altRegex = Regex(
            """<meta[^>]*content\s*=\s*["']([^"']*)["'][^>]*property\s*=\s*["']$property["'][^>]*/?>""",
            RegexOption.IGNORE_CASE,
        )
        return altRegex.find(html)?.groupValues?.get(1)
    }

    private fun extractHtmlTitle(html: String): String? {
        val regex = Regex("""<title[^>]*>([^<]*)</title>""", RegexOption.IGNORE_CASE)
        return regex.find(html)?.groupValues?.get(1)?.trim()
    }

    private data class OgMetadata(
        val title: String?,
        val description: String?,
        val imageUrl: String?,
    )

    companion object {
        const val KEY_CONTENT_ID = "content_id"
        private const val TIMEOUT_MS = 10_000
        private const val READ_BUFFER_SIZE = 8192
        private const val MAX_READ_SIZE = 100_000
        private const val USER_AGENT = "Mozilla/5.0 (Linux; Android) AppleWebKit/537.36"
    }
}
