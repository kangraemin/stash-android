package com.kangraemin.stash.share

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.kangraemin.stash.domain.contentparsing.UrlParser
import com.kangraemin.stash.domain.model.SavedContent
import com.kangraemin.stash.domain.repository.ContentRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class ShareActivity : ComponentActivity() {

    @Inject
    lateinit var contentRepository: ContentRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url = extractUrl()
        if (url == null) {
            Toast.makeText(this, "URL을 찾을 수 없습니다", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        saveContent(url)
    }

    private fun saveContent(url: String) {
        val contentType = UrlParser.parseContentType(url)
        val content = SavedContent(
            id = UUID.randomUUID().toString(),
            url = url,
            contentType = contentType,
            title = url,
            createdAt = Instant.now(),
        )

        lifecycleScope.launch {
            contentRepository.save(content)
            enqueueMetadataExtraction(content.id)
            Toast.makeText(this@ShareActivity, "저장 완료", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun enqueueMetadataExtraction(contentId: String) {
        val workRequest = OneTimeWorkRequestBuilder<MetadataWorker>()
            .setInputData(workDataOf(MetadataWorker.KEY_CONTENT_ID to contentId))
            .build()
        WorkManager.getInstance(this).enqueue(workRequest)
    }

    private fun extractUrl(): String? {
        if (intent?.action != Intent.ACTION_SEND) return null
        if (intent.type != "text/plain") return null
        return intent.getStringExtra(Intent.EXTRA_TEXT)?.takeIf { it.isNotBlank() }
    }
}
