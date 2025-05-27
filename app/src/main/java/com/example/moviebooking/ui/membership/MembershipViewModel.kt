package com.example.moviebooking.ui.membership

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviebooking.data.model.MembershipLevel
import com.example.moviebooking.data.model.UserModel
import com.example.moviebooking.data.repository.MembershipRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MembershipViewModel : ViewModel() {
    private val membershipRepository = MembershipRepository()

    private val _userMembership = MutableStateFlow<UserModel?>(null)
    val userMembership: StateFlow<UserModel?> = _userMembership.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        loadUserMembershipInfo()
    }

    fun loadUserMembershipInfo() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val result = membershipRepository.getUserMembershipInfo()
                result.onSuccess { userModel ->
                    _userMembership.value = userModel
                }
                result.onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Failed to load membership info"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addPoints(amount: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            try {
                val result = membershipRepository.addPoints(amount)
                result.onSuccess { userModel ->
                    _userMembership.value = userModel
                    val pointsEarned = (amount / 10000).toInt()
                    _successMessage.value = "Successfully earned $pointsEarned points!"
                }
                result.onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Failed to add points"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getNextLevelProgress(): Pair<Int, Int> {
        val currentPoints = _userMembership.value?.membershipPoints ?: 0
        val currentLevel = _userMembership.value?.membershipLevel ?: MembershipLevel.BASIC

        val nextLevelPoints = when (currentLevel) {
            MembershipLevel.BASIC -> 200
            MembershipLevel.SILVER -> 600
            MembershipLevel.GOLD -> 1200
            MembershipLevel.DIAMOND -> Int.MAX_VALUE
            MembershipLevel.PREMIUM -> Int.MAX_VALUE
        }

        return Pair(currentPoints, nextLevelPoints)
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }
} 