package com.kangraemin.stash.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_contents")
data class ContentEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val url: String,
    val contentType: String,
    val thumbnailUrl: String?,
    val description: String?,
    val createdAt: Long,
)
