package com.kangraemin.stash.features.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kangraemin.stash.domain.model.ContentType
import com.kangraemin.stash.domain.model.SavedContent
import com.kangraemin.stash.ui.theme.StashTheme

@Composable
fun ContentCard(
    content: SavedContent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column {
            AsyncImage(
                model = content.thumbnailUrl,
                contentDescription = content.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop,
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = content.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = content.contentType.displayName(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun ContentType.displayName(): String = when (this) {
    ContentType.YOUTUBE -> "YouTube"
    ContentType.INSTAGRAM -> "Instagram"
    ContentType.NAVER_MAP -> "네이버지도"
    ContentType.GOOGLE_MAP -> "구글맵"
    ContentType.COUPANG -> "쿠팡"
    ContentType.WEB -> "웹"
}

@Preview(showBackground = true)
@Composable
private fun ContentCardPreview() {
    StashTheme {
        ContentCard(
            content = SavedContent.mock,
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
