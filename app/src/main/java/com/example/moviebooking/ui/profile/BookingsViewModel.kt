package com.example.moviebooking.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviebooking.data.model.BookingModel
import com.example.moviebooking.data.model.BookingStatus
import com.example.moviebooking.data.repository.BookingRepository
import com.example.moviebooking.data.repository.CinemaRepository
import com.example.moviebooking.data.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

data class BookingListItemUiModel(
    val id: String,
    val movieTitle: String,
    val cinemaName: String,
    val date: String,
    val time: String,
    val seats: String,
    val status: BookingStatus,
    val totalAmount: String
)

class BookingsViewModel : ViewModel() {

    private val bookingRepository = BookingRepository()
    private val movieRepository = MovieRepository()
    private val cinemaRepository = CinemaRepository()

    private val _bookings = MutableStateFlow<List<BookingListItemUiModel>>(emptyList())
    val bookings: StateFlow<List<BookingListItemUiModel>> = _bookings.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadUserBookings()
    }

    fun loadUserBookings() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                bookingRepository.getUserBookings()
                    .catch { e ->
                        _errorMessage.value = e.message ?: "Failed to load bookings"
                        _isLoading.value = false
                    }
                    .collect { bookingsList ->
                        val uiModels = mutableListOf<BookingListItemUiModel>()

                        for (booking in bookingsList) {
                            try {
                                // Get movie title
                                val movieResult = movieRepository.getMovieById(booking.movieId)
                                val movieTitle = movieResult.getOrNull()?.title ?: "Unknown Movie"

                                // Get cinema name
                                val cinemaResult = cinemaRepository.getCinemaById(booking.cinemaId)
                                val cinemaName = cinemaResult.getOrNull()?.name ?: "Unknown Cinema"

                                // Format date and time
                                val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

                                val dateStr = booking.bookingDate?.toDate()?.let {
                                    dateFormat.format(it)
                                } ?: "Unknown Date"

                                val timeStr = booking.bookingDate?.toDate()?.let {
                                    timeFormat.format(it)
                                } ?: "Unknown Time"

                                // Format seats
                                val seatsStr = "${booking.seats.size} seats"

                                // Format total amount
                                val amountStr = String.format("%,.0f VND", booking.totalAmount)

                                uiModels.add(
                                    BookingListItemUiModel(
                                        id = booking.id,
                                        movieTitle = movieTitle,
                                        cinemaName = cinemaName,
                                        date = dateStr,
                                        time = timeStr,
                                        seats = seatsStr,
                                        status = booking.status,
                                        totalAmount = amountStr
                                    )
                                )
                            } catch (e: Exception) {
                                continue
                            }
                        }

                        _bookings.value = uiModels
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
                _isLoading.value = false
            }
        }
    }

    fun cancelBooking(bookingId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val result = bookingRepository.cancelBooking(bookingId)
                result.onSuccess {
                    loadUserBookings() // Reload the bookings list
                }
                result.onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Failed to cancel booking"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}