package com.cinemax.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cinemax.app.data.model.Movie
import com.cinemax.app.ui.components.*
import com.cinemax.app.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Home Screen - Main landing page
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onMovieClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onCategoryClick: (String, String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Pull to refresh state
    val pullToRefreshState = rememberPullToRefreshState()
    
    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            viewModel.refresh()
            delay(1000)
            pullToRefreshState.endRefresh()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(pullToRefreshState.nestedScrollConnection)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Banner Carousel
            item {
                if (uiState.isLoading) {
                    BannerShimmer(
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                } else if (uiState.banners.isNotEmpty()) {
                    BannerCarousel(
                        banners = uiState.banners,
                        onBannerClick = onMovieClick,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }
            }
            
            // Search Bar
            item {
                CompactSearchBar(
                    onClick = onSearchClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 24.dp)
                )
            }
            
            // Trending Now
            item {
                FeaturedCategoryRow(
                    title = "🔥 Trending Now",
                    movies = uiState.trendingMovies,
                    onMovieClick = { onMovieClick(it.id) },
                    onSeeAllClick = { onCategoryClick("trending", "Trending Now") },
                    isLoading = uiState.isLoading,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
            
            // Latest Releases
            item {
                CategoryRow(
                    title = "✨ Latest Releases",
                    movies = uiState.latestMovies,
                    onMovieClick = { onMovieClick(it.id) },
                    onSeeAllClick = { onCategoryClick("latest", "Latest Releases") },
                    isLoading = uiState.isLoading,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
            
            // Hollywood
            if (uiState.hollywoodMovies.isNotEmpty() || uiState.isLoading) {
                item {
                    CategoryRow(
                        title = "🎬 Hollywood",
                        movies = uiState.hollywoodMovies,
                        onMovieClick = { onMovieClick(it.id) },
                        onSeeAllClick = { onCategoryClick("hollywood", "Hollywood") },
                        isLoading = uiState.isLoading,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }
            }
            
            // Bollywood
            if (uiState.bollywoodMovies.isNotEmpty() || uiState.isLoading) {
                item {
                    CategoryRow(
                        title = "🎭 Bollywood",
                        movies = uiState.bollywoodMovies,
                        onMovieClick = { onMovieClick(it.id) },
                        onSeeAllClick = { onCategoryClick("bollywood", "Bollywood") },
                        isLoading = uiState.isLoading,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }
            }
            
            // Web Series
            if (uiState.seriesMovies.isNotEmpty() || uiState.isLoading) {
                item {
                    CategoryRow(
                        title = "📺 Web Series",
                        movies = uiState.seriesMovies,
                        onMovieClick = { onMovieClick(it.id) },
                        onSeeAllClick = { onCategoryClick("series", "Web Series") },
                        isLoading = uiState.isLoading,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }
            }
        }
        
        // Pull to Refresh Indicator
        PullToRefreshContainer(
            state = pullToRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            containerColor = Surface,
            contentColor = Primary
        )
        
        // Error State
        uiState.error?.let { error ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text(text = error)
            }
        }
    }
}
