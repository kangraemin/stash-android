package com.kangraemin.stash.domain.contentparsing

import com.kangraemin.stash.domain.model.ContentType
import java.net.URI

object UrlParser {

    fun parseContentType(url: String): ContentType {
        val host = runCatching {
            URI(url).host?.lowercase()
        }.getOrNull() ?: return ContentType.WEB

        return when {
            host.endsWith("youtube.com") || host.endsWith("youtu.be") -> ContentType.YOUTUBE
            host.endsWith("instagram.com") -> ContentType.INSTAGRAM
            host.endsWith("map.naver.com") || host.endsWith("naver.me") -> ContentType.NAVER_MAP
            host.endsWith("maps.google.com") || host.endsWith("maps.app.goo.gl") -> ContentType.GOOGLE_MAP
            host.endsWith("coupang.com") || host.endsWith("coupa.ng") -> ContentType.COUPANG
            else -> ContentType.WEB
        }
    }
}
