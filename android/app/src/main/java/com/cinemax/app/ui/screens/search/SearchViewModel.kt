package com.cinemax.app.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cinemax.app.data.model.Movie
import com.cinemax.app.data.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for Search Screen
 */
data class SearchUiState(
    val query: String = "",
    val isSearching: Boolean = false,
    val results: List<Movie> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val error: String? = null,
    val isEmpty: Boolean = false
)

/**
 * ViewModel for Search Screen
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val movieRepository: MovieRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    
    init {
        // Debounced search
        viewModelScope.launch {
            _searchQuery
                .debounce(500)
                .filter { it.length >= 2 }
                .distinctUntilChanged()
                .collect { query ->
                    performSearch(query)
                }
        }
    }
    
    /**
     * Update search query
     */
    fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query, isEmpty = false) }
        _searchQuery.value = query
        
        if (query.isEmpty()) {
            _uiState.update { it.copy(results = emptyList(), isSearching = false) }
        }
    }
    
    /**
     * Perform search with current query
     */
    fun search(query: String) {
        if (query.isNotBlank()) {
            addToRecentSearches(query)
            performSearch(query)
        }
    }
    
    /**
     * Internal search function
     */
    private fun performSearch(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, error = null) }
            
            movieRepository.searchMovies(query).collect { result ->
                result.onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            results = response.results,
                            isSearching = false,
                            isEmpty = response.results.isEmpty()
                        )
                    }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSearching = false,
                            error = error.message ?: "Search failed"
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Add query to recent searches
     */
    private fun addToRecentSearches(query: String) {
        val current = _uiState.value.recentSearches.toMutableList()
        current.remove(query)
        current.add(0, query)
        _uiState.update { it.copy(recentSearches = current.take(10)) }
    }
    
    /**
     * Clear recent search
     */
    fun clearRecentSearch(query: String) {
        val current = _uiState.value.recentSearches.toMutableList()
        current.remove(query)
        _uiState.update { it.copy(recentSearches = current) }
    }
    
    /**
     * Clear all recent searches
     */
    fun clearAllRecentSearches() {
        _uiState.update { it.copy(recentSearches = emptyList()) }
    }
}
