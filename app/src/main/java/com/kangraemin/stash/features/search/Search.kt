package com.kangraemin.stash.features.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kangraemin.stash.domain.model.ContentType
import com.kangraemin.stash.domain.model.SavedContent
import com.kangraemin.stash.features.home.ContentCard
import com.kangraemin.stash.ui.theme.StashTheme
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.components.SingletonComponent
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(SearchScreen::class, SingletonComponent::class)
@Composable
fun Search(state: SearchScreen.State, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = state.query,
                        onValueChange = { state.eventSink(SearchScreen.Event.OnQueryChanged(it)) },
                        placeholder = { Text("검색어를 입력하세요") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "검색",
                            )
                        },
                        trailingIcon = {
                            if (state.query.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        state.eventSink(SearchScreen.Event.OnQueryChanged(""))
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "지우기",
                                    )
                                }
                            }
                        },
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { state.eventSink(SearchScreen.Event.OnBackClicked) },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
                state.query.isNotBlank() && state.results.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "검색 결과가 없습니다.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                state.results.isNotEmpty() -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                    ) {
                        items(state.results, key = { it.id }) { content ->
                            ContentCard(
                                content = content,
                                onClick = {
                                    state.eventSink(SearchScreen.Event.OnResultClicked(content))
                                },
                                modifier = Modifier.padding(bottom = 12.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchEmptyPreview() {
    StashTheme {
        Search(
            state = SearchScreen.State(
                query = "테스트",
                results = emptyList(),
                isLoading = false,
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchResultsPreview() {
    StashTheme {
        Search(
            state = SearchScreen.State(
                query = "유튜브",
                results = listOf(
                    SavedContent.mock,
                    SavedContent(
                        id = "yt-1",
                        title = "유튜브 영상 제목",
                        url = "https://youtube.com/watch?v=123",
                        contentType = ContentType.YOUTUBE,
                        thumbnailUrl = null,
                        description = null,
                        createdAt = Instant.ofEpochMilli(1700000001000L),
                    ),
                ),
                isLoading = false,
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchLoadingPreview() {
    StashTheme {
        Search(
            state = SearchScreen.State(
                query = "검색중",
                results = emptyList(),
                isLoading = true,
            ),
        )
    }
}
