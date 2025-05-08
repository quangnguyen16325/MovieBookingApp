package com.example.moviebooking.data.repository

import android.util.Log
import com.example.moviebooking.data.model.ShowtimeModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date

class ShowtimeRepository {
    private val TAG = "ShowtimeRepository"
    private val firestore = FirebaseFirestore.getInstance()
    private val showtimesCollection = firestore.collection("showtimes")

    suspend fun getShowtimesForMovie(movieId: String): Flow<List<ShowtimeModel>> = flow {
        try {
            val snapshot = showtimesCollection
                .whereEqualTo("movieId", movieId)
                .whereGreaterThan("startTime", Timestamp.now())
                .get()
                .await()

            val showtimes = snapshot.documents.mapNotNull { document ->
                document.toObject(ShowtimeModel::class.java)
            }
            emit(showtimes)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getShowtimesForCinema(cinemaId: String): Flow<List<ShowtimeModel>> = flow {
        try {
            val snapshot = showtimesCollection
                .whereEqualTo("cinemaId", cinemaId)
                .whereGreaterThan("startTime", Timestamp.now())
                .get()
                .await()

            val showtimes = snapshot.documents.mapNotNull { document ->
                document.toObject(ShowtimeModel::class.java)
            }
            emit(showtimes)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getShowtimesForMovieAndDate(movieId: String, date: Date): Flow<List<ShowtimeModel>> = flow {
        try {
            Log.d(TAG, "Getting showtimes for movie: $movieId and date: $date")
            
            // Create Timestamp for the start of the day
            val calendar = java.util.Calendar.getInstance()
            calendar.time = date
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            val startOfDay = Timestamp(calendar.time)

            // Create Timestamp for the end of the day
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
            calendar.set(java.util.Calendar.MINUTE, 59)
            calendar.set(java.util.Calendar.SECOND, 59)
            val endOfDay = Timestamp(calendar.time)

            Log.d(TAG, "Querying showtimes between $startOfDay and $endOfDay")

            val snapshot = showtimesCollection
                .whereEqualTo("movieId", movieId)
                .whereGreaterThanOrEqualTo("startTime", startOfDay)
                .whereLessThanOrEqualTo("startTime", endOfDay)
                .get()
                .await()

            Log.d(TAG, "Found ${snapshot.documents.size} showtimes")

            val showtimes = snapshot.documents.mapNotNull { document ->
                try {
                    val showtime = document.toObject(ShowtimeModel::class.java)
                    Log.d(TAG, "Parsed showtime: id=${showtime?.id}, startTime=${showtime?.startTime}")
                    showtime
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing showtime from document ${document.id}: ${e.message}")
                    null
                }
            }

            Log.d(TAG, "Emitting ${showtimes.size} showtimes")
            emit(showtimes)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting showtimes: ${e.message}")
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getShowtimesForMovieAndCinema(movieId: String, cinemaId: String): Flow<List<ShowtimeModel>> = flow {
        try {
            val snapshot = showtimesCollection
                .whereEqualTo("movieId", movieId)
                .whereEqualTo("cinemaId", cinemaId)
                .whereGreaterThan("startTime", Timestamp.now())
                .get()
                .await()

            val showtimes = snapshot.documents.mapNotNull { document ->
                document.toObject(ShowtimeModel::class.java)
            }
            emit(showtimes)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getShowtimeById(showtimeId: String): Result<ShowtimeModel> = withContext(Dispatchers.IO) {
        try {
            val document = showtimesCollection.document(showtimeId).get().await()
            val showtime = document.toObject(ShowtimeModel::class.java)

            if (showtime != null) {
                Result.success(showtime)
            } else {
                Result.failure(Exception("Showtime not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}