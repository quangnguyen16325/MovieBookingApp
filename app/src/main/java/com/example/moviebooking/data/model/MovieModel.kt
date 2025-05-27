package com.example.moviebooking.data.model

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentSnapshot

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
    val isComingSoon: Boolean = false,
    val createAt: Timestamp? = null
) {
    companion object {
        private const val TAG = "MovieModel"
        
        fun fromDocument(document: DocumentSnapshot): MovieModel? {
            try {
                Log.d(TAG, "Converting document ${document.id} to MovieModel")
                Log.d(TAG, "Document data: ${document.data}")
                
                // Lấy dữ liệu trực tiếp từ document
                val data = document.data ?: return null
                val movie = MovieModel(
                    id = document.id,
                    title = data["title"] as? String ?: "",
                    overview = data["overview"] as? String ?: "",
                    posterUrl = data["posterUrl"] as? String ?: "",
                    backdropUrl = data["backdropUrl"] as? String ?: "",
                    duration = (data["duration"] as? Number)?.toInt() ?: 0,
                    genres = (data["genres"] as? List<*>)?.filterIsInstance<String>() ?: listOf(),
                    rating = (data["rating"] as? Number)?.toDouble() ?: 0.0,
                    cast = (data["cast"] as? List<*>)?.filterIsInstance<String>() ?: listOf(),
                    director = data["director"] as? String ?: "",
                    trailerUrl = data["trailerUrl"] as? String ?: "",
                    isNowShowing = data["isNowShowing"] as? Boolean ?: false,
                    isComingSoon = data["isComingSoon"] as? Boolean ?: false
                )
                
                Log.d(TAG, "Converted movie: id=${movie.id}, title=${movie.title}")
                Log.d(TAG, "isNowShowing: ${movie.isNowShowing}")
                Log.d(TAG, "isComingSoon: ${movie.isComingSoon}")
                return movie
            } catch (e: Exception) {
                Log.e(TAG, "Error converting document to MovieModel: ${e.message}", e)
                return null
            }
        }
    }

    fun toMap(): Map<String, Any?> {
        val map = mapOf(
            "title" to title,
            "overview" to overview,
            "posterUrl" to posterUrl,
            "backdropUrl" to backdropUrl,
            "duration" to duration,
            "genres" to genres,
            "rating" to rating,
            "cast" to cast,
            "director" to director,
            "trailerUrl" to trailerUrl,
            "isNowShowing" to isNowShowing,
            "isComingSoon" to isComingSoon
        )
        Log.d(TAG, "Converting MovieModel to map: $map")
        return map
    }
}