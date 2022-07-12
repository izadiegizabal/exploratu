package xyz.izadi.exploratu.currencies.others

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources.getSystem
import android.widget.ImageView
import android.widget.TextView
import coil.load
import xyz.izadi.exploratu.R
import xyz.izadi.exploratu.currencies.data.models.Currency
import java.math.BigDecimal


object Utils {

    fun round(numberToRound: Float, decimalPlaces: Int): Float {
        try {
            var bd = BigDecimal(numberToRound.toString())
            bd = bd.setScale(decimalPlaces, BigDecimal.ROUND_HALF_UP)
            return bd.toFloat()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return 0f
    }

    fun reformatIfNeeded(quantity: String): String {
        var quantityRes = quantity
        val amountParts = quantityRes.split(".")

        // Remove unwanted second comma 1,000.52.
        if (amountParts.size > 2) {
            quantityRes = quantityRes.dropLast(1)
        }

        // Add 1,000 thousand comma
        quantityRes = addCommas(quantityRes)

        // Remove unwanted leading zeros
        if (amountParts[0].length == 2) {
            if (amountParts[0][0] == '0') {
                quantityRes = quantityRes.substring(1)
            }
        }
        if (amountParts[0].length == 3) {
            if (amountParts[0][0] == '0') {
                quantityRes = quantityRes.substring(1)
                if (amountParts[0][1] == '0') {
                    quantityRes = quantityRes.substring(1)
                }
            }
        }

        // Limit the decimals to 4
        if (amountParts.size > 1 && amountParts[1].length >= 4) {
            quantityRes = amountParts[0] + "." + amountParts[1].substring(0, 4)
        }

        return quantityRes
    }

    private fun insertPeriodically(text: String, insert: String, period: Int): String {
        val builder = StringBuilder(text)

        var idx = builder.length - period

        while (idx > 0) {
            builder.insert(idx, insert)
            idx -= period
        }

        return builder.toString()
    }

    fun addCommas(numberString: String): String {
        var quantityRes = numberString
        val amountParts = quantityRes.split(".")

        // Add 1,000 thousand comma
        if (amountParts[0].length >= 4) {
            val originalString = amountParts[0].replace(",", "")
            val numberWithCommas = insertPeriodically(originalString, ",", 3)
            quantityRes = numberWithCommas
            if (amountParts.size > 1) {
                quantityRes += "." + amountParts[1]
            }
        }

        if (quantityRes == "0.0") {
            quantityRes = "0"
        }

        return quantityRes
    }

    fun isDarkTheme(activity: Context): Boolean {
        return activity.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

    fun Context.updateCurrencyViews(
        currency: Currency,
        flagIv: ImageView,
        codeTv: TextView,
        descTv: TextView
    ) {
        flagIv.loadFlag(currency)
        codeTv.text = currency.code
        descTv.text = getString(R.string.currency_desc, currency.name, currency.sign)
    }
}

val Int.dp: Int get() = (this / getSystem().displayMetrics.density).toInt()

fun ImageView.loadFlag(currency: Currency) {
    val flagPath = "file:///android_asset/flags/${currency.code}.png"
    load(flagPath) {
        crossfade(true)
        placeholder(R.drawable.ic_dollar_placeholder)
    }
}

