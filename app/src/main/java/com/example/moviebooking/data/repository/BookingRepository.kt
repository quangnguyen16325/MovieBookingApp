package com.example.moviebooking.data.repository

import com.example.moviebooking.data.model.BookingModel
import com.example.moviebooking.data.model.BookingStatus
import com.example.moviebooking.data.model.SeatModel
import com.example.moviebooking.data.model.ShowtimeModel
import com.example.moviebooking.data.model.MovieModel
import com.example.moviebooking.data.model.CinemaModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class BookingRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val bookingsCollection = firestore.collection("bookings")
    private val seatsCollection = firestore.collection("seats")
    private val showtimesCollection = firestore.collection("showtimes")
    private val moviesCollection = firestore.collection("movies")
    private val cinemasCollection = firestore.collection("cinemas")
    private val membershipRepository = MembershipRepository()

    suspend fun createBooking(
        showtimeId: String,
        movieId: String,
        cinemaId: String,
        selectedSeats: List<SeatModel>,
        totalAmount: Double,
        paymentMethod: String
    ): Result<BookingModel> = withContext(Dispatchers.IO) {
        try {
            val currentUser = auth.currentUser ?: throw Exception("User not authenticated")

            // Create booking document
            val booking = BookingModel(
                userId = currentUser.uid,
                showtimeId = showtimeId,
                movieId = movieId,
                cinemaId = cinemaId,
                seats = selectedSeats.map { it.id },
                totalAmount = totalAmount,
                bookingDate = Timestamp.now(),
                status = BookingStatus.PENDING,
                paymentMethod = paymentMethod
            )

            // Start a batch write
            val batch = firestore.batch()

            // Add booking document
            val bookingRef = bookingsCollection.document()
            batch.set(bookingRef, booking.toMap())

            // Create or update seat documents
            selectedSeats.forEach { seat ->
                val seatRef = seatsCollection.document(seat.id)
                // Tạo mới hoặc cập nhật document ghế
                batch.set(seatRef, seat.toMap(), com.google.firebase.firestore.SetOptions.merge())
            }

            // Update available seats count in showtime
            val showtimeRef = showtimesCollection.document(showtimeId)
            val showtime = showtimeRef.get().await()
            val currentAvailableSeats = showtime.getLong("availableSeats") ?: 0
            batch.update(showtimeRef, "availableSeats", currentAvailableSeats - selectedSeats.size)

            // Commit the batch
            batch.commit().await()

            // Tích điểm sau khi đặt vé thành công
            if (booking.status == BookingStatus.CONFIRMED) {
                membershipRepository.addPoints(booking.totalAmount)
            }

            // Return the created booking with the generated ID
            Result.success(booking.copy(id = bookingRef.id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserBookings(): Flow<List<BookingModel>> = flow {
        try {
            val currentUser = auth.currentUser ?: throw Exception("User not authenticated")

            val snapshot = bookingsCollection
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .await()

            val bookings = snapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(BookingModel::class.java)
                } catch (e: Exception) {
                    null
                }
            }
            
            if (bookings.isEmpty()) {
                println("No bookings found for user ${currentUser.uid}")
            } else {
                println("Found ${bookings.size} bookings for user ${currentUser.uid}")
            }
            
            emit(bookings)
        } catch (e: Exception) {
            println("Error getting user bookings: ${e.message}")
            throw e
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getBookingById(bookingId: String): Result<BookingModel> = withContext(Dispatchers.IO) {
        try {
            val document = bookingsCollection.document(bookingId).get().await()
            val booking = document.toObject(BookingModel::class.java)

            if (booking != null) {
                Result.success(booking)
            } else {
                Result.failure(Exception("Booking not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelBooking(bookingId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Get the booking first
            val bookingDoc = bookingsCollection.document(bookingId).get().await()
            val booking = bookingDoc.toObject(BookingModel::class.java)
                ?: throw Exception("Booking not found")

            // Can only cancel PENDING bookings
            if (booking.status != BookingStatus.PENDING) {
                throw Exception("Cannot cancel a ${booking.status} booking")
            }

            // Start a batch write
            val batch = firestore.batch()

            // Update booking status
            val bookingRef = bookingsCollection.document(bookingId)
            batch.update(bookingRef, "status", BookingStatus.CANCELLED.toString())

            // Update seat availability
            booking.seats.forEach { seatId ->
                val seatRef = seatsCollection.document(seatId)
                batch.update(seatRef, "isAvailable", true)
            }

            // Update available seats count in showtime
            val showtimeRef = showtimesCollection.document(booking.showtimeId)
            val showtime = showtimeRef.get().await()
            val currentAvailableSeats = showtime.getLong("availableSeats") ?: 0
            batch.update(showtimeRef, "availableSeats", currentAvailableSeats + booking.seats.size)

            // Commit the batch
            batch.commit().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun confirmBooking(bookingId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Get the booking first
            val bookingDoc = bookingsCollection.document(bookingId).get().await()
            val booking = bookingDoc.toObject(BookingModel::class.java)
                ?: throw Exception("Booking not found")

            // Can only confirm PENDING bookings
            if (booking.status != BookingStatus.PENDING) {
                throw Exception("Cannot confirm a ${booking.status} booking")
            }

            // Update booking status to CONFIRMED
            bookingsCollection.document(bookingId)
                .update("status", BookingStatus.CONFIRMED.toString())
                .await()

            // Tích điểm sau khi đặt vé thành công
            membershipRepository.addPoints(booking.totalAmount)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getShowtimeById(showtimeId: String): ShowtimeModel? = withContext(Dispatchers.IO) {
        try {
            val document = showtimesCollection.document(showtimeId).get().await()
            document.toObject(ShowtimeModel::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getMovieById(movieId: String): MovieModel? = withContext(Dispatchers.IO) {
        try {
            val document = moviesCollection.document(movieId).get().await()
            document.toObject(MovieModel::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getCinemaById(cinemaId: String): CinemaModel? = withContext(Dispatchers.IO) {
        try {
            val document = cinemasCollection.document(cinemaId).get().await()
            document.toObject(CinemaModel::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getBookedSeats(showtimeId: String): List<String> = withContext(Dispatchers.IO) {
        try {
            // Lấy tất cả các booking có showtimeId và status là CONFIRMED
            val bookingsSnapshot = bookingsCollection
                .whereEqualTo("showtimeId", showtimeId)
                .whereEqualTo("status", BookingStatus.CONFIRMED.toString())
                .get()
                .await()

            // Lấy tất cả seatId từ các booking
            val bookedSeats = mutableListOf<String>()
            bookingsSnapshot.documents.forEach { document ->
                val booking = document.toObject(BookingModel::class.java)
                booking?.seats?.let { seats ->
                    bookedSeats.addAll(seats)
                }
            }

            bookedSeats
        } catch (e: Exception) {
            println("Error getting booked seats: ${e.message}")
            emptyList()
        }
    }

    suspend fun updateBookingStatus(bookingId: String, status: BookingStatus): Result<BookingModel> = withContext(Dispatchers.IO) {
        try {
            val booking = getBookingById(bookingId).getOrThrow()
            
            // Nếu trạng thái chuyển sang CONFIRMED, tích điểm
            if (status == BookingStatus.CONFIRMED && booking.status != BookingStatus.CONFIRMED) {
                membershipRepository.addPoints(booking.totalAmount)
            }

            bookingsCollection.document(bookingId)
                .update("status", status.toString())
                .await()

            Result.success(booking.copy(status = status))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}