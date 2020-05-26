package xyz.izadi.exploratu.currencies.camera

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.text.FirebaseVisionText
import xyz.izadi.exploratu.currencies.camera.ui.GraphicOverlay
import xyz.izadi.exploratu.currencies.camera.ui.OcrGraphic

/** Helper type alias used for analysis use case callbacks */
typealias OCRListener = (price: FirebaseVisionText.Element) -> Unit

class OcrAnalyzer(private val context: Context, private val overlay: GraphicOverlay<OcrGraphic>, listener: OCRListener? = null) : ImageAnalysis.Analyzer {
    private val mTAG = this.javaClass.simpleName
    private var mToast = Toast(context)
    private val mListeners = ArrayList<OCRListener>().apply { listener?.let { add(it) } }

    /**
     * Used to add listeners that will be called with each frame is computed
     */
    fun onFrameAnalyzed(listener: OCRListener) = mListeners.add(listener)

    private fun degreesToFirebaseRotation(degrees: Int): Int = when(degrees) {
        0 -> FirebaseVisionImageMetadata.ROTATION_0
        90 -> FirebaseVisionImageMetadata.ROTATION_90
        180 -> FirebaseVisionImageMetadata.ROTATION_180
        270 -> FirebaseVisionImageMetadata.ROTATION_270
        else -> throw Exception("Rotation must be 0, 90, 180, or 270.")
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        // If there are no listeners attached, we don't need to perform analysis
        if (mListeners.isEmpty()) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        val imageRotation = degreesToFirebaseRotation(imageProxy.imageInfo.rotationDegrees)
        if (mediaImage != null) {
            val firebaseImage = FirebaseVisionImage.fromMediaImage(mediaImage, imageRotation)
            val detector = FirebaseVision.getInstance().onDeviceTextRecognizer
            val result = detector.processImage(firebaseImage)
                .addOnSuccessListener { firebaseVisionText ->
                    overlay.clear()
                    // Task completed successfully
                    // Logic
                    detectNumbers(firebaseVisionText.textBlocks)
                    // Close img for next use
                    imageProxy.close()
                }
                .addOnFailureListener { e ->
                    // Task failed with an exception
                    Log.d(mTAG, "What is going on $e")
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
            mToast.view.isShown // true if visible
            mToast.setText(st)
        } catch (e: java.lang.Exception) {         // invisible if exception
            mToast = Toast.makeText(context, st, Toast.LENGTH_LONG)
        }
        mToast.show() //finally display it
    }

    private fun detectNumbers(items: List<FirebaseVisionText.TextBlock>) {
        for (i in items.indices) {
            val item = items[i]
                val lines = item.lines
                for (line in lines) {
                    if (line != null) {
                        val words = line.elements
                        for (word in words) {
                            if (word != null) {
                                val number = extractNumbers(word.text)
                                val numberDouble = number!!.toDoubleOrNull()
                                if (numberDouble != null) {
                                    val extracted = FirebaseVisionText.Element(
                                        numberDouble.toString(),
                                        word.boundingBox,
                                        word.recognizedLanguages,
                                        word.confidence
                                    )
                                    // Call all listeners with new value
                                    mListeners.forEach { it(extracted) }
                                }
                            }
                        }
                    }
                }
        }
    }

    // Extracts numbers from the passed string, returns null if there aren't
    private fun extractNumbers(originalString: String): String? {
        val regexNotNumbersCommaDot =
            Regex("([^0-9.,]+[.,])") // select everything except numbers with commas/dots
        val onlyNumbers =
            originalString.replace(regexNotNumbersCommaDot, "") //remove irrelevant chars
        val dottedPriceRegex = Regex("\\d+([.,])\\d{1,4}") // select numbers with decimals
        var value = dottedPriceRegex.find(onlyNumbers)?.value
        if (value != null) {
            value = value.replace(',', '.')
            val valueParts = value.split(".")
            if (valueParts.size > 1 && valueParts[1].length > 2) { // if more than two decimals --> is not decimal, is 1.000s
                value = valueParts[0] + valueParts[1]
            }
            return value
        }
        return onlyNumbers
    }
}