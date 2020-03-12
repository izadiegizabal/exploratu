/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.izadi.exploratu.currencies.camera

import android.content.SharedPreferences
import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.text.TextBlock
import xyz.izadi.exploratu.currencies.camera.ui.GraphicOverlay
import xyz.izadi.exploratu.currencies.camera.ui.OcrGraphic


/**
 * A very simple Processor which gets detected TextBlocks and adds them to the overlay
 * as OcrGraphics.
 */
class OcrDetectorProcessor internal constructor(
    private val graphicOverlay: GraphicOverlay<OcrGraphic>?,
    private val drawable: Bitmap,
    private val sharedPreferences: SharedPreferences,
    private val isDarkTheme: Boolean = false
) :
    Detector.Processor<TextBlock> {

    /**
     * Called by the detector to deliver detection results.
     * If your application called for it, this could be a place to check for
     * equivalent detections by tracking TextBlocks that are similar in location and content from
     * previous frames, or reduce noise by eliminating TextBlocks that have not persisted through
     * multiple detections.
     */
    val LOG_TAG = this.javaClass.simpleName

    override fun receiveDetections(detections: Detector.Detections<TextBlock>) {
        graphicOverlay?.clear()
        val items = detections.detectedItems
        for (i in 0 until items.size()) {
            val item = items.valueAt(i)
            if (item != null && item.value != null) {
                val lines = item.components
                for (line in lines) {
                    if (line != null && line.value != null) {
                        val words = line.components
                        for (word in words) {
                            if (word != null && word.value != null) {
                                val number = extractNumbers(word.value)
                                val numberDouble = number!!.toDoubleOrNull()
                                if (numberDouble != null) {
//                                    val graphic = OcrGraphic(
//                                        graphicOverlay,
//                                        word,
//                                        numberDouble,
//                                        drawable,
//                                        sharedPreferences,
//                                        isDarkTheme
//                                    )
//                                    graphicOverlay?.add(graphic)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Frees the resources associated with this detection processor.
     */
    override fun release() {
        graphicOverlay?.clear()
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
