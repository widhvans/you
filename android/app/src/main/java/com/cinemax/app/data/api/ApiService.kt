package com.cinemax.app.data.api

import com.cinemax.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API interface for CineMax backend
 */
interface ApiService {

    /**
     * Health check
     */
    @GET("/")
    suspend fun healthCheck(): Response<Map<String, Any>>

    /**
     * Get paginated list of movies
     */
    @GET("/api/movies")
    suspend fun getMovies(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
        @Query("category") category: String? = null,
        @Query("sort_by") sortBy: String = "created_at",
        @Query("order") order: String = "desc"
    ): Response<MovieListResponse>

    /**
     * Get trending movies
     */
    @GET("/api/movies/trending")
    suspend fun getTrendingMovies(
        @Query("limit") limit: Int = 10
    ): Response<MovieListWrapper>

    /**
     * Get latest movies
     */
    @GET("/api/movies/latest")
    suspend fun getLatestMovies(
        @Query("limit") limit: Int = 20
    ): Response<MovieListWrapper>

    /**
     * Get movie by ID
     */
    @GET("/api/movies/{movieId}")
    suspend fun getMovieById(
        @Path("movieId") movieId: String
    ): Response<Movie>

    /**
     * Search movies
     */
    @GET("/api/search")
    suspend fun searchMovies(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<SearchResponse>

    /**
     * Get featured banners
     */
    @GET("/api/banners")
    suspend fun getBanners(
        @Query("limit") limit: Int = 5
    ): Response<BannerResponse>

    /**
     * Get categories
     */
    @GET("/api/categories")
    suspend fun getCategories(): Response<CategoryResponse>

    /**
     * Get download/stream info
     */
    @GET("/api/download/{movieId}")
    suspend fun getDownloadInfo(
        @Path("movieId") movieId: String
    ): Response<StreamInfo>
}

/**
 * Wrapper for movie list responses
 */
data class MovieListWrapper(
    val movies: List<Movie>,
    val total: Int
)
