package com.example.moviebooking.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class ShowtimeModel(
    @DocumentId val id: String = "",
    val movieId: String = "",
    val cinemaId: String = "",
    val screenId: String = "",
    val startTime: Timestamp? = null,
    val endTime: Timestamp? = null,
    val date: Timestamp? = null,
    val price: Double = 0.0,
    val availableSeats: Int = 0,
    val totalSeats: Int = 0,
    val format: String = "" // 2D, 3D, IMAX, etc.
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "movieId" to movieId,
            "cinemaId" to cinemaId,
            "screenId" to screenId,
            "startTime" to startTime,
            "endTime" to endTime,
            "date" to date,
            "price" to price,
            "availableSeats" to availableSeats,
            "totalSeats" to totalSeats,
            "format" to format
        )
    }
}