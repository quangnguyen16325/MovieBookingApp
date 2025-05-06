package com.example.moviebooking.data.model

import com.google.firebase.firestore.DocumentId

data class SeatModel(
    @DocumentId val id: String = "",
    val row: String = "",
    val number: Int = 0,
    val type: SeatType = SeatType.STANDARD,
    val isAvailable: Boolean = true,
    val isSelected: Boolean = false,
    val screenId: String = "",
    val showtimeId: String = ""
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "row" to row,
            "number" to number,
            "type" to type.toString(),
            "isAvailable" to isAvailable,
            "screenId" to screenId,
            "showtimeId" to showtimeId
        )
    }
}

enum class SeatType {
    STANDARD,
    PREMIUM,
    VIP,
    COUPLE
}