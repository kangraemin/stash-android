package com.kangraemin.stash.data.mapper

import com.kangraemin.stash.data.room.ContentEntity
import com.kangraemin.stash.domain.model.ContentType
import com.kangraemin.stash.domain.model.SavedContent
import java.time.Instant

fun ContentEntity.toDomain(): SavedContent = SavedContent(
    id = id,
    title = title,
    url = url,
    contentType = ContentType.valueOf(contentType),
    thumbnailUrl = thumbnailUrl,
    description = description,
    createdAt = Instant.ofEpochMilli(createdAt),
)

fun SavedContent.toEntity(): ContentEntity = ContentEntity(
    id = id,
    title = title,
    url = url,
    contentType = contentType.name,
    thumbnailUrl = thumbnailUrl,
    description = description,
    createdAt = createdAt.toEpochMilli(),
)
