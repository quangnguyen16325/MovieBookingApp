package com.example.moviebooking.data.repository

import com.example.moviebooking.data.model.CinemaModel
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

    suspend fun getCinemasForMovie(movieId: String): Flow<List<CinemaModel>> = flow {
        try {
            // Get showtimes for this movie
            val showtimeSnapshot = firestore.collection("showtimes")
                .whereEqualTo("movieId", movieId)
                .get()
                .await()

            // Extract unique cinema IDs
            val cinemaIds = showtimeSnapshot.documents
                .mapNotNull { it.getString("cinemaId") }
                .distinct()

            if (cinemaIds.isEmpty()) {
                emit(emptyList())
                return@flow
            }

            // Get cinemas by IDs
            val cinemasList = mutableListOf<CinemaModel>()
            cinemaIds.forEach { cinemaId ->
                val cinemaDoc = cinemasCollection.document(cinemaId).get().await()
                cinemaDoc.toObject(CinemaModel::class.java)?.let { cinemasList.add(it) }
            }

            emit(cinemasList)
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

    suspend fun getCinema(cinemaId: String): CinemaModel {
        val document = cinemasCollection.document(cinemaId).get().await()
        return document.toObject(CinemaModel::class.java) ?: throw Exception("Cinema not found")
    }
}