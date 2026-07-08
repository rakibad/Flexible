package com.example.ui

import androidx.lifecycle.ViewModel
import com.example.model.ScannedNumber
import com.example.repository.OcrRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OcrViewModel : ViewModel() {
    private val repository = OcrRepository()

    val scannedNumber: StateFlow<ScannedNumber?> = repository.scannedNumber
    val analyzer = repository.analyzer

    private val _isFlashOn = MutableStateFlow(false)
    val isFlashOn: StateFlow<Boolean> = _isFlashOn.asStateFlow()

    private val _isScanning = MutableStateFlow(true)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    fun toggleFlash() {
        _isFlashOn.value = !_isFlashOn.value
    }

    fun pauseScanning() {
        _isScanning.value = false
    }

    fun resumeScanning() {
        _isScanning.value = true
    }
}
