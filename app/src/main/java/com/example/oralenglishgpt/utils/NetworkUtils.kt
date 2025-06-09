package com.example.oralenglishgpt.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.material3.SnackbarHostState

object NetworkUtils {
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    suspend fun showNetworkErrorSnackbar(
        context: Context,
        snackbarHostState: SnackbarHostState
    ) {
        if (!isNetworkAvailable(context)) {
            snackbarHostState.showSnackbar(
                message = "No internet connection.",
                withDismissAction = true
            )
        }
    }
}