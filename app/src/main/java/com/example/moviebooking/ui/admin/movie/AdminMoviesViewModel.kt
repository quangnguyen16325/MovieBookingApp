package com.example.moviebooking.ui.admin.movie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviebooking.data.model.MovieModel
import com.example.moviebooking.data.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AdminMoviesViewModel : ViewModel() {
    private val movieRepository = MovieRepository()

    private val _movies = MutableStateFlow<List<MovieModel>>(emptyList())
    val movies: StateFlow<List<MovieModel>> = _movies.asStateFlow()

    private val _filteredMovies = MutableStateFlow<List<MovieModel>>(emptyList())
    val filteredMovies: StateFlow<List<MovieModel>> = _filteredMovies.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadMovies()
    }

    fun loadMovies() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                movieRepository.getAllMovies()
                    .catch { e ->
                        _errorMessage.value = e.message ?: "Failed to load movies"
                    }
                    .collectLatest { movieList ->
                        _movies.value = movieList
                        _filteredMovies.value = movieList
                    }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchMovies(query: String) {
        if (query.isBlank()) {
            _filteredMovies.value = _movies.value
            return
        }

        val searchQuery = query.lowercase()
        _filteredMovies.value = _movies.value.filter { movie ->
            movie.title.lowercase().contains(searchQuery) ||
            movie.genres.any { it.lowercase().contains(searchQuery) } ||
            movie.director.lowercase().contains(searchQuery) ||
            movie.cast.any { it.lowercase().contains(searchQuery) }
        }
    }

    fun deleteMovie(movieId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val result = movieRepository.deleteMovie(movieId)
                result.onSuccess {
                    // Reload movies after successful deletion
                    loadMovies()
                }
                result.onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Failed to delete movie"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
} 