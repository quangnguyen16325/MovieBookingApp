package com.example.moviebooking.ui.admin.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviebooking.data.model.UserModel
import com.example.moviebooking.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminUsersViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _users = MutableStateFlow<List<UserModel>>(emptyList())
    val users: StateFlow<List<UserModel>> = _users.asStateFlow()

    private val _filteredUsers = MutableStateFlow<List<UserModel>>(emptyList())
    val filteredUsers: StateFlow<List<UserModel>> = _filteredUsers.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _currentUser = MutableStateFlow<UserModel?>(null)
    val currentUser: StateFlow<UserModel?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val result = authRepository.getAllUsers()
                result.onSuccess { userList ->
                    _users.value = userList
                    filterUsers(_searchQuery.value)
                }
                result.onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Failed to load users"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        filterUsers(query)
    }

    private fun filterUsers(query: String) {
        if (query.isBlank()) {
            _filteredUsers.value = _users.value
            return
        }

        val filteredList = _users.value.filter { user ->
            user.fullName.contains(query, ignoreCase = true) ||
            user.email.contains(query, ignoreCase = true) ||
            (user.phoneNumber?.contains(query, ignoreCase = true) ?: false)
        }
        _filteredUsers.value = filteredList
    }

    fun loadUser(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val result = authRepository.getUserProfile(userId)
                result.onSuccess { user ->
                    _currentUser.value = user
                }
                result.onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Failed to load user"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateUser(user: UserModel) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val result = authRepository.updateUserByAdmin(user.uid, user)
                result.onSuccess {
                    _successMessage.value = "User updated successfully"
                    loadUsers() // Reload the list
                }
                result.onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Failed to update user"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val result = authRepository.deleteUser(userId)
                result.onSuccess {
                    _successMessage.value = "User deleted successfully"
                    loadUsers() // Reload the list
                }
                result.onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Failed to delete user"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }
} 