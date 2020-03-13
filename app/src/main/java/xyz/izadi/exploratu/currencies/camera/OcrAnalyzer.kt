package xyz.izadi.exploratu.currencies.camera

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata

class OcrAnalyzer() : ImageAnalysis.Analyzer {
    private val LOG_TAG = this.javaClass.simpleName

    private fun degreesToFirebaseRotation(degrees: Int): Int = when(degrees) {
        0 -> FirebaseVisionImageMetadata.ROTATION_0
        90 -> FirebaseVisionImageMetadata.ROTATION_90
        180 -> FirebaseVisionImageMetadata.ROTATION_180
        270 -> FirebaseVisionImageMetadata.ROTATION_270
        else -> throw Exception("Rotation must be 0, 90, 180, or 270.")
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        Log.d(LOG_TAG, "Trying to detect something")
        val mediaImage = imageProxy.image
        val imageRotation = degreesToFirebaseRotation(imageProxy.imageInfo.rotationDegrees)
        if (mediaImage != null) {
            val firebaseImage = FirebaseVisionImage.fromMediaImage(mediaImage, imageRotation)
            val detector = FirebaseVision.getInstance().onDeviceTextRecognizer
            val result = detector.processImage(firebaseImage)
                .addOnSuccessListener { firebaseVisionText ->
                    // Task completed successfully
                    // Logic
                    Log.d(LOG_TAG, "Text detected! ${firebaseVisionText.text}")
                    val resultText = firebaseVisionText.text
                    for (block in firebaseVisionText.textBlocks) {
                        val blockText = block.text
                        val blockConfidence = block.confidence
                        val blockLanguages = block.recognizedLanguages
                        val blockCornerPoints = block.cornerPoints
                        val blockFrame = block.boundingBox
                        for (line in block.lines) {
                            val lineText = line.text
                            val lineConfidence = line.confidence
                            val lineLanguages = line.recognizedLanguages
                            val lineCornerPoints = line.cornerPoints
                            val lineFrame = line.boundingBox
                            for (element in line.elements) {
                                val elementText = element.text
                                val elementConfidence = element.confidence
                                val elementLanguages = element.recognizedLanguages
                                val elementCornerPoints = element.cornerPoints
                                val elementFrame = element.boundingBox
                                Log.d(LOG_TAG, "Text detected: $elementText")
                            }
                        }
                    }

                    // Close img for next use
                    imageProxy.close()
                }
                .addOnFailureListener { e ->
                    // Task failed with an exception
                    // ...
                    Log.d(LOG_TAG, "What is going on $e")

                    // Close img for next use
                    imageProxy.close()
                }
        }
    }
}