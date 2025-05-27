package com.example.moviebooking.ui.admin.cinema

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviebooking.data.model.CinemaModel
import com.example.moviebooking.data.repository.CinemaRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminCinemasViewModel : ViewModel() {
    private val cinemaRepository = CinemaRepository()
    private val firestore = FirebaseFirestore.getInstance()

    private val _cinemas = MutableStateFlow<List<CinemaModel>>(emptyList())
    val cinemas: StateFlow<List<CinemaModel>> = _cinemas.asStateFlow()

    private val _filteredCinemas = MutableStateFlow<List<CinemaModel>>(emptyList())
    val filteredCinemas: StateFlow<List<CinemaModel>> = _filteredCinemas.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadCinemas()
    }

    fun loadCinemas() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                cinemaRepository.getAllCinemas()
                    .catch { e ->
                        _errorMessage.value = e.message ?: "Failed to load cinemas"
                    }
                    .collectLatest { cinemaList ->
                        _cinemas.value = cinemaList
                        _filteredCinemas.value = cinemaList
                    }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchCinemas(query: String) {
        if (query.isBlank()) {
            _filteredCinemas.value = _cinemas.value
            return
        }

        val searchQuery = query.lowercase()
        _filteredCinemas.value = _cinemas.value.filter { cinema ->
            cinema.name.lowercase().contains(searchQuery) ||
            cinema.address.lowercase().contains(searchQuery)
        }
    }

    fun deleteCinema(cinemaId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // Delete screens subcollection first
                val screensRef = firestore.collection("cinemas").document(cinemaId).collection("screens")
                val screens = screensRef.get().await()
                for (screen in screens.documents) {
                    screen.reference.delete().await()
                }

                // Delete cinema document
                firestore.collection("cinemas").document(cinemaId).delete().await()
                
                // Reload cinemas after successful deletion
                loadCinemas()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred while deleting cinema"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
} 