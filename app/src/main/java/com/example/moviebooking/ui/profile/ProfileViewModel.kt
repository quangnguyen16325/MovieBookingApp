package com.example.moviebooking.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviebooking.data.model.UserModel
import com.example.moviebooking.data.repository.AuthRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class ProfileViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _userProfile = MutableStateFlow<UserModel?>(null)
    val userProfile: StateFlow<UserModel?> = _userProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val currentUser = authRepository.currentUser
                if (currentUser != null) {
                    val userProfileResult = authRepository.getUserProfile(currentUser.uid)

                    userProfileResult.onSuccess { userModel ->
                        _userProfile.value = userModel
                    }

                    userProfileResult.onFailure { exception ->
                        _errorMessage.value = exception.message ?: "Failed to load user profile"
                    }
                } else {
                    _errorMessage.value = "User not authenticated"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(fullName: String, phoneNumber: String) {
        if (fullName.isBlank()) {
            _errorMessage.value = "Full name cannot be empty"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val currentUserProfile = _userProfile.value ?: return@launch

                val updatedProfile = currentUserProfile.copy(
                    fullName = fullName,
                    phoneNumber = phoneNumber
                )

                val result = authRepository.updateUserProfile(updatedProfile)

                result.onSuccess {
                    _userProfile.value = updatedProfile
                    _successMessage.value = "Profile updated successfully"
                }

                result.onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Failed to update profile"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        // Validate inputs
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            _errorMessage.value = "All fields are required"
            return
        }

        if (newPassword != confirmPassword) {
            _errorMessage.value = "New password and confirm password do not match"
            return
        }

        if (newPassword.length < 6) {
            _errorMessage.value = "Password must be at least 6 characters long"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val result = authRepository.changePassword(currentPassword, newPassword)

                result.onSuccess {
                    _successMessage.value = "Password changed successfully"
                }

                result.onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Failed to change password"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.logout()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to logout"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    fun formatDate(timestamp: Timestamp?): String {
        if (timestamp == null) return "N/A"
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        return sdf.format(timestamp.toDate())
    }
}