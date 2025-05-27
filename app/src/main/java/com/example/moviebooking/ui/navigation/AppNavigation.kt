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
import com.example.moviebooking.data.model.MovieModel
import com.example.moviebooking.ui.about.AboutScreen
import com.example.moviebooking.ui.admin.AdminDashboardScreen
import com.example.moviebooking.ui.auth.AuthViewModel
import com.example.moviebooking.ui.auth.EmailVerificationScreen
import com.example.moviebooking.ui.auth.ForgotPasswordScreen
import com.example.moviebooking.ui.auth.LoginScreen
import com.example.moviebooking.ui.auth.RegisterScreen
import com.example.moviebooking.ui.cinema.CinemaScreen
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
import com.example.moviebooking.ui.cinema.CinemaViewModel
import com.example.moviebooking.ui.movie.SeeAllMoviesScreen
import com.example.moviebooking.ui.movie.SeeAllComingSoonScreen
import com.example.moviebooking.ui.membership.MembershipScreen
import com.example.moviebooking.ui.admin.movie.AdminMoviesScreen
import com.example.moviebooking.ui.admin.movie.AdminMovieFormScreen
import com.example.moviebooking.ui.admin.movie.AdminMoviesViewModel
import com.example.moviebooking.ui.admin.cinema.AdminCinemasScreen
import com.example.moviebooking.ui.admin.cinema.AdminCinemaFormScreen
import com.example.moviebooking.ui.admin.cinema.AdminCinemasViewModel
import com.example.moviebooking.ui.admin.showtime.AdminShowtimeListScreen
import com.example.moviebooking.ui.admin.showtime.AdminShowtimeFormScreen
import com.example.moviebooking.ui.admin.showtime.AdminShowtimeFormViewModel
import com.example.moviebooking.ui.admin.showtime.AdminShowtimeListViewModel
import com.example.moviebooking.ui.admin.showtime.AdminShowtimeSeatsScreen
import com.example.moviebooking.ui.admin.showtime.AdminShowtimeSeatsViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import com.example.moviebooking.ui.admin.user.AdminUsersScreen
import com.example.moviebooking.ui.admin.user.AdminUsersViewModel
import com.example.moviebooking.ui.admin.user.AdminUserFormScreen
import com.example.moviebooking.ui.admin.membership.AdminMembershipScreen
import com.example.moviebooking.ui.chat.ChatBotScreen

@Composable
fun AppNavigation(
    startGoogleSignIn: () -> Unit,
    startFacebookSignIn: () -> Unit,
    onNavigationIconClick: () -> Unit,
    onScreenChange: (Screen) -> Unit,
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel(),
    cinemaViewModel: CinemaViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()

    // Monitor current back stack entry to track current screen
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route ?: Screen.Login.route

    // Update current screen based on route
    LaunchedEffect(currentRoute) {
        when {
            currentRoute == Screen.Home.route -> onScreenChange(Screen.Home)
            currentRoute == Screen.Profile.route -> onScreenChange(Screen.Profile)
            currentRoute == Screen.Bookings.route -> onScreenChange(Screen.Bookings)
            currentRoute == Screen.Cinemas.route -> onScreenChange(Screen.Cinemas)
            currentRoute.startsWith("movie_detail") -> onScreenChange(Screen.MovieDetail)
            currentRoute.startsWith("booking_seat") -> onScreenChange(Screen.BookingSeat)
            currentRoute.startsWith("booking_confirmation") -> onScreenChange(Screen.BookingConfirmation)
            currentRoute.startsWith("booking_detail") -> onScreenChange(Screen.BookingDetail)
            currentRoute == Screen.Search.route -> onScreenChange(Screen.Search)
            currentRoute == Screen.Notifications.route -> onScreenChange(Screen.Notifications)
            currentRoute == Screen.About.route -> onScreenChange(Screen.About)
            currentRoute == Screen.Payment.route -> onScreenChange(Screen.Payment)
            currentRoute == Screen.SeeAllMovies.route -> onScreenChange(Screen.SeeAllMovies)
            currentRoute == Screen.SeeAllComingSoon.route -> onScreenChange(Screen.SeeAllComingSoon)
            currentRoute == Screen.Membership.route -> onScreenChange(Screen.Membership)
            currentRoute == Screen.AdminDashboard.route -> onScreenChange(Screen.AdminDashboard)
            currentRoute == Screen.AdminMovies.route -> onScreenChange(Screen.AdminMovies)
            currentRoute == Screen.AdminMovieForm.route -> onScreenChange(Screen.AdminMovieForm)
            currentRoute == Screen.AdminCinemas.route -> onScreenChange(Screen.AdminCinemas)
            currentRoute == Screen.AdminCinemaForm.route -> onScreenChange(Screen.AdminCinemaForm)
            currentRoute == Screen.AdminShowtimes.route -> onScreenChange(Screen.AdminShowtimes)
            currentRoute == Screen.AdminShowtimeForm.route -> onScreenChange(Screen.AdminShowtimeForm)
            currentRoute == Screen.AdminShowtimeSeats.route -> onScreenChange(Screen.AdminShowtimeSeats)
            currentRoute == Screen.AdminUsers.route -> onScreenChange(Screen.AdminUsers)
            currentRoute == Screen.AdminUserForm.route -> onScreenChange(Screen.AdminUserForm)
            currentRoute == Screen.AdminMembership.route -> onScreenChange(Screen.AdminMembership)
            currentRoute == Screen.ChatBot.route -> onScreenChange(Screen.ChatBot)
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
                onNavigateToAdminDashboard = actions.navigateToAdminDashboard,
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
                onNavigateToCinemas = actions.navigateToCinemas,
                onNavigateToSeeAllMovies = actions.navigateToSeeAllMovies,
                onNavigateToSeeAllComingSoon = actions.navigateToSeeAllComingSoon,
                onNavigateToChatBot = actions.navigateToChatBot,
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
                onNavigateToMembership = actions.navigateToMembership,
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

        composable(Screen.SeeAllMovies.route) {
            val movies = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<List<MovieModel>>("movies") ?: emptyList()
            val title = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<String>("title") ?: "Movies"

            SeeAllMoviesScreen(
                movies = movies,
                onNavigateBack = { navController.navigateUp() },
                onMovieClick = { movieId ->
                    navController.navigate("movie_detail/$movieId")
                },
                title = title
            )
        }

        composable(Screen.SeeAllComingSoon.route) {
            val movies = navController.previousBackStackEntry?.savedStateHandle?.get<List<MovieModel>>("movies") ?: emptyList()
            val title = navController.previousBackStackEntry?.savedStateHandle?.get<String>("title") ?: "Coming Soon"
            SeeAllComingSoonScreen(
                movies = movies,
                onNavigateBack = { navController.navigateUp() },
                onMovieClick = { movieId -> actions.navigateToMovieDetail(movieId) },
                title = title
            )
        }

        composable(Screen.Membership.route) {
            MembershipScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("cinemas") {
            CinemaScreen(
                onNavigateBack = { navController.navigateUp() },
                onCinemaClick = { cinemaId ->
                    navController.navigate("cinema_detail/$cinemaId")
                },
                viewModel = cinemaViewModel
            )
        }

        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                onNavigateToMovies = { actions.navigateToAdminMovies() },
                onNavigateToUsers = { actions.navigateToAdminUsers() },
                onNavigateToBookings = { /* TODO: Implement booking management */ },
                onNavigateToMembership = { actions.navigateToAdminMembership() },
                onNavigateToShowtimes = { actions.navigateToAdminShowtimes() },
                onNavigateToCinemas = { actions.navigateToAdminCinemas() },
                onLogout = {
                    coroutineScope.launch {
                        authViewModel.logout()
                        actions.navigateToLogin()
                    }
                }
            )
        }

        composable(Screen.AdminMovies.route) {
            val viewModel: AdminMoviesViewModel = viewModel()
            AdminMoviesScreen(
                onNavigateBack = { navController.popBackStack() },
                onAddMovie = { actions.navigateToAdminMovieForm("new") },
                onEditMovie = { movieId -> actions.navigateToAdminMovieForm(movieId) },
                onDeleteMovie = { movieId ->
                    viewModel.deleteMovie(movieId)
                }
            )
        }

        composable(
            route = Screen.AdminMovieForm.route,
            arguments = listOf(navArgument("movieId") { type = NavType.StringType })
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId") ?: "new"
            AdminMovieFormScreen(
                movieId = movieId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminCinemas.route) {
            val viewModel: AdminCinemasViewModel = viewModel()
            AdminCinemasScreen(
                onNavigateBack = { navController.popBackStack() },
                onAddCinema = { actions.navigateToAdminCinemaForm("new") },
                onEditCinema = { cinemaId -> actions.navigateToAdminCinemaForm(cinemaId) },
                onDeleteCinema = { cinemaId ->
                    viewModel.deleteCinema(cinemaId)
                }
            )
        }

        composable(
            route = Screen.AdminCinemaForm.route,
            arguments = listOf(navArgument("cinemaId") { type = NavType.StringType })
        ) { backStackEntry ->
            val cinemaId = backStackEntry.arguments?.getString("cinemaId") ?: "new"
            AdminCinemaFormScreen(
                cinemaId = cinemaId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminShowtimes.route) {
            val viewModel: AdminShowtimeListViewModel = viewModel()
            AdminShowtimeListScreen(
                onNavigateToForm = { showtimeId -> actions.navigateToAdminShowtimeForm(showtimeId) },
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSeats = { showtimeId -> actions.navigateToAdminShowtimeSeats(showtimeId) }
            )
        }

        composable(
            route = Screen.AdminShowtimeForm.route,
            arguments = listOf(navArgument("showtimeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val showtimeId = backStackEntry.arguments?.getString("showtimeId") ?: ""
            val context = LocalContext.current
            val adminShowtimeFormViewModel: AdminShowtimeFormViewModel = viewModel(
                factory = AdminShowtimeFormViewModel.Factory(showtimeId, context)
            )

            AdminShowtimeFormScreen(
                showtimeId = showtimeId,
                onNavigateBack = { navController.popBackStack() },
                viewModel = adminShowtimeFormViewModel
            )
        }

        // Thêm màn hình AdminShowtimeSeatsScreen
        composable(
            route = Screen.AdminShowtimeSeats.route,
            arguments = listOf(navArgument("showtimeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val showtimeId = backStackEntry.arguments?.getString("showtimeId") ?: ""
            val adminShowtimeSeatsViewModel: AdminShowtimeSeatsViewModel = viewModel(
                factory = AdminShowtimeSeatsViewModel.Factory(showtimeId)
            )

            AdminShowtimeSeatsScreen(
                showtimeId = showtimeId,
                onNavigateBack = { navController.popBackStack() },
                viewModel = adminShowtimeSeatsViewModel
            )
        }

        composable(Screen.AdminUsers.route) {
            val viewModel: AdminUsersViewModel = viewModel()
            AdminUsersScreen(
                onNavigateBack = { navController.popBackStack() },
                onEditUser = { userId -> actions.navigateToAdminUserForm(userId) },
                onDeleteUser = { userId ->
                    viewModel.deleteUser(userId)
                }
            )
        }

        composable(
            route = Screen.AdminUserForm.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: "new"
            AdminUserFormScreen(
                userId = userId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminMembership.route) {
            AdminMembershipScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ChatBot.route) {
            ChatBotScreen(
                onNavigateBack = { navController.popBackStack() }
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
    object Cinemas : Screen("cinemas")
    object SeeAllMovies : Screen("see_all_movies")
    object SeeAllComingSoon : Screen("see_all_coming_soon")
    object Membership : Screen("membership")
    object AdminDashboard : Screen("admin_dashboard")
    object AdminMovies : Screen("admin_movies")
    object AdminMovieForm : Screen("admin_movie_form/{movieId}") {
        fun createRoute(movieId: String = "new") = "admin_movie_form/$movieId"
    }
    object AdminCinemas : Screen("admin_cinemas")
    object AdminCinemaForm : Screen("admin_cinema_form/{cinemaId}") {
        fun createRoute(cinemaId: String) = "admin_cinema_form/$cinemaId"
    }
    object AdminShowtimes : Screen("admin_showtimes")
    object AdminShowtimeForm : Screen("admin_showtime_form/{showtimeId}") {
        fun createRoute(showtimeId: String = "new") = "admin_showtime_form/$showtimeId"
    }
    object AdminShowtimeSeats : Screen("admin_showtime_seats/{showtimeId}") {
        fun createRoute(showtimeId: String) = "admin_showtime_seats/$showtimeId"
    }
    object AdminUsers : Screen("admin_users")
    object AdminUserForm : Screen("admin_user_form/{userId}") {
        fun createRoute(userId: String = "new") = "admin_user_form/$userId"
    }
    object AdminMembership : Screen("admin_membership")
    object ChatBot : Screen("chatbot")

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

    val navigateToAdminDashboard: () -> Unit = {
        navController.navigate(Screen.AdminDashboard.route) {
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

    val navigateToCinemas: () -> Unit = {
        navController.navigate(Screen.Cinemas.route)
    }

    val navigateToSeeAllMovies: (List<MovieModel>, String) -> Unit = { movies, title ->
        navController.currentBackStackEntry?.savedStateHandle?.set("movies", movies)
        navController.currentBackStackEntry?.savedStateHandle?.set("title", title)
        navController.navigate(Screen.SeeAllMovies.route)
    }

    val navigateToSeeAllComingSoon: (List<MovieModel>, String) -> Unit = { movies, title ->
        navController.currentBackStackEntry?.savedStateHandle?.set("movies", movies)
        navController.currentBackStackEntry?.savedStateHandle?.set("title", title)
        navController.navigate(Screen.SeeAllComingSoon.route)
    }

    val navigateToMembership: () -> Unit = {
        navController.navigate(Screen.Membership.route)
    }

    val navigateToAdminMovies: () -> Unit = {
        navController.navigate(Screen.AdminMovies.route)
    }

    val navigateToAdminMovieForm: (String) -> Unit = { movieId ->
        navController.navigate(Screen.AdminMovieForm.createRoute(movieId))
    }

    val navigateToAdminCinemas: () -> Unit = {
        navController.navigate(Screen.AdminCinemas.route)
    }

    val navigateToAdminCinemaForm: (String) -> Unit = { cinemaId ->
        navController.navigate(Screen.AdminCinemaForm.createRoute(cinemaId))
    }

    val navigateToAdminShowtimes: () -> Unit = {
        navController.navigate(Screen.AdminShowtimes.route)
    }

    val navigateToAdminShowtimeForm: (String) -> Unit = { showtimeId ->
        navController.navigate(Screen.AdminShowtimeForm.createRoute(showtimeId))
    }

    val navigateToAdminShowtimeSeats: (String) -> Unit = { showtimeId ->
        navController.navigate(Screen.AdminShowtimeSeats.createRoute(showtimeId))
    }

    val navigateToAdminUsers: () -> Unit = {
        navController.navigate(Screen.AdminUsers.route)
    }

    val navigateToAdminUserForm: (String) -> Unit = { userId ->
        navController.navigate(Screen.AdminUserForm.createRoute(userId))
    }

    val navigateToAdminMembership: () -> Unit = {
        navController.navigate(Screen.AdminMembership.route)
    }

    val navigateToChatBot: () -> Unit = {
        navController.navigate(Screen.ChatBot.route)
    }
}

@Composable
private fun AdminMoviesScreen(
    onNavigateBack: () -> Unit,
    onAddMovie: () -> Unit,
    onEditMovie: (String) -> Unit,
    onDeleteMovie: (String) -> Unit
) {
    val viewModel: AdminMoviesViewModel = viewModel()
    AdminMoviesScreen(
        onNavigateBack = onNavigateBack,
        onAddMovie = onAddMovie,
        onEditMovie = onEditMovie,
        onDeleteMovie = onDeleteMovie
    )
}