package com.alperen.fwear.presentation

import android.util.Log
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alperen.fwear.data.FDroidAppDto
import com.alperen.fwear.data.RetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class UiState {
    object Loading : UiState()
    data class Success(val apps: List<FDroidAppDto>) : UiState()
    data class Error(val message: String) : UiState()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _allApps = MutableStateFlow<List<FDroidAppDto>>(emptyList())

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    val uiState = combine(_allApps, _searchText) { apps, text ->
        if (apps.isEmpty()) {
            UiState.Loading
        } else if (text.isBlank()) {
            UiState.Success(apps)
        } else {
            val filtered = apps.filter {
                it.getBestName().contains(text, ignoreCase = true) ||
                        it.packageName.contains(text, ignoreCase = true)
            }
            UiState.Success(filtered)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    private val _selectedApp = MutableStateFlow<FDroidAppDto?>(null)
    val selectedApp = _selectedApp.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress = _downloadProgress.asStateFlow()

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading = _isDownloading.asStateFlow()

    init {
        refreshRepo()
    }

    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }

    fun refreshRepo() {
        viewModelScope.launch {
            try {
                Log.d("FWear", "Data loading...") // LOG 1
                val index = RetrofitClient.service.getIndex()

                Log.d("FWear", "Data: ${index.apps.size}") // LOG 2

                val sortedApps = index.apps.sortedByDescending { it.lastUpdated }.take(100)
                _allApps.value = sortedApps

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("FWear", "ERROR: ${e.message}") // LOG 3
            }
        }
    }
    fun selectApp(packageName: String) {
        _selectedApp.value = _allApps.value.find { it.packageName == packageName }
        _downloadProgress.value = 0f
        _isDownloading.value = false
    }

    fun startDownloadAndInstall(app: FDroidAppDto) {
        val installManager = InstallManager(getApplication())
        viewModelScope.launch {
            _isDownloading.value = true
            val downloadId = installManager.startDownload(app)
            var progress = 0f
            while (progress < 1.0f) {
                progress = installManager.getDownloadProgress(downloadId)
                _downloadProgress.value = progress
                delay(500)
            }
            _isDownloading.value = false
            _downloadProgress.value = 1.0f
            installManager.installApk(app)
        }
    }
}