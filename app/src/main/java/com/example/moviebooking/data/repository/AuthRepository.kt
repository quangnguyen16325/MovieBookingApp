package com.example.moviebooking.data.repository

import android.content.Context
import android.content.Intent
import com.example.moviebooking.data.model.UserModel
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    // Facebook CallbackManager
    val facebookCallbackManager = CallbackManager.Factory.create()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun login(email: String, password: String): Result<UserModel> = withContext(Dispatchers.IO) {
        try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user

            if (user != null) {
                // Check if email is verified
                if (!user.isEmailVerified) {
                    return@withContext Result.failure(Exception("Please verify your email before logging in. Check your inbox."))
                }

                // Update last login timestamp
                usersCollection.document(user.uid)
                    .update("lastLogin", Timestamp.now())
                    .await()

                // Get user data from Firestore
                val documentSnapshot = usersCollection.document(user.uid).get().await()
                val userModel = documentSnapshot.toObject(UserModel::class.java)

                if (userModel != null) {
                    Result.success(userModel)
                } else {
                    Result.failure(Exception("Failed to get user data"))
                }
            } else {
                Result.failure(Exception("Authentication failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(email: String, password: String, fullName: String): Result<UserModel> = withContext(Dispatchers.IO) {
        try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user

            if (user != null) {
                // Send email verification
                user.sendEmailVerification().await()

                val userModel = UserModel(
                    uid = user.uid,
                    email = email,
                    fullName = fullName,
                    profileImage = "",
                    phoneNumber = "",
                    createdAt = Timestamp.now(),
                    lastLogin = Timestamp.now()
                )

                // Save user data to Firestore
                usersCollection.document(user.uid)
                    .set(userModel.toMap())
                    .await()

                Result.success(userModel)
            } else {
                Result.failure(Exception("Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun resetPassword(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resendVerificationEmail(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val user = auth.currentUser
            if (user != null) {
                if (user.isEmailVerified) {
                    return@withContext Result.failure(Exception("Email is already verified"))
                }
                user.sendEmailVerification().await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("No user is logged in"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshUser(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val user = auth.currentUser
            if (user != null) {
                user.reload().await()
                Result.success(user.isEmailVerified)
            } else {
                Result.failure(Exception("No user is logged in"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Create Google Sign In Client
    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("YOUR_WEB_CLIENT_ID") // Replace with your Web Client ID from Firebase Console
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(context, gso)
    }

    // Handle Google Sign In Result
    suspend fun firebaseAuthWithGoogle(idToken: String): Result<UserModel> = withContext(Dispatchers.IO) {
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = signInWithCredential(credential)
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Handle Facebook Sign In
    suspend fun handleFacebookAccessToken(token: AccessToken): Result<UserModel> = withContext(Dispatchers.IO) {
        try {
            val credential = FacebookAuthProvider.getCredential(token.token)
            val result = signInWithCredential(credential)
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Sign in with credential (shared for Google and Facebook)
    private suspend fun signInWithCredential(credential: AuthCredential): Result<UserModel> = withContext(Dispatchers.IO) {
        try {
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user

            if (user != null) {
                // Check if user exists in Firestore
                val documentSnapshot = usersCollection.document(user.uid).get().await()

                if (!documentSnapshot.exists()) {
                    // Create new user in Firestore
                    val displayName = user.displayName ?: ""
                    val email = user.email ?: ""
                    val photoUrl = user.photoUrl?.toString() ?: ""

                    val userModel = UserModel(
                        uid = user.uid,
                        email = email,
                        fullName = displayName,
                        profileImage = photoUrl,
                        phoneNumber = "",
                        createdAt = Timestamp.now(),
                        lastLogin = Timestamp.now()
                    )

                    // Save user data to Firestore
                    usersCollection.document(user.uid)
                        .set(userModel.toMap())
                        .await()

                    Result.success(userModel)
                } else {
                    // Update last login for existing user
                    usersCollection.document(user.uid)
                        .update("lastLogin", Timestamp.now())
                        .await()

                    // Get user data
                    val userModel = documentSnapshot.toObject(UserModel::class.java)
                    if (userModel != null) {
                        Result.success(userModel)
                    } else {
                        Result.failure(Exception("Failed to get user data"))
                    }
                }
            } else {
                Result.failure(Exception("Authentication failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Setup Facebook Login
    suspend fun signInWithFacebook(context: Context): Result<Intent> = suspendCoroutine { continuation ->
        try {
            LoginManager.getInstance().logInWithReadPermissions(
                context as androidx.activity.ComponentActivity,
                listOf("email", "public_profile")
            )

            LoginManager.getInstance().registerCallback(
                facebookCallbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(result: LoginResult) {
                        // Firebase handles the login from here
                        // We'll handle the result in the activity
                        continuation.resume(Result.success(Intent()))
                    }

                    override fun onCancel() {
                        continuation.resume(Result.failure(Exception("Facebook login cancelled")))
                    }

                    override fun onError(error: FacebookException) {
                        continuation.resumeWithException(error)
                    }
                }
            )
        } catch (e: Exception) {
            continuation.resume(Result.failure(e))
        }
    }
}