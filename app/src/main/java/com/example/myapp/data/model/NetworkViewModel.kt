package com.example.myapp.data.model

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.myapp.NetworkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject


// NetworkViewModel.kt
@HiltViewModel
/**
 * NetworkViewModel
 *
 */
class NetworkViewModel @Inject constructor(
    @ApplicationContext context: Context
) : ViewModel() {

    // Use lazy to defer initialization until first access
    val networkManager: NetworkManager by lazy {
        NetworkManager(context.applicationContext)
    }


    override fun onCleared() {
        super.onCleared()
        networkManager.stopNetworkMonitoring()
    }
}