package com.example.moviebooking.ui.admin.cinema

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moviebooking.data.model.CinemaModel
import com.example.moviebooking.data.model.GeoLocation
import com.example.moviebooking.data.repository.CinemaRepository
import com.example.moviebooking.data.service.CloudinaryService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class CinemaFormState {
    object Loading : CinemaFormState()
    object Success : CinemaFormState()
    data class Error(val message: String) : CinemaFormState()
}

class AdminCinemaFormViewModel(
    private val cinemaId: String?,
    private val context: Context
) : ViewModel() {
    private val TAG = "AdminCinemaFormViewModel"
    private val cinemaRepository = CinemaRepository()
    private val cloudinaryService = CloudinaryService.getInstance(context)
    private val firestore = FirebaseFirestore.getInstance()

    private val _cinema = MutableStateFlow<CinemaModel?>(null)
    val cinema: StateFlow<CinemaModel?> = _cinema.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _saveResult = MutableStateFlow<Result<CinemaModel>?>(null)
    val saveResult: StateFlow<Result<CinemaModel>?> = _saveResult.asStateFlow()

    private val _imageUrl = MutableStateFlow<String?>(null)
    val imageUrl: StateFlow<String?> = _imageUrl.asStateFlow()

    private val _formState = MutableStateFlow<CinemaFormState>(CinemaFormState.Loading)
    val formState: StateFlow<CinemaFormState> = _formState.asStateFlow()

    init {
        if (cinemaId != null && cinemaId != "new") {
            loadCinema()
        } else {
            _formState.value = CinemaFormState.Success
        }
    }

    private fun loadCinema() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _formState.value = CinemaFormState.Loading

            try {
                Log.d(TAG, "Loading cinema with ID: $cinemaId")
                val result = cinemaRepository.getCinemaById(cinemaId!!)
                result.onSuccess { cinemaData ->
                    Log.d(TAG, "Cinema loaded successfully: ${cinemaData.name}")
                    _cinema.value = cinemaData
                    _imageUrl.value = cinemaData.imageUrl
                    _formState.value = CinemaFormState.Success
                }
                result.onFailure { exception ->
                    Log.e(TAG, "Failed to load cinema: ${exception.message}")
                    _errorMessage.value = exception.message ?: "Failed to load cinema"
                    _formState.value =
                        CinemaFormState.Error(exception.message ?: "Failed to load cinema")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading cinema: ${e.message}")
                _errorMessage.value = e.message ?: "An unexpected error occurred"
                _formState.value =
                    CinemaFormState.Error(e.message ?: "An unexpected error occurred")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun uploadImage(uri: Uri) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                _formState.value = CinemaFormState.Loading
                val url = cloudinaryService.uploadImage(uri, "MovieBooking/cinema")
                _imageUrl.value = url
                _formState.value = CinemaFormState.Success
            } catch (e: Exception) {
                _errorMessage.value = "Failed to upload image: ${e.message}"
                _formState.value = CinemaFormState.Error("Failed to upload image: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveCinema(
        name: String,
        address: String,
        city: String,
        numberOfScreens: Int,
        facilities: List<String>,
        latitude: Double,
        longitude: Double
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _formState.value = CinemaFormState.Loading

            try {
                Log.d(TAG, "Saving cinema with ID: $cinemaId")

                val imageUrl = _imageUrl.value
                if (imageUrl == null) {
                    _errorMessage.value = "Please upload an image"
                    _formState.value = CinemaFormState.Error("Please upload an image")
                    return@launch
                }

                val cinema = CinemaModel(
                    id = cinemaId ?: "",
                    name = name,
                    address = address,
                    city = city,
                    imageUrl = imageUrl,
                    facilities = facilities,
                    numberOfScreens = numberOfScreens,
                    location = GeoLocation(latitude, longitude)
                )

                val result = if (cinemaId == null || cinemaId == "new") {
                    Log.d(TAG, "Adding new cinema")
                    val newCinemaRef = firestore.collection("cinemas").document()
                    cinema.copy(id = newCinemaRef.id).let { newCinema ->
                        newCinemaRef.set(newCinema.toMap()).await()
                        // Create screens subcollection
                        for (i in 1..numberOfScreens) {
                            newCinemaRef.collection("screens").document(i.toString())
                                .set(mapOf(
                                    "screenNumber" to i,
                                    "totalSeats" to 100, // Default value
                                    "availableSeats" to 100 // Default value
                                )).await()
                        }
                        Result.success(newCinema)
                    }
                } else {
                    Log.d(TAG, "Updating existing cinema")
                    val cinemaRef = firestore.collection("cinemas").document(cinemaId)
                    val oldCinema = cinemaRef.get().await().toObject(CinemaModel::class.java)
                    
                    if (oldCinema != null && oldCinema.numberOfScreens != numberOfScreens) {
                        // Delete old screens
                        val oldScreens = cinemaRef.collection("screens").get().await()
                        for (screen in oldScreens.documents) {
                            screen.reference.delete().await()
                        }
                        
                        // Create new screens
                        for (i in 1..numberOfScreens) {
                            cinemaRef.collection("screens").document(i.toString())
                                .set(mapOf(
                                    "screenNumber" to i,
                                    "totalSeats" to 100, // Default value
                                    "availableSeats" to 100 // Default value
                                )).await()
                        }
                    }
                    
                    cinemaRef.set(cinema.toMap()).await()
                    Result.success(cinema)
                }

                result.onSuccess {
                    Log.d(TAG, "Cinema saved successfully")
                    _formState.value = CinemaFormState.Success
                }
                result.onFailure {
                    Log.e(TAG, "Failed to save cinema: ${it.message}")
                    _formState.value = CinemaFormState.Error(it.message ?: "Failed to save cinema")
                }

                _saveResult.value = result
            } catch (e: Exception) {
                Log.e(TAG, "Error saving cinema: ${e.message}")
                _errorMessage.value = e.message ?: "An unexpected error occurred"
                _formState.value =
                    CinemaFormState.Error(e.message ?: "An unexpected error occurred")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
        _formState.value = CinemaFormState.Success
    }

    class Factory(private val cinemaId: String?, private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AdminCinemaFormViewModel::class.java)) {
                return AdminCinemaFormViewModel(cinemaId, context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 