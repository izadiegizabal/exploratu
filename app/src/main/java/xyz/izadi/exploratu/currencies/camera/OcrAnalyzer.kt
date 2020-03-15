package xyz.izadi.exploratu.currencies.camera

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata

class OcrAnalyzer(private val context: Context) : ImageAnalysis.Analyzer {
    private val LOG_TAG = this.javaClass.simpleName
    private var toast = Toast(context)

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
                    showAToast(firebaseVisionText.text)
                    val resultText = firebaseVisionText.text

                    // Close img for next use
                    imageProxy.close()
                }
                .addOnFailureListener { e ->
                    // Task failed with an exception
                    // ...
                    Log.d(LOG_TAG, "What is going on $e")
                    e.printStackTrace()
                    showAToast("Error trying to perform OCR, use the manual mode instead.")

                    // Close img for next use
                    imageProxy.close()
                }
        }
    }

    @SuppressLint("ShowToast")
    private fun showAToast(st: String?) {
        try {
            toast.view.isShown // true if visible
            toast.setText(st)
        } catch (e: java.lang.Exception) {         // invisible if exception
            toast = Toast.makeText(context, st, Toast.LENGTH_LONG)
        }
        toast.show() //finally display it
    }
}