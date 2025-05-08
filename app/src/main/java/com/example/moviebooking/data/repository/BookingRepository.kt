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

    suspend fun createBooking(
        cinemaId: String,
        screenId: String,
        showtimeId: String,
        movieId: String,
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

            // Update seats in the showtime
            selectedSeats.forEach { seat ->
                val seatRef = firestore.collection("cinemas")
                    .document(cinemaId)
                    .collection("screens")
                    .document(screenId)
                    .collection("showtimes")
                    .document(showtimeId)
                    .collection("seats")
                    .document(seat.id)

                batch.update(seatRef, "isAvailable", false)
            }

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
                .orderBy("bookingDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
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

    suspend fun updateBookingStatus(bookingId: String, status: BookingStatus): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            bookingsCollection.document(bookingId)
                .update("status", status.toString())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelBooking(bookingId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val booking = bookingsCollection.document(bookingId).get().await()
                .toObject(BookingModel::class.java) ?: throw Exception("Booking not found")

            // Start a batch write
            val batch = firestore.batch()

            // Update booking status
            batch.update(bookingsCollection.document(bookingId), "status", BookingStatus.CANCELLED.toString())

            // Update seats back to available
            booking.seats.forEach { seatId ->
                val seatRef = firestore.collection("cinemas")
                    .document(booking.cinemaId)
                    .collection("screens")
                    .document(booking.screenId)
                    .collection("showtimes")
                    .document(booking.showtimeId)
                    .collection("seats")
                    .document(seatId)

                batch.update(seatRef, "isAvailable", true)
            }

            // Commit the batch
            batch.commit().await()
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
}