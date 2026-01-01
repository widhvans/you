package com.cinemax.app.ui.screens.search

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.cinemax.app.data.model.Movie
import com.cinemax.app.ui.components.*
import com.cinemax.app.ui.theme.*

/**
 * Search Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onMovieClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        // Top Bar with Search
        TopAppBar(
            title = { },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
            },
            actions = {
                SearchBar(
                    query = uiState.query,
                    onQueryChange = viewModel::updateQuery,
                    onSearch = viewModel::search,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp)
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = BackgroundPrimary
            )
        )
        
        // Content
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                // Loading
                uiState.isSearching -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Primary
                    )
                }
                
                // Empty state
                uiState.isEmpty -> {
                    EmptySearchState(
                        query = uiState.query,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                // Results
                uiState.results.isNotEmpty() -> {
                    SearchResultsGrid(
                        results = uiState.results,
                        onMovieClick = onMovieClick
                    )
                }
                
                // Initial state - show recent searches
                uiState.query.isEmpty() -> {
                    RecentSearches(
                        searches = uiState.recentSearches,
                        onSearchClick = { query ->
                            viewModel.updateQuery(query)
                            viewModel.search(query)
                        },
                        onClearClick = viewModel::clearRecentSearch,
                        onClearAllClick = viewModel::clearAllRecentSearches
                    )
                }
            }
        }
    }
}

/**
 * Search results grid
 */
@Composable
private fun SearchResultsGrid(
    results: List<Movie>,
    onMovieClick: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 100.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(results) { movie ->
            SearchResultItem(
                movie = movie,
                onClick = { onMovieClick(movie.id) }
            )
        }
    }
}

/**
 * Single search result item
 */
@Composable
private fun SearchResultItem(
    movie: Movie,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(100.dp)
            .clickable(onClick = onClick)
    ) {
        // Poster
        AsyncImage(
            model = movie.posterUrl,
            contentDescription = movie.title,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceVariant),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Title
        Text(
            text = movie.title,
            style = MaterialTheme.typography.labelMedium,
            color = TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        
        // Rating and Year
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (movie.rating > 0) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = Secondary
                )
                Text(
                    text = movie.formattedRating,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
            movie.year?.let {
                Text(
                    text = "• $it",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }
        }
    }
}

/**
 * Recent searches list
 */
@Composable
private fun RecentSearches(
    searches: List<String>,
    onSearchClick: (String) -> Unit,
    onClearClick: (String) -> Unit,
    onClearAllClick: () -> Unit
) {
    if (searches.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = TextTertiary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Search for movies",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Searches",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    TextButton(onClick = onClearAllClick) {
                        Text(
                            text = "Clear All",
                            color = Primary
                        )
                    }
                }
            }
            
            items(searches) { query ->
                ListItem(
                    headlineContent = {
                        Text(
                            text = query,
                            color = TextPrimary
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = TextTertiary
                        )
                    },
                    trailingContent = {
                        IconButton(onClick = { onClearClick(query) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove",
                                tint = TextTertiary
                            )
                        }
                    },
                    modifier = Modifier.clickable { onSearchClick(query) },
                    colors = ListItemDefaults.colors(
                        containerColor = BackgroundPrimary
                    )
                )
            }
        }
    }
}

/**
 * Empty search state
 */
@Composable
private fun EmptySearchState(
    query: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = TextTertiary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No results for \"$query\"",
            style = MaterialTheme.typography.titleMedium,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Try searching with different keywords",
            style = MaterialTheme.typography.bodyMedium,
            color = TextTertiary
        )
    }
}
