package com.example.moviebooking.ui.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moviebooking.data.model.BookingModel
import com.example.moviebooking.data.model.MovieModel
import com.example.moviebooking.data.model.CinemaModel
import com.example.moviebooking.data.model.ShowtimeModel
import com.example.moviebooking.data.model.SeatModel
import com.example.moviebooking.data.model.SeatType
import com.example.moviebooking.data.model.MembershipLevel
import com.example.moviebooking.data.repository.BookingRepository
import com.example.moviebooking.data.repository.MembershipRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PaymentViewModel(
    private val showtimeId: String,
    private val selectedSeats: List<String>,
    private val totalPrice: Double
) : ViewModel() {

    private val bookingRepository = BookingRepository()
    private val membershipRepository = MembershipRepository()

    // Payment state
    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Initial)
    val paymentState: StateFlow<PaymentState> = _paymentState.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error message
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Booking details
    private val _movieTitle = MutableStateFlow("")
    val movieTitle: StateFlow<String> = _movieTitle.asStateFlow()

    private val _cinemaName = MutableStateFlow("")
    val cinemaName: StateFlow<String> = _cinemaName.asStateFlow()

    private val _formattedSeats = MutableStateFlow("")
    val formattedSeats: StateFlow<String> = _formattedSeats.asStateFlow()

    private val _formattedTotalAmount = MutableStateFlow("")
    val formattedTotalAmount: StateFlow<String> = _formattedTotalAmount.asStateFlow()

    private val _selectedPaymentMethod = MutableStateFlow<PaymentMethod>(PaymentMethod.CREDIT_CARD)
    val selectedPaymentMethod: StateFlow<PaymentMethod> = _selectedPaymentMethod.asStateFlow()

    private val _bookingResult = MutableStateFlow<Result<BookingModel>?>(null)
    val bookingResult: StateFlow<Result<BookingModel>?> = _bookingResult.asStateFlow()

    // Membership discount
    private val _discountAmount = MutableStateFlow(0.0)
    val discountAmount: StateFlow<Double> = _discountAmount.asStateFlow()

    private val _finalAmount = MutableStateFlow(totalPrice)
    val finalAmount: StateFlow<Double> = _finalAmount.asStateFlow()

    private val _originalAmount = MutableStateFlow(totalPrice)
    val originalAmount: StateFlow<Double> = _originalAmount.asStateFlow()

    private val _membershipLevel = MutableStateFlow<MembershipLevel>(MembershipLevel.BASIC)
    val membershipLevel: StateFlow<MembershipLevel> = _membershipLevel.asStateFlow()

    // Store booking details
    private var movieId: String = ""
    private var cinemaId: String = ""
    private var screenId: String = ""

    init {
        // Format seats
        _formattedSeats.value = selectedSeats.joinToString(", ") { seat ->
            "${seat.first()}${seat.substring(1)}"
        }
        
        // Load booking details and calculate discount
        loadBookingDetails()
        calculateMembershipDiscount()
    }

    private fun calculateMembershipDiscount() {
        viewModelScope.launch {
            try {
                val membershipResult = membershipRepository.getUserMembershipInfo()
                membershipResult.onSuccess { userModel ->
                    _membershipLevel.value = userModel.membershipLevel
                    
                    // Calculate discount based on membership level
                    val discountPercentage = when (userModel.membershipLevel) {
                        MembershipLevel.BASIC -> 0.0
                        MembershipLevel.SILVER -> 0.10 // 10%
                        MembershipLevel.GOLD -> 0.15 // 15%
                        MembershipLevel.DIAMOND -> 0.25 // 25%
                        MembershipLevel.PREMIUM -> 1.0 // 100%
                    }
                    
                    _discountAmount.value = totalPrice * discountPercentage
                    _finalAmount.value = totalPrice - _discountAmount.value
                    _originalAmount.value = totalPrice
                    
                    // Update formatted total amount with discount
                    _formattedTotalAmount.value = String.format("%,.0f VND", _finalAmount.value)
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to calculate membership discount"
            }
        }
    }

    fun selectPaymentMethod(method: PaymentMethod) {
        _selectedPaymentMethod.value = method
    }

    private fun loadBookingDetails() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Load showtime details
                val showtime = bookingRepository.getShowtimeById(showtimeId)
                showtime?.let {
                    movieId = it.movieId
                    cinemaId = it.cinemaId
                    screenId = it.screenId
                    
                    // Load movie details
                    val movie = bookingRepository.getMovieById(it.movieId)
                    movie?.let {
                        _movieTitle.value = it.title
                    }
                    
                    // Load cinema details
                    val cinema = bookingRepository.getCinemaById(it.cinemaId)
                    cinema?.let {
                        _cinemaName.value = it.name
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to load booking details"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun processPayment() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val result = bookingRepository.createBooking(
                    showtimeId = showtimeId,
                    movieId = movieId,
                    cinemaId = cinemaId,
                    selectedSeats = selectedSeats.map { seatId ->
                        // Tách seatId dạng "A1" thành row "A" và number "1"
                        val row = seatId.first().toString()
                        val number = seatId.substring(1)
                        SeatModel(
                            id = "${showtimeId}_${row}_${number}",
                            row = row,
                            number = number.toInt(),
                            type = SeatType.STANDARD,
                            isAvailable = false,
                            isSelected = true,
                            screenId = screenId,
                            showtimeId = showtimeId
                        )
                    },
                    totalAmount = _finalAmount.value, // Use discounted amount
                    paymentMethod = _selectedPaymentMethod.value.toString()
                )

                _bookingResult.value = result
                result.onSuccess { booking ->
                    // Confirm the booking after successful payment
                    val confirmResult = bookingRepository.confirmBooking(booking.id)
                    confirmResult.onSuccess {
                        _paymentState.value = PaymentState.Success(booking.id)
                    }.onFailure { error ->
                        _errorMessage.value = error.message ?: "Failed to confirm booking"
                    }
                }.onFailure {
                    _errorMessage.value = it.message ?: "Payment failed"
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

    enum class PaymentMethod {
        CREDIT_CARD,
        MOBILE_PAYMENT,
        BANK_TRANSFER
    }

    sealed class PaymentState {
        object Initial : PaymentState()
        object Processing : PaymentState()
        data class Success(val bookingId: String) : PaymentState()
        data class Error(val message: String) : PaymentState()
    }

    class Factory(
        private val showtimeId: String,
        private val selectedSeats: List<String>,
        private val totalPrice: Double
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PaymentViewModel::class.java)) {
                return PaymentViewModel(showtimeId, selectedSeats, totalPrice) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 