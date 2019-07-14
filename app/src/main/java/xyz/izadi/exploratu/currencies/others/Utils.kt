package xyz.izadi.exploratu.currencies.others

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.telephony.TelephonyManager
import android.util.Log
import com.google.gson.Gson
import xyz.izadi.exploratu.currencies.data.models.Currencies
import java.io.IOException
import java.math.BigDecimal
import java.util.*


object Utils {

    private val LOG_TAG = this.javaClass.simpleName

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

    fun getDetectedCurrency(context: Context): String? {

        var currentCountry = ""

        try {
            val telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            Log.d(
                LOG_TAG,
                "Sim country iso: ${telephonyManager.simCountryIso} Network: ${telephonyManager.networkCountryIso}"
            )
            currentCountry = if(!telephonyManager.simCountryIso.isNullOrBlank()) {
                telephonyManager.simCountryIso
            } else {
                telephonyManager.networkCountryIso
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        Log.d(
            LOG_TAG,
            "Current country code is: $currentCountry"
        )

        return if (!currentCountry.isBlank()) {
            getCurrencyCodeFromCountryISO(currentCountry)
        } else {
            null
        }
    }

    private fun getCurrencyCodeFromCountryISO(countryISO: String): String? {
        try {
            val locale = Locale("", countryISO)
            return getCurrencyCodeFromLocale(locale)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun getCurrencyCodeFromDeviceLocale(): String {
        val currentLocale = Locale.getDefault()
        return getCurrencyCodeFromLocale(currentLocale)
    }

    private fun getCurrencyCodeFromLocale(locale: Locale): String {
        return Currency.getInstance(locale).currencyCode
    }

    fun isInternetAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        return activeNetwork?.isConnected == true
    }
}