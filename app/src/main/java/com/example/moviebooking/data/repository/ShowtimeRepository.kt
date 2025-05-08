package com.example.moviebooking.data.repository

import android.util.Log
import com.example.moviebooking.data.model.ShowtimeModel
import com.example.moviebooking.data.model.SeatModel
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

    suspend fun getShowtimesForMovie(movieId: String): Flow<List<ShowtimeModel>> = flow {
        try {
            // Get all cinemas
            val cinemasSnapshot = firestore.collection("cinemas").get().await()
            val showtimes = mutableListOf<ShowtimeModel>()

            // For each cinema, get showtimes for the movie
            for (cinemaDoc in cinemasSnapshot.documents) {
                val screensSnapshot = cinemaDoc.reference.collection("screens").get().await()
                
                for (screenDoc in screensSnapshot.documents) {
                    val showtimesSnapshot = screenDoc.reference.collection("showtimes")
                        .whereEqualTo("movieId", movieId)
                        .whereGreaterThan("startTime", Timestamp.now())
                        .get()
                        .await()

                    showtimes.addAll(showtimesSnapshot.documents.mapNotNull { document ->
                        document.toObject(ShowtimeModel::class.java)
                    })
                }
            }

            emit(showtimes)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getShowtimesForCinema(cinemaId: String): Flow<List<ShowtimeModel>> = flow {
        try {
            val screensSnapshot = firestore.collection("cinemas")
                .document(cinemaId)
                .collection("screens")
                .get()
                .await()

            val showtimes = mutableListOf<ShowtimeModel>()

            for (screenDoc in screensSnapshot.documents) {
                val showtimesSnapshot = screenDoc.reference.collection("showtimes")
                    .whereGreaterThan("startTime", Timestamp.now())
                    .get()
                    .await()

                showtimes.addAll(showtimesSnapshot.documents.mapNotNull { document ->
                    document.toObject(ShowtimeModel::class.java)
                })
            }

            emit(showtimes)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getShowtimeSeats(cinemaId: String, screenId: String, showtimeId: String): Flow<List<SeatModel>> = flow {
        try {
            val seatsSnapshot = firestore.collection("cinemas")
                .document(cinemaId)
                .collection("screens")
                .document(screenId)
                .collection("showtimes")
                .document(showtimeId)
                .collection("seats")
                .get()
                .await()

            val seats = seatsSnapshot.documents.mapNotNull { document ->
                document.toObject(SeatModel::class.java)
            }
            emit(seats)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)
}