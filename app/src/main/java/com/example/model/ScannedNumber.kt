package com.example.model

import android.graphics.Rect

data class ScannedNumber(
    val number: String,
    val boundingBox: Rect?,
    val rotationDegrees: Int,
    val imageWidth: Int,
    val imageHeight: Int
)
