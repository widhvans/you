package com.cinemax.app.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.cinemax.app.data.model.CastMember
import com.cinemax.app.data.model.Movie
import com.cinemax.app.ui.theme.*

/**
 * Movie Detail Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    onBack: () -> Unit,
    onPlayClick: (String, String) -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Primary
                )
            }
            
            uiState.error != null -> {
                ErrorState(
                    message = uiState.error!!,
                    onRetry = viewModel::loadMovie,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            uiState.movie != null -> {
                val movie = uiState.movie!!
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    // Hero Section
                    HeroSection(
                        movie = movie,
                        onBack = onBack
                    )
                    
                    // Content Section
                    ContentSection(
                        movie = movie,
                        isInWatchlist = uiState.isInWatchlist,
                        onPlayClick = {
                            uiState.streamInfo?.let { info ->
                                onPlayClick(movie.id, info.streamUrl)
                            } ?: onPlayClick(movie.id, "")
                        },
                        onDownloadClick = viewModel::startDownload,
                        onWatchlistClick = viewModel::toggleWatchlist
                    )
                }
            }
        }
    }
}

/**
 * Hero section with backdrop
 */
@Composable
private fun HeroSection(
    movie: Movie,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 10f)
    ) {
        // Backdrop
        AsyncImage(
            model = movie.backdropUrl ?: movie.posterUrl,
            contentDescription = movie.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Overlay,
                            BackgroundPrimary.copy(alpha = 0.3f),
                            BackgroundPrimary
                        )
                    )
                )
        )
        
        // Back button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .statusBarsPadding()
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = TextPrimary
            )
        }
        
        // Title at bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Meta info row
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rating
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Secondary
                    )
                    Text(
                        text = movie.formattedRating,
                        style = MaterialTheme.typography.labelLarge,
                        color = Secondary
                    )
                }
                
                movie.year?.let {
                    Text(
                        text = "•",
                        color = TextTertiary
                    )
                    Text(
                        text = it.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary
                    )
                }
                
                if (movie.formattedDuration != "N/A") {
                    Text(
                        text = "•",
                        color = TextTertiary
                    )
                    Text(
                        text = movie.formattedDuration,
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary
                    )
                }
                
                // Quality badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = BadgeHD
                ) {
                    Text(
                        text = movie.quality,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = BackgroundPrimary
                    )
                }
            }
        }
    }
}

/**
 * Content section with actions and info
 */
@Composable
private fun ContentSection(
    movie: Movie,
    isInWatchlist: Boolean,
    onPlayClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onWatchlistClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Genres
        if (movie.genres.isNotEmpty()) {
            Text(
                text = movie.genreText,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Play Button
            Button(
                onClick = onPlayClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Play Now",
                    style = MaterialTheme.typography.labelLarge
                )
            }
            
            // Download Button
            OutlinedButton(
                onClick = onDownloadClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
            ) {
                Icon(
                    imageVector = Icons.Filled.Download,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Download",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Watchlist button
        OutlinedButton(
            onClick = onWatchlistClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(vertical = 14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = if (isInWatchlist) Primary else TextPrimary
            )
        ) {
            Icon(
                imageVector = if (isInWatchlist) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isInWatchlist) "In My List" else "Add to My List",
                style = MaterialTheme.typography.labelLarge
            )
        }
        
        // Plot
        movie.plot?.let { plot ->
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Plot",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = plot,
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )
        }
        
        // File Info
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            InfoChip(
                icon = Icons.Filled.HighQuality,
                label = "Quality",
                value = movie.quality
            )
            InfoChip(
                icon = Icons.Filled.Storage,
                label = "Size",
                value = movie.formattedSize
            )
            InfoChip(
                icon = Icons.Filled.Language,
                label = "Language",
                value = movie.language
            )
        }
        
        // Cast
        if (movie.cast.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Cast",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(movie.cast) { actor ->
                    CastItem(actor = actor)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(100.dp)) // Bottom padding for nav bar
    }
}

/**
 * Info chip component
 */
@Composable
private fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = Primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = TextPrimary
        )
    }
}

/**
 * Cast member item
 */
@Composable
private fun CastItem(actor: CastMember) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        AsyncImage(
            model = actor.profileUrl,
            contentDescription = actor.name,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(SurfaceVariant),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = actor.name,
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        actor.character?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Error state
 */
@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Text("Retry")
        }
    }
}
