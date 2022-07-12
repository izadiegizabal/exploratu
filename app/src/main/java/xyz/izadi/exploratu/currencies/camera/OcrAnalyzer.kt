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
    private var mToast = Toast(context)
    private val mListeners = ArrayList<OCRListener>().apply { listener?.let { add(it) } }
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
        try {
            mToast.view?.isShown // true if visible
            mToast.setText(st)
        } catch (e: java.lang.Exception) {         // invisible if exception
            mToast = Toast.makeText(context, st, Toast.LENGTH_LONG)
        }
        mToast.show() //finally display it
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
                            val number = extractNumbers(word.text)
                            val numberDouble = number.toDoubleOrNull()
                            if (numberDouble != null) {
                                word.boundingBox?.let {
                                    val extracted = Price(
                                        amount = numberDouble,
                                        boundingBox = it,
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
    private fun extractNumbers(originalString: String): String {
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
