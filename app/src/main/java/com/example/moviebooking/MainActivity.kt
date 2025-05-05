package com.example.moviebooking

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.moviebooking.ui.theme.MovieBookingTheme
import com.example.moviebooking.ui.auth.AuthViewModel
import com.example.moviebooking.ui.navigation.AppNavigation
import com.facebook.CallbackManager
import com.google.android.gms.auth.api.signin.GoogleSignIn

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    // Google Sign In launcher
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        authViewModel.handleGoogleSignInResult(task)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MovieBookingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        startGoogleSignIn = { launchGoogleSignIn() },
                        startFacebookSignIn = { authViewModel.startFacebookSignIn(this) }
                    )
                }
            }
        }
    }

    private fun launchGoogleSignIn() {
        val signInIntent = authViewModel.getGoogleSignInIntent(this)
        googleSignInLauncher.launch(signInIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        authViewModel.handleActivityResult(requestCode, resultCode, data)
    }
}