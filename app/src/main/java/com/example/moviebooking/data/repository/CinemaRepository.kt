package com.example.moviebooking.data.repository

import com.example.moviebooking.data.model.CinemaModel
import com.example.moviebooking.data.model.ScreenModel
import com.example.moviebooking.data.model.ShowtimeModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CinemaRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val cinemasCollection = firestore.collection("cinemas")

    suspend fun getAllCinemas(): Flow<List<CinemaModel>> = flow {
        try {
            val snapshot = cinemasCollection.get().await()
            val cinemas = snapshot.documents.mapNotNull { document ->
                document.toObject(CinemaModel::class.java)
            }
            emit(cinemas)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getCinemaScreens(cinemaId: String): Flow<List<ScreenModel>> = flow {
        try {
            val screensSnapshot = cinemasCollection.document(cinemaId)
                .collection("screens")
                .get()
                .await()
            
            val screens = screensSnapshot.documents.mapNotNull { document ->
                document.toObject(ScreenModel::class.java)
            }
            emit(screens)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getScreenShowtimes(cinemaId: String, screenId: String): Flow<List<ShowtimeModel>> = flow {
        try {
            val showtimesSnapshot = cinemasCollection.document(cinemaId)
                .collection("screens")
                .document(screenId)
                .collection("showtimes")
                .get()
                .await()
            
            val showtimes = showtimesSnapshot.documents.mapNotNull { document ->
                document.toObject(ShowtimeModel::class.java)
            }
            emit(showtimes)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getCinemasForMovie(movieId: String): Flow<List<CinemaModel>> = flow {
        try {
            // Get all cinemas
            val cinemasSnapshot = cinemasCollection.get().await()
            val cinemas = mutableListOf<CinemaModel>()

            // For each cinema, check if it has showtimes for the movie
            for (cinemaDoc in cinemasSnapshot.documents) {
                val screensSnapshot = cinemaDoc.reference.collection("screens").get().await()
                
                for (screenDoc in screensSnapshot.documents) {
                    val showtimesSnapshot = screenDoc.reference.collection("showtimes")
                        .whereEqualTo("movieId", movieId)
                        .get()
                        .await()

                    if (!showtimesSnapshot.isEmpty) {
                        cinemaDoc.toObject(CinemaModel::class.java)?.let { cinema ->
                            if (!cinemas.contains(cinema)) {
                                cinemas.add(cinema)
                            }
                        }
                        break
                    }
                }
            }

            emit(cinemas)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getCinemaById(cinemaId: String): Result<CinemaModel> = withContext(Dispatchers.IO) {
        try {
            val document = cinemasCollection.document(cinemaId).get().await()
            val cinema = document.toObject(CinemaModel::class.java)

            if (cinema != null) {
                Result.success(cinema)
            } else {
                Result.failure(Exception("Cinema not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}