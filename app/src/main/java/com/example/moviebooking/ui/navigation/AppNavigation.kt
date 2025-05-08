package com.example.moviebooking.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.moviebooking.ui.about.AboutScreen
import com.example.moviebooking.ui.auth.AuthViewModel
import com.example.moviebooking.ui.auth.EmailVerificationScreen
import com.example.moviebooking.ui.auth.ForgotPasswordScreen
import com.example.moviebooking.ui.auth.LoginScreen
import com.example.moviebooking.ui.auth.RegisterScreen
import com.example.moviebooking.ui.home.HomeScreen
import com.example.moviebooking.ui.home.HomeViewModel
import com.example.moviebooking.ui.movie.MovieDetailScreen
import com.example.moviebooking.ui.movie.MovieDetailViewModel
import com.example.moviebooking.ui.movie.BookingSeatScreen
import com.example.moviebooking.ui.movie.BookingViewModel
import com.example.moviebooking.ui.movie.BookingConfirmationScreen
import com.example.moviebooking.ui.movie.BookingConfirmationViewModel
import com.example.moviebooking.ui.profile.BookingsScreen
import com.example.moviebooking.ui.profile.ProfileScreenWrapper
import com.example.moviebooking.ui.search.SearchScreen
import com.example.moviebooking.ui.payment.PaymentScreen
import com.example.moviebooking.ui.payment.PaymentViewModel
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    startGoogleSignIn: () -> Unit,
    startFacebookSignIn: () -> Unit,
    onNavigationIconClick: () -> Unit,
    onScreenChange: (Screen) -> Unit,
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()

    val homeViewModel: HomeViewModel = viewModel()

    // Monitor current back stack entry to track current screen
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route ?: Screen.Login.route

    // Update current screen based on route
    LaunchedEffect(currentRoute) {
        when {
            currentRoute == Screen.Home.route -> onScreenChange(Screen.Home)
            currentRoute == Screen.Profile.route -> onScreenChange(Screen.Profile)
            currentRoute == Screen.Bookings.route -> onScreenChange(Screen.Bookings)
            currentRoute.startsWith("movie_detail") -> onScreenChange(Screen.MovieDetail)
            currentRoute.startsWith("booking_seat") -> onScreenChange(Screen.BookingSeat)
            currentRoute.startsWith("booking_confirmation") -> onScreenChange(Screen.BookingConfirmation)
            currentRoute.startsWith("booking_detail") -> onScreenChange(Screen.BookingDetail)
            currentRoute == Screen.Search.route -> onScreenChange(Screen.Search)
            currentRoute == Screen.Notifications.route -> onScreenChange(Screen.Notifications)
            currentRoute == Screen.About.route -> onScreenChange(Screen.About)
            currentRoute == Screen.Payment.route -> onScreenChange(Screen.Payment)
            else -> { /* Login, register, etc. stay as is */ }
        }
    }

    // Actions
    val actions = remember(navController) { AppNavigationActions(navController) }

    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = actions.navigateToRegister,
                onNavigateToHome = actions.navigateToHome,
                onNavigateToForgotPassword = actions.navigateToForgotPassword,
                onNavigateToEmailVerification = actions.navigateToEmailVerification,
                onGoogleSignInClick = startGoogleSignIn,
                onFacebookSignInClick = startFacebookSignIn
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = actions.navigateToLogin,
                onNavigateToHome = actions.navigateToHome,
                onNavigateToEmailVerification = actions.navigateToEmailVerification
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onBackToLogin = actions.navigateToLogin
            )
        }

        composable(Screen.EmailVerification.route) {
            EmailVerificationScreen(
                viewModel = authViewModel,
                onBackToLogin = actions.navigateToLogin,
                onNavigateToHome = actions.navigateToHome
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                authViewModel = authViewModel,
                onNavigateToMovieDetail = actions.navigateToMovieDetail,
                onNavigateToSearch = actions.navigateToSearch,
                onNavigateToProfile = actions.navigateToProfile,
                onNavigateToNotifications = actions.navigateToNotifications,
                onNavigateToBookings = actions.navigateToBookings,
                onNavigationIconClick = onNavigationIconClick,
                onLogout = {
                    coroutineScope.launch {
                        authViewModel.logout()
                        actions.navigateToLogin()
                    }
                }
            )
        }

        composable(
            route = Screen.MovieDetail.route,
            arguments = listOf(navArgument("movieId") { type = NavType.StringType })
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId") ?: ""
            val movieDetailViewModel: MovieDetailViewModel = viewModel(
                factory = MovieDetailViewModel.Factory(movieId)
            )

            MovieDetailScreen(
                viewModel = movieDetailViewModel,
                onBackClick = { navController.popBackStack() },
                onSelectShowtime = actions.navigateToSeatSelection
            )
        }

        composable(
            route = Screen.BookingSeat.route,
            arguments = listOf(navArgument("showtimeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val showtimeId = backStackEntry.arguments?.getString("showtimeId") ?: ""
            val bookingViewModel: BookingViewModel = viewModel(
                factory = BookingViewModel.Factory(showtimeId)
            )

            BookingSeatScreen(
                viewModel = bookingViewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToPayment = {
                    navController.currentBackStackEntry?.savedStateHandle?.set("showtimeId", showtimeId)
                    navController.currentBackStackEntry?.savedStateHandle?.set("selectedSeats", bookingViewModel.selectedSeats.value.map { "${it.row}${it.number}" })
                    navController.currentBackStackEntry?.savedStateHandle?.set("totalPrice", bookingViewModel.totalPrice.value)
                    actions.navigateToPayment()
                }
            )
        }

        composable(
            route = Screen.BookingConfirmation.route,
            arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
            val bookingConfirmationViewModel: BookingConfirmationViewModel = viewModel(
                factory = BookingConfirmationViewModel.Factory(bookingId)
            )

            BookingConfirmationScreen(
                bookingId = bookingId,
                onViewMyBookings = actions.navigateToBookings,
                onBackToHome = actions.navigateToHome,
                viewModel = bookingConfirmationViewModel
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onMovieClick = actions.navigateToMovieDetail,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreenWrapper(
                authViewModel = authViewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToBookings = actions.navigateToBookings,
                onNavigateToLogin = actions.navigateToLogin,
                onNavigateToAbout = actions.navigateToAbout,
                onLogout = {
                    coroutineScope.launch {
                        authViewModel.logout()
                        actions.navigateToLogin()
                    }
                }
            )
        }

        composable(Screen.Bookings.route) {
            BookingsScreen(
                onBackClick = { navController.popBackStack() },
                onBookingClick = actions.navigateToBookingDetail
            )
        }

        composable(
            route = Screen.BookingDetail.route,
            arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""

            BookingConfirmationScreen(
                bookingId = bookingId,
                onViewMyBookings = actions.navigateToBookings,
                onBackToHome = actions.navigateToHome
            )
        }

        composable(Screen.Notifications.route) {
            // TODO: Implement notification screen
            // Tạm thời chuyển hướng về Home
            LaunchedEffect(Unit) {
                navController.popBackStack()
                navController.navigate(Screen.Home.route)
            }
        }

        composable(Screen.About.route) {
            AboutScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Payment.route) {
            val showtimeId = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<String>("showtimeId") ?: ""
            
            val selectedSeats = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<List<String>>("selectedSeats") ?: emptyList()
                
            val totalPrice = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<Double>("totalPrice") ?: 0.0
                
            val paymentViewModel: PaymentViewModel = viewModel(
                factory = PaymentViewModel.Factory(showtimeId, selectedSeats, totalPrice)
            )
            
            PaymentScreen(
                onBackClick = { navController.popBackStack() },
                onPaymentSuccess = { bookingId ->
                    navController.navigate("booking_confirmation/$bookingId") {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = false
                        }
                        launchSingleTop = true
                        restoreState = false
                    }
                },
                viewModel = paymentViewModel
            )
        }
    }
}

/**
 * Destinations used in the app
 */
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object EmailVerification : Screen("email_verification")
    object Home : Screen("home")
    object MovieDetail : Screen("movie_detail/{movieId}")
    object BookingSeat : Screen("booking_seat/{showtimeId}")
    object BookingConfirmation : Screen("booking_confirmation/{bookingId}")
    object Search : Screen("search")
    object Profile : Screen("profile")
    object Bookings : Screen("bookings")
    object BookingDetail : Screen("booking_detail/{bookingId}")
    object Notifications : Screen("notifications")
    object About : Screen("about")
    object Payment : Screen("payment")

    fun createRoute(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                route.replace(Regex("\\{[^}]*\\}"), arg)
            }
        }
    }
}

/**
 * Models the navigation actions in the app
 */
class AppNavigationActions(navController: NavHostController) {
    val navigateToLogin: () -> Unit = {
        navController.navigate(Screen.Login.route) {
            popUpTo(navController.graph.id) {
                inclusive = true
            }
        }
    }

    val navigateToRegister: () -> Unit = {
        navController.navigate(Screen.Register.route)
    }

    val navigateToForgotPassword: () -> Unit = {
        navController.navigate(Screen.ForgotPassword.route)
    }

    val navigateToEmailVerification: () -> Unit = {
        navController.navigate(Screen.EmailVerification.route)
    }

    val navigateToHome: () -> Unit = {
        navController.navigate(Screen.Home.route) {
            popUpTo(navController.graph.id) {
                inclusive = true
            }
        }
    }

    val navigateToMovieDetail: (String) -> Unit = { movieId ->
        navController.navigate(Screen.MovieDetail.route.replace("{movieId}", movieId))
    }

    val navigateToSeatSelection: (String) -> Unit = { showtimeId ->
        navController.navigate(Screen.BookingSeat.route.replace("{showtimeId}", showtimeId))
    }

    val navigateToBookingConfirmation: (String) -> Unit = { bookingId ->
        navController.navigate(Screen.BookingConfirmation.route.replace("{bookingId}", bookingId)) {
            popUpTo(Screen.Home.route)
        }
    }

    val navigateToSearch: () -> Unit = {
        navController.navigate(Screen.Search.route)
    }

    val navigateToProfile: () -> Unit = {
        navController.navigate(Screen.Profile.route)
    }

    val navigateToBookings: () -> Unit = {
        navController.navigate(Screen.Bookings.route)
    }

    val navigateToBookingDetail: (String) -> Unit = { bookingId ->
        navController.navigate(Screen.BookingDetail.route.replace("{bookingId}", bookingId))
    }

    val navigateToNotifications: () -> Unit = {
        navController.navigate(Screen.Notifications.route)
    }

    val navigateToAbout: () -> Unit = {
        navController.navigate(Screen.About.route)
    }

    val navigateToPayment: () -> Unit = {
        navController.navigate(Screen.Payment.route)
    }
}