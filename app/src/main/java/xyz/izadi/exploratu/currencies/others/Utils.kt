package xyz.izadi.exploratu.currencies.others

import android.content.Context
import com.google.gson.Gson
import xyz.izadi.exploratu.currencies.data.models.Currencies
import java.io.IOException
import java.math.BigDecimal


object Utils {

    fun insertPeriodically(text: String, insert: String, period: Int): String {
        val builder = StringBuilder(text)

        var idx = builder.length - period

        while (idx > 0) {
            builder.insert(idx, insert)
            idx -= period
        }

        return builder.toString()
    }

    /**
     * Round to certain number of decimals
     *
     * @param numberToRound
     * @param decimalPlaces
     * @return
     */
    fun round(numberToRound: Float, decimalPlaces: Int): Float {
        var bd = BigDecimal(numberToRound.toString())
        bd = bd.setScale(decimalPlaces, BigDecimal.ROUND_HALF_UP)
        return bd.toFloat()
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


    fun getCurrencies(context: Context): Currencies? {
        try {
            val gson = Gson()

            val jsonString =
                context.assets.open("currencyInfo.json").bufferedReader().use {
                    it.readText()
                }

            return gson.fromJson(jsonString, Currencies::class.java)

        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }
}