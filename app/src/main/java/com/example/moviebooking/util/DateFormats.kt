package com.example.moviebooking.util

import java.text.SimpleDateFormat
import java.util.Locale

object DateFormats {
    val FULL_DATE = SimpleDateFormat("MMMM dd, yyyy", Locale.US)
    val FULL_DATE_TIME = SimpleDateFormat("MMMM dd, yyyy, hh:mm a", Locale.US)
    val SHORT_DATE = SimpleDateFormat("EEE, dd MMM yyyy", Locale.US)
    val SHORT_DATE_NO_YEAR = SimpleDateFormat("EEE, dd MMM", Locale.US)
    val TIME = SimpleDateFormat("hh:mm a", Locale.US)
    val DATE_TIME = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US)
} 