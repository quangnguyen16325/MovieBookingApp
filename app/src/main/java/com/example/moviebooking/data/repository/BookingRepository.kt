package com.example.moviebooking.data.repository

import com.example.moviebooking.data.model.BookingModel
import com.example.moviebooking.data.model.BookingStatus
import com.example.moviebooking.data.model.SeatModel
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

    suspend fun createBooking(
        showtimeId: String,
        movieId: String,
        cinemaId: String,
        selectedSeats: List<SeatModel>,
        totalAmount: Double
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
                status = BookingStatus.PENDING
            )

            // Start a batch write
            val batch = firestore.batch()

            // Add booking document
            val bookingRef = bookingsCollection.document()
            batch.set(bookingRef, booking.toMap())

            // Update seat availability
            selectedSeats.forEach { seat ->
                val seatRef = seatsCollection.document(seat.id)
                batch.update(seatRef, "isAvailable", false)
            }

            // Update available seats count in showtime
            val showtimeRef = showtimesCollection.document(showtimeId)
            val showtime = showtimeRef.get().await()
            val currentAvailableSeats = showtime.getLong("availableSeats") ?: 0
            batch.update(showtimeRef, "availableSeats", currentAvailableSeats - selectedSeats.size)

            // Commit the batch
            batch.commit().await()

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
                document.toObject(BookingModel::class.java)
            }
            emit(bookings)
        } catch (e: Exception) {
            emit(emptyList())
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
}