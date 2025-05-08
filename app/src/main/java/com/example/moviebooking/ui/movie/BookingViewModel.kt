package com.example.moviebooking.ui.movie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moviebooking.data.model.BookingModel
import com.example.moviebooking.data.model.CinemaModel
import com.example.moviebooking.data.model.MovieModel
import com.example.moviebooking.data.model.SeatModel
import com.example.moviebooking.data.model.SeatType
import com.example.moviebooking.data.model.ShowtimeModel
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

class BookingViewModel(private val showtimeId: String) : ViewModel() {

    private val movieRepository = MovieRepository()
    private val cinemaRepository = CinemaRepository()
    private val showtimeRepository = ShowtimeRepository()
    private val bookingRepository = BookingRepository()

    // States for booking process
    private val _showtime = MutableStateFlow<ShowtimeModel?>(null)
    val showtime: StateFlow<ShowtimeModel?> = _showtime.asStateFlow()

    private val _movie = MutableStateFlow<MovieModel?>(null)
    val movie: StateFlow<MovieModel?> = _movie.asStateFlow()

    private val _cinema = MutableStateFlow<CinemaModel?>(null)
    val cinema: StateFlow<CinemaModel?> = _cinema.asStateFlow()

    // Available seats
    private val _seats = MutableStateFlow<List<SeatModel>>(emptyList())
    val seats: StateFlow<List<SeatModel>> = _seats.asStateFlow()

    // Selected seats
    private val _selectedSeats = MutableStateFlow<List<SeatModel>>(emptyList())
    val selectedSeats: StateFlow<List<SeatModel>> = _selectedSeats.asStateFlow()

    // Total price based on selected seats
    private val _totalPrice = MutableStateFlow(0.0)
    val totalPrice: StateFlow<Double> = _totalPrice.asStateFlow()

    // Booking result
    private val _bookingResult = MutableStateFlow<Result<BookingModel>?>(null)
    val bookingResult: StateFlow<Result<BookingModel>?> = _bookingResult.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error message
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
                // Load showtime details
                val showtimeResult = showtimeRepository.getShowtimeById(showtimeId)
                showtimeResult.onSuccess { showtimeData ->
                    _showtime.value = showtimeData

                    // Load movie details
                    val movieResult = movieRepository.getMovieById(showtimeData.movieId)
                    movieResult.onSuccess { movieData ->
                        _movie.value = movieData
                    }
                    movieResult.onFailure { exception ->
                        _errorMessage.value = "Failed to load movie details: ${exception.message}"
                    }

                    // Load cinema details
                    val cinemaResult = cinemaRepository.getCinemaById(showtimeData.cinemaId)
                    cinemaResult.onSuccess { cinemaData ->
                        _cinema.value = cinemaData
                    }
                    cinemaResult.onFailure { exception ->
                        _errorMessage.value = "Failed to load cinema details: ${exception.message}"
                    }

                    // Generate seats (in a real app, you would fetch this from the database)
                    generateSeats()
                }
                showtimeResult.onFailure { exception ->
                    _errorMessage.value = "Failed to load showtime details: ${exception.message}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "An unexpected error occurred: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun generateSeats() {
        // In a real app, this would be a fetch operation from a database
        // Here we'll create some dummy seats for demonstration purposes

        val rows = listOf("A", "B", "C", "D", "E", "F", "G", "H")
        val seatsPerRow = 10
        val seatsList = mutableListOf<SeatModel>()

        for (row in rows) {
            for (number in 1..seatsPerRow) {
                // Determine seat type based on row
                val type = when {
                    row in listOf("A", "B") -> SeatType.STANDARD
                    row in listOf("C", "D", "E") -> SeatType.PREMIUM
                    row == "F" -> SeatType.VIP
                    else -> SeatType.COUPLE
                }

                // Random availability
                val isAvailable = if (row == "D" && number in 4..6) false else true

                seatsList.add(
                    SeatModel(
                        id = "${showtimeId}_${row}_$number",
                        row = row,
                        number = number,
                        type = type,
                        isAvailable = isAvailable,
                        isSelected = false,
                        screenId = _showtime.value?.screenId ?: "",
                        showtimeId = showtimeId
                    )
                )
            }
        }

        _seats.value = seatsList
    }

    fun toggleSeatSelection(seat: SeatModel) {
        if (!seat.isAvailable) return

        val updatedSeats = _seats.value.map {
            if (it.id == seat.id) it.copy(isSelected = !it.isSelected) else it
        }
        _seats.value = updatedSeats

        _selectedSeats.value = updatedSeats.filter { it.isSelected }
        updateTotalPrice()
    }

    private fun updateTotalPrice() {
        val showtime = _showtime.value ?: return
        val basePrice = showtime.price

        var total = 0.0

        _selectedSeats.value.forEach { seat ->
            val priceMultiplier = when (seat.type) {
                SeatType.STANDARD -> 1.0
                SeatType.PREMIUM -> 1.3
                SeatType.VIP -> 1.8
                SeatType.COUPLE -> 2.2
            }
            total += basePrice * priceMultiplier
        }

        _totalPrice.value = total
    }

    fun confirmBooking() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                if (_selectedSeats.value.isEmpty()) {
                    _errorMessage.value = "Please select at least one seat"
                    _isLoading.value = false
                    return@launch
                }

                val showtime = _showtime.value ?: throw Exception("Showtime not found")
                val movie = _movie.value ?: throw Exception("Movie not found")
                val cinema = _cinema.value ?: throw Exception("Cinema not found")

                val result = bookingRepository.createBooking(
                    showtimeId = showtime.id,
                    movieId = movie.id,
                    cinemaId = cinema.id,
                    selectedSeats = _selectedSeats.value,
                    totalAmount = _totalPrice.value,
                    paymentMethod = "CREDIT_CARD" // Mặc định là CREDIT_CARD vì thanh toán sẽ được xử lý ở PaymentScreen
                )

                _bookingResult.value = result
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    // Format time for display
    fun formatTime(timestamp: com.google.firebase.Timestamp?): String {
        if (timestamp == null) return ""
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(timestamp.toDate())
    }

    // Format date for display
    fun formatDate(timestamp: com.google.firebase.Timestamp?): String {
        if (timestamp == null) return ""
        val sdf = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
        return sdf.format(timestamp.toDate())
    }

    // Format price for display
    fun formatPrice(price: Double): String {
        return String.format("%,.0f VND", price)
    }

    class Factory(private val showtimeId: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BookingViewModel::class.java)) {
                return BookingViewModel(showtimeId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}