package com.example.ocr

import android.annotation.SuppressLint
import android.graphics.Rect
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.model.ScannedNumber
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.regex.Pattern

class PhoneNumberAnalyzer : ImageAnalysis.Analyzer {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val bdPhonePattern = Pattern.compile("01[3-9][0-9]{8}")

    private val _scannedNumber = MutableStateFlow<ScannedNumber?>(null)
    val scannedNumber: StateFlow<ScannedNumber?> = _scannedNumber.asStateFlow()

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    var foundNumber: ScannedNumber? = null
                    
                    for (block in visionText.textBlocks) {
                        for (line in block.lines) {
                            val text = line.text.replace(Regex("[^0-9]"), "")
                            if (text.length >= 11) {
                                val matcher = bdPhonePattern.matcher(text)
                                while (matcher.find()) {
                                    val number = matcher.group()
                                    if (number.length == 11) {
                                        foundNumber = ScannedNumber(
                                            number = number,
                                            boundingBox = line.boundingBox,
                                            rotationDegrees = imageProxy.imageInfo.rotationDegrees,
                                            imageWidth = imageProxy.width,
                                            imageHeight = imageProxy.height
                                        )
                                        break
                                    }
                                }
                            }
                            if (foundNumber != null) break
                        }
                        if (foundNumber != null) break
                    }
                    _scannedNumber.value = foundNumber
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}
