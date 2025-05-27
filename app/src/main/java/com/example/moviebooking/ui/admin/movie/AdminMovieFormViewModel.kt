package com.example.moviebooking.ui.admin.movie

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moviebooking.data.model.MovieModel
import com.example.moviebooking.data.repository.MovieRepository
import com.example.moviebooking.data.service.CloudinaryService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MovieFormState {
    object Loading : MovieFormState()
    object Success : MovieFormState()
    data class Error(val message: String) : MovieFormState()
}

class AdminMovieFormViewModel(
    private val movieId: String?,
    private val context: Context
) : ViewModel() {
    private val TAG = "AdminMovieFormViewModel"
    private val movieRepository = MovieRepository()
    private val cloudinaryService = CloudinaryService.getInstance(context)

    private val _movie = MutableStateFlow<MovieModel?>(null)
    val movie: StateFlow<MovieModel?> = _movie.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _saveResult = MutableStateFlow<Result<MovieModel>?>(null)
    val saveResult: StateFlow<Result<MovieModel>?> = _saveResult.asStateFlow()

    private val _posterUrl = MutableStateFlow<String?>(null)
    val posterUrl: StateFlow<String?> = _posterUrl.asStateFlow()

    private val _backdropUrl = MutableStateFlow<String?>(null)
    val backdropUrl: StateFlow<String?> = _backdropUrl.asStateFlow()

    private val _formState = MutableStateFlow<MovieFormState>(MovieFormState.Loading)
    val formState: StateFlow<MovieFormState> = _formState.asStateFlow()

    init {
        if (movieId != null && movieId != "new") {
            loadMovie()
        } else {
            _formState.value = MovieFormState.Success
        }
    }

    private fun loadMovie() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _formState.value = MovieFormState.Loading

            try {
                Log.d(TAG, "Loading movie with ID: $movieId")
                val result = movieRepository.getMovieById(movieId!!)
                result.onSuccess { movieData ->
                    Log.d(TAG, "Movie loaded successfully: ${movieData.title}")
                    Log.d(TAG, "isNowShowing: ${movieData.isNowShowing}")
                    Log.d(TAG, "isComingSoon: ${movieData.isComingSoon}")
                    _movie.value = movieData
                    _posterUrl.value = movieData.posterUrl
                    _backdropUrl.value = movieData.backdropUrl
                    _formState.value = MovieFormState.Success
                }
                result.onFailure { exception ->
                    Log.e(TAG, "Failed to load movie: ${exception.message}")
                    _errorMessage.value = exception.message ?: "Failed to load movie"
                    _formState.value =
                        MovieFormState.Error(exception.message ?: "Failed to load movie")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading movie: ${e.message}")
                _errorMessage.value = e.message ?: "An unexpected error occurred"
                _formState.value = MovieFormState.Error(e.message ?: "An unexpected error occurred")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun uploadPosterImage(uri: Uri) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                _formState.value = MovieFormState.Loading
                val url = cloudinaryService.uploadImage(uri, "MovieBooking")
                _posterUrl.value = url
                _formState.value = MovieFormState.Success
            } catch (e: Exception) {
                _errorMessage.value = "Failed to upload poster image: ${e.message}"
                _formState.value =
                    MovieFormState.Error("Failed to upload poster image: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun uploadBackdropImage(uri: Uri) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                _formState.value = MovieFormState.Loading
                val url = cloudinaryService.uploadImage(uri, "MovieBooking/backdrop")
                _backdropUrl.value = url
                _formState.value = MovieFormState.Success
            } catch (e: Exception) {
                _errorMessage.value = "Failed to upload backdrop image: ${e.message}"
                _formState.value =
                    MovieFormState.Error("Failed to upload backdrop image: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveMovie(
        title: String,
        overview: String,
        duration: Int,
        genres: List<String>,
        rating: Double,
        cast: List<String>,
        director: String,
        trailerUrl: String,
        isNowShowing: Boolean,
        isComingSoon: Boolean,
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _formState.value = MovieFormState.Loading

            try {
                Log.d(TAG, "Saving movie with ID: $movieId")
                Log.d(TAG, "isNowShowing: $isNowShowing")
                Log.d(TAG, "isComingSoon: $isComingSoon")

                val posterUrl = _posterUrl.value
                val backdropUrl = _backdropUrl.value

                if (posterUrl == null || backdropUrl == null) {
                    _errorMessage.value = "Please upload both poster and backdrop images"
                    _formState.value =
                        MovieFormState.Error("Please upload both poster and backdrop images")
                    return@launch
                }

                val movie = MovieModel(
                    id = movieId ?: "",
                    title = title,
                    overview = overview,
                    posterUrl = posterUrl,
                    backdropUrl = backdropUrl,
                    duration = duration,
                    genres = genres,
                    rating = rating,
                    cast = cast,
                    director = director,
                    trailerUrl = trailerUrl,
                    isNowShowing = isNowShowing,
                    isComingSoon = isComingSoon
                )

                val result = if (movieId == null || movieId == "new") {
                    Log.d(TAG, "Adding new movie")
                    movieRepository.addMovie(movie)
                } else {
                    Log.d(TAG, "Updating existing movie")
                    movieRepository.updateMovie(movie)
                }

                result.onSuccess {
                    Log.d(TAG, "Movie saved successfully")
                    Log.d(TAG, "Final isNowShowing: ${it.isNowShowing}")
                    Log.d(TAG, "Final isComingSoon: ${it.isComingSoon}")
                    _formState.value = MovieFormState.Success
                }
                result.onFailure {
                    Log.e(TAG, "Failed to save movie: ${it.message}")
                    _formState.value = MovieFormState.Error(it.message ?: "Failed to save movie")
                }

                _saveResult.value = result
            } catch (e: Exception) {
                Log.e(TAG, "Error saving movie: ${e.message}")
                _errorMessage.value = e.message ?: "An unexpected error occurred"
                _formState.value = MovieFormState.Error(e.message ?: "An unexpected error occurred")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
        _formState.value = MovieFormState.Success
    }

    class Factory(private val movieId: String?, private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AdminMovieFormViewModel::class.java)) {
                return AdminMovieFormViewModel(movieId, context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 