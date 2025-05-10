package com.example.moviebooking.util

import java.text.SimpleDateFormat
import java.util.Locale

object DateFormats {
    val FULL_DATE = SimpleDateFormat("MMMM dd, yyyy", Locale.US)
    val FULL_DATE_TIME = SimpleDateFormat("MMMM dd, yyyy, HH:mm", Locale.US)
    val SHORT_DATE = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
    val SHORT_DATE_NO_YEAR = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())
    val TIME = SimpleDateFormat("HH:mm", Locale.getDefault())
    val DATE_TIME = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
} 