package xyz.izadi.exploratu.currencies.others

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources.getSystem
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateInterpolator
import java.math.BigDecimal
import kotlin.math.max


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

    fun isInternetAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        return activeNetwork?.isConnected == true
    }

    fun revealActivity(x: Int, y: Int, rootLayout: View) {
        val finalRadius = (max(rootLayout.width, rootLayout.height) * 1.1).toFloat()

        // create the animator for this view (the start radius is zero)
        val circularReveal =
            ViewAnimationUtils.createCircularReveal(rootLayout, x, y, 0f, finalRadius)
        circularReveal.duration = 400
        circularReveal.interpolator = AccelerateInterpolator()

        // make the view visible and start the animation
        rootLayout.visibility = View.VISIBLE
        circularReveal.start()
    }

    fun unRevealActivity(rootLayout: View, revealX: Int, revealY: Int, activity: Activity) {
        val finalRadius = (max(rootLayout.width, rootLayout.height) * 1.1).toFloat()
        val circularReveal = ViewAnimationUtils.createCircularReveal(
            rootLayout, revealX, revealY, finalRadius, 0f
        )

        circularReveal.duration = 400
        circularReveal.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                rootLayout.visibility = View.INVISIBLE
                activity.finish()
            }
        })

        circularReveal.start()
    }

    fun isDarkTheme(activity: Context): Boolean {
        return activity.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }
}

val Int.dp: Int get() = (this / getSystem().displayMetrics.density).toInt()

val Int.px: Int get() = (this * getSystem().displayMetrics.density).toInt()

