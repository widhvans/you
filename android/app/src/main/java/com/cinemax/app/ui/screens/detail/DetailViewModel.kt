package com.cinemax.app.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cinemax.app.data.model.Movie
import com.cinemax.app.data.model.StreamInfo
import com.cinemax.app.data.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for Detail Screen
 */
data class DetailUiState(
    val isLoading: Boolean = true,
    val movie: Movie? = null,
    val streamInfo: StreamInfo? = null,
    val error: String? = null,
    val isInWatchlist: Boolean = false
)

/**
 * ViewModel for Movie Detail Screen
 */
@HiltViewModel
class DetailViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val movieId: String = savedStateHandle.get<String>("movieId") ?: ""
    
    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()
    
    init {
        if (movieId.isNotEmpty()) {
            loadMovie()
        }
    }
    
    /**
     * Load movie details
     */
    fun loadMovie() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            movieRepository.getMovieById(movieId).collect { result ->
                result.onSuccess { movie ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            movie = movie
                        )
                    }
                    
                    // Also load stream info
                    loadStreamInfo()
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load movie"
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Load stream info for playback
     */
    private fun loadStreamInfo() {
        viewModelScope.launch {
            movieRepository.getStreamInfo(movieId).collect { result ->
                result.onSuccess { info ->
                    _uiState.update { it.copy(streamInfo = info) }
                }
            }
        }
    }
    
    /**
     * Toggle watchlist status
     */
    fun toggleWatchlist() {
        _uiState.update { it.copy(isInWatchlist = !it.isInWatchlist) }
        // TODO: Persist to database
    }
    
    /**
     * Start download
     */
    fun startDownload() {
        // TODO: Implement download with WorkManager
    }
}
