package com.kangraemin.stash.domain.contentparsing

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.kangraemin.stash.domain.model.ContentType

object DeepLinkHandler {

    fun open(context: Context, url: String, contentType: ContentType) {
        when (contentType) {
            ContentType.YOUTUBE,
            ContentType.INSTAGRAM,
            ContentType.COUPANG,
            -> openAppOrCustomTab(context, url)

            ContentType.GOOGLE_MAP -> openGoogleMap(context, url)
            ContentType.NAVER_MAP -> openNaverMap(context, url)
            ContentType.WEB -> openCustomTab(context, url)
        }
    }

    private fun openAppOrCustomTab(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            openCustomTab(context, url)
        }
    }

    private fun openGoogleMap(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            setPackage("com.google.android.apps.maps")
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            openCustomTab(context, url)
        }
    }

    private fun openNaverMap(context: Context, url: String) {
        val naverMapUri = url.replace("https://map.naver.com", "nmap://map.naver.com")
            .replace("https://naver.me", "nmap://naver.me")
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(naverMapUri))
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            val playStoreIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=com.nhn.android.nmap"),
            )
            if (playStoreIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(playStoreIntent)
            } else {
                openCustomTab(
                    context,
                    "https://play.google.com/store/apps/details?id=com.nhn.android.nmap",
                )
            }
        }
    }

    private fun openCustomTab(context: Context, url: String) {
        CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(url))
    }
}
