package xyz.izadi.exploratu.currencies.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.util.Size
import android.widget.Toast
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

/** Helper type alias used for analysis use case callbacks */
typealias OCRListener = (price: Price, bufferSize: Size) -> Unit

data class Price(
    val amount: Double,
    val boundingBox: Rect
)

class OcrAnalyzer(
    private val context: Context,
    private val overlay: GraphicOverlay<GraphicOverlay.Graphic>,
    listener: OCRListener? = null
) : ImageAnalysis.Analyzer {
    private var mToast: Toast? = null
    private val mListeners = mutableListOf<OCRListener>().apply { listener?.let { add(it) } }
    private val detector = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        // If there are no listeners attached, we don't need to perform analysis
        if (mListeners.isEmpty()) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )
            detector.process(image)
                .addOnSuccessListener { text ->
                    overlay.clearOld()
                    // Task completed successfully
                    // Logic
                    detectNumbers(
                        text.textBlocks,
                        Size(mediaImage.width, mediaImage.height)
                    )
                    // Close img for next use
                    imageProxy.close()
                }
                .addOnFailureListener { e ->
                    // Task failed with an exception
                    e.printStackTrace()
                    showAToast("Error trying to perform OCR, use the manual mode instead.")

                    // Close img for next use
                    imageProxy.close()
                }
        }
    }

    @SuppressLint("ShowToast")
    private fun showAToast(st: String?) {
        mToast?.cancel()
        mToast = Toast.makeText(context, st, Toast.LENGTH_LONG)?.also {
            it.show()
        }
    }

    private fun detectNumbers(items: List<Text.TextBlock>, bufferSize: Size) {
        for (i in items.indices) {
            val item = items[i]
            val lines = item.lines
            for (line in lines) {
                if (line != null) {
                    val words = line.elements
                    for (word in words) {
                        if (word != null) {
                            word.text.number?.takeIf { it > 0.0 }?.let { amount ->
                                word.boundingBox?.let { rect ->
                                    val extracted = Price(
                                        amount = amount,
                                        boundingBox = rect,
                                    )
                                    // Call all listeners with new value
                                    mListeners.forEach { it(extracted, bufferSize) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Extracts numbers from the passed string, returns null if there aren't
    private val String.number: Double?
        get() {
            // select everything except numbers with commas/dots
            val regexNotNumbersCommaDot = Regex("([^0-9.,]+[.,])")
            //remove irrelevant chars
            val onlyNumbers = replace(regexNotNumbersCommaDot, "")
            // select numbers with decimals
            val dottedPriceRegex = Regex("\\d+([.,])\\d{1,4}")
            var value = dottedPriceRegex.find(onlyNumbers)?.value
            return if (value != null) {
                value = value.replace(',', '.')
                val valueParts = value.split(".")
                // if more than two decimals --> is not decimal, is 1.000s
                if (valueParts.size > 1 && valueParts[1].length > 2) {
                    value = valueParts[0] + valueParts[1]
                }
                value
            } else {
                onlyNumbers
            }.toDoubleOrNull()
        }
}
