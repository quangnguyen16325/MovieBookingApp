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
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (query.isBlank()) {
                _searchResults.value = emptyList()
                return@launch
            }
            delay(300) // Debounce search
            _isSearching.value = true
            try {
                movieRepository.searchMovies(query)
                    .catch { e ->
                        _errorMessage.value = e.message
                        _searchResults.value = emptyList()
                    }
                    .collectLatest { movies ->
                        _searchResults.value = movies
                    }
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _searchResults.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun clearResults() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        searchJob?.cancel()
    }

    fun clearError() {
        _errorMessage.value = null
    }
}