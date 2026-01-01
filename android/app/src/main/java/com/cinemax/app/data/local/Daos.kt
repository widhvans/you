package com.cinemax.app.data.local

import androidx.room.*
import com.cinemax.app.data.model.Download
import com.cinemax.app.data.model.DownloadStatus
import com.cinemax.app.data.model.WatchHistory
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Downloads
 */
@Dao
interface DownloadDao {

    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun getAllDownloads(): Flow<List<Download>>

    @Query("SELECT * FROM downloads WHERE status = :status ORDER BY createdAt DESC")
    fun getDownloadsByStatus(status: DownloadStatus): Flow<List<Download>>

    @Query("SELECT * FROM downloads WHERE movieId = :movieId")
    suspend fun getDownloadByMovieId(movieId: String): Download?

    @Query("SELECT * FROM downloads WHERE movieId = :movieId")
    fun observeDownload(movieId: String): Flow<Download?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: Download)

    @Update
    suspend fun updateDownload(download: Download)

    @Query("UPDATE downloads SET downloadedBytes = :bytes, status = :status WHERE movieId = :movieId")
    suspend fun updateProgress(movieId: String, bytes: Long, status: DownloadStatus)

    @Query("UPDATE downloads SET status = :status WHERE movieId = :movieId")
    suspend fun updateStatus(movieId: String, status: DownloadStatus)

    @Delete
    suspend fun deleteDownload(download: Download)

    @Query("DELETE FROM downloads WHERE movieId = :movieId")
    suspend fun deleteByMovieId(movieId: String)

    @Query("SELECT COUNT(*) FROM downloads WHERE status = :status")
    suspend fun getCountByStatus(status: DownloadStatus): Int
}

/**
 * Data Access Object for Watch History
 */
@Dao
interface WatchHistoryDao {

    @Query("SELECT * FROM watch_history ORDER BY lastWatched DESC")
    fun getAllHistory(): Flow<List<WatchHistory>>

    @Query("SELECT * FROM watch_history ORDER BY lastWatched DESC LIMIT :limit")
    fun getRecentHistory(limit: Int): Flow<List<WatchHistory>>

    @Query("SELECT * FROM watch_history WHERE movieId = :movieId")
    suspend fun getHistoryByMovieId(movieId: String): WatchHistory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: WatchHistory)

    @Update
    suspend fun updateHistory(history: WatchHistory)

    @Query("UPDATE watch_history SET lastPosition = :position, lastWatched = :timestamp WHERE movieId = :movieId")
    suspend fun updatePosition(movieId: String, position: Long, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteHistory(history: WatchHistory)

    @Query("DELETE FROM watch_history WHERE movieId = :movieId")
    suspend fun deleteByMovieId(movieId: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearAllHistory()
}
