package com.example.moviebooking.ui.auth

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviebooking.data.model.UserModel
import com.example.moviebooking.data.repository.AuthRepository
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _currentUser = MutableStateFlow<UserModel?>(null)
    val currentUser: StateFlow<UserModel?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    var authState by mutableStateOf(AuthState.IDLE)
        private set

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        repository.currentUser?.let { user ->
            _currentUser.value = UserModel(
                uid = user.uid,
                email = user.email ?: "",
                fullName = user.displayName ?: ""
            )
            authState = if (user.isEmailVerified) AuthState.AUTHENTICATED else AuthState.EMAIL_NOT_VERIFIED
        }
    }

    fun login(email: String, password: String) {
        _isLoading.value = true
        _errorMessage.value = null
        authState = AuthState.LOADING

        viewModelScope.launch {
            val result = repository.login(email, password)
            _isLoading.value = false

            result.onSuccess { user ->
                _currentUser.value = user
                authState = AuthState.AUTHENTICATED
            }

            result.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Login failed"
                authState = AuthState.ERROR
            }
        }
    }

    fun register(email: String, password: String, fullName: String) {
        _isLoading.value = true
        _errorMessage.value = null
        authState = AuthState.LOADING

        viewModelScope.launch {
            val result = repository.register(email, password, fullName)
            _isLoading.value = false

            result.onSuccess { user ->
                _currentUser.value = user
                _successMessage.value = "Registration successful! Please check your email to verify your account."
                authState = AuthState.EMAIL_NOT_VERIFIED
            }

            result.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Registration failed"
                authState = AuthState.ERROR
            }
        }
    }

    fun resetPassword(email: String) {
        _isLoading.value = true
        _errorMessage.value = null
        _successMessage.value = null

        viewModelScope.launch {
            val result = repository.resetPassword(email)
            _isLoading.value = false

            result.onSuccess {
                _successMessage.value = "Password reset email sent. Please check your inbox."
            }

            result.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Failed to send reset email"
            }
        }
    }

    fun resendVerificationEmail() {
        _isLoading.value = true
        _errorMessage.value = null
        _successMessage.value = null

        viewModelScope.launch {
            val result = repository.resendVerificationEmail()
            _isLoading.value = false

            result.onSuccess {
                _successMessage.value = "Verification email sent. Please check your inbox."
            }

            result.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Failed to send verification email"
            }
        }
    }

    fun refreshUserEmailVerificationStatus() {
        _isLoading.value = true

        viewModelScope.launch {
            val result = repository.refreshUser()
            _isLoading.value = false

            result.onSuccess { isVerified ->
                if (isVerified) {
                    authState = AuthState.AUTHENTICATED
                    _successMessage.value = "Email verified successfully!"
                } else {
                    authState = AuthState.EMAIL_NOT_VERIFIED
                }
            }

            result.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Failed to refresh user status"
            }
        }
    }

    fun logout() {
        repository.logout()
        _currentUser.value = null
        authState = AuthState.UNAUTHENTICATED
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    fun resetAuthState() {
        authState = if (_currentUser.value != null) {
            if (repository.currentUser?.isEmailVerified == true) {
                AuthState.AUTHENTICATED
            } else {
                AuthState.EMAIL_NOT_VERIFIED
            }
        } else {
            AuthState.UNAUTHENTICATED
        }
    }

    fun isUserLoggedIn(): Boolean {
        return repository.currentUser != null && repository.currentUser?.isEmailVerified == true
    }

    // Google Sign In
    fun getGoogleSignInIntent(context: Context): Intent {
        return repository.getGoogleSignInClient(context).signInIntent
    }

    fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        _isLoading.value = true
        _errorMessage.value = null

        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken

            if (idToken != null) {
                viewModelScope.launch {
                    val result = repository.firebaseAuthWithGoogle(idToken)
                    _isLoading.value = false

                    result.onSuccess { user ->
                        _currentUser.value = user
                        authState = AuthState.AUTHENTICATED
                        _successMessage.value = "Successfully signed in with Google!"
                    }

                    result.onFailure { exception ->
                        _errorMessage.value = exception.message ?: "Google sign in failed"
                        authState = AuthState.ERROR
                    }
                }
            } else {
                _isLoading.value = false
                _errorMessage.value = "Google sign in failed: No ID token found"
                authState = AuthState.ERROR
            }
        } catch (e: ApiException) {
            _isLoading.value = false
            _errorMessage.value = "Google sign in failed: ${e.statusCode}"
            authState = AuthState.ERROR
        }
    }

    // Facebook Sign In
    fun startFacebookSignIn(context: Context) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            val result = repository.signInWithFacebook(context)
            result.onFailure { exception ->
                _isLoading.value = false
                _errorMessage.value = exception.message ?: "Facebook sign in failed"
                authState = AuthState.ERROR
            }
        }
    }

    fun handleFacebookSignInResult(accessToken: AccessToken?) {
        if (accessToken != null) {
            viewModelScope.launch {
                val result = repository.handleFacebookAccessToken(accessToken)
                _isLoading.value = false

                result.onSuccess { user ->
                    _currentUser.value = user
                    authState = AuthState.AUTHENTICATED
                    _successMessage.value = "Successfully signed in with Facebook!"
                }

                result.onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Facebook sign in failed"
                    authState = AuthState.ERROR
                }
            }
        } else {
            _isLoading.value = false
            _errorMessage.value = "Facebook sign in failed: No access token"
            authState = AuthState.ERROR
        }
    }

    // Handle activity result
    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Pass to Facebook callback manager
        repository.facebookCallbackManager.onActivityResult(requestCode, resultCode, data)
    }

    enum class AuthState {
        IDLE,
        LOADING,
        AUTHENTICATED,
        UNAUTHENTICATED,
        EMAIL_NOT_VERIFIED,
        ERROR
    }
}