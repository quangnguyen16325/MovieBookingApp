package com.example.moviebooking.ui.auth

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moviebooking.data.model.UserModel
import com.example.moviebooking.data.repository.AuthRepository
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.concurrent.TimeUnit

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    // Khởi tạo các phụ thuộc trong constructor
    private val appContext = application.applicationContext
    private val repository = AuthRepository()

    // SharedPreferences for persisting user data
    private val sharedPreferences: SharedPreferences =
        appContext.getSharedPreferences("cine_ai_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<UserModel?>(null)
    val currentUser: StateFlow<UserModel?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // Whether email needs verification
    private val _needsEmailVerification = MutableStateFlow(false)
    val needsEmailVerification: StateFlow<Boolean> = _needsEmailVerification.asStateFlow()

//    private val _isEmailVerified = MutableStateFlow(false)
//    val isEmailVerified: StateFlow<Boolean> = _isEmailVerified.asStateFlow()
//
//    private val _isPhoneVerified = MutableStateFlow(false)
//    val isPhoneVerified: StateFlow<Boolean> = _isPhoneVerified.asStateFlow()

    var authState by mutableStateOf(AuthState.IDLE)
        private set

    // SharedPreferences keys
    private companion object {
        const val KEY_USER_ID = "user_id"
        const val KEY_USER_EMAIL = "user_email"
        const val KEY_USER_FULL_NAME = "user_full_name"
        const val KEY_USER_PROFILE_IMAGE = "user_profile_image"
        const val KEY_USER_PHONE_NUMBER = "user_phone_number"
        const val KEY_USER_CREATED_AT = "user_created_at"
        const val KEY_USER_LAST_LOGIN = "user_last_login"
        const val KEY_IS_LOGGED_IN = "is_logged_in"
        const val KEY_EMAIL_VERIFIED = "email_verified"
    }

    init {
        // First check SharedPreferences for cached user data
        loadUserFromPreferences()

        // Then check Firebase auth state
        checkCurrentUser()

//        // Check verification status
//        checkVerificationStatus()
    }

//    // Kiểm tra trạng thái xác thực
//    private fun checkVerificationStatus() {
//        viewModelScope.launch {
//            val user = repository.currentUser
//            if (user != null) {
//                // Email được xác thực qua Firebase Auth
//                _isEmailVerified.value = user.isEmailVerified
//
//                // Kiểm tra xác thực số điện thoại từ Firestore
//                try {
//                    val db = FirebaseFirestore.getInstance()
//                    val document = db.collection("users").document(user.uid).get().await()
//                    _isPhoneVerified.value = document.getBoolean("phoneVerified") ?: false
//                } catch (e: Exception) {
//                    Log.e("AuthViewModel", "Error checking phone verification status", e)
//                    _isPhoneVerified.value = false
//                }
//            } else {
//                _isEmailVerified.value = false
//                _isPhoneVerified.value = false
//            }
//        }
//    }

    private fun loadUserFromPreferences() {
        val isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
        val isEmailVerified = sharedPreferences.getBoolean(KEY_EMAIL_VERIFIED, false)

        if (isLoggedIn) {
            try {
                val uid = sharedPreferences.getString(KEY_USER_ID, "") ?: ""
                val email = sharedPreferences.getString(KEY_USER_EMAIL, "") ?: ""
                val fullName = sharedPreferences.getString(KEY_USER_FULL_NAME, "") ?: ""

                // Xử lý profileImage và phoneNumber có thể null
                val profileImage = sharedPreferences.getString(KEY_USER_PROFILE_IMAGE, null)
                val phoneNumber = sharedPreferences.getString(KEY_USER_PHONE_NUMBER, null)

                val createdAtMillis = sharedPreferences.getLong(KEY_USER_CREATED_AT, 0)
                val lastLoginMillis = sharedPreferences.getLong(KEY_USER_LAST_LOGIN, System.currentTimeMillis())

                // Chuyển đổi Long timestamps thành Firebase Timestamp
                val createdAt = if (createdAtMillis > 0) Timestamp(Date(createdAtMillis)) else null
                val lastLogin = Timestamp(Date(lastLoginMillis)) // Sử dụng thời gian hiện tại nếu không có

                _currentUser.value = UserModel(
                    uid = uid,
                    email = email,
                    fullName = fullName,
                    profileImage = profileImage,
                    phoneNumber = phoneNumber,
                    createdAt = createdAt,
                    lastLogin = lastLogin
                )

                authState = if (isEmailVerified) AuthState.AUTHENTICATED else AuthState.EMAIL_NOT_VERIFIED
                _needsEmailVerification.value = !isEmailVerified
            } catch (e: Exception) {
                _errorMessage.value = "Error restoring user session: ${e.message}"
                clearUserFromPreferences()
                authState = AuthState.UNAUTHENTICATED
            }
        }
    }

    private fun checkCurrentUser() {
        repository.currentUser?.let { user ->
            try {
                // Kiểm tra và lấy URL ảnh đại diện từ Firebase User
                val profileImageUrl = user.photoUrl?.toString()

                // Lấy thời gian hiện tại (2025-05-06 09:55:37 UTC)
                val currentTime = Timestamp(Date())

                // Firebase user to UserModel conversion
                val userModel = UserModel(
                    uid = user.uid,
                    email = user.email ?: "",
                    fullName = user.displayName ?: "",
                    profileImage = profileImageUrl, // Đảm bảo lưu URL ảnh đại diện
                    phoneNumber = user.phoneNumber,
                    createdAt = null,
                    lastLogin = currentTime
                )

                // Nếu không có profileImage từ Firebase User, thì kiểm tra từ Firestore
                if (profileImageUrl.isNullOrEmpty()) {
                    fetchUserProfileFromFirestore(user.uid)
                } else {
                    _currentUser.value = userModel

                    // Update auth state based on email verification
                    val isEmailVerified = user.isEmailVerified
                    authState = if (isEmailVerified) AuthState.AUTHENTICATED else AuthState.EMAIL_NOT_VERIFIED
                    _needsEmailVerification.value = !isEmailVerified

                    // Save updated user data to preferences
                    saveUserToPreferences(userModel, isEmailVerified)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error retrieving user data: ${e.message}"
                Log.e("AuthViewModel", "Error retrieving user data", e)
            }
        } ?: run {
            // Nếu không có người dùng Firebase hiện tại
            if (_currentUser.value == null) {
                authState = AuthState.UNAUTHENTICATED
            }
        }
    }

    // Lấy thông tin người dùng từ Firestore
    private fun fetchUserProfileFromFirestore(userId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Lấy dữ liệu người dùng từ Firestore
                val db = FirebaseFirestore.getInstance()
                val documentSnapshot = db.collection("users").document(userId).get().await()

                if (documentSnapshot.exists()) {
                    val userData = documentSnapshot.data
                    val profileImage = userData?.get("profileImage") as? String
                    val phoneNumber = userData?.get("phoneNumber") as? String
                    val fullName = userData?.get("fullName") as? String ?: repository.currentUser?.displayName ?: ""
                    val email = userData?.get("email") as? String ?: repository.currentUser?.email ?: ""
                    val createdAt = userData?.get("createdAt") as? Timestamp
                    val lastLogin = Timestamp.now() // Cập nhật thời gian đăng nhập

                    val userModel = UserModel(
                        uid = userId,
                        email = email,
                        fullName = fullName,
                        profileImage = profileImage,
                        phoneNumber = phoneNumber,
                        createdAt = createdAt,
                        lastLogin = lastLogin
                    )

                    _currentUser.value = userModel

                    // Cập nhật trạng thái xác thực email
                    val isEmailVerified = repository.currentUser?.isEmailVerified == true
                    authState = if (isEmailVerified) AuthState.AUTHENTICATED else AuthState.EMAIL_NOT_VERIFIED
                    _needsEmailVerification.value = !isEmailVerified

                    // Lưu dữ liệu vào SharedPreferences
                    saveUserToPreferences(userModel, isEmailVerified)

                    // Cập nhật lastLogin trong Firestore
                    val updates = hashMapOf<String, Any>(
                        "lastLogin" to lastLogin
                    )
                    db.collection("users").document(userId).update(updates)
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error fetching user data from Firestore", e)
            } finally {
                _isLoading.value = false
            }
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
                // Cập nhật thời gian đăng nhập gần đây
                val updatedUser = user.copy(
                    lastLogin = Timestamp(Date()) // 2025-05-06 09:31:28 UTC
                )
                _currentUser.value = updatedUser

                val isEmailVerified = repository.currentUser?.isEmailVerified == true
                authState = if (isEmailVerified) AuthState.AUTHENTICATED else AuthState.EMAIL_NOT_VERIFIED
                _needsEmailVerification.value = !isEmailVerified

                // Save user data to preferences
                saveUserToPreferences(updatedUser, isEmailVerified)
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
                // Đảm bảo thời gian tạo và đăng nhập gần đây được thiết lập
                val currentTime = Timestamp(Date()) // 2025-05-06 09:31:28 UTC
                val updatedUser = user.copy(
                    createdAt = currentTime,
                    lastLogin = currentTime
                )
                _currentUser.value = updatedUser

                _successMessage.value = "Registration successful! Please check your email to verify your account."
                authState = AuthState.EMAIL_NOT_VERIFIED
                _needsEmailVerification.value = true

                // Save user data to preferences (not verified yet)
                saveUserToPreferences(updatedUser, false)
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
//                _isEmailVerified.value = isVerified

                if (isVerified) {
                    authState = AuthState.AUTHENTICATED
                    _needsEmailVerification.value = false
                    _successMessage.value = "Email verified successfully!"

                    // Update email verification status in preferences
                    _currentUser.value?.let { user ->
                        saveUserToPreferences(user, true)
                    }
                } else {
                    authState = AuthState.EMAIL_NOT_VERIFIED
                    _needsEmailVerification.value = true
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
        _needsEmailVerification.value = false
        authState = AuthState.UNAUTHENTICATED

        // Clear user data from preferences
        clearUserFromPreferences()
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
                        // Đảm bảo cập nhật lastLogin
                        val updatedUser = user.copy(lastLogin = Timestamp(Date())) // 2025-05-06 09:31:28 UTC
                        _currentUser.value = updatedUser

                        authState = AuthState.AUTHENTICATED
                        _successMessage.value = "Successfully signed in with Google!"

                        // Save user data to preferences (Google auth is auto-verified)
                        saveUserToPreferences(updatedUser, true)
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
                    // Đảm bảo cập nhật lastLogin
                    val updatedUser = user.copy(lastLogin = Timestamp(Date())) // 2025-05-06 09:31:28 UTC
                    _currentUser.value = updatedUser

                    authState = AuthState.AUTHENTICATED
                    _successMessage.value = "Successfully signed in with Facebook!"

                    // Save user data to preferences (Facebook auth is auto-verified)
                    saveUserToPreferences(updatedUser, true)
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

    // Save user data to SharedPreferences
    private fun saveUserToPreferences(user: UserModel, isEmailVerified: Boolean) {
        sharedPreferences.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putBoolean(KEY_EMAIL_VERIFIED, isEmailVerified)
            putString(KEY_USER_ID, user.uid)
            putString(KEY_USER_EMAIL, user.email)
            putString(KEY_USER_FULL_NAME, user.fullName)

            putString(KEY_USER_PROFILE_IMAGE, user.profileImage)
            putString(KEY_USER_PHONE_NUMBER, user.phoneNumber)

            // Lưu thời gian dưới dạng long milliseconds
            user.createdAt?.let { timestamp ->
                putLong(KEY_USER_CREATED_AT, timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000)
            } ?: run {
                remove(KEY_USER_CREATED_AT) // Nếu không có dữ liệu, xóa khỏi SharedPreferences
            }

            user.lastLogin?.let { timestamp ->
                putLong(KEY_USER_LAST_LOGIN, timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000)
            } ?: run {
                // Nếu không có lastLogin, dùng thời gian hiện tại
                putLong(KEY_USER_LAST_LOGIN, System.currentTimeMillis())
            }

            apply()
        }
    }

    // Clear user data from SharedPreferences
    private fun clearUserFromPreferences() {
        sharedPreferences.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, false)
            putBoolean(KEY_EMAIL_VERIFIED, false)
            remove(KEY_USER_ID)
            remove(KEY_USER_EMAIL)
            remove(KEY_USER_FULL_NAME)
            remove(KEY_USER_PROFILE_IMAGE)
            remove(KEY_USER_PHONE_NUMBER)
            remove(KEY_USER_CREATED_AT)
            remove(KEY_USER_LAST_LOGIN)
            apply()
        }
    }

    // Update profile image
    fun updateProfileImage(imageUrl: String) {
        val currentUserValue = _currentUser.value
        if (currentUserValue != null) {
            val updatedUser = currentUserValue.copy(profileImage = imageUrl)
            _currentUser.value = updatedUser

            // Cập nhật trong SharedPreferences
            val isEmailVerified = repository.currentUser?.isEmailVerified == true
            saveUserToPreferences(updatedUser, isEmailVerified)

            // Cập nhật trong Firestore
            viewModelScope.launch {
                try {
                    val db = FirebaseFirestore.getInstance()
                    db.collection("users").document(currentUserValue.uid)
                        .update("profileImage", imageUrl)
                } catch (e: Exception) {
                    Log.e("AuthViewModel", "Error updating profile image in Firestore", e)
                }
            }
        }
    }

//    // Gửi mã xác thực số điện thoại
//    fun sendPhoneVerificationCode(phoneNumber: String, activity: Activity, callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks) {
//        _isLoading.value = true
//
//        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
//            .setPhoneNumber(phoneNumber)
//            .setTimeout(60L, TimeUnit.SECONDS)
//            .setActivity(activity)
//            .setCallbacks(callbacks)
//            .build()
//
//        PhoneAuthProvider.verifyPhoneNumber(options)
//    }
//
//    // Xác nhận mã OTP
//    fun verifyPhoneWithCode(verificationId: String, code: String) {
//        _isLoading.value = true
//
//        viewModelScope.launch {
//            try {
//                val credential = PhoneAuthProvider.getCredential(verificationId, code)
//                val currentUser = FirebaseAuth.getInstance().currentUser
//
//                currentUser?.linkWithCredential(credential)?.await()
//
//                // Cập nhật trạng thái xác thực trong Firestore
//                val db = FirebaseFirestore.getInstance()
//                db.collection("users").document(currentUser.uid)
//                    .update("phoneVerified", true)
//                    .await()
//
//                _isPhoneVerified.value = true
//                _successMessage.value = "Phone number verified successfully!"
//                _isLoading.value = false
//
//                // Cập nhật currentUser để phản ánh số điện thoại mới
//                _currentUser.value?.let { user ->
//                    val updatedUser = user.copy(phoneNumber = currentUser.phoneNumber)
//                    _currentUser.value = updatedUser
//                    saveUserToPreferences(updatedUser, _isEmailVerified.value)
//                }
//            } catch (e: Exception) {
//                _errorMessage.value = "Failed to verify phone: ${e.message}"
//                _isLoading.value = false
//                Log.e("AuthViewModel", "Phone verification failed", e)
//            }
//        }
//    }
//
//    // Cập nhật trạng thái xác thực số điện thoại
//    fun updatePhoneVerificationStatus(isVerified: Boolean) {
//        _isPhoneVerified.value = isVerified
//
//        viewModelScope.launch {
//            try {
//                val uid = repository.currentUser?.uid ?: return@launch
//                val db = FirebaseFirestore.getInstance()
//                db.collection("users").document(uid)
//                    .update("phoneVerified", isVerified)
//                    .await()
//            } catch (e: Exception) {
//                Log.e("AuthViewModel", "Error updating phone verification status", e)
//            }
//        }
//    }

    enum class AuthState {
        IDLE,
        LOADING,
        AUTHENTICATED,
        UNAUTHENTICATED,
        EMAIL_NOT_VERIFIED,
        ERROR
    }

}