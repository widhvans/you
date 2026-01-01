package com.cinemax.app.data.repository

import com.cinemax.app.data.api.ApiService
import com.cinemax.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for movie data operations
 */
@Singleton
class MovieRepository @Inject constructor(
    private val apiService: ApiService
) {

    /**
     * Get featured banners for home carousel
     */
    fun getBanners(): Flow<Result<List<Banner>>> = flow {
        try {
            val response = apiService.getBanners()
            if (response.isSuccessful) {
                emit(Result.success(response.body()?.banners ?: emptyList()))
            } else {
                emit(Result.failure(Exception("Failed to load banners")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Get trending movies
     */
    fun getTrendingMovies(limit: Int = 10): Flow<Result<List<Movie>>> = flow {
        try {
            val response = apiService.getTrendingMovies(limit)
            if (response.isSuccessful) {
                emit(Result.success(response.body()?.movies ?: emptyList()))
            } else {
                emit(Result.failure(Exception("Failed to load trending movies")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Get latest movies
     */
    fun getLatestMovies(limit: Int = 20): Flow<Result<List<Movie>>> = flow {
        try {
            val response = apiService.getLatestMovies(limit)
            if (response.isSuccessful) {
                emit(Result.success(response.body()?.movies ?: emptyList()))
            } else {
                emit(Result.failure(Exception("Failed to load latest movies")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Get movies by category
     */
    fun getMoviesByCategory(
        category: String,
        page: Int = 1,
        perPage: Int = 20
    ): Flow<Result<MovieListResponse>> = flow {
        try {
            val response = apiService.getMovies(
                page = page,
                perPage = perPage,
                category = category
            )
            if (response.isSuccessful) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("Failed to load movies")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Get all movies with pagination
     */
    fun getMovies(
        page: Int = 1,
        perPage: Int = 20,
        sortBy: String = "created_at",
        order: String = "desc"
    ): Flow<Result<MovieListResponse>> = flow {
        try {
            val response = apiService.getMovies(
                page = page,
                perPage = perPage,
                sortBy = sortBy,
                order = order
            )
            if (response.isSuccessful) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("Failed to load movies")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Get movie details by ID
     */
    fun getMovieById(movieId: String): Flow<Result<Movie>> = flow {
        try {
            val response = apiService.getMovieById(movieId)
            if (response.isSuccessful) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("Movie not found")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Search movies
     */
    fun searchMovies(
        query: String,
        page: Int = 1,
        perPage: Int = 20
    ): Flow<Result<SearchResponse>> = flow {
        try {
            val response = apiService.searchMovies(query, page, perPage)
            if (response.isSuccessful) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("Search failed")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Get categories
     */
    fun getCategories(): Flow<Result<List<Category>>> = flow {
        try {
            val response = apiService.getCategories()
            if (response.isSuccessful) {
                emit(Result.success(response.body()?.categories ?: emptyList()))
            } else {
                emit(Result.failure(Exception("Failed to load categories")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Get download/stream info for a movie
     */
    fun getStreamInfo(movieId: String): Flow<Result<StreamInfo>> = flow {
        try {
            val response = apiService.getDownloadInfo(movieId)
            if (response.isSuccessful) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("Failed to get stream info")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
}
