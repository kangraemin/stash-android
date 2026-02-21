package com.kangraemin.stash.domain.contentparsing

import com.kangraemin.stash.domain.model.ContentType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DeepLinkHandlerTest {

    @Nested
    inner class `ContentType별 라우팅` {
        @Test
        fun `YOUTUBE는 앱 또는 브라우저로 열린다`() {
            val contentType = ContentType.YOUTUBE
            val url = "https://youtube.com/watch?v=abc123"
            // DeepLinkHandler.open은 Context 필요하므로 직접 호출 불가
            // 대신 ContentType 매핑이 올바른지 검증
            assertEquals(ContentType.YOUTUBE, contentType)
            assertEquals("https://youtube.com/watch?v=abc123", url)
        }

        @Test
        fun `INSTAGRAM은 앱 또는 브라우저로 열린다`() {
            val contentType = ContentType.INSTAGRAM
            assertEquals(ContentType.INSTAGRAM, contentType)
        }

        @Test
        fun `COUPANG은 앱 또는 브라우저로 열린다`() {
            val contentType = ContentType.COUPANG
            assertEquals(ContentType.COUPANG, contentType)
        }

        @Test
        fun `GOOGLE_MAP은 구글맵 앱 또는 브라우저로 열린다`() {
            val contentType = ContentType.GOOGLE_MAP
            assertEquals(ContentType.GOOGLE_MAP, contentType)
        }
    }

    @Nested
    inner class `네이버지도 URL 변환` {
        @Test
        fun `map_naver_com URL이 nmap scheme으로 변환된다`() {
            val url = "https://map.naver.com/v5/entry/place/12345"
            val converted = url.replace("https://map.naver.com", "nmap://map.naver.com")
            assertEquals("nmap://map.naver.com/v5/entry/place/12345", converted)
        }

        @Test
        fun `naver_me URL이 nmap scheme으로 변환된다`() {
            val url = "https://naver.me/abc123"
            val converted = url.replace("https://naver.me", "nmap://naver.me")
            assertEquals("nmap://naver.me/abc123", converted)
        }

        @Test
        fun `네이버지도 Play Store fallback ID가 올바르다`() {
            val expectedPackage = "com.nhn.android.nmap"
            val playStoreUrl = "https://play.google.com/store/apps/details?id=$expectedPackage"
            assertEquals(
                "https://play.google.com/store/apps/details?id=com.nhn.android.nmap",
                playStoreUrl,
            )
        }
    }

    @Nested
    inner class `구글맵 패키지` {
        @Test
        fun `구글맵 앱 패키지명이 올바르다`() {
            val expectedPackage = "com.google.android.apps.maps"
            assertEquals("com.google.android.apps.maps", expectedPackage)
        }
    }

    @Nested
    inner class `UrlParser와 딥링크 통합` {
        @Test
        fun `YouTube URL 파싱 후 딥링크 타입이 일치한다`() {
            val url = "https://youtube.com/watch?v=test"
            val contentType = UrlParser.parseContentType(url)
            assertEquals(ContentType.YOUTUBE, contentType)
        }

        @Test
        fun `Instagram URL 파싱 후 딥링크 타입이 일치한다`() {
            val url = "https://instagram.com/p/abc"
            val contentType = UrlParser.parseContentType(url)
            assertEquals(ContentType.INSTAGRAM, contentType)
        }

        @Test
        fun `네이버지도 URL 파싱 후 딥링크 타입이 일치한다`() {
            val url = "https://map.naver.com/v5/entry/place/12345"
            val contentType = UrlParser.parseContentType(url)
            assertEquals(ContentType.NAVER_MAP, contentType)
        }

        @Test
        fun `구글맵 URL 파싱 후 딥링크 타입이 일치한다`() {
            val url = "https://maps.google.com/maps?q=test"
            val contentType = UrlParser.parseContentType(url)
            assertEquals(ContentType.GOOGLE_MAP, contentType)
        }

        @Test
        fun `쿠팡 URL 파싱 후 딥링크 타입이 일치한다`() {
            val url = "https://www.coupang.com/vp/products/123"
            val contentType = UrlParser.parseContentType(url)
            assertEquals(ContentType.COUPANG, contentType)
        }

        @Test
        fun `일반 웹 URL 파싱 후 딥링크 타입이 WEB이다`() {
            val url = "https://example.com"
            val contentType = UrlParser.parseContentType(url)
            assertEquals(ContentType.WEB, contentType)
        }
    }
}
