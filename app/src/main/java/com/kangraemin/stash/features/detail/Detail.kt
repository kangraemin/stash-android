package com.kangraemin.stash.features.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kangraemin.stash.domain.contentparsing.DeepLinkHandler
import com.kangraemin.stash.domain.model.ContentType
import com.kangraemin.stash.domain.model.SavedContent
import com.kangraemin.stash.features.common.ErrorStateView
import com.kangraemin.stash.features.common.LoadingStateView
import com.kangraemin.stash.ui.theme.StashTheme
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.components.SingletonComponent
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(DetailScreen::class, SingletonComponent::class)
@Composable
fun Detail(state: DetailScreen.State, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("상세") },
                navigationIcon = {
                    IconButton(onClick = { state.eventSink(DetailScreen.Event.OnBackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기",
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        val context = LocalContext.current
        val content = state.content
        if (content == null) {
            if (state.error != null) {
                ErrorStateView(
                    message = state.error,
                    modifier = Modifier.padding(paddingValues),
                )
            } else {
                LoadingStateView(modifier = Modifier.padding(paddingValues))
            }
        } else {
            DetailContent(
                content = content,
                onOpenClicked = {
                    DeepLinkHandler.open(context, content.url, content.contentType)
                },
                onDeleteClicked = { state.eventSink(DetailScreen.Event.OnDeleteClicked) },
                modifier = Modifier.padding(paddingValues),
            )
        }

        if (state.showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { state.eventSink(DetailScreen.Event.OnDeleteDismissed) },
                confirmButton = {
                    TextButton(onClick = { state.eventSink(DetailScreen.Event.OnDeleteConfirmed) }) {
                        Text("삭제", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { state.eventSink(DetailScreen.Event.OnDeleteDismissed) }) {
                        Text("취소")
                    }
                },
                title = { Text("콘텐츠 삭제") },
                text = { Text("이 콘텐츠를 삭제하시겠습니까?") },
            )
        }
    }
}

@Composable
private fun DetailContent(
    content: SavedContent,
    onOpenClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        AsyncImage(
            model = content.thumbnailUrl,
            contentDescription = content.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentScale = ContentScale.Crop,
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = content.title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(8.dp))

            AssistChip(
                onClick = {},
                label = { Text(content.contentType.displayName()) },
            )

            content.description?.let { desc ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = content.url,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onOpenClicked,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("원본 열기")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onDeleteClicked,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("삭제", color = MaterialTheme.colorScheme.error)
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
private fun DetailPreview() {
    StashTheme {
        Detail(
            state = DetailScreen.State(
                content = SavedContent(
                    id = "1",
                    title = "맛있는 레스토랑 추천",
                    url = "https://map.naver.com/v5/entry/place/12345",
                    contentType = ContentType.NAVER_MAP,
                    thumbnailUrl = null,
                    description = "강남에서 가장 맛있는 파스타를 먹을 수 있는 레스토랑입니다.",
                    createdAt = Instant.now(),
                ),
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailEmptyPreview() {
    StashTheme {
        Detail(
            state = DetailScreen.State(content = null),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailDeleteDialogPreview() {
    StashTheme {
        Detail(
            state = DetailScreen.State(
                content = SavedContent.mock,
                showDeleteDialog = true,
            ),
        )
    }
}
