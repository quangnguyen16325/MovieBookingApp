package com.example.moviebooking.ui.admin.showtime

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moviebooking.data.model.CinemaModel
import com.example.moviebooking.data.model.MovieModel
import com.example.moviebooking.data.model.ShowtimeModel
import com.example.moviebooking.data.repository.CinemaRepository
import com.example.moviebooking.data.repository.MovieRepository
import com.example.moviebooking.data.repository.ShowtimeRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

class AdminShowtimeFormViewModel(
    private val showtimeId: String?,
    private val context: Context
) : ViewModel() {
    private val showtimeRepository = ShowtimeRepository()
    private val movieRepository = MovieRepository()
    private val cinemaRepository = CinemaRepository()
    private val firestore = FirebaseFirestore.getInstance()

    private val _showtime = MutableStateFlow<ShowtimeModel?>(null)
    val showtime: StateFlow<ShowtimeModel?> = _showtime.asStateFlow()

    private val _movies = MutableStateFlow<List<MovieModel>>(emptyList())
    val movies: StateFlow<List<MovieModel>> = _movies.asStateFlow()

    private val _cinemas = MutableStateFlow<List<CinemaModel>>(emptyList())
    val cinemas: StateFlow<List<CinemaModel>> = _cinemas.asStateFlow()

    private val _screens = MutableStateFlow<List<ScreenModel>>(emptyList())
    val screens: StateFlow<List<ScreenModel>> = _screens.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _showError = MutableStateFlow(false)
    val showError: StateFlow<Boolean> = _showError.asStateFlow()

    private val _saveResult = MutableStateFlow<Result<ShowtimeModel>?>(null)
    val saveResult: StateFlow<Result<ShowtimeModel>?> = _saveResult.asStateFlow()

    init {
        if (showtimeId != null && showtimeId != "new") {
            loadShowtime()
        }
        loadMovies()
        loadCinemas()
    }

    private fun loadShowtime() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val showtimeDoc = firestore.collection("showtimes")
                    .document(showtimeId!!)
                    .get()
                    .await()

                if (showtimeDoc.exists()) {
                    val showtime = ShowtimeModel(
                        id = showtimeDoc.id,
                        movieId = showtimeDoc.getString("movieId") ?: "",
                        movieName = showtimeDoc.getString("movieName") ?: "",
                        cinemaId = showtimeDoc.getString("cinemaId") ?: "",
                        cinemaName = showtimeDoc.getString("cinemaName") ?: "",
                        screenId = showtimeDoc.getString("screenId") ?: "",
                        startTime = showtimeDoc.getTimestamp("startTime"),
                        endTime = showtimeDoc.getTimestamp("endTime"),
                        date = showtimeDoc.getTimestamp("date"),
                        price = showtimeDoc.getDouble("price") ?: 0.0,
                        availableSeats = showtimeDoc.getLong("availableSeats")?.toInt() ?: 0,
                        format = showtimeDoc.getString("format") ?: "2D"
                    )
                    _showtime.value = showtime
                    
                    // Load screens for the selected cinema
                    loadScreens(showtime.cinemaId)
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load showtime"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadMovies() {
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
                    }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadCinemas() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                cinemaRepository.getAllCinemas()
                    .catch { e ->
                        _errorMessage.value = e.message ?: "Failed to load cinemas"
                    }
                    .collectLatest { cinemaList ->
                        _cinemas.value = cinemaList
                    }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadScreens(cinemaId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // Get cinema document to get numberOfScreens
                val cinemaDoc = firestore.collection("cinemas")
                    .document(cinemaId)
                    .get()
                    .await()

                val numberOfScreens = cinemaDoc.getLong("numberOfScreens")?.toInt() ?: 0

                // Get screens from subcollection
                val screensSnapshot = firestore.collection("cinemas")
                    .document(cinemaId)
                    .collection("screens")
                    .get()
                    .await()

                val screensList = mutableListOf<ScreenModel>()

                // Add screens from subcollection
                screensSnapshot.documents.forEach { document ->
                    try {
                        val screenNumber = document.getLong("screenNumber")?.toInt() ?: return@forEach
                        val totalSeats = document.getLong("totalSeats")?.toInt() ?: 100
                        val availableSeats = document.getLong("availableSeats")?.toInt() ?: 100
                        screensList.add(
                            ScreenModel(
                                id = document.id,
                                screenNumber = screenNumber,
                                totalSeats = totalSeats,
                                availableSeats = availableSeats
                            )
                        )
                    } catch (e: Exception) {
                        // Skip invalid documents
                    }
                }

                // If no screens in subcollection, create default screens
                if (screensList.isEmpty() && numberOfScreens > 0) {
                    for (i in 1..numberOfScreens) {
                        screensList.add(
                            ScreenModel(
                                id = i.toString(),
                                screenNumber = i,
                                totalSeats = 100,
                                availableSeats = 100
                            )
                        )
                    }
                }

                _screens.value = screensList
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load screens"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveShowtime(
        movieId: String,
        cinemaId: String,
        screenId: String,
        startTime: Date,
        endTime: Date,
        price: Double,
        format: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _showError.value = false

            try {
                // Get movie name
                val movie = movies.value.find { it.id == movieId }
                val movieName = movie?.title ?: ""

                // Get cinema name
                val cinema = cinemas.value.find { it.id == cinemaId }
                val cinemaName = cinema?.name ?: ""

                val showtime = ShowtimeModel(
                    id = showtimeId ?: "",
                    movieId = movieId,
                    movieName = movieName,
                    cinemaId = cinemaId,
                    cinemaName = cinemaName,
                    screenId = screenId,
                    startTime = Timestamp(startTime),
                    endTime = Timestamp(endTime),
                    date = Timestamp(startTime),
                    price = price,
                    availableSeats = 72,
                    format = format
                )

                val result = if (showtimeId == null || showtimeId == "new") {
                    showtimeRepository.createShowtime(showtime)
                } else {
                    showtimeRepository.updateShowtime(showtime)
                }

                when {
                    result.isSuccess -> {
                        _saveResult.value = result
                    }
                    result.isFailure -> {
                        _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to save showtime"
                        _showError.value = true
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
                _showError.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
        _showError.value = false
    }

    data class ScreenModel(
        val id: String,
        val screenNumber: Int,
        val totalSeats: Int,
        val availableSeats: Int
    )

    class Factory(private val showtimeId: String?, private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AdminShowtimeFormViewModel::class.java)) {
                return AdminShowtimeFormViewModel(showtimeId, context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 