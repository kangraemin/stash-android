package com.kangraemin.stash.domain.model

import java.time.Instant

data class SavedContent(
    val id: String,
    val title: String,
    val url: String,
    val contentType: ContentType,
    val thumbnailUrl: String? = null,
    val description: String? = null,
    val createdAt: Instant,
    val embedding: List<Float>? = null,
) {
    companion object {
        val mock get() = SavedContent(
            id = "test-id",
            title = "테스트 콘텐츠",
            url = "https://example.com",
            contentType = ContentType.WEB,
            thumbnailUrl = null,
            description = null,
            createdAt = Instant.now(),
        )
    }
}
