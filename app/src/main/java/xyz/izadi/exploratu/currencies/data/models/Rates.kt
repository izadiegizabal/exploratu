package xyz.izadi.exploratu.currencies.data.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import xyz.izadi.exploratu.currencies.others.Utils
import java.util.*

@Entity
data class Rates(
    @PrimaryKey
    val date: Date,
    @Embedded
    val rates: Exchanges
) {
    fun convert(quantity: Float, from: String, to: String): String {
        // from --> euro --> to
        val fromRate = rates.getRate(from)
        val toRate = rates.getRate(to)
        val conversion: Float = (quantity / fromRate!!) * toRate!!
        val roundedString = Utils.round(conversion, 4).toString()
        return Utils.addCommas(roundedString)
    }
}