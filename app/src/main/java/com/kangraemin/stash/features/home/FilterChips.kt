package com.kangraemin.stash.features.home

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kangraemin.stash.domain.model.ContentType
import com.kangraemin.stash.ui.theme.StashTheme

private data class FilterItem(
    val label: String,
    val type: ContentType?,
)

private val filterItems = listOf(
    FilterItem("전체", null),
    FilterItem("영상", ContentType.YOUTUBE),
    FilterItem("장소", ContentType.NAVER_MAP),
    FilterItem("쇼핑", ContentType.COUPANG),
    FilterItem("아티클", ContentType.WEB),
    FilterItem("인스타", ContentType.INSTAGRAM),
)

@Composable
fun FilterChips(
    selected: ContentType?,
    onSelected: (ContentType?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        filterItems.forEach { item ->
            FilterChip(
                selected = selected == item.type,
                onClick = { onSelected(item.type) },
                label = { Text(item.label) },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FilterChipsPreview() {
    StashTheme {
        FilterChips(
            selected = null,
            onSelected = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FilterChipsSelectedPreview() {
    StashTheme {
        FilterChips(
            selected = ContentType.YOUTUBE,
            onSelected = {},
        )
    }
}
