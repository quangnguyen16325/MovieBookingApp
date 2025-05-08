package com.example.moviebooking.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class BookingModel(
    @DocumentId val id: String = "",
    val userId: String = "",
    val showtimeId: String = "",
    val movieId: String = "",
    val cinemaId: String = "",
    val seats: List<String> = listOf(), // List of seat IDs
    val totalAmount: Double = 0.0,
    val bookingDate: Timestamp? = Timestamp.now(),
    val status: BookingStatus = BookingStatus.PENDING,
    val paymentMethod: String = ""
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "showtimeId" to showtimeId,
            "movieId" to movieId,
            "cinemaId" to cinemaId,
            "seats" to seats,
            "totalAmount" to totalAmount,
            "bookingDate" to bookingDate,
            "status" to status.toString(),
            "paymentMethod" to paymentMethod
        )
    }
}

enum class BookingStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    COMPLETED
}