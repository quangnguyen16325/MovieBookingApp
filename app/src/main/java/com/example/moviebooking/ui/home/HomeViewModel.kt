package com.example.moviebooking.ui.home

import android.util.Log
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

class HomeViewModel : ViewModel() {
    private val movieRepository = MovieRepository()

    // State for now showing movies
    private val _nowShowingMovies = MutableStateFlow<List<MovieModel>>(emptyList())
    val nowShowingMovies: StateFlow<List<MovieModel>> = _nowShowingMovies.asStateFlow()

    // State for coming soon movies
    private val _comingSoonMovies = MutableStateFlow<List<MovieModel>>(emptyList())
    val comingSoonMovies: StateFlow<List<MovieModel>> = _comingSoonMovies.asStateFlow()

    // Loading states
    private val _isNowShowingLoading = MutableStateFlow(false)
    val isNowShowingLoading: StateFlow<Boolean> = _isNowShowingLoading.asStateFlow()

    private val _isComingSoonLoading = MutableStateFlow(false)
    val isComingSoonLoading: StateFlow<Boolean> = _isComingSoonLoading.asStateFlow()

    // Error states
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadNowShowingMovies()
        loadComingSoonMovies()
    }

//    fun loadNowShowingMovies() {
//        viewModelScope.launch {
//            _isNowShowingLoading.value = true
//            _errorMessage.value = null
//
//            try {
//                movieRepository.getNowShowingMovies()
//                    .catch { e ->
//                        _errorMessage.value = e.message ?: "Failed to load Now Showing movies"
//                        _isNowShowingLoading.value = false
//                    }
//                    .collectLatest { movies ->
//                        _nowShowingMovies.value = movies
//                        _isNowShowingLoading.value = false
//                    }
//            } catch (e: Exception) {
//                _errorMessage.value = e.message ?: "An unexpected error occurred"
//                _isNowShowingLoading.value = false
//            }
//        }
//    }
    fun loadNowShowingMovies() {
        viewModelScope.launch {
            _isNowShowingLoading.value = true
            _errorMessage.value = null

            Log.d("HomeViewModel", "Starting to load now showing movies")

            try {
                movieRepository.getNowShowingMovies()
                    .catch { e ->
                        Log.e("HomeViewModel", "Error loading now showing movies: ${e.message}", e)
                        _errorMessage.value = e.message ?: "Failed to load Now Showing movies"
                        _isNowShowingLoading.value = false
                    }
                    .collectLatest { movies ->
                        Log.d("HomeViewModel", "Loaded now showing movies: ${movies.size}")
                        for (movie in movies) {
                            Log.d("HomeViewModel", "Movie: ${movie.title}, isNowShowing: ${movie.isNowShowing}")
                        }
                        _nowShowingMovies.value = movies
                        _isNowShowingLoading.value = false
                    }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Exception in loadNowShowingMovies: ${e.message}", e)
                _errorMessage.value = e.message ?: "An unexpected error occurred"
                _isNowShowingLoading.value = false
            }
        }
    }

    fun loadComingSoonMovies() {
        viewModelScope.launch {
            _isComingSoonLoading.value = true
            _errorMessage.value = null

            try {
                movieRepository.getComingSoonMovies()
                    .catch { e ->
                        _errorMessage.value = e.message ?: "Failed to load Coming Soon movies"
                        _isComingSoonLoading.value = false
                    }
                    .collectLatest { movies ->
                        _comingSoonMovies.value = movies
                        _isComingSoonLoading.value = false
                    }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
                _isComingSoonLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun refreshAllMovies() {
        loadNowShowingMovies()
        loadComingSoonMovies()
    }
}