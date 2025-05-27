package com.example.moviebooking.ui.admin.showtime

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviebooking.data.model.CinemaModel
import com.example.moviebooking.data.model.MovieModel
import com.example.moviebooking.data.model.ShowtimeModel
import com.example.moviebooking.data.repository.CinemaRepository
import com.example.moviebooking.data.repository.MovieRepository
import com.example.moviebooking.data.repository.ShowtimeRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminShowtimeListViewModel : ViewModel() {
    private val showtimeRepository = ShowtimeRepository()
    private val movieRepository = MovieRepository()
    private val cinemaRepository = CinemaRepository()
    private val firestore = FirebaseFirestore.getInstance()

    private val _showtimes = MutableStateFlow<List<ShowtimeModel>>(emptyList())
    val showtimes: StateFlow<List<ShowtimeModel>> = _showtimes.asStateFlow()

    private val _movies = MutableStateFlow<List<MovieModel>>(emptyList())
    val movies: StateFlow<List<MovieModel>> = _movies.asStateFlow()

    private val _cinemas = MutableStateFlow<List<CinemaModel>>(emptyList())
    val cinemas: StateFlow<List<CinemaModel>> = _cinemas.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadShowtimes()
        loadMovies()
        loadCinemas()
    }

    fun loadShowtimes() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val showtimesSnapshot = firestore.collection("showtimes")
                    .get()
                    .await()

                val showtimesList = showtimesSnapshot.documents.mapNotNull { document ->
                    try {
                        ShowtimeModel(
                            id = document.id,
                            movieId = document.getString("movieId") ?: "",
                            movieName = document.getString("movieName") ?: "",
                            cinemaId = document.getString("cinemaId") ?: "",
                            cinemaName = document.getString("cinemaName") ?: "",
                            screenId = document.getString("screenId") ?: "",
                            startTime = document.getTimestamp("startTime"),
                            endTime = document.getTimestamp("endTime"),
                            date = document.getTimestamp("date"),
                            price = document.getDouble("price") ?: 0.0,
                            availableSeats = document.getLong("availableSeats")?.toInt() ?: 0,
                            format = document.getString("format") ?: "2D"
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                _showtimes.value = showtimesList
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load showtimes"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadMovies() {
        viewModelScope.launch {
            try {
                movieRepository.getAllMovies()
                    .collect { movieList ->
                        _movies.value = movieList
                    }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load movies"
            }
        }
    }

    private fun loadCinemas() {
        viewModelScope.launch {
            try {
                cinemaRepository.getAllCinemas()
                    .collect { cinemaList ->
                        _cinemas.value = cinemaList
                    }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load cinemas"
            }
        }
    }

    fun deleteShowtime(showtimeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                firestore.collection("showtimes")
                    .document(showtimeId)
                    .delete()
                    .await()
                
                // Reload the list after deletion
                loadShowtimes()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to delete showtime"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
} 