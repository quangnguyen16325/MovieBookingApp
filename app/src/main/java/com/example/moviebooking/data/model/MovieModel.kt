package com.example.moviebooking.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class MovieModel(
    @DocumentId val id: String = "",
    val title: String = "",
    val overview: String = "",
    val posterUrl: String = "",
    val backdropUrl: String = "",
    val releaseDate: Timestamp? = null,
    val duration: Int = 0, // thời lượng phút
    val genres: List<String> = listOf(),
    val rating: Double = 0.0,
    val cast: List<String> = listOf(),
    val director: String = "",
    val trailerUrl: String = "",
    val isNowShowing: Boolean = false,
    val isComingSoon: Boolean = false
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "title" to title,
            "overview" to overview,
            "posterUrl" to posterUrl,
            "backdropUrl" to backdropUrl,
            "releaseDate" to releaseDate,
            "duration" to duration,
            "genres" to genres,
            "rating" to rating,
            "cast" to cast,
            "director" to director,
            "trailerUrl" to trailerUrl,
            "isNowShowing" to isNowShowing,
            "isComingSoon" to isComingSoon
        )
    }
}