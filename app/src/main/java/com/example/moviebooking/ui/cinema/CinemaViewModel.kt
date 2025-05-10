package com.example.moviebooking.ui.cinema

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviebooking.data.model.CinemaModel
import com.example.moviebooking.data.repository.CinemaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class CinemaViewModel : ViewModel() {
    private val cinemaRepository = CinemaRepository()

    private val _cinemas = MutableStateFlow<List<CinemaModel>>(emptyList())
    val cinemas: StateFlow<List<CinemaModel>> = _cinemas.asStateFlow()

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
                        _isLoading.value = false
                    }
                    .collect { cinemasList ->
                        _cinemas.value = cinemasList
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
} 