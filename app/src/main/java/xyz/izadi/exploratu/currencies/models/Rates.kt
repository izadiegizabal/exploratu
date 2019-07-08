package xyz.izadi.exploratu.currencies.models

import xyz.izadi.exploratu.currencies.others.Utils
import java.util.*

data class Rates(
    val timestamp: Date,
    val exchanges: Exchanges
) {
    fun convert(quantity: Float, from: String, to: String): String {
        // from --> euro --> to
        val fromRate = exchanges.getRate(from)
        val toRate = exchanges.getRate(to)
        val conversion: Float = (quantity / fromRate!!) * toRate!!
        return Utils.round(conversion, 4).toString()
    }
}