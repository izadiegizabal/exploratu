package xyz.izadi.exploratu.currencies.data.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import xyz.izadi.exploratu.currencies.others.Utils
import java.util.*

@Entity
data class Rates(
    @PrimaryKey
    val timestamp: Date,
    @Embedded
    val exchanges: Exchanges
) {
    fun convert(quantity: Float, from: String, to: String): String {
        // from --> euro --> to
        val fromRate = exchanges.getRate(from)
        val toRate = exchanges.getRate(to)
        val conversion: Float = (quantity / fromRate!!) * toRate!!
        val roundedString = Utils.round(conversion, 4).toString()
        return Utils.addCommas(roundedString)
    }
}