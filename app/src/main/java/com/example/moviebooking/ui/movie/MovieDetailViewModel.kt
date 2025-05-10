package com.example.moviebooking.ui.movie

import android.util.Log
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.example.moviebooking.util.DateFormats

class MovieDetailViewModel(private val movieId: String) : ViewModel() {

    private val TAG = "MovieDetailViewModel"
    private val movieRepository = MovieRepository()
    private val cinemaRepository = CinemaRepository()
    private val showtimeRepository = ShowtimeRepository()

    // States for movie details
    private val _movie = MutableStateFlow<MovieModel?>(null)
    val movie: StateFlow<MovieModel?> = _movie.asStateFlow()

    // States for cinemas showing the movie
    private val _cinemas = MutableStateFlow<List<CinemaModel>>(emptyList())
    val cinemas: StateFlow<List<CinemaModel>> = _cinemas.asStateFlow()

    // States for available dates
    private val _availableDates = MutableStateFlow<List<Date>>(emptyList())
    val availableDates: StateFlow<List<Date>> = _availableDates.asStateFlow()

    // Selected date
    private val _selectedDate = MutableStateFlow<Date?>(null)
    val selectedDate: StateFlow<Date?> = _selectedDate.asStateFlow()

    // Selected cinema
    private val _selectedCinema = MutableStateFlow<CinemaModel?>(null)
    val selectedCinema: StateFlow<CinemaModel?> = _selectedCinema.asStateFlow()

    // Showtimes for selected date and cinema
    private val _showtimes = MutableStateFlow<List<ShowtimeModel>>(emptyList())
    val showtimes: StateFlow<List<ShowtimeModel>> = _showtimes.asStateFlow()

    // Loading states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadMovieDetails()
        generateAvailableDates()
    }

    private fun loadMovieDetails() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val result = movieRepository.getMovieById(movieId)
                result.onSuccess { movieData ->
                    _movie.value = movieData
                    loadCinemasForMovie()
                }
                result.onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Failed to load movie details"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadCinemasForMovie() {
        viewModelScope.launch {
            try {
                cinemaRepository.getCinemasForMovie(movieId)
                    .catch { e ->
                        _errorMessage.value = e.message ?: "Failed to load cinemas"
                    }
                    .collectLatest { cinemaList ->
                        _cinemas.value = cinemaList
                        if (cinemaList.isNotEmpty()) {
                            _selectedCinema.value = cinemaList.first()
                            loadShowtimesForSelectedDateAndCinema()
                        }
                    }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            }
        }
    }

    private fun generateAvailableDates() {
        val dates = mutableListOf<Date>()
        val calendar = Calendar.getInstance()
        val currentDate = calendar.time

        // Current date and next 6 days (1 week total)
        for (i in 0..6) {
            // Chỉ thêm ngày nếu là ngày hiện tại hoặc tương lai
            if (calendar.time >= currentDate) {
                dates.add(calendar.time)
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        _availableDates.value = dates
        _selectedDate.value = dates.firstOrNull()
    }

    fun selectDate(date: Date) {
        _selectedDate.value = date
        loadShowtimesForSelectedDateAndCinema()
    }

    fun selectCinema(cinema: CinemaModel) {
        _selectedCinema.value = cinema
        loadShowtimesForSelectedDateAndCinema()
    }

    private fun loadShowtimesForSelectedDateAndCinema() {
        val date = _selectedDate.value ?: return
        val cinema = _selectedCinema.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d(TAG, "Loading showtimes for date: ${formatDate(date)} and cinema: ${cinema.id}")
                
                val showtimesFlow = showtimeRepository.getShowtimesForMovieAndDate(movieId, date)
                showtimesFlow.catch { e ->
                    Log.e(TAG, "Error loading showtimes: ${e.message}")
                    _errorMessage.value = e.message ?: "Failed to load showtimes"
                    _isLoading.value = false
                }.collect { allShowtimes ->
                    Log.d(TAG, "Total showtimes found: ${allShowtimes.size}")
                    
                    // Lọc và sắp xếp showtimes
                    val filteredShowtimes = allShowtimes
                        .filter { showtime ->
                            val isValid = showtime.cinemaId == cinema.id &&
                                    isShowtimeValid(showtime) &&
                                    showtime.availableSeats > 0
                            
                            Log.d(TAG, "Showtime ${showtime.id} - " +
                                    "cinemaId match: ${showtime.cinemaId == cinema.id}, " +
                                    "isValid: ${isShowtimeValid(showtime)}, " +
                                    "availableSeats: ${showtime.availableSeats}")
                            
                            isValid
                        }
                        .sortedBy { it.startTime?.toDate()?.time }

                    Log.d(TAG, "Filtered showtimes: ${filteredShowtimes.size}")
                    _showtimes.value = filteredShowtimes
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading showtimes: ${e.message}")
                _errorMessage.value = e.message ?: "An unexpected error occurred"
                _isLoading.value = false
            }
        }
    }

    private fun isShowtimeValid(showtime: ShowtimeModel): Boolean {
        val now = Calendar.getInstance()
        val showtimeDate = showtime.startTime?.toDate() ?: return false
        
        // Nếu là ngày hôm nay, kiểm tra thời gian
        if (isToday(showtimeDate)) {
            // Thêm 30 phút buffer để người dùng có thời gian đặt vé
            now.add(Calendar.MINUTE, 30)
            return showtimeDate.after(now.time)
        }
        
        // Nếu là ngày khác, luôn hợp lệ
        return true
    }

    private fun isToday(date: Date): Boolean {
        val calendar1 = Calendar.getInstance()
        val calendar2 = Calendar.getInstance()
        calendar2.time = date
        
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
               calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
    }

    fun clearError() {
        _errorMessage.value = null
    }

    // Format timestamp to readable time
    fun formatTime(timestamp: Timestamp?): String {
        if (timestamp == null) return ""
        return DateFormats.TIME.format(timestamp.toDate())
    }

    // Format date to readable string
    fun formatDate(date: Date): String {
        return DateFormats.SHORT_DATE_NO_YEAR.format(date)
    }

    class Factory(private val movieId: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MovieDetailViewModel::class.java)) {
                return MovieDetailViewModel(movieId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}