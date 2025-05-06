package com.example.moviebooking.ui.movie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moviebooking.data.model.BookingModel
import com.example.moviebooking.data.repository.BookingRepository
import com.example.moviebooking.data.repository.CinemaRepository
import com.example.moviebooking.data.repository.MovieRepository
import com.example.moviebooking.data.repository.ShowtimeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

data class BookingDetailsUiModel(
    val movieTitle: String,
    val cinemaName: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val seats: String,
    val totalAmount: String
)

class BookingConfirmationViewModel(private val bookingId: String) : ViewModel() {

    private val bookingRepository = BookingRepository()
    private val movieRepository = MovieRepository()
    private val cinemaRepository = CinemaRepository()
    private val showtimeRepository = ShowtimeRepository()

    private val _bookingDetails = MutableStateFlow<BookingDetailsUiModel?>(null)
    val bookingDetails: StateFlow<BookingDetailsUiModel?> = _bookingDetails.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadBookingDetails()
    }

    private fun loadBookingDetails() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val bookingResult = bookingRepository.getBookingById(bookingId)

                bookingResult.onSuccess { booking ->
                    processBookingDetails(booking)
                }

                bookingResult.onFailure { exception ->
                    _errorMessage.value = "Failed to load booking: ${exception.message}"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "An unexpected error occurred: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    private suspend fun processBookingDetails(booking: BookingModel) {
        try {
            // Load movie details
            val movieResult = movieRepository.getMovieById(booking.movieId)
            val movieTitle = if (movieResult.isSuccess) {
                movieResult.getOrNull()?.title ?: "Unknown Movie"
            } else {
                "Unknown Movie"
            }

            // Load cinema details
            val cinemaResult = cinemaRepository.getCinemaById(booking.cinemaId)
            val cinemaName = if (cinemaResult.isSuccess) {
                cinemaResult.getOrNull()?.name ?: "Unknown Cinema"
            } else {
                "Unknown Cinema"
            }

            // Load showtime details
            val showtimeResult = showtimeRepository.getShowtimeById(booking.showtimeId)
            val (dateStr, startTimeStr, endTimeStr) = if (showtimeResult.isSuccess) {
                val showtime = showtimeResult.getOrNull()
                val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

                val date = showtime?.date?.toDate()?.let { dateFormat.format(it) } ?: "Unknown Date"
                val startTime = showtime?.startTime?.toDate()?.let { timeFormat.format(it) } ?: "Unknown"
                val endTime = showtime?.endTime?.toDate()?.let { timeFormat.format(it) } ?: "Unknown"

                Triple(date, startTime, endTime)
            } else {
                Triple("Unknown Date", "Unknown", "Unknown")
            }

            // Format seats
            val seatsList = booking.seats.joinToString(", ") { seatId ->
                // From seatId like "showtime1_A_1", extract the row and number "A1"
                val parts = seatId.split("_")
                if (parts.size >= 3) {
                    "${parts[1]}${parts[2]}"
                } else {
                    seatId
                }
            }

            // Format total amount
            val formattedAmount = String.format("%,.0f VND", booking.totalAmount)

            // Create UI model
            _bookingDetails.value = BookingDetailsUiModel(
                movieTitle = movieTitle,
                cinemaName = cinemaName,
                date = dateStr,
                startTime = startTimeStr,
                endTime = endTimeStr,
                seats = seatsList,
                totalAmount = formattedAmount
            )
        } catch (e: Exception) {
            _errorMessage.value = "Error processing booking details: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    class Factory(private val bookingId: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BookingConfirmationViewModel::class.java)) {
                return BookingConfirmationViewModel(bookingId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}