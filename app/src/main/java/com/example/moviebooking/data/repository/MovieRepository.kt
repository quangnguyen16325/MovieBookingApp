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
    private val TAG = "MovieRepository"
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
        Log.d(TAG, "Getting now showing movies")
        try {
            val snapshot = moviesCollection
                .whereEqualTo("isNowShowing", true)
                .get()
                .await()

            Log.d(TAG, "Got ${snapshot.documents.size} documents")

            val movies = snapshot.documents.mapNotNull { document ->
                try {
                    val movie = MovieModel.fromDocument(document)
                    Log.d(TAG, "Parsed movie: ${movie?.title}, id: ${movie?.id}")
                    movie
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing movie from document ${document.id}: ${e.message}", e)
                    null
                }
            }

            Log.d(TAG, "Emitting ${movies.size} movies")
            emit(movies)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting now showing movies: ${e.message}", e)
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
                MovieModel.fromDocument(document)
            }
            emit(movies)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getMovieById(movieId: String): Result<MovieModel> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Getting movie by ID: $movieId")
            val document = moviesCollection.document(movieId).get().await()
            Log.d(TAG, "Document data: ${document.data}")

            val movie = MovieModel.fromDocument(document)
            if (movie != null) {
                Log.d(TAG, "Successfully loaded movie: ${movie.title}")
                Log.d(TAG, "isNowShowing: ${movie.isNowShowing}")
                Log.d(TAG, "isComingSoon: ${movie.isComingSoon}")
                Result.success(movie)
            } else {
                Log.e(TAG, "Movie not found")
                Result.failure(Exception("Movie not found"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting movie: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun searchMovies(query: String): Flow<List<MovieModel>> = flow {
        try {
            Log.d("MovieRepository", "Searching for: $query")
            
            // Lấy tất cả phim
            val snapshot = moviesCollection
                .get()
                .await()

            // Lọc phim theo tiêu đề và thể loại
            val movies = snapshot.documents.mapNotNull { document ->
                document.toObject(MovieModel::class.java)
            }.filter { movie ->
                // Tìm kiếm theo tiêu đề (không phân biệt chữ hoa/thường)
                movie.title.contains(query, ignoreCase = true) ||
                // Tìm kiếm theo thể loại (không phân biệt chữ hoa/thường)
                movie.genres.any { genre ->
                    genre.contains(query, ignoreCase = true)
                }
            }

            Log.d("MovieRepository", "Found ${movies.size} movies")
            emit(movies)
        } catch (e: Exception) {
            Log.e("MovieRepository", "Error searching movies: ${e.message}", e)
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    suspend fun addMovie(movie: MovieModel): Result<MovieModel> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Adding new movie: ${movie.title}")
            Log.d(TAG, "isNowShowing: ${movie.isNowShowing}")
            Log.d(TAG, "isComingSoon: ${movie.isComingSoon}")
            
            val documentRef = moviesCollection.document()
            val newMovie = movie.copy(id = documentRef.id)
            documentRef.set(newMovie.toMap()).await()
            Result.success(newMovie)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding movie: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updateMovie(movie: MovieModel): Result<MovieModel> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Updating movie: ${movie.title}")
            Log.d(TAG, "isNowShowing: ${movie.isNowShowing}")
            Log.d(TAG, "isComingSoon: ${movie.isComingSoon}")
            
            moviesCollection.document(movie.id).update(movie.toMap()).await()
            Result.success(movie)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating movie: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteMovie(movieId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Deleting movie with ID: $movieId")
            moviesCollection.document(movieId).delete().await()
            Log.d(TAG, "Movie deleted successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting movie: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getAllMovies(): Flow<List<MovieModel>> = flow {
        try {
            val snapshot = moviesCollection.get().await()
            val movies = snapshot.documents.mapNotNull { document ->
                MovieModel.fromDocument(document)
            }
            emit(movies)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getMovie(movieId: String): MovieModel {
        val document = moviesCollection.document(movieId).get().await()
        return document.toObject(MovieModel::class.java) ?: throw Exception("Movie not found")
    }
}