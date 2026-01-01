package com.cinemax.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Movie data class representing a movie from the API
 */
data class Movie(
    @SerializedName("_id")
    val id: String,
    val title: String,
    @SerializedName("original_title")
    val originalTitle: String? = null,
    val year: Int? = null,
    val quality: String = "HD",
    val size: String? = null,
    @SerializedName("size_bytes")
    val sizeBytes: Long = 0,
    @SerializedName("duration_minutes")
    val durationMinutes: Int? = null,
    val genres: List<String> = emptyList(),
    val language: String = "Hindi",
    @SerializedName("poster_url")
    val posterUrl: String? = null,
    @SerializedName("backdrop_url")
    val backdropUrl: String? = null,
    val rating: Float = 0f,
    val plot: String? = null,
    @SerializedName("release_date")
    val releaseDate: String? = null,
    val tagline: String? = null,
    val cast: List<CastMember> = emptyList(),
    @SerializedName("trailer_url")
    val trailerUrl: String? = null,
    @SerializedName("is_series")
    val isSeries: Boolean = false,
    val category: String = "hollywood",
    val views: Int = 0,
    val downloads: Int = 0
) {
    val formattedDuration: String
        get() = durationMinutes?.let {
            val hours = it / 60
            val mins = it % 60
            if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
        } ?: "N/A"
    
    val formattedSize: String
        get() = size ?: run {
            when {
                sizeBytes >= 1_073_741_824 -> "%.1f GB".format(sizeBytes / 1_073_741_824.0)
                sizeBytes >= 1_048_576 -> "%.0f MB".format(sizeBytes / 1_048_576.0)
                else -> "$sizeBytes bytes"
            }
        }
    
    val formattedRating: String
        get() = "%.1f".format(rating)
    
    val genreText: String
        get() = genres.take(3).joinToString(" • ")
}

/**
 * Cast member in a movie
 */
data class CastMember(
    val name: String,
    val character: String? = null,
    @SerializedName("profile_url")
    val profileUrl: String? = null
)

/**
 * Banner for carousel display
 */
data class Banner(
    @SerializedName("_id")
    val id: String,
    @SerializedName("movie_id")
    val movieId: String,
    val title: String,
    @SerializedName("backdrop_url")
    val backdropUrl: String,
    @SerializedName("poster_url")
    val posterUrl: String? = null,
    val tagline: String? = null
)

/**
 * Category with movie count
 */
data class Category(
    val name: String,
    val slug: String,
    val count: Int,
    val icon: String? = null
)

/**
 * Search result wrapper
 */
data class SearchResponse(
    val query: String,
    val results: List<Movie>,
    val total: Int
)

/**
 * Paginated movie list response
 */
data class MovieListResponse(
    val movies: List<Movie>,
    val total: Int,
    val page: Int,
    @SerializedName("per_page")
    val perPage: Int,
    @SerializedName("total_pages")
    val totalPages: Int
)

/**
 * Banner list response
 */
data class BannerResponse(
    val banners: List<Banner>
)

/**
 * Category list response
 */
data class CategoryResponse(
    val categories: List<Category>
)

/**
 * Stream info for playback
 */
data class StreamInfo(
    @SerializedName("movie_id")
    val movieId: String,
    val title: String,
    @SerializedName("stream_url")
    val streamUrl: String,
    @SerializedName("size_bytes")
    val sizeBytes: Long? = null,
    @SerializedName("mime_type")
    val mimeType: String = "video/mp4"
)

/**
 * Room entity for downloaded movies
 */
@Entity(tableName = "downloads")
data class Download(
    @PrimaryKey
    val movieId: String,
    val title: String,
    val posterUrl: String? = null,
    val filePath: String,
    val sizeBytes: Long,
    val downloadedBytes: Long = 0,
    val status: DownloadStatus = DownloadStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
) {
    val progress: Float
        get() = if (sizeBytes > 0) (downloadedBytes.toFloat() / sizeBytes) * 100 else 0f
    
    val isComplete: Boolean
        get() = status == DownloadStatus.COMPLETED
}

/**
 * Download status enum
 */
enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED
}

/**
 * Watch history entry
 */
@Entity(tableName = "watch_history")
data class WatchHistory(
    @PrimaryKey
    val movieId: String,
    val title: String,
    val posterUrl: String? = null,
    val lastPosition: Long = 0,
    val duration: Long = 0,
    val lastWatched: Long = System.currentTimeMillis()
) {
    val progress: Float
        get() = if (duration > 0) (lastPosition.toFloat() / duration) * 100 else 0f
}
