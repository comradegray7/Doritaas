package com.example.myapp.view.utils

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

// Helper function to format
fun Long.toRemainingMonths(): Int {
    val currentTime = System.currentTimeMillis()
    val remainingTime = this - currentTime
    val remainingDays = TimeUnit.MILLISECONDS.toDays(remainingTime)
    return maxOf(0, (remainingDays / 30).toInt())
}

/**
 * formatDate
 *
 *
 * @param timestamp The timestamp parameter
 */
fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return format.format(date)
}

/**
 * formatTimestamp
 *
 *
 * @param timestamp The timestamp parameter
 */
fun formatTimestamp(timestamp: Timestamp?): String {
    if (timestamp == null) return "N/A"
    val date = timestamp.toDate()
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return formatter.format(date)
}

/**
 * Convert a [Timestamp] to a human readable "time ago" string.
 *
 * The function returns relative strings such as "Just now", "5 minutes ago",
 * "3 hours ago", "2 days ago" or "1 weeks ago".
 *
 * @param timestamp the [Timestamp] to convert.
 * @return a human readable relative time string.
 */
fun getTimeAgo(timestamp: Timestamp): String {
    val now = System.currentTimeMillis()
    val time = timestamp.toDate().time
    val diff = now - time

    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000} minutes ago"
        diff < 86400000 -> "${diff / 3600000} hours ago"
        diff < 604800000 -> "${diff / 86400000} days ago"
        else -> "${diff / 604800000} weeks ago"
    }
}
