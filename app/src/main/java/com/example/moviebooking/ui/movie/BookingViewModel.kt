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
import java.util.*
import com.example.moviebooking.util.DateFormats

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
        loadShowtimeDetails()
    }

    private fun loadShowtimeDetails() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // Load showtime
                val showtime = showtimeRepository.getShowtime(showtimeId)
                _showtime.value = showtime

                // Load movie
                val movie = movieRepository.getMovie(showtime.movieId)
                _movie.value = movie

                // Load cinema
                val cinema = cinemaRepository.getCinema(showtime.cinemaId)
                _cinema.value = cinema

                // Generate seats
                generateSeats()

                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
                _isLoading.value = false
            }
        }
    }

    private fun generateSeats() {
        viewModelScope.launch {
            try {
                // Lấy danh sách ghế đã đặt
                val bookedSeats = bookingRepository.getBookedSeats(showtimeId)

                val rows = listOf("A", "B", "C", "D", "E", "F", "G", "H")
                val seatsPerRow = 9
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

                        // Tạo seatId theo định dạng showtimeId_row_number
                        val seatId = "${showtimeId}_${row}_$number"
                        
                        // Kiểm tra xem ghế đã được đặt chưa
                        val isAvailable = !bookedSeats.contains(seatId)

                        seatsList.add(
                            SeatModel(
                                id = seatId,
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
            } catch (e: Exception) {
                _errorMessage.value = "Error loading seats: ${e.message}"
            }
        }
    }

    fun toggleSeatSelection(seat: SeatModel) {
        if (!seat.isAvailable) return

        val currentSeats = _seats.value.toMutableList()
        val index = currentSeats.indexOfFirst { it.id == seat.id }
        if (index != -1) {
            val updatedSeat = currentSeats[index].copy(isSelected = !currentSeats[index].isSelected)
            currentSeats[index] = updatedSeat
            _seats.value = currentSeats

            // Update selected seats list
            _selectedSeats.value = currentSeats.filter { it.isSelected }
            
            // Update total price
            updateTotalPrice()
        }
    }

    private fun updateTotalPrice() {
        val basePrice = _showtime.value?.price ?: 0.0
        var total = 0.0

        _selectedSeats.value.forEach { seat ->
            val multiplier = when (seat.type) {
                SeatType.STANDARD -> 1.0
                SeatType.PREMIUM -> 1.2
                SeatType.VIP -> 1.5
                SeatType.COUPLE -> 2.0
            }
            total += basePrice * multiplier
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
        val date = timestamp.toDate()
        val formatter = SimpleDateFormat("hh:mm a", Locale.US)
        return formatter.format(date)
    }

    // Format date for display
    fun formatDate(timestamp: com.google.firebase.Timestamp?): String {
        if (timestamp == null) return ""
        val date = timestamp.toDate()
        val formatter = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US)
        return formatter.format(date)
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