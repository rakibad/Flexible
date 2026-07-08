package com.example.repository

import com.example.model.ScannedNumber
import com.example.ocr.PhoneNumberAnalyzer
import kotlinx.coroutines.flow.StateFlow

class OcrRepository {
    val analyzer = PhoneNumberAnalyzer()
    
    val scannedNumber: StateFlow<ScannedNumber?>
        get() = analyzer.scannedNumber
}
