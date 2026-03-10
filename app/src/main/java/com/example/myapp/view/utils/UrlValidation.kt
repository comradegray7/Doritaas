package com.example.myapp.view.utils

import android.util.Patterns

/**
 * isValidUrl
 *
 *
 * @param url The url parameter
 */
fun isValidUrl(url: String): Boolean {
    return Patterns.WEB_URL.matcher(url).matches()
}
