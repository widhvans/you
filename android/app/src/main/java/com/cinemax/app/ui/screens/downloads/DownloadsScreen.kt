package com.cinemax.app.ui.screens.downloads

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cinemax.app.data.model.Download
import com.cinemax.app.data.model.DownloadStatus
import com.cinemax.app.ui.theme.*

/**
 * Downloads Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    downloads: List<Download> = emptyList(), // TODO: Get from ViewModel
    onDownloadClick: (Download) -> Unit = {},
    onDeleteClick: (Download) -> Unit = {},
    onPauseClick: (Download) -> Unit = {},
    onResumeClick: (Download) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = "Downloads",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = BackgroundPrimary
            )
        )
        
        if (downloads.isEmpty()) {
            // Empty State
            EmptyDownloadsState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
            )
        } else {
            // Downloads List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(downloads) { download ->
                    DownloadItem(
                        download = download,
                        onClick = { onDownloadClick(download) },
                        onDeleteClick = { onDeleteClick(download) },
                        onPauseClick = { onPauseClick(download) },
                        onResumeClick = { onResumeClick(download) }
                    )
                }
                
                // Bottom padding for nav bar
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

/**
 * Single download item
 */
@Composable
private fun DownloadItem(
    download: Download,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            AsyncImage(
                model = download.posterUrl,
                contentDescription = download.title,
                modifier = Modifier
                    .width(80.dp)
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = download.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val (statusText, statusColor) = when (download.status) {
                        DownloadStatus.PENDING -> "Pending" to TextTertiary
                        DownloadStatus.DOWNLOADING -> "Downloading" to Info
                        DownloadStatus.PAUSED -> "Paused" to Warning
                        DownloadStatus.COMPLETED -> "Downloaded" to Success
                        DownloadStatus.FAILED -> "Failed" to Error
                    }
                    
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelMedium,
                        color = statusColor
                    )
                    
                    Text(
                        text = "•",
                        color = TextTertiary
                    )
                    
                    Text(
                        text = formatSize(download.sizeBytes),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                }
                
                // Progress Bar
                if (download.status == DownloadStatus.DOWNLOADING || download.status == DownloadStatus.PAUSED) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { download.progress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = Primary,
                        trackColor = Surface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${download.progress.toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }
            }
            
            // Actions
            Column {
                when (download.status) {
                    DownloadStatus.DOWNLOADING -> {
                        IconButton(onClick = onPauseClick) {
                            Icon(
                                imageVector = Icons.Filled.Pause,
                                contentDescription = "Pause",
                                tint = TextSecondary
                            )
                        }
                    }
                    DownloadStatus.PAUSED -> {
                        IconButton(onClick = onResumeClick) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Resume",
                                tint = Primary
                            )
                        }
                    }
                    DownloadStatus.COMPLETED -> {
                        IconButton(onClick = onClick) {
                            Icon(
                                imageVector = Icons.Filled.PlayCircle,
                                contentDescription = "Play",
                                tint = Primary
                            )
                        }
                    }
                    else -> {}
                }
                
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = Error
                    )
                }
            }
        }
    }
}

/**
 * Empty downloads state
 */
@Composable
private fun EmptyDownloadsState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Download,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = TextTertiary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "No Downloads",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Start exploring and download movies to watch offline!",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

/**
 * Format file size
 */
private fun formatSize(bytes: Long): String {
    return when {
        bytes >= 1_073_741_824 -> "%.1f GB".format(bytes / 1_073_741_824.0)
        bytes >= 1_048_576 -> "%.0f MB".format(bytes / 1_048_576.0)
        bytes >= 1024 -> "%.0f KB".format(bytes / 1024.0)
        else -> "$bytes B"
    }
}
