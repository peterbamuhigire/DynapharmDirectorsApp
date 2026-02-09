package com.dynapharm.owner.util

import android.util.Patterns
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Extension functions for common operations across the app.
 */

// ────────────────────────────────────────────────────────────────────────────────
// String Extensions
// ────────────────────────────────────────────────────────────────────────────────

/**
 * Validates if a string is a valid email address.
 * Uses Android's built-in email pattern matcher.
 *
 * @return true if the string is a valid email format, false otherwise
 *
 * Example:
 * ```
 * "user@example.com".isValidEmail() // true
 * "invalid.email".isValidEmail() // false
 * "".isValidEmail() // false
 * ```
 */
fun String.isValidEmail(): Boolean {
    return this.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

/**
 * Capitalizes the first letter of the string.
 *
 * @return String with first letter capitalized
 *
 * Example:
 * ```
 * "hello".capitalizeFirst() // "Hello"
 * "WORLD".capitalizeFirst() // "WORLD"
 * ```
 */
fun String.capitalizeFirst(): String {
    return this.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }
}

/**
 * Truncates a string to a maximum length and adds ellipsis if needed.
 *
 * @param maxLength The maximum length of the string
 * @param ellipsis The string to append when truncated (default: "...")
 * @return Truncated string with ellipsis if it exceeds maxLength
 *
 * Example:
 * ```
 * "This is a long text".truncate(10) // "This is a..."
 * "Short".truncate(10) // "Short"
 * ```
 */
fun String.truncate(maxLength: Int, ellipsis: String = "..."): String {
    return if (this.length <= maxLength) {
        this
    } else {
        this.take(maxLength - ellipsis.length) + ellipsis
    }
}

/**
 * Checks if a string represents a valid phone number.
 * Basic validation: 10-15 digits, may start with +
 *
 * @return true if valid phone format, false otherwise
 *
 * Example:
 * ```
 * "+254712345678".isValidPhone() // true
 * "0712345678".isValidPhone() // true
 * "123".isValidPhone() // false
 * ```
 */
fun String.isValidPhone(): Boolean {
    val phonePattern = "^[+]?[0-9]{10,15}$".toRegex()
    return this.isNotBlank() && phonePattern.matches(this.replace("\\s".toRegex(), ""))
}

// ────────────────────────────────────────────────────────────────────────────────
// Number Extensions
// ────────────────────────────────────────────────────────────────────────────────

/**
 * Formats a Long value as currency.
 * Uses the system default locale for currency formatting.
 *
 * @param currencyCode The ISO 4217 currency code (default: "KES" for Kenyan Shilling)
 * @return Formatted currency string
 *
 * Example:
 * ```
 * 1000L.toFormattedCurrency() // "KES 1,000.00"
 * 1234567L.toFormattedCurrency("USD") // "$1,234,567.00"
 * ```
 */
fun Long.toFormattedCurrency(currencyCode: String = "KES"): String {
    val format = NumberFormat.getCurrencyInstance()
    format.currency = java.util.Currency.getInstance(currencyCode)
    return format.format(this)
}

/**
 * Formats a Double value as currency.
 *
 * @param currencyCode The ISO 4217 currency code (default: "KES" for Kenyan Shilling)
 * @return Formatted currency string
 *
 * Example:
 * ```
 * 1000.50.toFormattedCurrency() // "KES 1,000.50"
 * ```
 */
fun Double.toFormattedCurrency(currencyCode: String = "KES"): String {
    val format = NumberFormat.getCurrencyInstance()
    format.currency = java.util.Currency.getInstance(currencyCode)
    return format.format(this)
}

/**
 * Formats a number with thousand separators.
 *
 * @return Formatted number string
 *
 * Example:
 * ```
 * 1000.formatWithSeparators() // "1,000"
 * 1234567.formatWithSeparators() // "1,234,567"
 * ```
 */
fun Int.formatWithSeparators(): String {
    return NumberFormat.getNumberInstance(Locale.getDefault()).format(this)
}

/**
 * Formats a Long with thousand separators.
 */
fun Long.formatWithSeparators(): String {
    return NumberFormat.getNumberInstance(Locale.getDefault()).format(this)
}

/**
 * Calculates percentage and returns formatted string.
 *
 * @param total The total value
 * @param decimals Number of decimal places (default: 1)
 * @return Formatted percentage string
 *
 * Example:
 * ```
 * 25.toPercentageOf(100) // "25.0%"
 * 33.toPercentageOf(100, 2) // "33.00%"
 * ```
 */
fun Number.toPercentageOf(total: Number, decimals: Int = 1): String {
    if (total.toDouble() == 0.0) return "0%"
    val percentage = (this.toDouble() / total.toDouble()) * 100
    return "%.${decimals}f%%".format(percentage)
}

// ────────────────────────────────────────────────────────────────────────────────
// Date Extensions
// ────────────────────────────────────────────────────────────────────────────────

/**
 * Formats a Date to a readable string.
 *
 * @param pattern The date format pattern (default: "dd MMM yyyy")
 * @return Formatted date string
 *
 * Example:
 * ```
 * Date().toFormattedDate() // "09 Feb 2026"
 * Date().toFormattedDate("yyyy-MM-dd") // "2026-02-09"
 * ```
 */
fun Date.toFormattedDate(pattern: String = "dd MMM yyyy"): String {
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(this)
}

/**
 * Formats a timestamp (Long) to a readable date string.
 *
 * @param pattern The date format pattern (default: "dd MMM yyyy")
 * @return Formatted date string
 *
 * Example:
 * ```
 * System.currentTimeMillis().toFormattedDate() // "09 Feb 2026"
 * ```
 */
fun Long.toFormattedDate(pattern: String = "dd MMM yyyy"): String {
    val date = Date(this)
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(date)
}

/**
 * Formats a timestamp to a time string.
 *
 * @param pattern The time format pattern (default: "HH:mm")
 * @return Formatted time string
 *
 * Example:
 * ```
 * System.currentTimeMillis().toFormattedTime() // "14:30"
 * System.currentTimeMillis().toFormattedTime("hh:mm a") // "02:30 PM"
 * ```
 */
fun Long.toFormattedTime(pattern: String = "HH:mm"): String {
    val date = Date(this)
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(date)
}

/**
 * Formats a timestamp to a relative time string (e.g., "2 hours ago").
 * For times within the last 24 hours, returns relative time.
 * For older times, returns formatted date.
 *
 * @return Relative or formatted date string
 *
 * Example:
 * ```
 * (System.currentTimeMillis() - 3600000).toRelativeTime() // "1 hour ago"
 * (System.currentTimeMillis() - 86400000 * 2).toRelativeTime() // "07 Feb 2026"
 * ```
 */
fun Long.toRelativeTime(): String {
    val now = System.currentTimeMillis()
    val diff = now - this

    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000} minutes ago"
        diff < 86400_000 -> "${diff / 3600_000} hours ago"
        else -> this.toFormattedDate()
    }
}

// ────────────────────────────────────────────────────────────────────────────────
// Collection Extensions
// ────────────────────────────────────────────────────────────────────────────────

/**
 * Safely gets an element at index or returns null.
 * Alternative to getOrNull that's more explicit.
 */
fun <T> List<T>.getOrDefault(index: Int, default: T): T {
    return if (index in indices) this[index] else default
}

/**
 * Checks if a list is null or empty.
 */
fun <T> List<T>?.isNullOrEmpty(): Boolean {
    return this == null || this.isEmpty()
}
