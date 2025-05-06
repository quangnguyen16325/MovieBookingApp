package com.example.moviebooking.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviebooking.data.model.MovieModel
import com.example.moviebooking.data.repository.MovieRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    private val movieRepository = MovieRepository()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<MovieModel>>(emptyList())
    val searchResults: StateFlow<List<MovieModel>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var searchJob: Job? = null

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query

        // Cancel previous search job if exists
        searchJob?.cancel()

        // If query is empty, clear results and return
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        // Debounce search to avoid excessive API calls
        searchJob = viewModelScope.launch {
            _isSearching.value = true

            // Add a small delay to wait for user to finish typing
            delay(300)

            try {
                movieRepository.searchMovies(query)
                    .catch { e ->
                        _errorMessage.value = e.message ?: "Failed to search movies"
                        _isSearching.value = false
                    }
                    .collectLatest { movies ->
                        _searchResults.value = movies
                        _isSearching.value = false
                    }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
                _isSearching.value = false
            }
        }
    }

    fun clearResults() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }

    fun clearError() {
        _errorMessage.value = null
    }
}