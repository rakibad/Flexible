package com.example.camera

import android.content.Context
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.model.ScannedNumber
import java.util.concurrent.Executors

@Composable
fun CameraPreviewScreen(
    analyzer: ImageAnalysis.Analyzer,
    scannedNumber: ScannedNumber?,
    isFlashOn: Boolean,
    isScanning: Boolean
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    val executor = remember { Executors.newSingleThreadExecutor() }

    LaunchedEffect(isFlashOn) {
        cameraControl?.enableTorch(isFlashOn)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { previewView ->
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    
                    if (!isScanning) {
                        cameraProvider.unbindAll()
                        return@addListener
                    }

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(executor, analyzer)
                        }

                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build()

                    try {
                        cameraProvider.unbindAll()
                        val camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                        cameraControl = camera.cameraControl
                        camera.cameraControl.enableTorch(isFlashOn)
                    } catch (exc: Exception) {
                        exc.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(context))
            }
        )

        // Draw bounding box if we have a scanned number
        scannedNumber?.let { number ->
            number.boundingBox?.let { rect ->
                BoundingBoxOverlay(number)
            }
        }
    }
}

@Composable
fun BoundingBoxOverlay(scannedNumber: ScannedNumber) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val rect = scannedNumber.boundingBox ?: return@Canvas
        
        // Image dimensions
        val imageWidth = scannedNumber.imageWidth.toFloat()
        val imageHeight = scannedNumber.imageHeight.toFloat()
        
        // Canvas dimensions
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        // Handle rotation for scale calculation
        // When device is in portrait, the image from camera is typically rotated 90 degrees
        val isPortrait = scannedNumber.rotationDegrees == 90 || scannedNumber.rotationDegrees == 270
        
        val effectiveImageWidth = if (isPortrait) imageHeight else imageWidth
        val effectiveImageHeight = if (isPortrait) imageWidth else imageHeight
        
        // Calculate scale factor (assuming Fill Center scale type)
        val scaleX = canvasWidth / effectiveImageWidth
        val scaleY = canvasHeight / effectiveImageHeight
        val scale = maxOf(scaleX, scaleY)
        
        // Calculate offsets to center the scaled image
        val scaledImageWidth = effectiveImageWidth * scale
        val scaledImageHeight = effectiveImageHeight * scale
        val offsetX = (canvasWidth - scaledImageWidth) / 2
        val offsetY = (canvasHeight - scaledImageHeight) / 2

        // Transform rect coordinates
        val scaledRectLeft = rect.left * scale + offsetX
        val scaledRectTop = rect.top * scale + offsetY
        val scaledRectRight = rect.right * scale + offsetX
        val scaledRectBottom = rect.bottom * scale + offsetY
        
        drawRect(
            color = Color.Green,
            topLeft = Offset(scaledRectLeft, scaledRectTop),
            size = Size(scaledRectRight - scaledRectLeft, scaledRectBottom - scaledRectTop),
            style = Stroke(width = 8f)
        )
    }
}
