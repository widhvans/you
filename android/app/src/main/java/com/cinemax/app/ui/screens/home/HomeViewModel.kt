package com.cinemax.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cinemax.app.data.model.Banner
import com.cinemax.app.data.model.Movie
import com.cinemax.app.data.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for Home Screen
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val banners: List<Banner> = emptyList(),
    val trendingMovies: List<Movie> = emptyList(),
    val latestMovies: List<Movie> = emptyList(),
    val hollywoodMovies: List<Movie> = emptyList(),
    val bollywoodMovies: List<Movie> = emptyList(),
    val seriesMovies: List<Movie> = emptyList(),
    val error: String? = null
)

/**
 * ViewModel for Home Screen
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val movieRepository: MovieRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadHomeData()
    }
    
    /**
     * Load all home screen data
     */
    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // Load banners
            launch {
                movieRepository.getBanners().collect { result ->
                    result.onSuccess { banners ->
                        _uiState.update { it.copy(banners = banners) }
                    }
                }
            }
            
            // Load trending
            launch {
                movieRepository.getTrendingMovies(10).collect { result ->
                    result.onSuccess { movies ->
                        _uiState.update { it.copy(trendingMovies = movies) }
                    }
                }
            }
            
            // Load latest
            launch {
                movieRepository.getLatestMovies(20).collect { result ->
                    result.onSuccess { movies ->
                        _uiState.update { it.copy(latestMovies = movies) }
                    }
                }
            }
            
            // Load Hollywood
            launch {
                movieRepository.getMoviesByCategory("hollywood", perPage = 15).collect { result ->
                    result.onSuccess { response ->
                        _uiState.update { it.copy(hollywoodMovies = response.movies) }
                    }
                }
            }
            
            // Load Bollywood
            launch {
                movieRepository.getMoviesByCategory("bollywood", perPage = 15).collect { result ->
                    result.onSuccess { response ->
                        _uiState.update { it.copy(bollywoodMovies = response.movies) }
                    }
                }
            }
            
            // Load Series
            launch {
                movieRepository.getMoviesByCategory("series", perPage = 15).collect { result ->
                    result.onSuccess { response ->
                        _uiState.update { it.copy(seriesMovies = response.movies) }
                    }
                }
            }
            
            // Set loading to false after a short delay
            kotlinx.coroutines.delay(500)
            _uiState.update { it.copy(isLoading = false) }
        }
    }
    
    /**
     * Refresh home data
     */
    fun refresh() {
        loadHomeData()
    }
}
