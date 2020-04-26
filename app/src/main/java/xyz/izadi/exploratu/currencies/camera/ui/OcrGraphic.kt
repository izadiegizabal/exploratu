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
package xyz.izadi.exploratu.currencies.camera.ui

import android.content.SharedPreferences
import android.graphics.*
import com.google.firebase.ml.vision.text.FirebaseVisionText
import xyz.izadi.exploratu.currencies.others.Utils
import android.graphics.Typeface
import android.util.Log
import java.text.DecimalFormat


/**
 * Graphic instance for rendering TextBlock position, size, and ID within an associated graphic
 * overlay view.
 */
class OcrGraphic(
    overlay: GraphicOverlay<*>?,
    val text: FirebaseVisionText.Element,
    private val number: Double,
    private val graphic: Bitmap,
    private val sharedPreferences: SharedPreferences,
    private val isDarkTheme: Boolean = false
) : GraphicOverlay.Graphic(overlay) {
    var id: Int = 0

    init {

        if (rectPaint == null) {
            rectPaint = Paint()
            rectPaint!!.color = TEXT_COLOR
            rectPaint!!.style = Paint.Style.STROKE
            rectPaint!!.strokeWidth = 4.0f
        }

        if (textPaint == null) {
            textPaint = Paint()
            textPaint!!.color = if (isDarkTheme) {
                TEXT_COLOR
            } else {
                PRICE_COLOR
            }
            textPaint!!.textSize = 52.0f
            textPaint!!.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }
        // Redraw the overlay, as this graphic has been added.
        postInvalidate()
    }

    /**
     * Checks whether a point is within the bounding box of this graphic.
     * The provided point should be relative to this graphic's containing overlay.
     *
     * @param x An x parameter in the relative context of the canvas.
     * @param y A y parameter in the relative context of the canvas.
     * @return True if the provided point is contained within this graphic's bounding box.
     */
    override fun contains(x: Float, y: Float): Boolean {
        var rect = RectF(text.boundingBox)
        rect = translateRect(rect)
        return rect.contains(x, y)
    }

    /**
     * Draws the text block annotations for position, size, and raw value on the supplied canvas.
     */
    override fun draw(canvas: Canvas) {
        textPaint!!.color = if (isDarkTheme) {
            TEXT_COLOR
        } else {
            PRICE_COLOR
        }

        val conversionRate = sharedPreferences.getFloat("currency_conversion_rate_AR", 1f)
        val symbol = sharedPreferences.getString("currency_to_symbol", "")
        val convertedNum = (number * conversionRate).toFloat()
        val roundedNum = when {
            convertedNum < 10000 -> {
                Utils.round(convertedNum, 1)
            }
            convertedNum < 100 -> {
                Utils.round(convertedNum, 2)
            }
            else -> {
                Utils.round(convertedNum, 0)
            }
        }
        val commasString = Utils.addCommas(DecimalFormat("#.##").format(roundedNum))
        val convertedSting = "$symbol$commasString"

        val end = translateX(text.boundingBox?.right?.toFloat()!!)
        val bottom = translateY(text.boundingBox?.bottom?.toFloat()!!)
        canvas.drawBitmap(
            graphic,
            end,
            (bottom - (text.boundingBox!!.height() / 2 + graphic.height / 2 + 15)),
            textPaint
        )
        canvas.drawText(
            convertedSting,
            end + 72 + getApproxXToCenterText(convertedSting, textPaint!!, graphic.width - 56),
            // (end + 64 + ((convertedSting.length + 1)/2 * 12)),
            (bottom - (text.boundingBox!!.height() / 2 - 4)),
            textPaint!!
        )
    }

    companion object {
        private const val TEXT_COLOR = Color.WHITE
        private const val PRICE_COLOR = Color.BLACK
        private var rectPaint: Paint? = null
        private var textPaint: Paint? = null
    }

    private fun getApproxXToCenterText(
        text: String,
        p: Paint,
        widthToFitStringInto: Int
    ): Float {
        val textWidth = p.measureText(text)
        return ((widthToFitStringInto - textWidth) / 2f) - (p.textSize / 2f)
    }
}
