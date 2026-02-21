package com.kangraemin.stash.domain.contentparsing

import com.kangraemin.stash.domain.model.ContentType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UrlParserTest {

    @Nested
    inner class `유튜브 URL 분류` {
        @Test
        fun `youtube_com URL은 YOUTUBE로 분류된다`() {
            assertEquals(
                ContentType.YOUTUBE,
                UrlParser.parseContentType("https://www.youtube.com/watch?v=abc123"),
            )
        }

        @Test
        fun `youtu_be 단축 URL은 YOUTUBE로 분류된다`() {
            assertEquals(
                ContentType.YOUTUBE,
                UrlParser.parseContentType("https://youtu.be/abc123"),
            )
        }
    }

    @Nested
    inner class `인스타그램 URL 분류` {
        @Test
        fun `instagram_com URL은 INSTAGRAM으로 분류된다`() {
            assertEquals(
                ContentType.INSTAGRAM,
                UrlParser.parseContentType("https://www.instagram.com/p/abc123"),
            )
        }
    }

    @Nested
    inner class `네이버지도 URL 분류` {
        @Test
        fun `map_naver_com URL은 NAVER_MAP으로 분류된다`() {
            assertEquals(
                ContentType.NAVER_MAP,
                UrlParser.parseContentType("https://map.naver.com/v5/entry/place/12345"),
            )
        }

        @Test
        fun `naver_me 단축 URL은 NAVER_MAP으로 분류된다`() {
            assertEquals(
                ContentType.NAVER_MAP,
                UrlParser.parseContentType("https://naver.me/abc123"),
            )
        }
    }

    @Nested
    inner class `구글맵 URL 분류` {
        @Test
        fun `maps_google_com URL은 GOOGLE_MAP으로 분류된다`() {
            assertEquals(
                ContentType.GOOGLE_MAP,
                UrlParser.parseContentType("https://maps.google.com/maps?q=Seoul"),
            )
        }

        @Test
        fun `maps_app_goo_gl 단축 URL은 GOOGLE_MAP으로 분류된다`() {
            assertEquals(
                ContentType.GOOGLE_MAP,
                UrlParser.parseContentType("https://maps.app.goo.gl/abc123"),
            )
        }
    }

    @Nested
    inner class `쿠팡 URL 분류` {
        @Test
        fun `coupang_com URL은 COUPANG으로 분류된다`() {
            assertEquals(
                ContentType.COUPANG,
                UrlParser.parseContentType("https://www.coupang.com/vp/products/12345"),
            )
        }

        @Test
        fun `coupa_ng 단축 URL은 COUPANG으로 분류된다`() {
            assertEquals(
                ContentType.COUPANG,
                UrlParser.parseContentType("https://coupa.ng/abc123"),
            )
        }
    }

    @Nested
    inner class `기타 URL 분류` {
        @Test
        fun `기타 URL은 WEB으로 분류된다`() {
            assertEquals(
                ContentType.WEB,
                UrlParser.parseContentType("https://www.example.com/article/123"),
            )
        }

        @Test
        fun `잘못된 URL은 WEB으로 분류된다`() {
            assertEquals(
                ContentType.WEB,
                UrlParser.parseContentType("not a valid url"),
            )
        }

        @Test
        fun `빈 문자열은 WEB으로 분류된다`() {
            assertEquals(
                ContentType.WEB,
                UrlParser.parseContentType(""),
            )
        }
    }

    @Nested
    inner class `쿼리 파라미터 처리` {
        @Test
        fun `쿼리 파라미터가 포함된 URL도 올바르게 분류된다`() {
            assertEquals(
                ContentType.YOUTUBE,
                UrlParser.parseContentType("https://www.youtube.com/watch?v=abc123&list=PLdef&index=1"),
            )
        }

        @Test
        fun `프래그먼트가 포함된 URL도 올바르게 분류된다`() {
            assertEquals(
                ContentType.INSTAGRAM,
                UrlParser.parseContentType("https://www.instagram.com/p/abc123#comments"),
            )
        }
    }
}
