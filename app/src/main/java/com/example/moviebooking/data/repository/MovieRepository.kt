package com.example.moviebooking.data.repository

import android.util.Log
import com.example.moviebooking.data.model.MovieModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MovieRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val moviesCollection = firestore.collection("movies")

//    suspend fun getNowShowingMovies(): Flow<List<MovieModel>> = flow {
//        try {
//            val snapshot = moviesCollection
//                .whereEqualTo("isNowShowing", true)
//                .get()
//                .await()
//
//            val movies = snapshot.documents.mapNotNull { document ->
//                document.toObject(MovieModel::class.java)
//            }
//            emit(movies)
//        } catch (e: Exception) {
//            emit(emptyList())
//        }
//    }.flowOn(Dispatchers.IO)
    suspend fun getNowShowingMovies(): Flow<List<MovieModel>> = flow {
        Log.d("MovieRepository", "Getting now showing movies")
        try {
            val snapshot = moviesCollection
                .whereEqualTo("isNowShowing", true)
                .get()
                .await()

            Log.d("MovieRepository", "Got ${snapshot.documents.size} documents")

            val movies = snapshot.documents.mapNotNull { document ->
                try {
                    val movie = document.toObject(MovieModel::class.java)
                    Log.d("MovieRepository", "Parsed movie: ${movie?.title}, id: ${movie?.id}")
                    movie
                } catch (e: Exception) {
                    Log.e("MovieRepository", "Error parsing movie from document ${document.id}: ${e.message}", e)
                    null
                }
            }

            Log.d("MovieRepository", "Emitting ${movies.size} movies")
            emit(movies)
        } catch (e: Exception) {
            Log.e("MovieRepository", "Error getting now showing movies: ${e.message}", e)
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getComingSoonMovies(): Flow<List<MovieModel>> = flow {
        try {
            val snapshot = moviesCollection
                .whereEqualTo("isComingSoon", true)
                .get()
                .await()

            val movies = snapshot.documents.mapNotNull { document ->
                document.toObject(MovieModel::class.java)
            }
            emit(movies)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getMovieById(movieId: String): Result<MovieModel> = withContext(Dispatchers.IO) {
        try {
            val document = moviesCollection.document(movieId).get().await()
            val movie = document.toObject(MovieModel::class.java)

            if (movie != null) {
                Result.success(movie)
            } else {
                Result.failure(Exception("Movie not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchMovies(query: String): Flow<List<MovieModel>> = flow {
        try {
            val snapshot = moviesCollection
                .whereGreaterThanOrEqualTo("title", query)
                .whereLessThanOrEqualTo("title", query + '\uf8ff')
                .get()
                .await()

            val movies = snapshot.documents.mapNotNull { document ->
                document.toObject(MovieModel::class.java)
            }
            emit(movies)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)
}