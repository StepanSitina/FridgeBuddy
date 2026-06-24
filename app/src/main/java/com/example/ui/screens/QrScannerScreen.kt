package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.data.PairingUtils
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@Composable
fun QrScannerScreen(
    onCodeScanned: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission) {
        Box(modifier = Modifier.fillMaxSize()) {
            CameraPreviewWithOverlay(
                onCodeDetected = { code ->
                    onCodeScanned(code)
                }
            )

            // Close button
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(100))
            ) {
                Icon(Icons.Default.Close, contentDescription = "Zavřít", tint = Color.White)
            }
            
            // Text navádění
            Text(
                text = "Zamiř foťák na QR kód\nna displeji druhého telefonu.",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 200.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    } else {
        // Permissions rational
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp).background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Pro skenování QR kódu je nutné povolit fotoaparát.", color = Color.White, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                Text("Povolit")
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = onClose) {
                Text("Zpět", color = Color.White)
            }
        }
    }
}

@Composable
fun CameraPreviewWithOverlay(
    onCodeDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isProcessing = false
    
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // Overlay Animation State
    val infiniteTransition = rememberInfiniteTransition()
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val options = BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                    .build()
                val scanner = BarcodeScanning.getClient(options)

                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor) { imageProxy ->
                            if (!isProcessing) {
                                val mediaImage = imageProxy.image
                                if (mediaImage != null) {
                                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                    scanner.process(image)
                                        .addOnSuccessListener { barcodes ->
                                            if (barcodes.isNotEmpty()) {
                                                val rawValue = barcodes.first().rawValue
                                                val householdId = PairingUtils.parseDeepLinkUrlOrCode(rawValue)
                                                if (householdId != null && !isProcessing) {
                                                    isProcessing = true
                                                    // Haptic feedback with Graceful Degradation
                                                    try {
                                                        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                                                        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                                                    } catch (e: Exception) {
                                                        // Ignorováno pro zamezení pádu (Graceful Degradation)
                                                    }
                                                    
                                                    onCodeDetected(householdId)
                                                }
                                            }
                                        }
                                        .addOnCompleteListener {
                                            imageProxy.close()
                                        }
                                } else {
                                    imageProxy.close()
                                }
                            } else {
                                imageProxy.close()
                            }
                        }
                    }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalyzer
                    )
                } catch (exc: Exception) {
                    exc.printStackTrace()
                    android.widget.Toast.makeText(context, "Chyba při práci s fotoaparátem.", android.widget.Toast.LENGTH_LONG).show()
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )

    // Scanner Overlay
    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        val boxWidth = 280.dp.toPx()
        val boxHeight = 280.dp.toPx()
        
        val boxLeft = (canvasWidth - boxWidth) / 2
        val boxTop = (canvasHeight - boxHeight) / 2
        
        // Vykreslení tmavé poloprůhledné vrstvy
        drawRect(
            color = Color.Black.copy(alpha = 0.6f),
            size = size
        )
        // Vystřižení díry pro zaměřovací čtverec
        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(boxLeft, boxTop),
            size = Size(boxWidth, boxHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx()),
            blendMode = BlendMode.Clear // Vymaže tmavou vrstvu
        )
        
        // Ohraničení čtverce
        val strokeWidth = 4.dp.toPx()
        drawRoundRect(
            color = Color(0xFF4CAF50), // Zelená (FreshGreenPrimary)
            topLeft = Offset(boxLeft, boxTop),
            size = Size(boxWidth, boxHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx()),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
        
        // Běžící laserová linka (shora dolů)
        val laserLineY = boxTop + (laserY * boxHeight)
        drawLine(
            color = Color.Red.copy(alpha = 0.8f),
            start = Offset(boxLeft + 16.dp.toPx(), laserLineY),
            end = Offset(boxLeft + boxWidth - 16.dp.toPx(), laserLineY),
            strokeWidth = 3.dp.toPx()
        )
    }
}
